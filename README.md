# Item Attribute Stealer

A Fabric client mod for **Minecraft 26.1.2** that replicates the vanilla
attribute-swap exploit ([MC-28289](https://mojira.dev/MC-28289)).

When enabled, hitting an entity automatically performs a same-tick weapon swap
between two hotbar slots, merging the attributes and enchantments of both weapons
for that attack. On success the message **"Magic attribute merge has been used!"**
appears on screen.

All settings are configurable in-game through **ModMenu**.

---

## ⚠️ Responsible Use

- ✅ Singleplayer and servers you own
- ❌ Public competitive servers — likely against their rules
- ❌ Servers with anti-cheat — will be detected and blocked (expected)

---

## Requirements

| | Version |
|---|---|
| Minecraft | 26.1.2 |
| Fabric Loader | 0.19.2+ |
| Fabric API | 0.150.0+26.1.2 |
| [Cloth Config](https://modrinth.com/mod/cloth-config) | 26.1.x |
| [ModMenu](https://github.com/TerraformersMC/ModMenu) | 18.x |
| Java | 25 |

---

## Installation

Drop the mod JAR into your `.minecraft/mods/` folder alongside Fabric API,
Cloth Config, and ModMenu.

### Build from source

```bash
git clone https://github.com/dev-limucc/item-attribute-stealer.git
cd item-attribute-stealer
.\gradlew.bat build
```

The JAR lands in `build/libs/`.

---

## Configuration

Open **ModMenu → Item Attribute Stealer → Config**:

- **General** — enable / disable the mod
- **Slots** — choose which two hotbar slots are your Weapon A and Weapon B
- **Feedback** — toggle the success message and pick Action Bar / Chat / Toast

---

## Credits

Built by **[Limucc-dev](https://github.com/dev-limucc)**.  
Exploit documented at [MC-28289](https://mojira.dev/MC-28289).
