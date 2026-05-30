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
 * Core swap logic for the MC-28289 attribute-merge exploit.
 *
 * Correct packet order (split across two mixin injection points):
 *
 *   [HEAD]   → SetCarriedItem(slotB)   server: "player holds weapon B"
 *   [vanilla] → attack packet           server: processes hit with weapon B active
 *   [RETURN] → SetCarriedItem(slotA)   server: "player holds weapon A again"
 *
 * Both slot-change packets and the attack packet all land in the same server
 * tick, which is what triggers the MC-28289 hybrid-attribute state.
 *
 * Previous bug: both slot changes were sent at HEAD (before the attack packet),
 * so they cancelled each other out and the server just attacked with slot A.
 */
public class AttributeSwapper {

    private static final String SUCCESS_MSG = "Magic attribute merge has been used!";

    /**
     * Slot to swap back to after the attack, or -1 if no swap is pending.
     * Set by onPreAttack, consumed by onPostAttack.
     * Static is safe — there is only ever one LocalPlayer on the client.
     */
    private static int pendingSwapBack = -1;

    /**
     * Called at HEAD of MultiPlayerGameMode#attack — BEFORE the attack packet.
     * Validates conditions and sends the first slot-change (A → B).
     */
    public static void onPreAttack(Entity target) {
        pendingSwapBack = -1;

        ModConfig cfg = ConfigManager.get();
        if (!cfg.enabled) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        int slotA = cfg.weaponSlotA;
        int slotB = cfg.weaponSlotB;

        if (slotA == slotB) return;
        if (slotA < 0 || slotA > 8 || slotB < 0 || slotB > 8) return;

        if (cfg.requireBothSlotsFilled) {
            ItemStack stackA = player.getInventory().getItem(slotA);
            ItemStack stackB = player.getInventory().getItem(slotB);
            if (stackA.isEmpty() || stackB.isEmpty()) return;
        }

        if (player.getInventory().getSelectedSlot() != slotA) return;

        // Switch to weapon B — attack packet will follow immediately after this method returns
        sendSlotChange(player, slotB);

        // Remember to swap back after the attack
        pendingSwapBack = slotA;

        ItemAttributeStealer.LOGGER.debug("Pre-attack swap: {} → {}", slotA, slotB);
    }

    /**
     * Called at RETURN of MultiPlayerGameMode#attack — AFTER the attack packet.
     * Sends the second slot-change (B → A) and shows the feedback message.
     */
    public static void onPostAttack() {
        if (pendingSwapBack < 0) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            pendingSwapBack = -1;
            return;
        }

        int slotA = pendingSwapBack;
        pendingSwapBack = -1;

        // Switch back to weapon A
        sendSlotChange(player, slotA);

        ItemAttributeStealer.LOGGER.debug("Post-attack swap back to {}", slotA);

        // Show feedback
        ModConfig cfg = ConfigManager.get();
        if (cfg.showMessage) {
            showFeedback(mc, player, cfg.feedbackStyle);
        }
    }

    private static void sendSlotChange(LocalPlayer player, int slot) {
        player.getInventory().setSelectedSlot(slot);
        player.connection.send(new ServerboundSetCarriedItemPacket(slot));
    }

    private static void showFeedback(Minecraft mc, LocalPlayer player, ModConfig.FeedbackStyle style) {
        Component text = Component.literal(SUCCESS_MSG);
        switch (style) {
            case ACTION_BAR -> player.sendOverlayMessage(text);
            case CHAT       -> player.sendSystemMessage(text);
            case TOAST      -> SystemToast.add(mc.getToastManager(),
                    SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                    Component.literal("Item Attribute Stealer"), text);
        }
    }
}
