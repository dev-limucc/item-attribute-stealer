package dev.limucc.itemattributestealer;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common (server + client) entrypoint.
 * This mod is client-only in practice, so this class just defines shared
 * constants and the logger that every other class imports.
 */
public class ItemAttributeStealer implements ModInitializer {

    /** The mod ID — must match fabric.mod.json and mixins.json. */
    public static final String MOD_ID = "item_attribute_stealer";

    /** Shared logger. Usage: ItemAttributeStealer.LOGGER.info("...") */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Item Attribute Stealer loaded!");
    }
}
