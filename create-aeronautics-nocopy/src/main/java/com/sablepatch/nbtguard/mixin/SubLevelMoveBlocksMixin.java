package com.sablepatch.nbtguard.mixin;

import com.sablepatch.nbtguard.NbtItemStackSanitizer;
import com.sablepatch.nbtguard.SableNbtGuard;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Intercepts BlockEntity.loadWithComponents in SubLevelAssemblyHelper.moveBlocks
 * to sanitize ItemStack NBT data before loading, preventing
 * "Item must not be minecraft:air" errors and corrupted inventories.
 */
@Mixin(SubLevelAssemblyHelper.class)
public abstract class SubLevelMoveBlocksMixin {

    @Redirect(
        method = "moveBlocks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V"
        ),
        remap = false
    )
    private static void sablenbtguard$sanitizeBeforeLoad(
            BlockEntity blockEntity,
            CompoundTag tag,
            HolderLookup.Provider registries) {

        // Sanitize the tag to remove invalid ItemStack entries
        NbtItemStackSanitizer.SanitizeResult result = NbtItemStackSanitizer.sanitize(blockEntity, tag);

        if (result.removedCount > 0) {
            BlockPos pos = blockEntity.getBlockPos();
            String blockId = blockEntity.getBlockState().getBlock().getDescriptionId();
            String beType = blockEntity.getClass().getSimpleName();

            SableNbtGuard.LOGGER.warn(
                "[SableNbtGuard] Sanitized {} invalid ItemStack(s) during block move!",
                result.removedCount);
            SableNbtGuard.LOGGER.warn("  Block: {} | BlockEntity: {} | Pos: {}",
                blockId, beType, pos);
            for (String detail : result.details) {
                SableNbtGuard.LOGGER.warn(detail);
            }
        }

        // Proceed with loading the sanitized tag
        blockEntity.loadWithComponents(tag, registries);
    }
}
