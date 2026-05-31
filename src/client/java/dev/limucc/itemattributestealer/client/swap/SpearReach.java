package dev.limucc.itemattributestealer.client.swap;

import dev.limucc.itemattributestealer.client.config.ConfigManager;
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
 * Extends the client-side entity interaction range when the copy-weapon is a
 * spear and a spear exists in the hotbar.
 *
 * HOW IT WORKS:
 *   LocalPlayer.raycastHitResult() — called every render tick for the crosshair —
 *   checks if the held item has an AttackRange component. If not (e.g. you're
 *   holding an axe), it falls back to pick() which uses Attributes.ENTITY_INTERACTION_RANGE.
 *   By adding a modifier to that attribute, we extend crosshair targeting range so
 *   you can "see" a distant mob and the attack swap to the spear will then fire.
 *
 * CAVEAT: Server validates reach independently. Far hits may whiff on strict servers.
 *
 * CONDITIONS TO ACTIVATE:
 *   - Mod enabled
 *   - Mode = WEAPON
 *   - Copy Weapon = SPEAR
 *   - Spear Reach toggle = ON
 *   - A spear actually exists somewhere in the hotbar
 */
public class SpearReach {

    private static final Identifier MODIFIER_ID =
            Identifier.fromNamespaceAndPath("item_attribute_stealer", "spear_reach");

    /**
     * Fallback extra reach added when a spear is in the hotbar but its AttackRange
     * component can't be read or reports no difference from vanilla.
     * Vanilla ENTITY_INTERACTION_RANGE default = 3.0; spears typically reach ~6.0.
     */
    private static final double FALLBACK_EXTRA = 3.0;

    private static double appliedExtra = 0.0;

    public static void tick(Minecraft mc) {
        ModConfig cfg = ConfigManager.get();
        LocalPlayer player = mc.player;

        // Only active when: enabled, weapon mode, copy = SPEAR, toggle on
        boolean shouldApply = player != null
                && cfg.enabled
                && cfg.spearReach
                && cfg.mode == ModConfig.Mode.WEAPON
                && cfg.copyKind == ModConfig.CopyKind.SPEAR;

        if (!shouldApply) {
            removeModifier(player);
            return;
        }

        // Find the extra reach from the spear in the hotbar
        double extra = 0.0;
        for (int i = 0; i < 9; i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (!s.has(DataComponents.KINETIC_WEAPON)) continue;

            AttackRange ar = s.get(DataComponents.ATTACK_RANGE);
            if (ar != null) {
                double spearMax = ar.effectiveMaxRange(player);
                double vanilla  = player.getAttributeBaseValue(Attributes.ENTITY_INTERACTION_RANGE);
                double diff = spearMax - vanilla;
                if (diff > extra) extra = diff;
            }
        }

        // If no spear found in hotbar, or spear's range equals vanilla, use fallback
        if (extra <= 0.0) {
            // Check if any spear is in hotbar at all
            boolean hasSpear = false;
            for (int i = 0; i < 9; i++) {
                if (player.getInventory().getItem(i).has(DataComponents.KINETIC_WEAPON)) {
                    hasSpear = true;
                    break;
                }
            }
            if (!hasSpear) {
                removeModifier(player);
                return;
            }
            extra = FALLBACK_EXTRA;
        }

        // Skip if already applied with the same value
        if (Math.abs(extra - appliedExtra) < 0.001) return;

        AttributeInstance attr = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (attr == null) return;

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
