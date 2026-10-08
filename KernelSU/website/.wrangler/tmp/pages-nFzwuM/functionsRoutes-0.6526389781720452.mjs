import { onRequestGet as __download_ts_onRequestGet } from "I:\\文档\\sukisuultra\\KernelSU\\website\\functions\\download.ts"
import { onRequestGet as __download_html_ts_onRequestGet } from "I:\\文档\\sukisuultra\\KernelSU\\website\\functions\\download.html.ts"

export const routes = [
    {
      routePath: "/download",
      mountPath: "/",
      method: "GET",
      middlewares: [],
      modules: [__download_ts_onRequestGet],
    },
  {
      routePath: "/download.html",
      mountPath: "/",
      method: "GET",
      middlewares: [],
      modules: [__download_html_ts_onRequestGet],
    },
  ]