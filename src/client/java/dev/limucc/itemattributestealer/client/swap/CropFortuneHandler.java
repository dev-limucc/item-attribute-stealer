package dev.limucc.itemattributestealer.client.swap;

import dev.limucc.itemattributestealer.ItemAttributeStealer;
import dev.limucc.itemattributestealer.client.config.ConfigManager;
import dev.limucc.itemattributestealer.client.util.ItemKinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Handles the "Use Fortune for crops" feature.
 *
 * When the player breaks a crop block, we briefly swap to the hotbar tool with
 * the highest Fortune level so the server's block-drop calculation sees it.
 * Crops are instant-breaks, so the swap happens in exactly one tick — no
 * durability is spent on the Fortune tool.
 *
 * Hooked via @Inject into MultiPlayerGameMode.destroyBlock(BlockPos):
 *   HEAD   → onPreBreak(pos)   switch to Fortune tool (if any)
 *   RETURN → onPostBreak()     switch back
 */
public class CropFortuneHandler {

    private static int pendingSwapBack = -1;

    public static void onPreBreak(BlockPos pos) {
        pendingSwapBack = -1;

        var cfg = ConfigManager.get();
        if (!cfg.enabled || !cfg.useFortuneForCrops) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        // Only trigger on crop blocks (wheat, carrots, potatoes, beetroot)
        // and nether wart (extends VegetationBlock directly, not CropBlock)
        BlockState state = mc.level.getBlockState(pos);
        boolean isCrop = state.getBlock() instanceof CropBlock
                      || state.getBlock() instanceof NetherWartBlock;
        if (!isCrop) return;

        int heldSlot = player.getInventory().getSelectedSlot();

        // Find the hotbar slot with the highest Fortune level
        int fortuneSlot = ItemKinds.findBestFortuneSlot(player, mc.level);
        if (fortuneSlot < 0 || fortuneSlot == heldSlot) return;

        AttributeSwapper.sendSlotChange(player, fortuneSlot);
        pendingSwapBack = heldSlot;
        ItemAttributeStealer.LOGGER.debug("Fortune crop swap: slot {} → {}", heldSlot, fortuneSlot);
    }

    public static void onPostBreak() {
        if (pendingSwapBack < 0) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) { pendingSwapBack = -1; return; }

        int back = pendingSwapBack;
        pendingSwapBack = -1;
        AttributeSwapper.sendSlotChange(player, back);
        ItemAttributeStealer.LOGGER.debug("Fortune crop swap back to slot {}", back);
    }
}
