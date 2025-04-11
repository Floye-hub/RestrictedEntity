package com.floye.restrictedentity.mixin;

import com.floye.restrictedentity.EntityAccessor;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.entity.EntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin implements EntityAccessor {

    @Unique
    private SpawnReason spawnReason;

    @Inject(method = "initialize", at = @At("HEAD"), cancellable = true)
    private void onInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                              EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        this.spawnReason = spawnReason;
        System.out.println("[RestrictedEntity] Entité spawnée avec raison: " + spawnReason);
    }

    @Override
    public SpawnReason getSpawnReason() {
        return this.spawnReason;
    }
}
