# -*- coding: utf-8 -*-
import os

ROOT = r'I:\文档\sukisuultra\KernelSU\website\docs'
ANCHOR = "      link: https://github.com/qianyumeng0228/ShizuSU"
RELEASES = "https://github.com/qianyumeng0228/ShizuSU/releases/latest"

labels = {
    "root": "Download ShizuSU",
    "zh_CN": "下载 ShizuSU",
    "zh_TW": "下載 ShizuSU",
    "ja_JP": "ShizuSU をダウンロード",
    "vi_VN": "Tải ShizuSU",
    "id_ID": "Unduh ShizuSU",
    "ru_RU": "Скачать ShizuSU",
    "pt_BR": "Baixar ShizuSU",
}

for lang, label in labels.items():
    p = os.path.join(ROOT, '' if lang == 'root' else lang, 'index.md')
    with open(p, encoding='utf-8', newline='') as f:
        t = f.read()
    t = t.replace('\r\n', '\n')
    if ANCHOR not in t:
        print('MISS', lang)
        continue
    if 'releases/latest' in t:
        print('SKIP (already has download)', lang)
        continue
    block = ANCHOR + "\n    - theme: alt\n      text: " + label + "\n      link: " + RELEASES
    t = t.replace(ANCHOR, block, 1)
    with open(p, 'w', encoding='utf-8', newline='\n') as f:
        f.write(t)
    print('OK', lang)
