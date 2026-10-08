# -*- coding: utf-8 -*-
import os

ROOT = r'I:\文档\sukisuultra\KernelSU\website\docs'
ANCHOR = "      link: https://github.com/qianyumeng0228/ShizuSU/releases/latest"
SPONSOR = "https://zanzhuwang.cc.cd"

labels = {
    "root": "Sponsor ShizuSU",
    "zh_CN": "赞助 ShizuSU",
    "zh_TW": "贊助 ShizuSU",
    "ja_JP": "ShizuSU を支援する",
    "vi_VN": "Ủng hộ ShizuSU",
    "id_ID": "Dukung ShizuSU",
    "ru_RU": "Поддержать ShizuSU",
    "pt_BR": "Apoiar o ShizuSU",
}

for lang, label in labels.items():
    p = os.path.join(ROOT, '' if lang == 'root' else lang, 'index.md')
    with open(p, encoding='utf-8', newline='') as f:
        t = f.read()
    t = t.replace('\r\n', '\n')
    if ANCHOR not in t:
        print('MISS anchor', lang)
        continue
    if SPONSOR in t:
        print('SKIP (already)', lang)
        continue
    block = ANCHOR + "\n    - theme: sponsor\n      text: " + label + "\n      link: " + SPONSOR
    t = t.replace(ANCHOR, block, 1)
    with open(p, 'w', encoding='utf-8', newline='\n') as f:
        f.write(t)
    print('OK', lang)
