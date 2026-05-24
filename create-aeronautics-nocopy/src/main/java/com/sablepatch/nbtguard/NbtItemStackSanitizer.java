package com.sablepatch.nbtguard;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Sanitizes CompoundTag data by removing invalid ItemStack entries
 * (minecraft:air, empty id, count <= 0) before loading into BlockEntities.
 */
public final class NbtItemStackSanitizer {

    private NbtItemStackSanitizer() {}

    /**
     * Result of a sanitization pass.
     */
    public static class SanitizeResult {
        public int removedCount = 0;
        public final List<String> details = new ArrayList<>();
    }

    /**
     * Sanitizes a BlockEntity CompoundTag, removing all invalid ItemStack entries.
     *
     * @param be   the BlockEntity being loaded (for logging context)
     * @param tag  the NBT tag to sanitize (mutated in place)
     * @return sanitization result with count and details
     */
    public static SanitizeResult sanitize(BlockEntity be, CompoundTag tag) {
        SanitizeResult result = new SanitizeResult();
        sanitizeCompound(tag, be.getBlockPos(), result);
        return result;
    }

    private static void sanitizeCompound(CompoundTag tag, BlockPos bePos, SanitizeResult result) {
        for (String key : List.copyOf(tag.getAllKeys())) {
            Tag value = tag.get(key);
            if (value instanceof ListTag listTag) {
                sanitizeItemList(listTag, bePos, key, result);
            } else if (value instanceof CompoundTag compoundTag) {
                if (looksLikeItemStack(compoundTag)) {
                    if (isInvalidItem(compoundTag)) {
                        result.details.add(String.format(
                            "  key=%s, id=%s, count=%s, bePos=%s",
                            key,
                            compoundTag.getString("id"),
                            compoundTag.contains("count", Tag.TAG_INT) ? compoundTag.getInt("count") : "?",
                            bePos));
                        tag.remove(key);
                        result.removedCount++;
                    }
                } else {
                    sanitizeCompound(compoundTag, bePos, result);
                }
            }
        }
    }

    private static void sanitizeItemList(ListTag list, BlockPos bePos, String listKey, SanitizeResult result) {
        for (int i = list.size() - 1; i >= 0; i--) {
            Tag entry = list.get(i);
            if (entry instanceof CompoundTag compound) {
                if (looksLikeItemStack(compound)) {
                    if (isInvalidItem(compound)) {
                        int slot = compound.contains("slot", Tag.TAG_INT) ? compound.getInt("slot") : -1;
                        result.details.add(String.format(
                            "  list=%s[%d], slot=%d, id=%s, count=%s, bePos=%s",
                            listKey, i, slot,
                            compound.getString("id"),
                            compound.contains("count", Tag.TAG_INT) ? compound.getInt("count") : "?",
                            bePos));
                        list.remove(i);
                        result.removedCount++;
                    }
                } else {
                    sanitizeCompound(compound, bePos, result);
                }
            }
        }
    }

    /**
     * Checks if a CompoundTag looks like an ItemStack in 1.21.1 format.
     * Modern: {id: "minecraft:stone", count: 1, components: {...}}
     * Legacy: {id: "minecraft:stone", Count: 1b, tag: {...}}
     */
    private static boolean looksLikeItemStack(CompoundTag tag) {
        if (!tag.contains("id", Tag.TAG_STRING)) return false;
        return tag.contains("count", Tag.TAG_INT)
            || tag.contains("Count", Tag.TAG_ANY_NUMERIC)
            || tag.contains("slot", Tag.TAG_INT)
            || tag.contains("components", Tag.TAG_COMPOUND);
    }

    /**
     * Checks if an ItemStack CompoundTag represents an invalid/empty item.
     */
    private static boolean isInvalidItem(CompoundTag tag) {
        String id = tag.getString("id");
        if (id.isEmpty() || "minecraft:air".equals(id)) return true;
        if (tag.contains("count", Tag.TAG_INT) && tag.getInt("count") <= 0) return true;
        if (tag.contains("Count", Tag.TAG_BYTE) && tag.getByte("Count") <= 0) return true;
        if (tag.contains("Count", Tag.TAG_INT) && tag.getInt("Count") <= 0) return true;
        return false;
    }
}
