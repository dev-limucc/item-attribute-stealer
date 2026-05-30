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
 * Registers our mod with ModMenu so a "Config" button appears on the mod list.
 *
 * Learning note:
 * ModMenuApi is a Fabric entrypoint interface.  We declared the class in
 * fabric.mod.json under "modmenu".  ModMenu calls getModConfigScreenFactory()
 * to know how to open our settings screen.
 *
 * Cloth Config provides ConfigBuilder — a fluent builder that produces a fully
 * styled Minecraft Screen with categories (tabs), labels, sliders, toggles, etc.
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // The lambda receives the parent screen so Cloth Config can return to it
        // when the user clicks "Done" or presses Escape.
        return this::buildConfigScreen;
    }

    /**
     * Builds the Cloth Config screen.
     *
     * Tabs (categories):
     *  1. General  — master toggle
     *  2. Slots    — pick which hotbar slots are weapon A and B
     *  3. Feedback — message toggle + style selector
     *
     * @param parent the screen to return to when this screen is closed
     */
    private Screen buildConfigScreen(Screen parent) {
        ModConfig cfg = ConfigManager.get();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Item Attribute Stealer — Settings"))
                .setSavingRunnable(ConfigManager::save);  // called when user clicks "Save"

        ConfigEntryBuilder entry = builder.entryBuilder();

        // ── Tab 1: General ────────────────────────────────────────────────────
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

        general.addEntry(entry
                .startBooleanToggle(Component.literal("Enable Mod"), cfg.enabled)
                .setDefaultValue(false)
                .setTooltip(Component.literal("Master switch. When off the mod does nothing."))
                .setSaveConsumer(val -> cfg.enabled = val)
                .build());

        // ── Tab 2: Slots ──────────────────────────────────────────────────────
        ConfigCategory slots = builder.getOrCreateCategory(Component.literal("Slots"));

        slots.addEntry(entry
                .startIntSlider(Component.literal("Weapon Slot A"), cfg.weaponSlotA, 0, 8)
                .setDefaultValue(0)
                .setTooltip(Component.literal("Hotbar slot you normally hold (0 = leftmost)."))
                .setSaveConsumer(val -> cfg.weaponSlotA = val)
                .build());

        slots.addEntry(entry
                .startIntSlider(Component.literal("Weapon Slot B"), cfg.weaponSlotB, 0, 8)
                .setDefaultValue(1)
                .setTooltip(Component.literal("Hotbar slot that gets swapped to-and-from during an attack."))
                .setSaveConsumer(val -> cfg.weaponSlotB = val)
                .build());

        slots.addEntry(entry
                .startBooleanToggle(Component.literal("Require Both Slots Filled"), cfg.requireBothSlotsFilled)
                .setDefaultValue(true)
                .setTooltip(Component.literal("Only fire the swap when both chosen slots contain an item."))
                .setSaveConsumer(val -> cfg.requireBothSlotsFilled = val)
                .build());

        // ── Tab 3: Feedback ───────────────────────────────────────────────────
        ConfigCategory feedback = builder.getOrCreateCategory(Component.literal("Feedback"));

        feedback.addEntry(entry
                .startBooleanToggle(Component.literal("Show Message"), cfg.showMessage)
                .setDefaultValue(true)
                .setTooltip(Component.literal("Show \"Magic attribute merge has been used!\" on a successful swap."))
                .setSaveConsumer(val -> cfg.showMessage = val)
                .build());

        feedback.addEntry(entry
                .startEnumSelector(
                        Component.literal("Message Style"),
                        ModConfig.FeedbackStyle.class,
                        cfg.feedbackStyle)
                .setDefaultValue(ModConfig.FeedbackStyle.ACTION_BAR)
                .setTooltip(Component.literal(
                        "ACTION_BAR = above hotbar (recommended)\n" +
                        "CHAT = in the chat window\n" +
                        "TOAST = top-right notification"))
                .setSaveConsumer(val -> cfg.feedbackStyle = val)
                .build());

        return builder.build();
    }
}
