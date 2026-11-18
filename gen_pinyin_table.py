# -*- coding: utf-8 -*-
from pypinyin import pinyin, Style

table = []
for hi in range(0xB0, 0xF8):
    for lo in range(0xA1, 0xFF):
        try:
            ch = bytes([hi, lo]).decode('gb2312')
        except Exception:
            table.append('#')
            continue
        if len(ch) != 1 or not ('\u4e00' <= ch <= '\u9fff'):
            table.append('#')
            continue
        try:
            py = pinyin(ch, style=Style.NORMAL, errors='default')
            first = py[0][0][0].upper() if py and py[0] and py[0][0] else '#'
        except Exception:
            first = '#'
        if not ('A' <= first <= 'Z'):
            first = '#'
        table.append(first)

assert len(table) == 6768, len(table)
s = ''.join(table)

lines = []
for i in range(0, 6768, 94):
    seg = s[i:i + 94]
    lines.append('        "' + seg + '" +')
body = '\n'.join(lines)[:-2]

kotlin = (
    "package io.github.mangi.flymefreeform.window\n\n"
    "/**\n"
    " * @author bomo 自动生成：GB2312 汉字区（0xB0A1-0xF7FE，72 区 x 94 位 = 6768 码位）\n"
    " * 每个码位对应的拼音首字母（pypinyin 生成；非汉字位 / 无法转拼音为 '#'）。\n"
    " * 索引 = (hi - 0xB0) * 94 + (lo - 0xA1)。\n"
    " */\n"
    "internal object PinyinInitialTable {\n"
    "    val TABLE: String =\n"
    + body
    + "\n}\n"
)
out = r"D:\LIHH\VibeCoding\FlymeFreeform\app\src\main\kotlin\io\github\mangi\flymefreeform\window\PinyinInitialTable.kt"
with open(out, 'w', encoding='utf-8', newline='') as f:
    f.write(kotlin)
print('WRITTEN', len(s), 'chars')
