# -*- coding: utf-8 -*-
import os

ROOT = r'I:\文档\sukisuultra\KernelSU\website\docs'
OLD = "      link: https://github.com/qianyumeng0228/ShizuSU/releases/latest"
NEW = "      link: /download"

langs = ["root", "zh_CN", "zh_TW", "ja_JP", "vi_VN", "id_ID", "ru_RU", "pt_BR"]
for lang in langs:
    p = os.path.join(ROOT, '' if lang == 'root' else lang, 'index.md')
    with open(p, encoding='utf-8', newline='') as f:
        t = f.read()
    t = t.replace('\r\n', '\n')
    if OLD not in t:
        print('MISS', lang)
        continue
    t = t.replace(OLD, NEW, 1)
    with open(p, 'w', encoding='utf-8', newline='\n') as f:
        f.write(t)
    print('OK', lang)
