package com.sablepatch.nbtguard.mixin;

import com.sablepatch.nbtguard.SableNbtGuard;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Clears dropped items within the structure bounding box after physics
 * assembly and disassembly. Uses the actual bounding box as the clear range
 * so it's dynamic and won't affect items outside the operation area.
 */
@Mixin(SubLevelAssemblyHelper.class)
public abstract class SubLevelAssemblyHelperMixin {

    @Inject(
        method = "assembleBlocks",
        at = @At("RETURN"),
        remap = false
    )
    private static void sablenbtguard$clearItemsOnAssemble(
            ServerLevel level,
            BlockPos anchor,
            Iterable<BlockPos> blocks,
            BoundingBox3ic bounds,
            CallbackInfoReturnable<ServerSubLevel> cir) {
        if (cir.getReturnValue() == null) return;

        int count = 0;
        for (BlockPos ignored : blocks) { count++; }
        int dx = bounds.maxX() - bounds.minX() + 1;
        int dy = bounds.maxY() - bounds.minY() + 1;
        int dz = bounds.maxZ() - bounds.minZ() + 1;
        SableNbtGuard.LOGGER.info(
            "[SableNbtGuard] Physics assembly: {} blocks, bounds {}x{}x{}, anchor={}, dim={}",
            count, dx, dy, dz, anchor, level.dimension().location());

        // Clear items within structure bounding box
        AABB box = new AABB(bounds.minX(), bounds.minY(), bounds.minZ(),
                            bounds.maxX() + 1, bounds.maxY() + 1, bounds.maxZ() + 1);
        clearItemsInBox(level, box);
    }

    @Inject(
        method = "moveBlocks",
        at = @At("TAIL"),
        remap = false
    )
    private static void sablenbtguard$clearItemsOnMove(
            ServerLevel level,
            SubLevelAssemblyHelper.AssemblyTransform transform,
            Iterable<BlockPos> blocks,
            CallbackInfo ci) {
        // Compute bounding box from block positions
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        boolean hasBlocks = false;
        for (BlockPos pos : blocks) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
            hasBlocks = true;
        }
        if (!hasBlocks) return;

        AABB box = new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        clearItemsInBox(level, box);
    }

    private static void clearItemsInBox(ServerLevel level, AABB box) {
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box);
        if (items.isEmpty()) return;

        for (ItemEntity item : items) {
            item.discard();
        }
        SableNbtGuard.LOGGER.info(
            "[SableNbtGuard] Cleared {} dropped item(s) in bounds [{}, {}, {}] ~ [{}, {}, {}]",
            items.size(),
            (int) box.minX, (int) box.minY, (int) box.minZ,
            (int) box.maxX, (int) box.maxY, (int) box.maxZ);
    }
}
