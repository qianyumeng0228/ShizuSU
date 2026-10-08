# build-release.ps1 - ShizuSU manager release build pipeline
# gradle assembleRelease -> inject libksud.so -> apksigner v2 -> dated output
[CmdletBinding()]
param(
    [string]$KsudPath,
    [string]$OutDir,
    [string]$KeystoreProperties,
    [switch]$SkipGradle
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Date = Get-Date -Format "yyyyMMdd"
if (-not $KeystoreProperties) { $KeystoreProperties = Join-Path $Root "manager\keystore.properties" }
if (-not $OutDir) { $OutDir = Join-Path $Root "..\ShizuSU-phase0\release-artifacts\phase-final\apk" }
$OutDir = [System.IO.Path]::GetFullPath($OutDir)
if (-not $KsudPath) { $KsudPath = Join-Path $Root "..\ShizuSU-phase0\release-artifacts\phase-final\lib\libksud.so" }
$KsudPath = [System.IO.Path]::GetFullPath($KsudPath)
$OutApk = Join-Path $OutDir "shizusu-manager-release-$Date.apk"

function Log($m) { Write-Host "[build-release] $m" }

Log "== preflight =="
if (-not (Test-Path $KeystoreProperties)) { throw "keystore.properties not found: $KeystoreProperties" }
$ksProps = @{}
Get-Content $KeystoreProperties | ForEach-Object {
    if ($_ -match '^\s*([^#=]+)=(.*)$') { $ksProps[$Matches[1].Trim()] = $Matches[2].Trim() }
}
$storeFile = $ksProps['storeFile']
if (-not (Test-Path $storeFile)) { throw "storeFile not found: $storeFile" }
Log "keystore loaded (alias=$($ksProps['keyAlias']))"

$subst = $false
if (-not (Test-Path S:\)) {
    subst S: $Root 2>&1 | Out-Null
    $subst = $true
}

try {
    $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

    if (-not $SkipGradle) {
        Log "== (a) gradle assembleRelease =="
        Push-Location S:\manager
        try {
            .\gradlew.bat :app:assembleRelease --no-daemon 2>&1 | Out-Host
            if ($LASTEXITCODE -ne 0) { throw "gradle failed" }
        } finally { Pop-Location }
    }

    $srcApk = "S:\manager\app\build\outputs\apk\release\ShizuSU_v1.0.6_37307-release.apk"
    if (-not (Test-Path $srcApk)) { throw "src apk missing: $srcApk" }
    Log "src APK: $srcApk ($((Get-Item $srcApk).Length) bytes)"
    if (-not (Test-Path $KsudPath)) { throw "libksud.so missing: $KsudPath" }
    Log "libksud.so: $KsudPath ($((Get-Item $KsudPath).Length) bytes)"

    $work = Join-Path $env:TEMP "shizusu-build-$Date"
    if (Test-Path $work) { Remove-Item -Recurse -Force $work }
    New-Item -ItemType Directory -Path $work | Out-Null
    $unsigned = Join-Path $work "unsigned.apk"
    Copy-Item $srcApk $unsigned

    Log "== (c) inject =="
    $py = @"
import zipfile, sys, shutil, os
apk = sys.argv[1]; ksud = sys.argv[2]; entry = 'lib/arm64-v8a/libksud.so'
tmp = apk + '.tmp'
with zipfile.ZipFile(apk, 'r') as zin, zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zout:
    for item in zin.infolist():
        if item.filename == entry: continue
        zout.writestr(item, zin.read(item.filename))
    with open(ksud, 'rb') as f:
        data = f.read()
    zi = zipfile.ZipInfo(entry)
    zi.compress_type = zipfile.ZIP_STORED
    zi.external_attr = (0o644 << 16)
    zout.writestr(zi, data)
shutil.move(tmp, apk)
print('injected', os.path.getsize(apk))
with zipfile.ZipFile(apk) as z:
    i = z.getinfo(entry)
    print('  zip entry', entry, 'size=', i.file_size, 'stored=', i.compress_type==zipfile.ZIP_STORED)
"@
    $pyFile = Join-Path $work "inject.py"
    Set-Content -Path $pyFile -Value $py -Encoding UTF8
    python $pyFile $unsigned $KsudPath

    $apksigner = "F:\Android\sdk\build-tools\35.0.0\apksigner.bat"
    $signed = Join-Path $work "signed.apk"
    Copy-Item $unsigned $signed
    Log "== (d) apksigner v2 sign =="
    & $apksigner sign --v1-signing-enabled false --v2-signing-enabled true --v3-signing-enabled false `
        --ks $storeFile --ks-pass "pass:$($ksProps['storePassword'])" `
        --ks-key-alias $ksProps['keyAlias'] --key-pass "pass:$($ksProps['keyPassword'])" `
        $signed
    if ($LASTEXITCODE -ne 0) { throw "apksigner sign failed" }

    Log "== (e) apksigner verify =="
    & $apksigner verify $signed 2>&1 | Out-Host

    Log "== (f) output =="
    if (-not (Test-Path $OutDir)) { New-Item -ItemType Directory -Path $OutDir | Out-Null }
    Copy-Item $signed $OutApk -Force
    $size = (Get-Item $OutApk).Length
    $md5 = (Get-FileHash $OutApk -Algorithm MD5).Hash.ToLower()
    $sha = (Get-FileHash $OutApk -Algorithm SHA256).Hash.ToLower()
    Log "output: $OutApk"
    Log "  size: $size bytes"
    Log "  MD5: $md5"
    Log "  SHA-256: $sha"
    Log "DONE"
} finally {
    if ($subst) { subst S: /D 2>&1 | Out-Null }
}
