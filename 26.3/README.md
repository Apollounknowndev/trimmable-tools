# Trimmable Tools - datapack / resource pack port for Minecraft **26.3**

The published datapack build (`trimmable-tools-2.0.1-datapack-and-resourcepack.zip`)
no longer works on 26.3. The **recipes are fine** - what breaks is the pack metadata
and the trim-texture pipeline. This folder contains a drop-in replacement:
one datapack + one resource pack, plus the scripts used to build them.

## Why the old pack stops working on 26.3

| # | Cause | Detail |
|---|---|---|
| 1 | **Pack formats changed** | The old zip declares `pack_format: 61, supported_formats: [46, 61]`. 26.3 expects **data pack 121.0** and **resource pack 97.1** (`pack_version` in the client's `version.json`: `data_major=121`, `resource_major=97`). Clients reject the whole pack, so nothing is applied at all. |
| 2 | **Armor-trim palettes moved** | `minecraft:trims/color_palettes/trim_palette` and `.../color_palettes/<material>` no longer exist. They now live at `assets/minecraft/textures/palettes/trim_base.png` and `assets/minecraft/textures/palettes/trim/<material>.png`. A `paletted_permutations` source that still points at the old paths produces no textures. |
| 3 | **Item atlas was split out** | 26.3 has a separate `assets/minecraft/atlases/items.json`. The old pack *overwrites* `atlases/blocks.json`, which would wipe the vanilla block atlas; and pack-merging tools (e.g. Nexo, which regenerates the client pack server-side) replace `atlases/items.json` with their own copy, silently dropping a third-party atlas source. |

## What this port does

* **Two separate packs**, each with correct metadata
  (`min_format`/`max_format`, data `[121,0]...[999,0]`, resource `[97,1]...[999,0]`).
* **No atlas edits at all.** Instead of `paletted_permutations`, all
  **90 patterns x 15 materials = 1350** trims are **pre-baked** into
  `assets/minecraft/textures/item/trimmable/<tool>/<pattern>_<material>.png`
  (16x16 RGBA, colours sampled from the vanilla 26.3 palettes). The vanilla
  `items` atlas picks them up through its `minecraft:directory` source, so no
  `atlases/*.json` override is needed and the pack survives being merged into a
  server-generated pack.
* **5940 item models** re-point their `layer1` texture from
  `trimmable_tools:trims/items/...` to `minecraft:item/trimmable/...`.
* **All 5940 `minecraft:smithing_transform` recipes are unchanged** - the
  recipe schema did not change in 26.3 (`trim` and `item_model` item components
  are still valid), verified with `/recipe give` and `item_model` component
  round-trips on a 26.3 server.

## Files

| File | Size | SHA-1 |
|---|---|---|
| `datapack/TrimmableTools-26.3-datapack.zip` | 2,154,177 B | `fec6888f4692328dce574f77b2aaa28295a7e646` |
| `resourcepack/TrimmableTools-26.3-resourcepack.zip` | 2,451,480 B | `d217082061837dbd56e3aed3215720aa5340393d` |

* Datapack -> any `world/datapacks/` folder (or `config/` on Fabric/NeoForge datapack loading).
* Resource pack -> ship it as a normal resource pack, or merge it into a server-side
  generated pack (copy the files, do **not** copy any `atlases/` file).

## Rebuilding

```bash
# 1) extract the original 2.0.1 pack
unzip trimmable-tools-2.0.1-datapack-and-resourcepack.zip -d src

# 2) split it into the two packs (keeps the recipes, fixes pack.mcmeta,
#    drops the old blocks.json atlas override)
python3 tools/build_tt.py --src src --out out

# 3) bake the trim textures (replaces paletted_permutations entirely)
python3 tools/bake_rp.py --rp out/resourcepack --palettes palettes

# 4) repack deterministically (zips what is currently in out/, does not rebuild)
python3 tools/build_tt.py --out out --zip-only
```

`--palettes` must contain the vanilla 26.3 palettes and trim sprites:

```
palettes/trim_base.png
palettes/trim/<material>.png        # 15 files (incl. *_darker variants)
```

The 90 base trim sprites are read from the extracted pack itself, so nothing else
is needed.

> Note: rebuilding reproduces these artifacts byte-for-byte, with one cosmetic
> exception - a trailing newline in the resource pack's `pack.mcmeta` (the zips
> shipped here were produced by the same scripts, one build earlier). Both scripts are dependency-free (the PNG codec is built in - no Pillow),
and packaging is deterministic (fixed timestamps), so re-running the build on the
same inputs yields the same SHA-1s.

## What is generated

| | count |
|---|---|
| recipes (`smithing_transform`) | 5,940 |
| item models | 5,940 |
| item definitions (`assets/trimmable_tools/items`) | 540 |
| baked trim textures (`assets/minecraft/textures/item/trimmable`) | 1,350 |

Tool variants: 6 tiers (wooden, stone, iron, golden, diamond, netherite) x 5 tools
(sword, pickaxe, axe, shovel, hoe) = **30**.
Trim patterns: **18** (`bolt, coast, dune, eye, flow, host, raiser, rib, sentry,
shaper, silence, snout, spire, tide, vex, ward, wayfinder, wild`).
Trim materials in the recipes: **11** (`amethyst, copper, diamond, emerald, gold,
iron, lapis, netherite, quartz, redstone, resin`); the baked textures cover the
**15** palette files (the extra ones are the `*_darker` variants used by vanilla
armor trims).

## Two more traps when the pack is merged server-side

If the pack is not shipped as-is but **merged into a generated pack** (Nexo, ItemsAdder,
`pack.zip` pipelines, anything that re-serialises JSON), two things break in practice and
both are silent - the client just renders the affected item as a broken square:

| Symptom | Cause | Fix |
|---|---|---|
| A vanilla item (e.g. `filled_map`) turns into a square blob | Some third-party pack still ships a 1.21.x `assets/minecraft/items/<item>.json` with `{"type": "minecraft:map_color"}` tints. **26.3 removed the `map_color` tint type** - vanilla `filled_map.json` is now just `{"model":{"type":"minecraft:model","model":"minecraft:item/filled_map"}}` - so the whole item model fails to parse. | Drop `map_color` tints from overridden item definitions. |
| Any custom item renders as a broken square | Merging tools re-serialise item model JSON and **strip the namespace from `type`** (`minecraft:select` -> `select`). 26.3 rejects unknown type ids, so the model is discarded. | Re-add `minecraft:` to every `type` value. |

`tools/fix-itemmodels.py` fixes both in one pass over an extracted pack:

```bash
python3 tools/fix-itemmodels.py /path/to/extracted-pack          # apply
python3 tools/fix-itemmodels.py /path/to/extracted-pack -n       # dry-run
```

It walks `assets/*/items/**/*.json`, namespaces every `type` value and removes
`map_color` tints (2391 files / 13333 namespace fixes / 3 tints on the pack we ship).
Run it **before** zipping/touching timestamps, so the pack hash stays stable.
