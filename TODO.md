# Nordic

A NeoForge mod that adds Nordic biomes, plants, creatures and folklore.

Author: Jesper-Andersson

| Minecraft | Loader          | Dependencies                                                                               |
|-----------|-----------------|--------------------------------------------------------------------------------------------|
| 1.21.1    | NeoForge 21.1.x | GeckoLib 4.9.3, TerraBlender, Lithostitched 1.8.0 (+ Apollib 1.2.0), SmartBrainLib 1.16.11 |

**Localisation:** English, Swedish, Ukrainian

---

## Status

Items marked 🚧 have been started (e.g. a model exists) but are not wired up in game yet.

### Biomes
- [x] Pine forest
- [x] Spruce/pine forest
- [x] Bog
- [x] Lake (custom lake basins)
- [x] River
- [x] Birch/pine forest
- [x] Nordic plains (vanilla plains vegetation with rare fallen birches)
- [x] Tundra (sparse small pines, heather, berries)
- [x] Fallen trees (pine and birch, with stumps, moss and mushrooms) in all Nordic land biomes
- [x] Snake fern replaces ferns in vanilla taigas; lingonberries, blueberries and chantarelle in non-snowy taigas

### Trees
- [x] Pine: log, planks, leaves, sapling, tree feature with ground decorations
- [x] Spruce variant for the spruce/pine forest
- [x] Pine bark blending (bark → peeling → peeled red log up the trunk, also on fallen pines; placeholder textures)
- [ ] Beech
- [ ] Juniper → juniper berry → juniper drink (enbärsdricka)

### Plants and blocks
- [x] Lingonberry shrub and lingonberries
- [x] Blueberry shrub and blueberries
- [x] Chantarelle
- [x] Pine needles (layered ground cover, like pink petals)
- [x] Reed
- [x] Cattail (placeholder art)
- [x] Heather (placeholder, tinted short grass)
- [x] Peat (furnace fuel)
- [ ] Wild strawberry
- [x] Cloudberry shrub and cloudberries (bog only)
- [x] Cotton grass
- [x] Sundew (decorative)
- [x] Peat iron ore (bog, smelts into raw iron)

### Animals
- [x] Pike: raw/cooked, bucket, spawn egg, spawns in bog, lake and river
- [x] Roach: raw/cooked, bucket, spawn egg, spawns in bog, lake and river
- [ ] 🚧 Perch, using the bass model

### Monsters
- [x] Barrow wight: animated model with glowmask, spawn egg, loot, spawns in bog, lake and river
- [ ] 🚧 Tomte / Nisse (model and texture only)
- [ ] 🚧 Gloson (model and texture only)

### Food
- [x] Cooked pike and cooked roach (furnace, smoker, campfire)
- [ ] Blood pudding: bottle + pig → bottle of blood
- [ ] Pickled herring

### Structures
- [ ] Runestone
- [ ] Ship burial

### Compatibility
- [x] Shader packs via Iris (see `shaderpack-compat/`)
- [ ] Farmer's Delight
- [ ] Create

### Biome preview pictures
The biome menu shows a picture of each biome. To (re)take the shipped pictures:
1. Run `Client`, create a fresh creative world, render distance 12+, shaders off.
2. Run `/nordic_preview capture nordic` and `/nordic_preview capture minecraft` (or `capture` for everything; `cancel` stops a run). Cave biomes are skipped.
3. Check the PNGs in `run/config/nordic/biome_previews/<ns>/`, retake or replace bad shots.
4. Copy them to `src/main/resources/assets/nordic/textures/gui/biome_preview/<ns>/` and delete the config copies.

Players can run the same command for other mods' biomes; those pictures stay in their config folder.

---

## Considered APIs

### Keep
- **TerraBlender:** still the best way to inject overworld biomes on NeoForge 1.21.1. Fjäll/heath and the skärgård
  become more boxes in `BiomeRegion`. Test alongside Terralith and Tectonic.
- **GeckoLib:** already integrated, and pairs well with SmartBrainLib.

### Built into NeoForge (no new dependency)
- **Data maps:** `neoforge:furnace_fuels` for peat, `neoforge:compostables` for berries, chantarelle, reed and cotton grass.
- **Biome modifiers:** add pike/roach spawns to vanilla rivers, and scatter berries and erratics into vanilla taiga.
- **Worldgen datagen (`DatapackBuiltinEntriesProvider`):** replace the hand-written feature and biome JSON.
- **Jigsaw structures:** runestone, ship setting, barrow mound, longhouse ruin and hillfort, all data-driven.
- **`neoforge:mod_loaded` conditions:** optional compat recipes without hard dependencies.

### Adopted
- **SmartBrainLib:** brain-based mob AI (sensors, memories with expiry, activities) instead of vanilla goals. The
  barrow wight runs on it. Next up are the stateful mobs: moose (calm → provoked → charge → cooldown), troll
  (night/day stone), tomte (porridge/prank mood), skogsrå, lyktgubbe and gloson. Its attack behaviours can delay damage
  until the GeckoLib animation's hit frame. Fish and simple animals can stay on vanilla goals.
- **Lithostitched (+ Apollib):** data-driven worldgen modifiers that go beyond NeoForge biome modifiers. Surface rules
  now live in `data/nordic/lithostitched/worldgen_modifier/surface_rules.json` instead of TerraBlender's
  `SurfaceRuleManager`. TerraBlender still handles the biome regions. Next: add barrows and runestones to existing
  structure sets and village template pools, set structure spacing and prevent structure overlap. Tectonic depends on
  it, so it helps compat.
  - `LakeBasin` stays on `ChunkMapMixin`/`RandomStateAccessor`. Lithostitched's `wrap_noise_router` only gets
    `RegistryAccess` and runs before `RandomState` exists, but the shore field needs the level's `BiomeSource`, the
    climate sampler and the original densities. The mixin runs after Lithostitched's wrapping, so the two coexist.

### Worth adding
- **JEI or EMI:** recipe viewing for the cooking chain.
- **Jade:** tooltips for tomte mood, fish-trap contents and peat fuel.
- **Farmer's Delight:** cutting board and cooking pot recipes for fish, chantarelle and berries (data-only).
- **Create:** crushing peat iron ore and milling berries/juniper (data-only).
- **Serene Seasons:** ice fishing, berry seasons and bear hibernation.
- **Patchouli:** an in-game folklore book (optional flavour).

### Not needed
- **Cloth Config:** NeoForge `ModConfigSpec` and its built-in config screen already cover `Config.java`.

---

## Ideas backlog

### Biomes and terrain
- **Fjäll / heath:** bare mountain tundra with low shrubs and lichen-covered stone, fading into heather heath
  (ljunghed) with juniper. A home for reindeer, lemmings, the troll and adders.
- **Archipelago (skärgård):** smooth granite skerries with pine trees on top, between lakes and the sea. A home for
  seals and herring.
- **Glacial erratics (flyttblock):** huge lone boulders as a world-gen feature.

### Bog
- **Bog body or bog oak:** a rare buried structure. Bog oak gives dark, preserved "ancient wood" planks, and could
  come with a sacrificial hoard chest.
- **Lyktgubbe / will-o'-the-wisp:** a floating light at night that lures players towards pools. It could lead to
  hidden loot, or into deep water.
- **Crane (trana):** a passive wading bird with a spring dance animation.
- **Mosquito swarm:** particle-based nuisance in bogs. Could be kept away with tar or smoke.

### Pine and spruce forests
- **Moose (älg):** a large neutral mob that charges if provoked. It drops hide and antlers, and the antlers could be a
  wall trophy block.
- **Lynx:** a stealthy predator that hunts hares and grouse.
- **Brown bear:** stronger than the vanilla polar bear. It sleeps in dens in winter and loves the existing berries.
- **Capercaillie (tjäder):** a ground bird that drops feathers and meat.
- **Troll:** a big night mob that turns to stone in sunlight and leaves a mossy boulder. Fits forests and mountain
  edges.
- **Skogsrå (forest spirit):** passive at a distance. It trades or curses depending on whether the player has been
  cutting trees.
- **Tomte / vätte:** a tiny mob that guards a structure and helps if you leave it porridge, otherwise it pranks you.
- **Lichen and reindeer moss:** ground cover that could be reindeer food.
- **Stinging nettle:** hurts to walk through, and makes nettle soup.
- **Slanted rail fence (gärdesgård):** a fence block.
- **Red squirrel:** eats pine cones. Pine cones could be a new drop from pine leaves.
- **Black woodpecker:** drums on pine logs.
- **Gloson:** a glowing-eyed sow that charges between your legs at crossroads at night (model and texture exist).
- **Lingonberry jam, blueberry soup (blåbärssoppa), mushroom stew with chantarelle:** a cooking chain that reuses
  existing items.

### Lakes and rivers
- **Perch (abborre):** wire up the bass model as a perch.
- **Zander (gös), brown trout and Arctic char:** more fish. Char could be lake-only in cold areas.
- **Freshwater crayfish (kräftor):** a bottom crawler you catch with traps, which ties into a crayfish party
  (kräftskiva) feast item.
- **Fish trap / fyke net:** a placeable block that slowly collects fish from lake water.
- **Ice fishing:** fishing through an ice hole in frozen lakes.
- **Otter and beaver:** beavers could build dams across narrow river stretches.
- **White-tailed eagle:** dives for fish in lakes.
- **Herring:** a fish for the archipelago, used for pickled herring.
- **Lindworm (lindorm):** a wingless serpent boss living in deep lakes or bogs.

### Iron Age and Viking (ties into the barrow wight)
- **Barrow mound (gravhög):** a structure that spawns barrow wights, with grave goods inside.
- **Runestones:** carvable blocks with rune variants. A world-gen version could mark barrows.
- **Ship setting (skeppssättning):** a ring of standing stones in a ship shape.
- **Hillfort (fornborg), stone labyrinth (trojeborg) and longhouse ruin:** structures.
- **Rock carvings (hällristningar):** carved blocks on flat bedrock.
- **Sacrificial spring (offerkälla):** throw in items for a luck effect.
