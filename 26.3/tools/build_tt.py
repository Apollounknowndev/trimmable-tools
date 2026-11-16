#!/usr/bin/env python3
"""把 trimmable-tools 2.0.1（面向 1.21.4）适配成 MC 26.3 的数据包 + 材质包。

关键差异：
  * 数据包格式 data_major = 121.0，材质包格式 resource_major = 97.1（pack.mcmeta 里 pack_format 61 已过期）
  * 26.3 把 trim 调色板挪到 textures/palettes/ 下（palette_key 用 minecraft:trim_base，
    各个材质用 minecraft:trim/<name>），旧的 minecraft:trims/color_palettes/* 已不存在
  * 26.3 的 items 图集独立（assets/minecraft/atlases/items.json），不能再用 blocks.json 塞 item 贴图，
    且不能整个覆盖 items.json（会丢原版条目）→ 取原版 items.json 追加我们的 paletted_permutations 源
"""
import json, os, shutil, zipfile, sys

SRC = os.environ.get('TT_SRC', 'src')                # 已解包的原版 2.0.1
CLIENT = os.environ.get('TT_CLIENT', 'client-26.3.jar')  # 26.3 客户端 jar（仅 --atlas 时需要）
OUT = os.environ.get('TT_OUT', 'out')
DP_VER = [121, 0]                    # data_major/minor
RP_VER = [97, 1]                     # resource_major/minor
FIXED_TS = (1980, 1, 1, 0, 0, 0)      # 固定时间戳，保证 sha1 稳定
ATLAS = False                         # 是否生成 paletted_permutations 图集（默认关）

def load(p):
    with open(p, encoding='utf-8') as f:
        return json.load(f)

def dump(p, obj):
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, 'w', encoding='utf-8') as f:
        json.dump(obj, f, ensure_ascii=False, indent=2)
        f.write('\n')

def copytree(src, dst):
    for root, dirs, files in os.walk(src):
        rel = os.path.relpath(root, src)
        for name in files:
            s = os.path.join(root, name)
            d = os.path.join(dst, rel, name) if rel != '.' else os.path.join(dst, name)
            os.makedirs(os.path.dirname(d), exist_ok=True)
            shutil.copy2(s, d)

def zipdir(srcdir, outzip):
    if os.path.exists(outzip):
        os.remove(outzip)
    names = []
    for root, dirs, files in os.walk(srcdir):
        for name in files:
            full = os.path.join(root, name)
            names.append(os.path.relpath(full, srcdir).replace(os.sep, '/'))
    names.sort()
    with zipfile.ZipFile(outzip, 'w', zipfile.ZIP_DEFLATED) as z:
        for rel in names:
            zi = zipfile.ZipInfo(rel, date_time=FIXED_TS)
            zi.compress_type = zipfile.ZIP_DEFLATED
            zi.external_attr = 0o644 << 16
            with open(os.path.join(srcdir, rel), 'rb') as f:
                z.writestr(zi, f.read())
    return outzip

def main():
    global SRC, OUT, CLIENT, ATLAS
    argv = sys.argv[1:]
    if '-h' in argv or '--help' in argv:
        print(__doc__)
        print('用法: build_tt.py [--src DIR] [--out DIR] [--atlas] [--client JAR] [--zip-only]')
        return 0
    ziponly = '--zip-only' in argv
    if ziponly:
        argv = [a for a in argv if a != '--zip-only']
    if '--atlas' in argv:
        ATLAS = True
        argv = [a for a in argv if a != '--atlas']
    it = iter(argv)
    for a in it:
        if a == '--src': SRC = next(it)
        elif a == '--out': OUT = next(it)
        elif a == '--client': CLIENT = next(it)
        else:
            print(f'未知参数: {a}'); return 2
    print(f'SRC={SRC}\nOUT={OUT}\n图集模式={"paletted_permutations" if ATLAS else "预烘焙贴图（默认）"}')
    if ziponly:
        dpz = zipdir(os.path.join(OUT, 'datapack'), os.path.join(OUT, 'TrimmableTools-26.3-datapack.zip'))
        rpz = zipdir(os.path.join(OUT, 'resourcepack'), os.path.join(OUT, 'TrimmableTools-26.3-resourcepack.zip'))
        for z in (dpz, rpz):
            print(f'{os.path.basename(z)}: {os.path.getsize(z)} B')
        return 0
    if os.path.isdir(OUT):
        shutil.rmtree(OUT)
    dp = os.path.join(OUT, 'datapack')
    rp = os.path.join(OUT, 'resourcepack')

    # ---------- 数据包 ----------
    copytree(os.path.join(SRC, 'data'), os.path.join(dp, 'data'))
    for extra in ('pack.png', 'LICENSE'):
        s = os.path.join(SRC, extra)
        if os.path.exists(s):
            shutil.copy2(s, os.path.join(dp, extra))
    dump(os.path.join(dp, 'pack.mcmeta'), {
        'pack': {
            'description': ['Trimmable Tools (Apollo)', '适配 Minecraft 26.3 · 数据包'],
            'min_format': DP_VER,
            'max_format': [999, 0],
        }
    })

    # ---------- 材质包 ----------
    copytree(os.path.join(SRC, 'assets'), os.path.join(rp, 'assets'))
    for extra in ('pack.png', 'LICENSE'):
        s = os.path.join(SRC, extra)
        if os.path.exists(s):
            shutil.copy2(s, os.path.join(rp, extra))

    # 旧的 blocks.json 图集整体覆盖会在 26.3 里把原版方块贴图清空 → 删掉
    old_atlas = os.path.join(rp, 'assets/minecraft/atlases/blocks.json')
    if os.path.exists(old_atlas):
        os.remove(old_atlas)

    # 默认不用图集置换（贴图已由 bake_rp.py 预烘焙）：不写任何 atlases/*.json，
    # 这样服务端打包工具（Nexo 等）重写 items.json 时也不会丢掉本包的条目。
    # 想回到 paletted_permutations 方案时加 --atlas，需要 26.3 客户端 jar 提供原版 items.json。
    if ATLAS and os.path.exists(CLIENT):
        with zipfile.ZipFile(CLIENT) as z:
            vanilla_items = json.loads(z.read('assets/minecraft/atlases/items.json'))
        old = load(os.path.join(SRC, 'assets/minecraft/atlases/blocks.json'))['sources'][0]
        perms = {k: 'minecraft:trim/' + k for k in old['permutations']}
        added = {
            'type': 'minecraft:paletted_permutations',
            'palette_key': 'minecraft:trim_base',
            'permutations': perms,
            'textures': old['textures'],
        }
        vanilla_items['sources'].append(added)
        dump(os.path.join(rp, 'assets/minecraft/atlases/items.json'), vanilla_items)

    dump(os.path.join(rp, 'pack.mcmeta'), {
        'pack': {
            'description': ['Trimmable Tools (Apollo)', '适配 Minecraft 26.3 · 材质包'],
            'min_format': RP_VER,
            'max_format': [999, 0],
        }
    })

    # ---------- 一致性自检 ----------
    def rel(p, root):
        return os.path.relpath(p, root).replace(os.sep, '/')

    rp_root = rp
    problems = []
    # 1) item model 定义 -> 引用的 model 必须存在
    items_dir = os.path.join(rp_root, 'assets/trimmable_tools/items')
    models_dir = os.path.join(rp_root, 'assets/trimmable_tools/models')
    n_items = 0
    for name in sorted(os.listdir(items_dir)):
        n_items += 1
        d = load(os.path.join(items_dir, name))
        txt = json.dumps(d)
        # 收集所有 "model":"ns:path"
        import re
        for ns_path in set(re.findall(r'"model"\s*:\s*"([a-z0-9_.\-]+:[a-z0-9_./\-]+)"', txt)):
            ns, path = ns_path.split(':')
            if ns == 'trimmable_tools':
                if not os.path.exists(os.path.join(rp_root, 'assets', ns, 'models', path + '.json')):
                    problems.append(f'缺 model: {ns_path} (由 items/{name} 引用)')
    # 2) models 里的贴图引用
    tex_missing = set()
    for root, _, files in os.walk(models_dir):
        for name in files:
            if not name.endswith('.json'):
                continue
            d = load(os.path.join(root, name))
            layers = d.get('textures') or {}
            for _, tex in layers.items():
                if not isinstance(tex, str) or tex.startswith('#'):
                    continue
                ns, path = tex.split(':')
                if ns == 'minecraft':
                    continue
                if not os.path.exists(os.path.join(rp_root, 'assets', ns, 'textures', path + '.png')):
                    tex_missing.add(tex)
    # 3) 90 个基础图案贴图是否都在
    base_txt = os.path.join(rp_root, 'assets/trimmable_tools/textures/trims/items')
    n_base = sum(len(fs) for _, _, fs in os.walk(base_txt))

    print(f'数据包 recipe: {len(os.listdir(os.path.join(dp, "data/trimmable_tools/recipe")))} 个')
    print(f'材质包 items 定义: {n_items} 个；基础图案贴图: {n_base} 张')
    print(f'缺失 model 引用: {len(problems)}')
    print(f'缺失贴图引用: {len(tex_missing)} {sorted(tex_missing)[:5]}')

    dpz = zipdir(dp, os.path.join(OUT, 'TrimmableTools-26.3-datapack.zip'))
    rpz = zipdir(rp, os.path.join(OUT, 'TrimmableTools-26.3-resourcepack.zip'))
    for z in (dpz, rpz):
        print(f'{os.path.basename(z)}: {os.path.getsize(z)} B')
    return 0

if __name__ == '__main__':
    sys.exit(main())
