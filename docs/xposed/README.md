# ShizuSU Xposed Module Mirror

This directory contains a daily snapshot of the official LSPosed Xposed module repository.

- Source: `https://modules.lsposed.org/modules.json`
- Fallback: `https://modules-blogcdn.lsposed.org/modules.json`
- Refresh: GitHub Actions `.github/workflows/xposed-mirror.yml` (daily 04:17 UTC, plus manual dispatch)
- Consumers: ShizuSU manager Xposed repo screen (4th mirror source)

The file is a JSON array of LSPosed `OnlineModule` objects. The schema matches the upstream repository exactly.
