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
 * Mixin that hooks into the client-side attack logic.
 *
 * Learning note — what is a Mixin?
 * A Mixin lets you inject code into a Minecraft class WITHOUT modifying its source.
 * Fabric uses SpongePowered Mixin under the hood.  The @Inject annotation inserts
 * our code at the specified point (HEAD = very start of the method).
 *
 * Why MultiPlayerGameMode#attack?
 * This is the client-side method that fires when you left-click an entity.
 * It runs before the attack packet is sent, giving us the perfect window to
 * send our slot-swap packets first.
 *
 * IMPORTANT: If Mojang rename this method in a future patch, update the method
 * descriptor here.  Use `./gradlew genSources` and search for "attack" in the
 * decompiled MultiPlayerGameMode to find the right name.
 */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    /**
     * Injected at the very start of the attack method.
     *
     * @param player the local player performing the attack
     * @param target the entity being attacked
     * @param ci     CallbackInfo — we can use ci.cancel() to stop the attack entirely,
     *               but we don't want to; we just want to inject side-effects.
     */
    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttackHead(Player player, Entity target, CallbackInfo ci) {
        // Delegate to our swapper — it handles all the logic and guards
        AttributeSwapper.onPreAttack(target);
    }
}
