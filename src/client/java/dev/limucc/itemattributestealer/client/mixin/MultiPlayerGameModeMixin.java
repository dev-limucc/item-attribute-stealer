package dev.limucc.itemattributestealer.client.mixin;

import dev.limucc.itemattributestealer.client.swap.AttributeSwapper;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Two injection points into MultiPlayerGameMode#attack:
 *
 *   HEAD   — before the attack packet is sent → switch to weapon B
 *   RETURN — after  the attack packet is sent → switch back to weapon A
 *
 * This ensures the server receives the packets in the correct order:
 *   SetCarriedItem(B) → attack → SetCarriedItem(A)
 * all within the same server tick, which triggers MC-28289.
 */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttackHead(Player player, Entity target, CallbackInfo ci) {
        AttributeSwapper.onPreAttack(target);
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void onAttackReturn(Player player, Entity target, CallbackInfo ci) {
        AttributeSwapper.onPostAttack();
    }
}
