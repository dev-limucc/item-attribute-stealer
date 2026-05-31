package dev.limucc.itemattributestealer.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.limucc.itemattributestealer.client.config.ConfigManager;
import dev.limucc.itemattributestealer.client.config.ModConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Two tabs: Info and Settings.
 *
 * Settings tab has a Mode dropdown (WEAPON / SLOT).
 * Weapon settings are shown only while Mode = WEAPON.
 * Slot settings are shown only while Mode = SLOT.
 * Extra features (Empty Hand, Fortune Crops) are always visible.
 * Spear Reach appears only when Mode = WEAPON and Take Attrib From = SPEAR.
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::buildConfigScreen;
    }

    private Screen buildConfigScreen(Screen parent) {
        ModConfig cfg = ConfigManager.get();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Item Attribute Stealer"))
                .setSavingRunnable(ConfigManager::save);

        ConfigEntryBuilder e = builder.entryBuilder();

        // ══ INFO TAB ══════════════════════════════════════════════════════════
        ConfigCategory info = builder.getOrCreateCategory(Component.literal("Info"));

        info.addEntry(e.startTextDescription(
                Component.literal("§lItem Attribute Stealer")).build());
        info.addEntry(e.startTextDescription(
                Component.literal("Exploits MC-28289 to merge two weapons' attributes in one hit.")).build());
        info.addEntry(e.startTextDescription(
                Component.literal(" ")).build());
        info.addEntry(e.startTextDescription(
                Component.literal("§eKeybind: H§r — toggles the mod on/off in-game.")).build());
        info.addEntry(e.startTextDescription(
                Component.literal("Rebind it anytime in Options → Controls → Gameplay.")).build());
        info.addEntry(e.startTextDescription(
                Component.literal(" ")).build());
        info.addEntry(e.startTextDescription(
                Component.literal("§eWeapon Mode§r — auto-detects your held weapon.")).build());
        info.addEntry(e.startTextDescription(
                Component.literal("§eSlot Mode§r — swaps two specific hotbar slots.")).build());
        info.addEntry(e.startTextDescription(
                Component.literal(" ")).build());
        info.addEntry(e.startTextDescription(
                Component.literal("§cFor singleplayer and servers you own only.")).build());
        info.addEntry(e.startTextDescription(
                Component.literal("Anti-cheat servers will block the swaps.")).build());

        // ══ SETTINGS TAB ═════════════════════════════════════════════════════
        ConfigCategory settings = builder.getOrCreateCategory(Component.literal("Settings"));

        // ── Master toggle ─────────────────────────────────────────────────────
        settings.addEntry(e
                .startBooleanToggle(Component.literal("Enable"), cfg.enabled)
                .setDefaultValue(false)
                .setTooltip(Component.literal("Master on/off. Press H in-game to toggle quickly."))
                .setSaveConsumer(v -> cfg.enabled = v)
                .build());

        // ── Mode selector — drives all conditional display below ───────────────
        var modeEntry = e
                .startEnumSelector(Component.literal("Mode"), ModConfig.Mode.class, cfg.mode)
                .setDefaultValue(ModConfig.Mode.WEAPON)
                .setTooltip(Component.literal(
                        "WEAPON — triggers on a specific weapon type you hold.\n" +
                        "SLOT   — triggers when holding a specific hotbar slot."))
                .setSaveConsumer(v -> cfg.mode = v)
                .build();
        settings.addEntry(modeEntry);

        // ══ WEAPON MODE SETTINGS (hidden when Mode = SLOT) ═══════════════════

        // Main Weapon — what you hold to trigger the swap (ANY = any tool)
        var heldEntry = e
                .startEnumSelector(Component.literal("Main Weapon"), ModConfig.HeldKind.class, cfg.heldKind)
                .setDefaultValue(ModConfig.HeldKind.ANY)
                .setTooltip(Component.literal(
                        "The weapon type you must hold to trigger the swap.\n" +
                        "ANY = fires when holding ANY tool (sword, axe, pickaxe, etc.)"))
                .setSaveConsumer(v -> cfg.heldKind = v)
                .build();
        heldEntry.setDisplayRequirement(
                Requirement.isTrue(() -> modeEntry.getValue() == ModConfig.Mode.WEAPON));
        settings.addEntry(heldEntry);

        // Take Attrib From — weapon in hotbar to copy attributes from (no ANY)
        var copyEntry = e
                .startEnumSelector(Component.literal("Take Attrib From"), ModConfig.CopyKind.class, cfg.copyKind)
                .setDefaultValue(ModConfig.CopyKind.SWORD)
                .setTooltip(Component.literal(
                        "Weapon type in your hotbar whose attributes are merged.\n" +
                        "The mod finds the first matching item in your hotbar automatically."))
                .setSaveConsumer(v -> cfg.copyKind = v)
                .build();
        copyEntry.setDisplayRequirement(
                Requirement.isTrue(() -> modeEntry.getValue() == ModConfig.Mode.WEAPON));
        settings.addEntry(copyEntry);

        // ══ SLOT MODE SETTINGS (hidden when Mode = WEAPON) ═══════════════════

        var slotAEntry = e
                .startIntField(Component.literal("Slot 1  (your weapon)"), cfg.slotA)
                .setDefaultValue(1)
                .setMin(1).setMax(9)
                .setTooltip(Component.literal(
                        "Hotbar slot you hold to trigger the swap (1 = leftmost)."))
                .setSaveConsumer(v -> cfg.slotA = Math.max(1, Math.min(9, v)))
                .build();
        slotAEntry.setDisplayRequirement(
                Requirement.isTrue(() -> modeEntry.getValue() == ModConfig.Mode.SLOT));
        settings.addEntry(slotAEntry);

        var slotBEntry = e
                .startIntField(Component.literal("Slot 2  (take attrib from)"), cfg.slotB)
                .setDefaultValue(2)
                .setMin(1).setMax(9)
                .setTooltip(Component.literal(
                        "Hotbar slot whose attributes are merged into the attack (1–9)."))
                .setSaveConsumer(v -> cfg.slotB = Math.max(1, Math.min(9, v)))
                .build();
        slotBEntry.setDisplayRequirement(
                Requirement.isTrue(() -> modeEntry.getValue() == ModConfig.Mode.SLOT));
        settings.addEntry(slotBEntry);

        // ══ EXTRA FEATURES — always visible ══════════════════════════════════

        settings.addEntry(e.startTextDescription(
                Component.literal("§8─────────── Extra Features ───────────")).build());

        settings.addEntry(e
                .startBooleanToggle(Component.literal("Empty Hand Mode"), cfg.emptyHandUsage)
                .setDefaultValue(false)
                .setTooltip(Component.literal(
                        "When ON: the attrib swap fires even when your hand is empty.\n" +
                        "Lets you benefit from a tool's attributes without using its durability.\n" +
                        "In Weapon Mode with ANY selected, this is required for empty-hand hits."))
                .setSaveConsumer(v -> cfg.emptyHandUsage = v)
                .build());

        settings.addEntry(e
                .startBooleanToggle(Component.literal("Fortune for Crops"), cfg.useFortuneForCrops)
                .setDefaultValue(false)
                .setTooltip(Component.literal(
                        "Breaking a crop (wheat, carrots, potatoes, beetroot, nether wart)\n" +
                        "auto-swaps to the highest-Fortune tool in your hotbar for the break.\n" +
                        "Works with empty hand too (Empty Hand Mode does NOT need to be ON).\n" +
                        "Crops are insta-break so no durability is spent on the Fortune tool."))
                .setSaveConsumer(v -> cfg.useFortuneForCrops = v)
                .build());

        // Spear Reach — only shown when Mode = WEAPON and Take Attrib From = SPEAR
        var spearEntry = e
                .startBooleanToggle(Component.literal("Spear Reach  [experimental]"), cfg.spearReach)
                .setDefaultValue(false)
                .setTooltip(Component.literal(
                        "Extends your targeting range to spear reach while a spear is in your hotbar.\n" +
                        "Only available when Take Attrib From = SPEAR.\n" +
                        "⚠ Server validates reach — far hits may whiff on strict servers."))
                .setSaveConsumer(v -> cfg.spearReach = v)
                .build();
        spearEntry.setDisplayRequirement(Requirement.isTrue(() ->
                modeEntry.getValue() == ModConfig.Mode.WEAPON
                && copyEntry.getValue() == ModConfig.CopyKind.SPEAR));
        settings.addEntry(spearEntry);

        return builder.build();
    }
}
