package dev.limucc.itemattributestealer.client;

import dev.limucc.itemattributestealer.ItemAttributeStealer;
import dev.limucc.itemattributestealer.client.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side entrypoint.
 *
 * Learning note:
 * Fabric calls onInitializeClient() only when running as a Minecraft client
 * (not a dedicated server).  Since this mod is client-only, nearly everything
 * lives here or in classes called from here.
 *
 * Things we do here:
 *  - Load the config so it is available before any game ticks happen.
 *
 * Things we do NOT do here (they're handled elsewhere):
 *  - Register events → the Mixin handles the attack hook.
 *  - Build the config screen → ModMenuIntegration does that on demand.
 */
public class ItemAttributeStealerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Load saved settings from disk (or write defaults if first run)
        ConfigManager.load();

        ItemAttributeStealer.LOGGER.info("Item Attribute Stealer client ready.");
    }
}
