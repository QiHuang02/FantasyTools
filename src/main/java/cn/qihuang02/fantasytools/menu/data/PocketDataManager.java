package cn.qihuang02.fantasytools.menu.data;

import cn.qihuang02.fantasytools.FantasyTools;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PocketDataManager extends SavedData {
    private static final String DATA_NAME = "four_dimensional_pocket_data";
    private static final String NBT_INVENTORIES = "PocketInventories";

    private final Map<UUID, PocketInventory> inventories = new HashMap<>();

    private PocketDataManager() {
    }

    private static PocketDataManager create() {
        return new PocketDataManager();
    }

    /**
     * 加载所有口袋库存数据。
     * SavedData.Factory 用于处理 SavedData 的加载和创建。
     *
     * @param tag        NBT 数据
     * @param registries 注册表查找提供者
     * @return PocketDataManager 实例
     */
    private static PocketDataManager load(CompoundTag tag, HolderLookup.Provider registries) {
        PocketDataManager manager = create();

        CompoundTag inventoriesTag = tag.getCompound(NBT_INVENTORIES);
        FantasyTools.LOGGER.debug("Loading PocketDataManager. Incoming Inventories NBT: {}", inventoriesTag);

        for (String key : inventoriesTag.getAllKeys()) {
            try {
                UUID pocketId = UUID.fromString(key);
                CompoundTag inventoryNbt = inventoriesTag.getCompound(key);

                PocketInventory inventory = new PocketInventory();
                inventory.setPocketUUID(pocketId);
                inventory.setDataManager(manager);
                inventory.load(inventoryNbt, registries);

                manager.inventories.put(pocketId, inventory);
                FantasyTools.LOGGER.debug("Successfully loaded and mapped PocketInventory for UUID: {}", pocketId);

            } catch (IllegalArgumentException e) {
                FantasyTools.LOGGER.error("Failed to parse UUID for key '{}' in PocketDataManager", key, e);
            } catch (Exception e) {
                FantasyTools.LOGGER.error("Failed to load inventory data for key '{}' in PocketDataManager", key, e);
            }
        }
        FantasyTools.LOGGER.debug("Finished loading PocketDataManager. {} inventories loaded.", manager.inventories.size());
        return manager;
    }

    /**
     * 获取当前服务器维度的 PocketDataManager 实例。
     * 每个世界（主世界、下界、末地）都会有一个独立的 SavedData 实例。
     * 通常我们只在主世界 (OVERWORLD) 存储这种全局数据。
     *
     * @param level 服务器世界
     * @return PocketDataManager 实例
     */
    public static PocketDataManager get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();

        SavedData.Factory<PocketDataManager> factory = new SavedData.Factory<>(
                PocketDataManager::create,
                PocketDataManager::load,
                null
        );

        PocketDataManager manager = storage.computeIfAbsent(factory, DATA_NAME);

        if (manager == null) {
            FantasyTools.LOGGER.error("Failed to get or create PocketDataManager!");
            manager = create();
        }

        return manager;
    }

    /**
     * 将所有口袋库存数据保存到 NBT。
     *
     * @param nbt        要写入的 NBT 标签
     * @param registries 注册表查找提供者
     * @return 包含保存数据的 CompoundTag
     */
    @Override
    public @NotNull CompoundTag save(CompoundTag nbt, HolderLookup.@NotNull Provider registries) {
        CompoundTag inventoriesTag = new CompoundTag();
        inventories.forEach((uuid, inventory) -> {
            if (uuid == null || inventory == null) {
                FantasyTools.LOGGER.error("Skipping saving inventory due to null key or value. UUID: {}, Inventory present: {}", uuid, inventory != null);
                return;
            }
            inventory.setPocketUUID(uuid);
            inventory.setDataManager(this);
            inventoriesTag.put(uuid.toString(), inventory.save(new CompoundTag(), registries));
        });
        nbt.put(NBT_INVENTORIES, inventoriesTag);
        return nbt;
    }

    /**
     * 获取指定 UUID 的口袋库存。如果不存在，则创建一个新的。
     *
     * @param pocketId 口袋的 UUID
     * @return 对应的 PocketInventory
     */
    public PocketInventory getOrCreateInventory(UUID pocketId) {
        PocketInventory inventory = inventories.computeIfAbsent(pocketId, k -> {
            setDirty();
            FantasyTools.LOGGER.debug("Creating new PocketInventory for UUID: {}", k);
            return new PocketInventory();
        });

        inventory.setDataManager(this);
        inventory.setPocketUUID(pocketId);

        return inventory;
    }

    /**
     * (可选) 移除指定 UUID 的口袋库存。
     * 可能在物品被销毁时调用，以清理数据。
     *
     * @param pocketId 要移除的口袋 UUID
     */
    public void removeInventory(UUID pocketId) {
        if (inventories.remove(pocketId) != null) {
            setDirty();
        }
    }
}
