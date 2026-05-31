package dev.limucc.itemattributestealer.client.swap;

import dev.limucc.itemattributestealer.ItemAttributeStealer;
import dev.limucc.itemattributestealer.client.config.ConfigManager;
import dev.limucc.itemattributestealer.client.config.ModConfig;
import dev.limucc.itemattributestealer.client.util.ItemKinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/**
 * Core attribute-swap logic (MC-28289).
 *
 * Packet order (HEAD → vanilla attack → RETURN):
 *   HEAD   → SetCarriedItem(copySlot)    server: "player holds copy-weapon"
 *   vanilla → attack packet              server: processes hit with copy-weapon active
 *   RETURN  → SetCarriedItem(heldSlot)   server: "player holds original weapon again"
 *
 * No "merge used" message — removed in v2.
 */
public class AttributeSwapper {

    /** Slot to swap back to after the attack, or -1 if no swap is pending. */
    private static int pendingSwapBack = -1;

    // ── Called from mixin HEAD ────────────────────────────────────────────────

    public static void onPreAttack(Entity target) {
        pendingSwapBack = -1;

        ModConfig cfg = ConfigManager.get();
        if (!cfg.enabled) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        int heldSlot  = player.getInventory().getSelectedSlot();
        ItemStack held = player.getInventory().getItem(heldSlot);

        int copySlot;

        if (cfg.mode == ModConfig.Mode.SLOT) {
            copySlot = slotMode(cfg, heldSlot);
        } else {
            copySlot = weaponMode(cfg, player, heldSlot, held);
        }

        if (copySlot < 0) return;

        sendSlotChange(player, copySlot);
        pendingSwapBack = heldSlot;
        ItemAttributeStealer.LOGGER.debug("Pre-attack swap: slot {} → {}", heldSlot, copySlot);
    }

    // ── Called from mixin RETURN ──────────────────────────────────────────────

    public static void onPostAttack() {
        if (pendingSwapBack < 0) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) { pendingSwapBack = -1; return; }

        int slotA = pendingSwapBack;
        pendingSwapBack = -1;

        sendSlotChange(player, slotA);
        ItemAttributeStealer.LOGGER.debug("Post-attack swap back to slot {}", slotA);
    }

    // ── Mode logic ────────────────────────────────────────────────────────────

    /** Slot mode: held must be slotA (0-indexed), swap to slotB (0-indexed). */
    private static int slotMode(ModConfig cfg, int heldSlot) {
        int a = cfg.slotA - 1; // user sees 1–9, we use 0–8
        int b = cfg.slotB - 1;
        if (a == b || a < 0 || a > 8 || b < 0 || b > 8) return -1;
        if (heldSlot != a) return -1;
        return b;
    }

    /**
     * Weapon mode: detect what the player is holding, find the copy-weapon in the hotbar.
     * Handles the empty-hand case when emptyHandUsage is on.
     */
    private static int weaponMode(ModConfig cfg, LocalPlayer player, int heldSlot, ItemStack held) {
        if (held.isEmpty()) {
            // Empty-hand usage: swap to any tool in hotbar
            if (!cfg.emptyHandUsage) return -1;
            return ItemKinds.findHotbarSlot(player, heldSlot, ItemKinds::isTool);
        }

        // Held item must match the configured HeldKind
        if (!ItemKinds.matchesHeld(held, cfg.heldKind)) return -1;

        // Find a copy-weapon in the hotbar that matches CopyKind
        return ItemKinds.findHotbarSlot(player, heldSlot,
                s -> ItemKinds.matchesCopy(s, cfg.copyKind));
    }

    // ── Shared packet helper ──────────────────────────────────────────────────

    public static void sendSlotChange(LocalPlayer player, int slot) {
        player.getInventory().setSelectedSlot(slot);
        player.connection.send(new ServerboundSetCarriedItemPacket(slot));
    }
}
