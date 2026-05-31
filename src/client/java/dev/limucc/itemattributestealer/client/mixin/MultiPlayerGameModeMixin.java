package dev.limucc.itemattributestealer.client.mixin;

import dev.limucc.itemattributestealer.client.swap.AttributeSwapper;
import dev.limucc.itemattributestealer.client.swap.CropFortuneHandler;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Four injection points into MultiPlayerGameMode:
 *
 *   attack(Player, Entity)  HEAD   → swap to copy-weapon BEFORE attack packet
 *   attack(Player, Entity)  RETURN → swap back AFTER attack packet
 *
 *   destroyBlock(BlockPos)  HEAD   → swap to Fortune tool BEFORE block-break packet
 *   destroyBlock(BlockPos)  RETURN → swap back AFTER block-break packet
 *
 * destroyBlock is the single chokepoint for all break paths (instant, creative,
 * survival finish) so one pair of injects covers all crop-break scenarios.
 */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    // ── Attack hooks ──────────────────────────────────────────────────────────

    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttackHead(Player player, Entity target, CallbackInfo ci) {
        AttributeSwapper.onPreAttack(target);
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void onAttackReturn(Player player, Entity target, CallbackInfo ci) {
        AttributeSwapper.onPostAttack();
    }

    // ── Block-break hooks (for Fortune crops) ─────────────────────────────────

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void onDestroyBlockHead(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        CropFortuneHandler.onPreBreak(pos);
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void onDestroyBlockReturn(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        CropFortuneHandler.onPostBreak();
    }
}
