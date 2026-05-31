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
 * Two tabs — Weapon Mode and Slot Mode — are the two primary modes.
 *
 * Weapon Mode tab:  master toggle, mode selector, weapon settings.
 * Slot Mode tab:    slot A and B number inputs.
 *
 * The mode selector lives at the top of the Weapon Mode tab because that is
 * where you switch between the two modes.  Slot Mode tab only holds its two
 * slot numbers — nothing else.
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

        // ── Weapon Mode tab ───────────────────────────────────────────────────
        // This tab also holds the master toggle and mode selector so the user
        // can switch to Slot Mode from here without needing a separate General tab.
        ConfigCategory weapon = builder.getOrCreateCategory(Component.literal("Weapon Mode"));

        weapon.addEntry(e
                .startBooleanToggle(Component.literal("Enable"), cfg.enabled)
                .setDefaultValue(false)
                .setTooltip(Component.literal("Master on/off switch. Press H in-game to toggle quickly."))
                .setSaveConsumer(v -> cfg.enabled = v)
                .build());

        weapon.addEntry(e
                .startEnumSelector(Component.literal("Active Mode"), ModConfig.Mode.class, cfg.mode)
                .setDefaultValue(ModConfig.Mode.WEAPON)
                .setTooltip(Component.literal(
                        "WEAPON — auto-detect your held weapon and find the copy-weapon.\n" +
                        "SLOT   — swap between two fixed hotbar slots (configure in Slot Mode tab)."))
                .setSaveConsumer(v -> cfg.mode = v)
                .build());

        weapon.addEntry(e
                .startEnumSelector(Component.literal("Held Weapon"), ModConfig.HeldKind.class, cfg.heldKind)
                .setDefaultValue(ModConfig.HeldKind.ANY)
                .setTooltip(Component.literal(
                        "Which weapon type in your hand triggers the swap.\n" +
                        "ANY = any tool (sword, axe, mace, pickaxe, spear, shovel, hoe)."))
                .setSaveConsumer(v -> cfg.heldKind = v)
                .build());

        weapon.addEntry(e
                .startEnumSelector(Component.literal("Copy Weapon"), ModConfig.CopyKind.class, cfg.copyKind)
                .setDefaultValue(ModConfig.CopyKind.SWORD)
                .setTooltip(Component.literal(
                        "Weapon type in your hotbar whose attributes are merged into the attack.\n" +
                        "The mod searches your hotbar for the first match."))
                .setSaveConsumer(v -> cfg.copyKind = v)
                .build());

        weapon.addEntry(e
                .startBooleanToggle(Component.literal("Empty Hand Usage"), cfg.emptyHandUsage)
                .setDefaultValue(false)
                .setTooltip(Component.literal(
                        "When ON: even with an empty hand, swap to any tool in your hotbar.\n" +
                        "Lets you apply a tool's damage without spending its durability."))
                .setSaveConsumer(v -> cfg.emptyHandUsage = v)
                .build());

        weapon.addEntry(e
                .startBooleanToggle(Component.literal("Fortune for Crops"), cfg.useFortuneForCrops)
                .setDefaultValue(false)
                .setTooltip(Component.literal(
                        "Breaking a crop auto-swaps to your highest-Fortune tool for the break.\n" +
                        "Crops are instant-break so no durability is spent on the Fortune tool."))
                .setSaveConsumer(v -> cfg.useFortuneForCrops = v)
                .build());

        weapon.addEntry(e
                .startBooleanToggle(Component.literal("Spear Reach [EXPERIMENTAL]"), cfg.spearReach)
                .setDefaultValue(false)
                .setTooltip(Component.literal(
                        "Client-side reach extension when a spear is in your hotbar.\n" +
                        "⚠ The server validates reach too — far hits may whiff on strict servers.\n" +
                        "For guaranteed range: hold the spear (Held Weapon = SPEAR)."))
                .setSaveConsumer(v -> cfg.spearReach = v)
                .build());

        // ── Slot Mode tab ─────────────────────────────────────────────────────
        ConfigCategory slot = builder.getOrCreateCategory(Component.literal("Slot Mode"));

        slot.addEntry(e
                .startIntField(Component.literal("Slot A  (your weapon)"), cfg.slotA)
                .setDefaultValue(1)
                .setMin(1).setMax(9)
                .setTooltip(Component.literal("Hotbar slot you hold to trigger the swap (1 = leftmost)."))
                .setSaveConsumer(v -> cfg.slotA = Math.max(1, Math.min(9, v)))
                .build());

        slot.addEntry(e
                .startIntField(Component.literal("Slot B  (copy from)"), cfg.slotB)
                .setDefaultValue(2)
                .setMin(1).setMax(9)
                .setTooltip(Component.literal("Hotbar slot whose attributes are merged (1–9)."))
                .setSaveConsumer(v -> cfg.slotB = Math.max(1, Math.min(9, v)))
                .build());

        return builder.build();
    }
}
