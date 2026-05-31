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
 * How the attribute swap actually works (MC-28289):
 *
 *   The server holds an ATTRIBUTE CACHE for each player.
 *   When you change your held slot, the cache is flagged but not necessarily
 *   updated instantly — it may update at the end of the tick.
 *
 *   So if we send:  SetCarriedItem(mace) → Attack → SetCarriedItem(sword)
 *   The server processes the attack while inventory.selected = mace, but the
 *   ATTRIBUTE CACHE may still contain the previous weapon's modifiers (sword).
 *   Meanwhile getMainHandItem() returns the mace → mace enchants (Breach4) apply.
 *   Result: sword damage attributes + mace enchants = the real merge.
 *
 *   swapBackDelayTicks controls how many ticks we hold the copy weapon after the
 *   attack before returning. 0 = same-tick swap back, 1+ = delayed. A delay of 1
 *   gives the server a full tick to resolve everything before we revert.
 */
public class AttributeSwapper {

    // Slot to return to, or -1 if no swap is pending.
    private static int pendingSwapBack = -1;
    // Ticks remaining before we swap back.
    private static int swapBackCountdown = 0;

    // ── Called from mixin HEAD (before attack packet) ─────────────────────────

    public static void onPreAttack(Entity target) {
        pendingSwapBack = -1;

        ModConfig cfg = ConfigManager.get();
        if (!cfg.enabled) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        int heldSlot  = player.getInventory().getSelectedSlot();
        ItemStack held = player.getInventory().getItem(heldSlot);

        int copySlot = cfg.mode == ModConfig.Mode.SLOT
                ? slotMode(cfg, heldSlot)
                : weaponMode(cfg, player, heldSlot, held);

        if (copySlot < 0) return;

        sendSlotChange(player, copySlot);
        pendingSwapBack    = heldSlot;
        swapBackCountdown  = Math.max(0, cfg.swapBackDelayTicks);

        ItemAttributeStealer.LOGGER.debug(
                "Pre-attack swap: slot {} → {} (swap-back delay: {} ticks)",
                heldSlot, copySlot, swapBackCountdown);
    }

    // ── Called from mixin RETURN (after attack packet) ───────────────────────

    public static void onPostAttack() {
        if (pendingSwapBack < 0) return;

        // If no delay requested, swap back immediately in the same tick.
        if (swapBackCountdown <= 0) {
            doSwapBack();
        }
        // If delay > 0, the tick handler will count down and swap back later.
    }

    // ── Called every client tick from the tick handler ───────────────────────

    public static void onTick() {
        if (pendingSwapBack < 0 || swapBackCountdown <= 0) return;

        swapBackCountdown--;
        if (swapBackCountdown <= 0) {
            doSwapBack();
        }
    }

    // ── Mode logic ────────────────────────────────────────────────────────────

    private static int slotMode(ModConfig cfg, int heldSlot) {
        int a = cfg.slotA - 1;
        int b = cfg.slotB - 1;
        if (a == b || a < 0 || a > 8 || b < 0 || b > 8) return -1;
        if (heldSlot != a) return -1;
        return b;
    }

    private static int weaponMode(ModConfig cfg, LocalPlayer player,
                                   int heldSlot, ItemStack held) {
        if (held.isEmpty()) {
            if (!cfg.emptyHandUsage) return -1;
            return ItemKinds.findHotbarSlot(player, heldSlot, ItemKinds::isTool);
        }
        if (!ItemKinds.matchesHeld(held, cfg.heldKind)) return -1;
        return ItemKinds.findHotbarSlot(player, heldSlot,
                s -> ItemKinds.matchesCopy(s, cfg.copyKind));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void doSwapBack() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        int slot = pendingSwapBack;
        pendingSwapBack   = -1;
        swapBackCountdown = 0;
        if (player == null) return;
        sendSlotChange(player, slot);
        ItemAttributeStealer.LOGGER.debug("Swap back to slot {}", slot);
    }

    public static void sendSlotChange(LocalPlayer player, int slot) {
        player.getInventory().setSelectedSlot(slot);
        player.connection.send(new ServerboundSetCarriedItemPacket(slot));
    }
}
