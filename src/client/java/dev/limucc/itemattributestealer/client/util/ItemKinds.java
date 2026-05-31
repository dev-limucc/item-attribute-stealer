package dev.limucc.itemattributestealer.client.util;

import dev.limucc.itemattributestealer.client.config.ModConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * Static helpers for detecting weapon/tool types and enchantments in 26.1.2.
 *
 * Key 26.1 facts:
 *  - There is NO SwordItem or PickaxeItem class.
 *  - Weapon detection uses the ATTRIBUTE_MODIFIERS or WEAPON data component.
 *  - Tool detection uses DataComponents.TOOL (excludes BlockItems).
 *  - Sword/Pickaxe are detected by item-id path suffix ("_sword", "_pickaxe").
 */
public final class ItemKinds {

    private ItemKinds() {}

    // ── Generic tool / weapon ─────────────────────────────────────────────────

    /** True if this stack is a tool (pickaxe, axe, sword, mace, trident, spear, shovel, hoe…)
     *  but NOT a placeable block item. */
    public static boolean isTool(ItemStack s) {
        return s.has(DataComponents.TOOL) && !(s.getItem() instanceof BlockItem);
    }

    /** True if this stack deals melee attack damage (has ATTACK_DAMAGE attribute or WEAPON component). */
    public static boolean isWeapon(ItemStack s) {
        if (s.has(DataComponents.WEAPON)) return true;
        ItemAttributeModifiers mods = s.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        return mods.modifiers().stream().anyMatch(e ->
                e.attribute().is(Attributes.ATTACK_DAMAGE) && e.modifier().amount() > 0);
    }

    // ── HeldKind matching (incl. ANY) ─────────────────────────────────────────

    public static boolean matchesHeld(ItemStack s, ModConfig.HeldKind k) {
        if (s.isEmpty()) return false;
        return switch (k) {
            case ANY     -> isTool(s);
            case SWORD   -> idPathEndsWith(s, "_sword");
            case AXE     -> s.getItem() instanceof AxeItem;
            case MACE    -> s.getItem() instanceof MaceItem;
            case TRIDENT -> s.getItem() instanceof TridentItem;
            case PICKAXE -> idPathEndsWith(s, "_pickaxe");
            case SHOVEL  -> s.getItem() instanceof ShovelItem;
            case HOE     -> s.getItem() instanceof HoeItem;
        };
    }

    // ── CopyKind matching (no ANY) ────────────────────────────────────────────

    public static boolean matchesCopy(ItemStack s, ModConfig.CopyKind k) {
        if (s.isEmpty()) return false;
        return switch (k) {
            case SWORD   -> idPathEndsWith(s, "_sword");
            case AXE     -> s.getItem() instanceof AxeItem;
            case MACE    -> s.getItem() instanceof MaceItem;
            case TRIDENT -> s.getItem() instanceof TridentItem;
            case PICKAXE -> idPathEndsWith(s, "_pickaxe");
            case SHOVEL  -> s.getItem() instanceof ShovelItem;
            case HOE     -> s.getItem() instanceof HoeItem;
        };
    }

    // ── Enchantment helpers ───────────────────────────────────────────────────

    /** Returns the Fortune level on this stack, or 0 if none / registry not available. */
    public static int fortuneLevel(Level level, ItemStack s) {
        if (s.isEmpty() || level == null) return 0;
        try {
            Holder<Enchantment> fortune = level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.FORTUNE);
            return s.getEnchantments().getLevel(fortune);
        } catch (Exception e) {
            return 0;
        }
    }

    // ── Hotbar scan ───────────────────────────────────────────────────────────

    /**
     * Returns the first hotbar slot (0–8) where the stack satisfies the predicate,
     * optionally skipping a slot, or -1 if not found.
     */
    public static int findHotbarSlot(LocalPlayer player, int skipSlot,
                                     java.util.function.Predicate<ItemStack> predicate) {
        for (int i = 0; i < 9; i++) {
            if (i == skipSlot) continue;
            if (predicate.test(player.getInventory().getItem(i))) return i;
        }
        return -1;
    }

    /**
     * Finds the hotbar slot (0–8) with the HIGHEST Fortune level (> 0) among all tools.
     * Returns -1 if no Fortune tool exists in the hotbar.
     */
    public static int findBestFortuneSlot(LocalPlayer player, Level level) {
        int bestSlot  = -1;
        int bestLevel = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (isTool(s)) {
                int lvl = fortuneLevel(level, s);
                if (lvl > bestLevel) {
                    bestLevel = lvl;
                    bestSlot  = i;
                }
            }
        }
        return bestSlot;
    }

    // ── Private ───────────────────────────────────────────────────────────────

    /** Checks the item's registry id path (e.g. "diamond_sword") ends with the given suffix. */
    private static boolean idPathEndsWith(ItemStack s, String suffix) {
        var key = BuiltInRegistries.ITEM.getKey(s.getItem());
        return key != null && key.getPath().endsWith(suffix);
    }
}
