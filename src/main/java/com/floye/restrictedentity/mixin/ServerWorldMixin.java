package com.floye.restrictedentity.mixin;

import com.floye.restrictedentity.EntityAccessor;
import com.floye.restrictedentity.config.ConfigLoader;
import com.floye.restrictedentity.config.ForbiddenSpawnConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    /**
     * Intercepte spawnEntity et ajoute des logs pour le debug.
     */
    @Inject(method = "spawnEntity", at = @At("HEAD"), cancellable = true)
    private void onSpawnEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        ForbiddenSpawnConfig config = ConfigLoader.getConfig();
        if (config == null || config.restrictedZones.isEmpty()) {
            return;
        }

        ServerWorld world = (ServerWorld) (Object) this;
        String rawDim = world.getRegistryKey().toString();
        String worldDimension = normalizeDimension(rawDim);
        Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
        BlockPos pos = entity.getBlockPos();

        System.out.println("[RestrictedEntity] Tentative de spawn pour l'entité " + entityId + " en dimension " + worldDimension + " à " + pos);

        for (ForbiddenSpawnConfig.RestrictedZone zone : config.restrictedZones) {
            // Vérification de la dimension
            if (!worldDimension.equals(zone.dimension)) {
                System.out.println("[RestrictedEntity] Ignoré, dimension non correspondante (zone " + zone.name + " attend " + zone.dimension + ")");
                continue;
            }
            // Vérification du type d'entité
            if (entityId == null || !entityId.toString().equals(zone.entity)) {
                System.out.println("[RestrictedEntity] Ignoré, type d'entité non correspondante (zone " + zone.name + " attend " + zone.entity + ")");
                continue;
            }

            // Pour les zones configurées en "natural", vérification de la raison du spawn
            if ("natural".equals(zone.spawnType)) {
                SpawnReason reason = ((EntityAccessor) entity).getSpawnReason();
                System.out.println("[RestrictedEntity] SpawnReason pour " + entityId + " : " + reason);
                if (!SpawnReason.NATURAL.equals(reason)) {
                    System.out.println("[RestrictedEntity] Ignoré, spawn non naturel (zone " + zone.name + " configurée en natural)");
                    continue;
                }
            }

            if (isInRestrictedZone(pos, zone)) {
                System.out.println("[RestrictedEntity] Spawn bloqué dans la zone \"" + zone.name + "\" pour l'entité " + entityId);
                cir.setReturnValue(false);
                cir.cancel();
                return;
            } else {
                System.out.println("[RestrictedEntity] Position hors de la zone \"" + zone.name + "\".");
            }
        }
    }

    private String normalizeDimension(String rawDim) {
        if (rawDim == null) return "";
        int slashIndex = rawDim.indexOf("/");
        int closeBracketIndex = rawDim.indexOf("]");
        if (slashIndex != -1 && closeBracketIndex != -1 && closeBracketIndex > slashIndex) {
            String dim = rawDim.substring(slashIndex + 1, closeBracketIndex);
            return dim.trim();
        }
        return rawDim;
    }

    private boolean isInRestrictedZone(BlockPos pos, ForbiddenSpawnConfig.RestrictedZone zone) {
        ForbiddenSpawnConfig.Position min = zone.min;
        ForbiddenSpawnConfig.Position max = zone.max;
        return pos.getX() >= min.x && pos.getX() <= max.x &&
                pos.getY() >= min.y && pos.getY() <= max.y &&
                pos.getZ() >= min.z && pos.getZ() <= max.z;
    }
}