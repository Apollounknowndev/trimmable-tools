#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
rp-fix-263.py —— 修正第三方资源包里「按 1.21.x 写的、在 26.3 上会坏掉」的物品模型定义。

26.3 的变化（实测自官方客户端 assets/minecraft/items/*.json）：
  1. 原版 `filled_map` 的物品模型已经不再带 `tints`，`minecraft:map_color` 这个 tint
     类型也随之消失。凡是还写着 map_color tint 的包，filled_map 会整个模型解析失败，
     游戏里就表现为「地图贴图坏掉 / 变成一块方形色块」。
  2. `type` 字段必须是带命名空间的 id（`minecraft:model` / `minecraft:select` …），
     第三方打包工具常把 `minecraft:` 前缀省掉。

本脚本对指定目录里的 assets/**/items/**/*.json 做两件事：
  * 给缺命名空间的 `type` 补 `minecraft:`
  * 删掉 type 为 map_color 的 tint（连同它所在的那一条 tint 记录）

用法: rp-fix-263.py <解包后的资源包目录> [-n]
"""

import json
import os
import re
import sys

# 26.3 里合法、而且第三方包常省略命名空间的类型名
TYPE_NAMES = {
    "model", "select", "special", "composite", "range_dispatch", "empty",
    "condition", "with",
    "constant", "grass", "dye", "firework", "potion", "custom_model_data",
    "team", "map_color",
}

ITEMS_JSON = re.compile(r"assets/[^/]+/items/.*\.json$")


def namespacify(node, stats):
    if isinstance(node, dict):
        for key, value in list(node.items()):
            if key == "type" and isinstance(value, str) and ":" not in value and value in TYPE_NAMES:
                node[key] = "minecraft:" + value
                stats["namespaced"] += 1
            else:
                namespacify(value, stats)
    elif isinstance(node, list):
        for item in node:
            namespacify(item, stats)


def drop_map_color(node, stats):
    """递归删除 type=map_color 的 tint；返回是否把整条 tint 数组删空。"""
    if isinstance(node, dict):
        for key, value in list(node.items()):
            if key == "tints" and isinstance(value, list):
                kept = []
                for tint in value:
                    if isinstance(tint, dict) and str(tint.get("type", "")).endswith("map_color"):
                        stats["map_color"] += 1
                        continue
                    kept.append(tint)
                if kept:
                    node[key] = kept
                else:
                    del node[key]
            else:
                drop_map_color(value, stats)
    elif isinstance(node, list):
        for item in node:
            drop_map_color(item, stats)


def main():
    if len(sys.argv) < 2:
        print("用法: rp-fix-263.py <资源包目录> [-n]", file=sys.stderr)
        return 2
    root = sys.argv[1]
    dry = "-n" in sys.argv[2:]

    targets = []
    for base, _dirs, files in os.walk(os.path.join(root, "assets")):
        for name in files:
            if not name.endswith(".json"):
                continue
            full = os.path.join(base, name)
            rel = os.path.relpath(full, root).replace(os.sep, "/")
            if ITEMS_JSON.match(rel):
                targets.append(full)

    total = {"files": 0, "namespaced": 0, "map_color": 0}
    for full in sorted(targets):
        with open(full, encoding="utf-8") as fh:
            try:
                data = json.load(fh)
            except Exception as exc:
                print("  跳过（JSON 解析失败）%s: %s" % (os.path.relpath(full, root), exc))
                continue
        stats = {"namespaced": 0, "map_color": 0}
        namespacify(data, stats)
        drop_map_color(data, stats)
        if not stats["namespaced"] and not stats["map_color"]:
            continue
        total["files"] += 1
        total["namespaced"] += stats["namespaced"]
        total["map_color"] += stats["map_color"]
        print("  %s: 补命名空间 %d 处, 删 map_color tint %d 个"
              % (os.path.relpath(full, root), stats["namespaced"], stats["map_color"]))
        if not dry:
            with open(full, "w", encoding="utf-8", newline="\n") as fh:
                json.dump(data, fh, ensure_ascii=False, separators=(",", ":"))

    print("  共修正 %d 个物品定义文件（补命名空间 %d 处，删 map_color %d 个）%s"
          % (total["files"], total["namespaced"], total["map_color"],
             "（dry-run，未写入）" if dry else ""))
    return 0


if __name__ == "__main__":
    sys.exit(main())
