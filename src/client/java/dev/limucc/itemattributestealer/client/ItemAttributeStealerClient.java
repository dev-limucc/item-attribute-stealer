package dev.limucc.itemattributestealer.client;

import dev.limucc.itemattributestealer.ItemAttributeStealer;
import dev.limucc.itemattributestealer.client.config.ConfigManager;
import dev.limucc.itemattributestealer.client.config.ModConfig;
import dev.limucc.itemattributestealer.client.swap.SpearReach;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;

/**
 * Client entrypoint.
 *
 * Responsibilities:
 *  1. Load config from disk.
 *  2. Register the toggle keybind (default: H).
 *  3. Register a client tick handler that:
 *       - Polls the keybind and flips cfg.enabled.
 *       - Runs SpearReach.tick() to update the client-side reach modifier.
 */
public class ItemAttributeStealerClient implements ClientModInitializer {

    /** The toggle keybind, stored so the tick handler can poll it. */
    public static KeyMapping TOGGLE_KEY;

    @Override
    public void onInitializeClient() {
        ConfigManager.load();

        // Register the keybind — default H (InputConstants.KEY_H = 72)
        // The user can rebind or clear it in Options → Controls.
        TOGGLE_KEY = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.item_attribute_stealer.toggle",  // translation key
                        InputConstants.KEY_H,                  // default: H
                        KeyMapping.Category.GAMEPLAY           // shown under "Gameplay" in Controls
                ));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            // Toggle enabled state on each key press (consumeClick drains the queue)
            while (TOGGLE_KEY.consumeClick()) {
                ModConfig cfg = ConfigManager.get();
                cfg.enabled = !cfg.enabled;
                ConfigManager.save();
                if (mc.player != null) {
                    String state = cfg.enabled ? "§aON" : "§cOFF";
                    mc.player.sendOverlayMessage(
                            Component.literal("Item Attribute Stealer: " + state));
                }
            }

            // Update spear-reach attribute modifier every tick
            SpearReach.tick(mc);
        });

        ItemAttributeStealer.LOGGER.info("Item Attribute Stealer client ready. Toggle: H");
    }
}
