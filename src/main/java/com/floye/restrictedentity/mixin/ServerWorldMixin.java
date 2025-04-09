package com.floye.restrictedentity.mixin;


import com.floye.restrictedentity.config.ConfigLoader;
import com.floye.restrictedentity.config.ForbiddenSpawnConfig;
import net.minecraft.entity.Entity;
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

        // Récupère et normalise la dimension du monde.
        String rawDim = world.getRegistryKey().toString();  // par ex : "ResourceKey[minecraft:dimension / minecraft:the_end]"
        String worldDimension = normalizeDimension(rawDim);

        Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
        BlockPos pos = entity.getBlockPos();

        // Log de la tentative de spawn
        System.out.println("[CobbleProtect] spawnEntity attempt: entity="
                + (entityId != null ? entityId.toString() : "null")
                + ", dimension=" + worldDimension + ", position=" + pos);

        // Parcours des zones restreintes
        for (ForbiddenSpawnConfig.RestrictedZone zone : config.restrictedZones) {
            // Compare la dimension normalisée et celle définie dans la configuration
            if (!worldDimension.equals(zone.dimension)) {
                continue;
            }
            if (entityId == null || !entityId.toString().equals(zone.entity)) {
                continue;
            }
            if (isInRestrictedZone(pos, zone)) {
                System.out.println("[CobbleProtect] Cancelling spawnEntity for "
                        + entityId.toString() + " in zone " + zone.name + " at " + pos);
                cir.setReturnValue(false);
                cir.cancel();
                return;
            }
        }
    }

    /**
     * Extrait la partie de la dimension après le "/" et retire les crochets.
     * Par exemple, "ResourceKey[minecraft:dimension / minecraft:the_end]"
     * devient "minecraft:the_end"
     */
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