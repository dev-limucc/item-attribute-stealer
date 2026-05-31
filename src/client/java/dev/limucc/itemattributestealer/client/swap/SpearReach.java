package dev.limucc.itemattributestealer.client.swap;

import dev.limucc.itemattributestealer.client.config.ConfigManager;
import dev.limucc.itemattributestealer.client.util.ItemKinds;
import dev.limucc.itemattributestealer.client.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;

/**
 * EXPERIMENTAL: Extends the client-side entity interaction range to match
 * the longest spear in the hotbar so the player can target distant entities.
 *
 * IMPORTANT CAVEAT: The server independently validates reach. Far hits may
 * silently whiff on strict servers. For guaranteed server-side range, hold
 * the spear (Weapon Mode, heldKind = SPEAR).
 *
 * Called every client tick from ItemAttributeStealerClient.
 */
public class SpearReach {

    private static final Identifier MODIFIER_ID =
            Identifier.fromNamespaceAndPath("item_attribute_stealer", "spear_reach");

    /** Last applied extra reach, or 0 if the modifier is not currently active. */
    private static double appliedExtra = 0.0;

    public static void tick(Minecraft mc) {
        ModConfig cfg = ConfigManager.get();
        LocalPlayer player = mc.player;

        if (player == null || !cfg.enabled || !cfg.spearReach || cfg.mode != ModConfig.Mode.WEAPON) {
            removeModifier(player);
            return;
        }

        // Find the spear with the greatest maxReach in the hotbar
        double extra = 0.0;
        for (int i = 0; i < 9; i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.has(DataComponents.KINETIC_WEAPON)) {
                AttackRange ar = s.get(DataComponents.ATTACK_RANGE);
                if (ar != null) {
                    // default entity interaction range is 3.0; spear adds on top
                    double spearMax = ar.effectiveMaxRange(player);
                    double vanilla  = player.getAttributeBaseValue(Attributes.ENTITY_INTERACTION_RANGE);
                    double diff = spearMax - vanilla;
                    if (diff > extra) extra = diff;
                }
            }
        }

        if (extra <= 0.0) {
            removeModifier(player);
            return;
        }

        AttributeInstance attr = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (attr == null) return;

        // Only update if the value changed (avoid thrashing every tick)
        if (Math.abs(extra - appliedExtra) < 0.001) return;

        attr.removeModifier(MODIFIER_ID);
        attr.addTransientModifier(new AttributeModifier(
                MODIFIER_ID, extra, AttributeModifier.Operation.ADD_VALUE));
        appliedExtra = extra;
    }

    private static void removeModifier(LocalPlayer player) {
        if (appliedExtra == 0.0 || player == null) return;
        AttributeInstance attr = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (attr != null) attr.removeModifier(MODIFIER_ID);
        appliedExtra = 0.0;
    }
}
