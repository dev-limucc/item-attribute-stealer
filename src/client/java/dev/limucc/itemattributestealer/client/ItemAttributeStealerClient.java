package dev.limucc.itemattributestealer.client;

import dev.limucc.itemattributestealer.ItemAttributeStealer;
import dev.limucc.itemattributestealer.client.config.ConfigManager;
import dev.limucc.itemattributestealer.client.config.ModConfig;
import dev.limucc.itemattributestealer.client.swap.AttributeSwapper;
import dev.limucc.itemattributestealer.client.swap.CropFortuneHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;

public class ItemAttributeStealerClient implements ClientModInitializer {

    public static KeyMapping TOGGLE_KEY;

    @Override
    public void onInitializeClient() {
        ConfigManager.load();

        TOGGLE_KEY = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.item_attribute_stealer.toggle",
                        InputConstants.KEY_H,
                        KeyMapping.Category.GAMEPLAY
                ));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            // Keybind toggle
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

            // Tick-delayed swap-back (for both attack and crop fortune)
            AttributeSwapper.onTick();
            CropFortuneHandler.onTick();
        });

        ItemAttributeStealer.LOGGER.info("Item Attribute Stealer client ready. Toggle: H");
    }
}
