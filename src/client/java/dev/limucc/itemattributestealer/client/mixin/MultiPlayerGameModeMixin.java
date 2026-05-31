package dev.limucc.itemattributestealer.client.mixin;

import dev.limucc.itemattributestealer.client.swap.AttributeSwapper;
import dev.limucc.itemattributestealer.client.swap.CropFortuneHandler;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Attack: HEAD/RETURN around attack(Player, Entity).
 *
 * Fortune crops: HEAD/RETURN around startDestroyBlock(BlockPos, Direction).
 *   WHY startDestroyBlock and NOT destroyBlock:
 *   For instant-break blocks (crops), the server receives START_DESTROY_BLOCK inside
 *   startDestroyBlock — BEFORE destroyBlock is called as a client-side prediction.
 *   If we swap slots at destroyBlock HEAD, the server has already computed drops
 *   without Fortune. We must swap BEFORE the packet leaves the client, which means
 *   injecting at startDestroyBlock HEAD.
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

    // ── Fortune crops hooks ───────────────────────────────────────────────────
    // Hook startDestroyBlock so our slot swap reaches the server BEFORE the
    // START_DESTROY_BLOCK packet, giving Fortune to the server's drop calculation.

    @Inject(method = "startDestroyBlock", at = @At("HEAD"))
    private void onStartDestroyBlockHead(BlockPos pos, Direction direction,
                                         CallbackInfoReturnable<Boolean> cir) {
        CropFortuneHandler.onPreBreak(pos);
    }

    @Inject(method = "startDestroyBlock", at = @At("RETURN"))
    private void onStartDestroyBlockReturn(BlockPos pos, Direction direction,
                                           CallbackInfoReturnable<Boolean> cir) {
        CropFortuneHandler.onPostBreak();
    }
}
