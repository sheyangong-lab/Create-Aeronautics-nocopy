# Create-Aeronautics-nocopy

A server-side NeoForge mod that prevents item duplication caused by Sable physics assembly/disassembly of structures containing BlockEntities with NBT data.

服务端 NeoForge mod，防止 Sable 物理引擎组装/反组装含 NBT 方块实体结构时的物品复制问题。

---

## Problem | 问题描述

When using [Sable](https://github.com/ryanhcode/sable) physics engine (or [Create Aeronautics](https://github.com/Simulated-Team/Simulated-Project)) to assemble/disassemble structures, blocks with inventories (chests, furnaces, etc.) can duplicate their items. This also causes `Item must not be minecraft:air` errors and potential inventory corruption.

使用 [Sable](https://github.com/ryanhcode/sable) 物理引擎（或 [机械动力航空学](https://github.com/Simulated-Team/Simulated-Project)）组装/反组装结构时，带有物品栏的方块（箱子、熔炉等）可能复制其中的物品。同时会导致 `Item must not be minecraft:air` 错误和物品栏损坏。

## Solution | 解决方案

Sable NBT Guard takes a non-intrusive approach:

Sable NBT Guard 采用非侵入式方案：

1. **Dropped item cleanup / 掉落物清理** - After each physics assembly and disassembly, all dropped items (`ItemEntity`) within the structure's bounding box are removed. This eliminates duplicated items without blocking normal physics operations.

   每次物理组装和反组装后，清除结构包围盒范围内所有掉落物（`ItemEntity`），在不阻止正常物理操作的前提下消除复制物品。

2. **NBT sanitization / NBT 清洗** - During block moves, invalid ItemStack entries (`minecraft:air`, empty id, count <= 0) are stripped from BlockEntity NBT data to prevent crashes.

   方块移动过程中，从方块实体 NBT 数据中移除无效的 ItemStack 条目（`minecraft:air`、空 id、count <= 0），防止崩溃。

3. **Structure logging / 结构日志** - Each assembly logs block count, bounding box dimensions, anchor position, and dimension to server logs.

   每次组装在服务端日志中记录方块数量、包围盒尺寸、锚点坐标和维度信息。

## Requirements | 运行环境

| Dependency | Version | Type | 类型 |
|------------|---------|------|------|
| Minecraft | 1.21.1 | Required | 必须 |
| NeoForge | 21.1.x | Required | 必须 |
| Sable | 1.2.x | Required | 必须 |
| Create Aeronautics | 1.2.x | Optional | 可选 |

**Server-side only. Do not install on the client.**
**仅服务端，不要安装到客户端。**

## Building | 构建

You need the Sable and Create Aeronautics mod jars for compilation:

编译需要 Sable 和 Create Aeronautics 的 mod jar：

1. Place `sable-neoforge-*.jar` and `create-aeronautics-bundled-*.jar` in a `libs/` folder

   将 `sable-neoforge-*.jar` 和 `create-aeronautics-bundled-*.jar` 放入 `libs/` 目录

2. Update `build.gradle` to point to the jar files

   修改 `build.gradle` 中的 jar 文件路径

3. Build / 构建：

```bash
./gradlew build
```

The output jar is in `build/libs/SableNbtGuard-1.0.0.jar`.

构建产物在 `build/libs/SableNbtGuard-1.0.0.jar`。

## Installation | 安装

Copy `SableNbtGuard-1.0.0.jar` to your server's `mods/` folder. Restart the server.

将 `SableNbtGuard-1.0.0.jar` 复制到服务端的 `mods/` 目录，重启服务端即可。

## Log Output | 日志示例

```
[SableNbtGuard] Physics assembly: 47 blocks, bounds 5x3x4, anchor=BlockPos{x=-1,y=102,z=-3}, dim=minecraft:overworld
[SableNbtGuard] Cleared 3 dropped item(s) in bounds [-1, 102, -3] ~ [4, 105, 1]
[SableNbtGuard] Sanitized 2 invalid ItemStack(s) during block move!
```

## How It Works | 工作原理

| Mixin | Target | Purpose |
|-------|--------|---------|
| `SubLevelAssemblyHelperMixin` | `assembleBlocks` + `moveBlocks` | Clear ItemEntity within structure bounds / 清除包围盒内掉落物 |
| `SubLevelMoveBlocksMixin` | `BlockEntity.loadWithComponents` in `moveBlocks` | Sanitize invalid ItemStack NBT / 清洗无效 ItemStack NBT |
| `NbtGuardMixinPlugin` | Mixin config plugin | Skip mixins if Sable is not loaded / Sable 未加载时跳过 |

## License | 许可证

MIT
