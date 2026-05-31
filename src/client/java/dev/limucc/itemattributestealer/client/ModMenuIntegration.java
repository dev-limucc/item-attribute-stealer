package dev.limucc.itemattributestealer.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.limucc.itemattributestealer.client.config.ConfigManager;
import dev.limucc.itemattributestealer.client.config.ModConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Registers our mod with ModMenu and builds the Cloth Config screen.
 *
 * Tabs:
 *   General    — master toggle, mode selector, keybind reminder
 *   Weapon Mode — heldKind, copyKind, emptyHandUsage, useFortuneForCrops, spearReach
 *   Slot Mode   — slotA and slotB number fields (1–9)
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

        // ── General ───────────────────────────────────────────────────────────
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

        general.addEntry(e
                .startBooleanToggle(Component.literal("Enable"), cfg.enabled)
                .setDefaultValue(false)
                .setTooltip(Component.literal("Master toggle. Also bindable to a key in Options → Controls."))
                .setSaveConsumer(v -> cfg.enabled = v)
                .build());

        general.addEntry(e
                .startEnumSelector(Component.literal("Mode"), ModConfig.Mode.class, cfg.mode)
                .setDefaultValue(ModConfig.Mode.SLOT)
                .setTooltip(Component.literal(
                        "SLOT: swap between two specific hotbar slots.\n" +
                        "WEAPON: auto-detect your held weapon type and find the copy-weapon."))
                .setSaveConsumer(v -> cfg.mode = v)
                .build());

        // ── Weapon Mode ───────────────────────────────────────────────────────
        ConfigCategory weapon = builder.getOrCreateCategory(Component.literal("Weapon Mode"));

        weapon.addEntry(e
                .startEnumSelector(Component.literal("Held Weapon"), ModConfig.HeldKind.class, cfg.heldKind)
                .setDefaultValue(ModConfig.HeldKind.ANY)
                .setTooltip(Component.literal(
                        "The type of weapon you hold to trigger the swap.\n" +
                        "ANY = any tool (sword, axe, mace, pickaxe, etc.)"))
                .setSaveConsumer(v -> cfg.heldKind = v)
                .build());

        weapon.addEntry(e
                .startEnumSelector(Component.literal("Copy Weapon"), ModConfig.CopyKind.class, cfg.copyKind)
                .setDefaultValue(ModConfig.CopyKind.SWORD)
                .setTooltip(Component.literal(
                        "The type of weapon in your hotbar whose attributes get merged.\n" +
                        "Must be present somewhere in your hotbar."))
                .setSaveConsumer(v -> cfg.copyKind = v)
                .build());

        weapon.addEntry(e
                .startBooleanToggle(Component.literal("Empty Hand Usage"), cfg.emptyHandUsage)
                .setDefaultValue(false)
                .setTooltip(Component.literal(
                        "When ON: if you're holding nothing, still swap to any tool in\n" +
                        "your hotbar. Lets you apply a tool's damage without spending its durability."))
                .setSaveConsumer(v -> cfg.emptyHandUsage = v)
                .build());

        weapon.addEntry(e
                .startBooleanToggle(Component.literal("Fortune for Crops"), cfg.useFortuneForCrops)
                .setDefaultValue(false)
                .setTooltip(Component.literal(
                        "When ON: breaking a crop auto-swaps to the highest-Fortune tool\n" +
                        "in your hotbar. Crops are instant-break, so no durability is used."))
                .setSaveConsumer(v -> cfg.useFortuneForCrops = v)
                .build());

        weapon.addEntry(e
                .startBooleanToggle(Component.literal("Spear Reach [EXPERIMENTAL]"), cfg.spearReach)
                .setDefaultValue(false)
                .setTooltip(Component.literal(
                        "Gives client-side spear reach if a spear is in your hotbar.\n" +
                        "⚠ Server validates reach separately — far hits may whiff on strict servers.\n" +
                        "For guaranteed range: hold the spear (heldKind = SPEAR)."))
                .setSaveConsumer(v -> cfg.spearReach = v)
                .build());

        // ── Slot Mode ─────────────────────────────────────────────────────────
        ConfigCategory slot = builder.getOrCreateCategory(Component.literal("Slot Mode"));

        slot.addEntry(e
                .startIntField(Component.literal("Slot A (held)"), cfg.slotA)
                .setDefaultValue(1)
                .setMin(1).setMax(9)
                .setTooltip(Component.literal("The hotbar slot you hold to trigger the swap (1–9)."))
                .setSaveConsumer(v -> cfg.slotA = Math.max(1, Math.min(9, v)))
                .build());

        slot.addEntry(e
                .startIntField(Component.literal("Slot B (copy from)"), cfg.slotB)
                .setDefaultValue(2)
                .setMin(1).setMax(9)
                .setTooltip(Component.literal("The hotbar slot whose attributes are merged (1–9)."))
                .setSaveConsumer(v -> cfg.slotB = Math.max(1, Math.min(9, v)))
                .build());

        return builder.build();
    }
}
