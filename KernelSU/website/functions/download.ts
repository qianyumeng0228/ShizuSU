// Pages Function: /download 与 /download.html 共享逻辑
// 点击后解析 ShizuSU 最新 GitHub Release，302 重定向到最新 APK 资产直链。
// 优先 GitHub API；API 限流(403)时回退：解析 releases/latest 页 tag → expanded_assets 端点拿资产链接。
const REPO = "qianyumeng0228/ShizuSU";
const UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36";

export async function onRequestGet(context) {
  let url = null;

  // 1) GitHub API
  const headers = { "User-Agent": "ShizuSU-website", "Accept": "application/vnd.github+json" };
  if (context.env && context.env.GITHUB_TOKEN) {
    headers["Authorization"] = "Bearer " + context.env.GITHUB_TOKEN;
  }
  try {
    const res = await fetch("https://api.github.com/repos/" + REPO + "/releases/latest", { headers });
    if (res.ok) {
      const rel = await res.json();
      const asset = (rel && rel.assets || []).find((a) => typeof a.name === "string" && /\.apk$/i.test(a.name));
      if (asset && asset.browser_download_url) url = asset.browser_download_url;
    }
  } catch (e) {}

  // 2) HTML + expanded_assets 回退
  if (!url) {
    try {
      const res2 = await fetch("https://github.com/" + REPO + "/releases/latest", { headers: { "User-Agent": UA } });
      if (res2.ok) {
        const html = await res2.text();
        const tagM = html.match(/releases\/tag\/(v[0-9][^"'<>\s]*)/);
        if (tagM) {
          const res3 = await fetch("https://github.com/" + REPO + "/releases/expanded_assets/" + tagM[1], {
            headers: { "User-Agent": UA },
          });
          if (res3.ok) {
            const frag = await res3.text();
            const m = frag.match(/href="([^"]+\/releases\/download\/[^"]+\.apk)"/i);
            if (m) url = m[1].charAt(0) === "/" ? "https://github.com" + m[1] : m[1];
          }
        }
      }
    } catch (e) {}
  }

  if (!url) {
    return new Response("Failed to resolve the latest ShizuSU release. Please try again later.", {
      status: 502,
      headers: { "Content-Type": "text/plain; charset=utf-8", "Cache-Control": "no-store" },
    });
  }
  return Response.redirect(url, 302);
}
