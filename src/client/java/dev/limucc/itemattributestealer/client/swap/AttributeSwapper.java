package dev.limucc.itemattributestealer.client.swap;

import dev.limucc.itemattributestealer.ItemAttributeStealer;
import dev.limucc.itemattributestealer.client.config.ConfigManager;
import dev.limucc.itemattributestealer.client.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.toasts.SystemToast;

/**
 * Core swap logic that implements the "attribute merge" trick.
 *
 * How MC-28289 works (the vanilla bug we're exploiting):
 * When a player attacks, the server computes damage based on the item the player
 * is CURRENTLY holding.  But if we send a slot-change packet (SetCarriedItem)
 * in the SAME TICK just before and after the attack, the server processes all
 * three packets in one tick — resulting in the attack being processed while the
 * damage modifiers from one weapon and the enchants from another are both active.
 *
 * We drive this purely through vanilla client→server packets so no server-side
 * mod is needed.  Anti-cheat and the AntiSwap plugin will detect and block this.
 *
 * Learning note: This class is called from our Mixin, not registered as a Fabric
 * event.  Either approach works; mixins are more powerful but trickier to debug.
 */
public class AttributeSwapper {

    /** Feedback message shown on a successful swap. */
    private static final String SUCCESS_MSG = "Magic attribute merge has been used!";

    /**
     * Called from {@link dev.limucc.itemattributestealer.client.mixin.MultiPlayerGameModeMixin}
     * just BEFORE the vanilla attack packet is sent.
     *
     * The sequence:
     *  1. Send SetCarriedItem(slotB)  → server now thinks we hold weapon B
     *  2. Let the attack proceed      → server processes hit with weapon B's stats
     *  3. Send SetCarriedItem(slotA)  → swap back so we appear to hold weapon A again
     *
     * The key is that steps 1, 2 (the attack), and 3 all arrive at the server in
     * the same game tick, so it calculates damage in an unexpected hybrid state.
     *
     * @param target the entity being attacked (unused here, available for future checks)
     */
    public static void onPreAttack(Entity target) {
        ModConfig cfg = ConfigManager.get();

        // 1. Master toggle
        if (!cfg.enabled) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        // 2. Slot sanity checks
        int slotA = cfg.weaponSlotA;
        int slotB = cfg.weaponSlotB;

        if (slotA == slotB) {
            // Both slots are the same — nothing to swap
            ItemAttributeStealer.LOGGER.warn("Slot A and Slot B are identical ({}), skipping swap.", slotA);
            return;
        }

        if (slotA < 0 || slotA > 8 || slotB < 0 || slotB > 8) {
            ItemAttributeStealer.LOGGER.warn("Slot out of hotbar range (A={}, B={}), skipping.", slotA, slotB);
            return;
        }

        // 3. Optional: require both slots to be non-empty
        if (cfg.requireBothSlotsFilled) {
            ItemStack stackA = player.getInventory().getItem(slotA);
            ItemStack stackB = player.getInventory().getItem(slotB);
            if (stackA.isEmpty() || stackB.isEmpty()) {
                // One of the slots is empty — silently skip
                return;
            }
        }

        // 4. We should currently be holding slotA; if not, bail to avoid confusion
        if (player.getInventory().selected != slotA) {
            return;
        }

        // ── The actual swap sequence ──────────────────────────────────────────

        // Switch to weapon B (server receives this in the same tick as the attack)
        sendSlotChange(player, slotB);

        // (The attack packet is sent by vanilla code immediately after this method
        //  returns, in the same tick — that's the timing window MC-28289 depends on.)

        // Switch back to weapon A (also same tick)
        sendSlotChange(player, slotA);

        // ── Feedback ─────────────────────────────────────────────────────────
        if (cfg.showMessage) {
            showFeedback(mc, player, cfg.feedbackStyle);
        }

        ItemAttributeStealer.LOGGER.debug("Attribute swap fired: A={} B={}", slotA, slotB);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Sends the vanilla slot-change packet to the server AND updates the local
     * player's selected slot so the client inventory stays in sync.
     */
    private static void sendSlotChange(LocalPlayer player, int slot) {
        player.getInventory().selected = slot;
        player.connection.send(new ServerboundSetCarriedItemPacket(slot));
    }

    /** Displays the success message according to the chosen feedback style. */
    private static void showFeedback(Minecraft mc, LocalPlayer player, ModConfig.FeedbackStyle style) {
        Component text = Component.literal(SUCCESS_MSG);
        switch (style) {
            case ACTION_BAR -> player.displayClientMessage(text, true);
            case CHAT       -> player.displayClientMessage(text, false);
            case TOAST      -> mc.getToastManager().addToast(
                    SystemToast.multiline(mc, SystemToast.SystemToastId.TUTORIAL_HINT,
                            Component.literal("Item Attribute Stealer"), text));
        }
    }
}
