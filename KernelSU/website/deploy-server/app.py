#!/usr/bin/env python3
# ShizuSU latest-APK download redirect service (127.0.0.1:8088)
import json
import re
import urllib.request
from http.server import BaseHTTPRequestHandler, HTTPServer

REPO = "qianyumeng0228/ShizuSU"
UA_API = "ShizuSU-website"
UA_WEB = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36"


def resolve_latest():
    # 1) GitHub API
    try:
        req = urllib.request.Request(
            "https://api.github.com/repos/%s/releases/latest" % REPO,
            headers={"User-Agent": UA_API, "Accept": "application/vnd.github+json"},
        )
        with urllib.request.urlopen(req, timeout=15) as r:
            rel = json.load(r)
        for a in rel.get("assets", []):
            name = a.get("name", "")
            if name.lower().endswith(".apk") and a.get("browser_download_url"):
                return a["browser_download_url"]
    except Exception:
        pass
    # 2) HTML tag + expanded_assets 回退
    try:
        req = urllib.request.Request(
            "https://github.com/%s/releases/latest" % REPO,
            headers={"User-Agent": UA_WEB},
        )
        with urllib.request.urlopen(req, timeout=15) as r:
            html = r.read().decode("utf-8", "ignore")
        m = re.search(r"releases/tag/(v[0-9][^\"'<>\s]*)", html)
        if m:
            tag = m.group(1)
            req2 = urllib.request.Request(
                "https://github.com/%s/releases/expanded_assets/%s" % (REPO, tag),
                headers={"User-Agent": UA_WEB},
            )
            with urllib.request.urlopen(req2, timeout=15) as r2:
                frag = r2.read().decode("utf-8", "ignore")
            m2 = re.search(r'href="([^"]+/releases/download/[^"]+\.apk)"', frag, re.I)
            if m2:
                u = m2.group(1)
                return "https://github.com" + u if u.startswith("/") else u
    except Exception:
        pass
    return None


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        path = self.path.split("?")[0].rstrip("/")
        if path in ("/download", "/download.html", "/download/", "/download.html/") or path.startswith("/download"):
            url = resolve_latest()
            if url:
                self.send_response(302)
                self.send_header("Location", url)
                self.send_header("Cache-Control", "no-store")
                self.end_headers()
            else:
                body = b"Failed to resolve the latest ShizuSU release. Please try again later."
                self.send_response(502)
                self.send_header("Content-Type", "text/plain; charset=utf-8")
                self.send_header("Content-Length", str(len(body)))
                self.end_headers()
                self.wfile.write(body)
        else:
            self.send_response(404)
            self.end_headers()

    def log_message(self, *args):
        pass


if __name__ == "__main__":
    HTTPServer(("127.0.0.1", 8088), Handler).serve_forever()
