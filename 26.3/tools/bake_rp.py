#!/usr/bin/env python3
"""把 trimmable-tools 的 paletted_permutations 图集方案改成「预烘焙贴图」。

26.3 里 Nexo 会用自带的 nexo_26_3 覆盖层重写 assets/minecraft/atlases/items.json，
导致外部材质包里的图集条目被丢掉 → 干脆不用图集置换：
  * 用原版 textures/palettes/trim_base.png + textures/palettes/trim/<mat>.png 做颜色映射
  * 把 90 张图案 × 15 种材质烘焙成 1350 张 PNG，放在 textures/item/trims/items/... 下
    （items 图集有 minecraft:directory 源，会自动收录所有命名空间的 textures/item/**）
  * 把 5942 个模型的 layer1 从 trims/items/... 改指 item/trims/items/...
"""
import json, os, struct, sys, zlib
from itertools import product

RP = os.environ.get('TT_RP', 'out/resourcepack')                       # build_tt.py 产出的材质包目录
PAL = os.environ.get('TT_PAL', 'palettes')                             # 里面要有 trim_base.png 与 trim/<材质>.png
MATS = ['amethyst', 'copper', 'diamond', 'diamond_darker', 'emerald', 'gold',
        'gold_darker', 'iron', 'iron_darker', 'lapis', 'netherite',
        'netherite_darker', 'quartz', 'redstone', 'resin']


# ---------------- 极简 PNG 编解码（只处理 8bit、非隔行、RGB/RGBA） ----------------
def png_read(path):
    data = open(path, 'rb').read()
    assert data[:8] == b'\x89PNG\r\n\x1a\n', path
    pos, idat, w = 8, b'', None
    plte, trns = None, b''
    while pos < len(data):
        ln = struct.unpack('>I', data[pos:pos + 4])[0]
        typ = data[pos + 4:pos + 8]
        chunk = data[pos + 8:pos + 8 + ln]
        if typ == b'IHDR':
            w, h, depth, ctype, comp, filt, inter = struct.unpack('>IIBBBBB', chunk)
            assert depth == 8 and inter == 0 and comp == 0 and filt == 0
        elif typ == b'PLTE':
            plte = chunk
        elif typ == b'tRNS':
            trns = chunk
        elif typ == b'IDAT':
            idat += chunk
        elif typ == b'IEND':
            break
        pos += 12 + ln
    raw = zlib.decompress(idat)
    bpp = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}.get(ctype)
    if bpp is None:
        raise SystemExit(f'暂不支持的 PNG 颜色类型: {ctype} ({path})')
    stride = w * bpp
    out, prev = [], bytearray(stride)
    p = 0
    for _ in range(h):
        f = raw[p]; p += 1
        line = bytearray(raw[p:p + stride]); p += stride
        if f == 1:
            for i in range(bpp, stride):
                line[i] = (line[i] + line[i - bpp]) & 0xFF
        elif f == 2:
            for i in range(stride):
                line[i] = (line[i] + prev[i]) & 0xFF
        elif f == 3:
            for i in range(stride):
                a = line[i - bpp] if i >= bpp else 0
                line[i] = (line[i] + ((a + prev[i]) >> 1)) & 0xFF
        elif f == 4:
            for i in range(stride):
                a = line[i - bpp] if i >= bpp else 0
                b = prev[i]
                c = prev[i - bpp] if i >= bpp else 0
                pp = a + b - c
                pa, pb, pc = abs(pp - a), abs(pp - b), abs(pp - c)
                pr = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[i] = (line[i] + pr) & 0xFF
        rows = []
        for x in range(w):
            px = tuple(line[x * bpp:(x + 1) * bpp])
            if bpp == 1:
                px = (px[0], px[0], px[0], 255)
            elif bpp == 2:
                px = (px[0], px[0], px[0], px[1])
            elif bpp == 3 and ctype == 2:
                px = px + (255,)
            elif ctype == 3:
                idx = px[0]
                a = trns[idx] if idx < len(trns) else 255
                px = (plte[idx*3], plte[idx*3+1], plte[idx*3+2], a)
            rows.append(px)
        out.append(rows)
        prev = line
    return w, h, out


def png_write(path, w, h, rows):
    raw = b''.join(b'\x00' + b''.join(bytes(px) for px in row) for row in rows)
    def chunk(typ, data):
        return struct.pack('>I', len(data)) + typ + data + struct.pack('>I', zlib.crc32(typ + data) & 0xFFFFFFFF)
    ihdr = struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)
    png = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', ihdr) + chunk(b'IDAT', zlib.compress(raw, 9)) + chunk(b'IEND', b'')
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, 'wb').write(png)


def main():
    global RP, PAL
    argv = sys.argv[1:]
    if '-h' in argv or '--help' in argv:
        print(__doc__)
        print('用法: bake_rp.py [--rp DIR] [--palettes DIR]')
        return 0
    it = iter(argv)
    for a in it:
        if a == '--rp': RP = next(it)
        elif a == '--palettes': PAL = next(it)
        else:
            print('未知参数: ' + a); return 2
    print('RP=%s\nPAL=%s' % (RP, PAL))
    # 1) 调色板
    kw, kh, krows = png_read(os.path.join(PAL, 'trim_base.png'))
    key = [px for row in krows for px in row]
    mats = {}
    for m in MATS:
        _, _, rows = png_read(os.path.join(PAL, 'trim', m + '.png'))
        mats[m] = [px for row in rows for px in row]
        if len(mats[m]) != len(key):
            print(f'!! 调色板长度不一致: {m} {len(mats[m])} vs {len(key)}')
    print(f'调色板: {kw}x{kh}, {len(key)} 色')

    # 2) 基础图案
    base_dir = os.path.join(RP, 'assets/trimmable_tools/textures/trims/items')
    bases = []
    for root, _, files in os.walk(base_dir):
        for f in files:
            if f.endswith('.png'):
                bases.append(os.path.relpath(os.path.join(root, f), base_dir)[:-4])
    bases.sort()
    print(f'基础图案: {len(bases)} 张')

    # 3) 烘焙
    out_dir = os.path.join(RP, 'assets/minecraft/textures/item/trimmable')
    baked = 0
    for rel in bases:
        w, h, rows = png_read(os.path.join(base_dir, rel + '.png'))
        flat = [px for row in rows for px in row]
        for m in MATS:
            lut = {key[i]: mats[m][i] for i in range(len(key)) if key[i][3] != 0}
            new_flat = [lut.get(px, px) for px in flat]
            new_rows = [new_flat[y * w:(y + 1) * w] for y in range(h)]
            png_write(os.path.join(out_dir, f'{rel}_{m}.png'), w, h, new_rows)
            baked += 1
    print(f'烘焙贴图: {baked} 张 → assets/minecraft/textures/item/trimmable/')

    # 4) 模型改指向
    mdir = os.path.join(RP, 'assets/trimmable_tools/models/item')
    changed = 0
    for name in os.listdir(mdir):
        if not name.endswith('.json'):
            continue
        p = os.path.join(mdir, name)
        d = json.load(open(p, encoding='utf-8'))
        tex = d.get('textures') or {}
        hit = False
        for k, v in list(tex.items()):
            if isinstance(v, str) and v.startswith('trimmable_tools:trims/items/'):
                tex[k] = v.replace('trimmable_tools:trims/items/', 'minecraft:item/trimmable/', 1)
                hit = True
        if hit:
            with open(p, 'w', encoding='utf-8') as f:
                json.dump(d, f, ensure_ascii=False, separators=(',', ':'))
            changed += 1
    print(f'模型改指向: {changed} 个')

    # 5) 旧的基础图案贴图（不再被引用）删掉，避免和图集源冲突
    import shutil
    shutil.rmtree(base_dir, ignore_errors=True)
    print('已删除未引用的 textures/trims/items/')
    return 0


if __name__ == '__main__':
    sys.exit(main())
