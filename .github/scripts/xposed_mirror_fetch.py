#!/usr/bin/env python3
"""Fetch LSPosed module list into docs/xposed/modules.json.

Source order:
  1. backup.modules.lsposed.org/modules.json  (official backup, full fields, same as ShizuSU-Xposed)
  2. modules.lsposed.org/module-list/{page}.json  (official website paginated API)
  3. modules.lsposed.org/modules.json  (old primary endpoint, usually 403)

Exits 1 if all sources fail. Output must be a non-empty JSON array (OnlineModule[]).
"""
import json
import os
import subprocess
import sys

UA = "Mozilla/5.0 (compatible; ShizuSU-mirror/1.0)"
OUT = "docs/xposed/modules.json"


def curl(url, out, timeout):
    cmd = ["curl", "-fkSL", "--compressed", "--max-time", str(timeout), "-A", UA, "-o", out, url]
    r = subprocess.run(cmd, capture_output=True)
    return r.returncode == 0


def write_list(modules):
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(modules, f, ensure_ascii=False, indent=2)


def main():
    ok = False
    # 1) Primary: official backup source (same data as ShizuSU-Xposed)
    print("== trying backup source")
    if curl("https://backup.modules.lsposed.org/modules.json", OUT, 90):
        print("OK: backup source")
        ok = True

    # 2) Fallback: official website paginated API
    if not ok:
        modules = []
        for p in range(1, 61):
            u = f"https://modules.lsposed.org/module-list/{p}.json"
            tmp = f"docs/xposed/page-{p}.json"
            print(f"== trying {u}")
            if curl(u, tmp, 60):
                ok = True
                try:
                    with open(tmp, encoding="utf-8") as f:
                        data = json.load(f)
                    modules.extend(data.get("modules", []))
                except Exception as e:  # noqa: BLE001
                    print("skip", os.path.basename(tmp), e)
                os.remove(tmp)
            else:
                break
        if ok:
            write_list(modules)
            print("entries:", len(modules))

    # 3) Last fallback: old primary endpoint
    if not ok:
        print("== fallback trying modules.lsposed.org")
        if curl("https://modules.lsposed.org/modules.json", OUT, 60):
            print("OK: fallback modules.lsposed.org")
            ok = True

    if not ok:
        print("::error::all upstream mirrors failed")
        sys.exit(1)

    # sanity: must be a non-empty JSON array
    with open(OUT, encoding="utf-8") as f:
        d = json.load(f)
    assert isinstance(d, list), "not an array"
    assert len(d) > 0, "empty module list"
    print("final entries:", len(d))


if __name__ == "__main__":
    main()
