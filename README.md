# Item Attribute Stealer

A **learning-first** Fabric client mod for **Minecraft 26.1.2** that replicates the
well-known vanilla exploit [MC-28289](https://mojira.dev/MC-28289) ("attribute swapping" /
"sword swapping").

When enabled, hitting any entity automatically swaps your held hotbar slot to a second
weapon and back **within the same game tick**, causing the server to evaluate the attack
with the merged attributes (damage, attack speed) and enchantments of both weapons.

On a successful swap the client shows: **"Magic attribute merge has been used!"**

---

## ⚠️ Responsible Use

This mod exploits a vanilla Minecraft bug.

- ✅ Singleplayer and your own private servers — go wild.
- ✅ Lab / learning environment — that's exactly what this is for.
- ❌ Public competitive servers — almost certainly against their rules.
- ❌ Anti-cheat servers (running AntiSwap or modern anti-cheat) — will be detected and blocked. That's expected behaviour.

**Please do not use this to grief other players.**

---

## How It Works

Minecraft processes incoming network packets sequentially within each server tick.
When the client sends:

```
[tick N start]
  → ServerboundSetCarriedItemPacket(slot B)   ← swap to weapon B
  → ServerboundPlayerActionPacket (attack)    ← hit entity
  → ServerboundSetCarriedItemPacket(slot A)   ← swap back to weapon A
[tick N end]
```

…all three packets are processed in a single tick. The server's damage calculation
reads the attribute state in an intermediate ordering, resulting in attributes from
one weapon and enchantments from another being active simultaneously — the "merge".

This mod sends those extra slot-change packets automatically around every left-click
attack when enabled.

---

## Features

- **Master toggle** — enable/disable the mod without restarting
- **Slot picker** — configure which two hotbar slots are your Weapon A and Weapon B
- **Safety check** — optionally require both slots to be non-empty before firing
- **Feedback** — "Magic attribute merge has been used!" shown as Action Bar, Chat, or Toast
- **Cloth Config + ModMenu** — all settings accessible in-game via the Mods screen

---

## Installation

### Prerequisites

| Tool | Version |
|---|---|
| Java | **25** |
| Minecraft | **26.1.2** |
| Fabric Loader | **0.19.2+** |
| Fabric API | **0.150.0+26.1.2** |

Also install:
- [Cloth Config API](https://modrinth.com/mod/cloth-config) for 26.1.2
- [ModMenu](https://github.com/TerraformersMC/ModMenu) for 26.1

### From source

```bash
# Clone the repo
git clone https://github.com/Limucc-dev/item-attribute-stealer.git
cd item-attribute-stealer

# Build (downloads MC 26.1.2 dev environment on first run — may take a few minutes)
./gradlew build

# The built mod JAR lands in build/libs/
```

To run the mod in a dev client:
```bash
./gradlew runClient
```

---

## Project Structure (learning guide)

```
src/
  main/                               ← Common code (server + client)
    java/dev/limucc/itemattributestealer/
      ItemAttributeStealer.java       ← Mod ID, shared logger
    resources/
      fabric.mod.json                 ← Mod metadata, entrypoints, deps

  client/                             ← Client-only code
    java/dev/limucc/itemattributestealer/client/
      ItemAttributeStealerClient.java ← Client entrypoint (loads config)
      ModMenuIntegration.java         ← Cloth Config screen via ModMenu
      config/
        ModConfig.java                ← Config fields (POJO)
        ConfigManager.java            ← Load / save JSON to disk
      swap/
        AttributeSwapper.java         ← The swap logic + feedback
      mixin/
        MultiPlayerGameModeMixin.java ← @Inject into vanilla attack method
    resources/
      item_attribute_stealer.client.mixins.json
```

### Key concepts this project teaches

| Concept | Where to look |
|---|---|
| Mod entrypoints | `fabric.mod.json` + `ItemAttributeStealer`, `ItemAttributeStealerClient` |
| Fabric Loom / Gradle setup | `build.gradle`, `gradle.properties` |
| Mojang mappings (26.1+ style) | `build.gradle` — note: no `mappings` line |
| Mixins | `MultiPlayerGameModeMixin.java` |
| Cloth Config UI | `ModMenuIntegration.java` |
| Config persistence | `ConfigManager.java` (Gson + FabricLoader paths) |
| Networking (vanilla packets) | `AttributeSwapper.java` — `ServerboundSetCarriedItemPacket` |

---

## Contributing

PRs welcome! Ideas for future learning additions:
- [ ] Configurable cooldown between swaps
- [ ] Key-bind toggle (no need to open ModMenu)
- [ ] HUD overlay showing current swap state
- [ ] Server-side detection avoidance metrics (for research)

---

## Credits

Built by **[Limucc-dev](https://github.com/dev-limucc)**.  
Powered by [Fabric](https://fabricmc.net/), [Cloth Config](https://github.com/shedaniel/cloth-config),
and [ModMenu](https://github.com/TerraformersMC/ModMenu).  
Exploit documented at [MC-28289](https://mojira.dev/MC-28289).
