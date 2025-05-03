package cn.qihuang02.fantasytools.menu.data;

import cn.qihuang02.fantasytools.FantasyTools;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PocketInventory implements Container {
    public static final int PAGE_SIZE = 54;
    public static final int STACK_LIMIT = 99;
    private static final String NBT_PAGES = "Pages";
    private static final String NBT_PAGE_INDEX = "PageIndex";
    private static final String NBT_PAGE_ITEMS = "Items";

    private final Map<Integer, NonNullList<ItemStack>> itemsByPage;
    private PocketDataManager dataManager;
    private UUID pocketUUID;
    private int maxPages;
    private int currentPageForContainer = 0;

    public PocketInventory() {
        this.itemsByPage = new HashMap<>();
        this.maxPages = 1;
        getPage(0);
    }

    public void setDataManager(PocketDataManager manager) {
        this.dataManager = manager;
    }

    public void setPocketUUID(UUID uuid) {
        this.pocketUUID = uuid;
    }

    public NonNullList<ItemStack> getPage(int page) {
        if (page < 0) {
            page = 0;
        }
        maxPages = Math.max(maxPages, page + 1);
        return itemsByPage.computeIfAbsent(page, k -> NonNullList.withSize(PAGE_SIZE, ItemStack.EMPTY));
    }

    public ItemStack getItem(int page, int slot) {
        if (slot < 0 || slot >= PAGE_SIZE) {
            return ItemStack.EMPTY;
        }
        return getPage(page).get(slot);
    }

    public void setItem(int page, int slot, ItemStack stack) {
        if (slot >= 0 && slot < PAGE_SIZE) {
            if (stack.getCount() > getSlotStackLimit(slot)) {
                stack.setCount(getSlotStackLimit(slot));
            }
            getPage(page).set(slot, stack);
            setChanged();
        }
    }

    public int getMaxPages() {
        return itemsByPage.keySet().stream().mapToInt(i -> i + 1).max().orElse(1);
    }

    public int getHighestPageIndex() {
        int highest = 0;
        for (Map.Entry<Integer, NonNullList<ItemStack>> entry : itemsByPage.entrySet()) {
            if (entry.getKey() > highest && !isPageEmpty(entry.getKey())) {
                highest = entry.getKey();
            }
        }
        return highest;
    }

    public boolean isPageFull(int page) {
        return getPage(page).stream().noneMatch(ItemStack::isEmpty);
    }

    public boolean isPageEmpty(int page) {
        return getPage(page).stream().allMatch(ItemStack::isEmpty);
    }

    public boolean canAddPage() {
        int highestNonEmptyPage = getHighestPageIndex();
        return isPageFull(highestNonEmptyPage);
    }

    public int getPageSize() {
        return PAGE_SIZE;
    }

    @Override
    public int getMaxStackSize() {
        return STACK_LIMIT;
    }

    public int getSlotStackLimit(int slotIndex) {
        return STACK_LIMIT;
    }

    /**
     * Sets the target page for the standard Container interface methods.
     * Called by PocketMenu when the page changes.
     */
    public void setCurrentPageForContainer(int page) {
        this.currentPageForContainer = Math.max(0, page);
        getPage(this.currentPageForContainer);
    }

    @Override
    public int getContainerSize() {
        return PAGE_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return getPage(currentPageForContainer).stream().allMatch(ItemStack::isEmpty);
    }

    @NotNull
    @Override
    public ItemStack getItem(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= PAGE_SIZE) {
            return ItemStack.EMPTY;
        }
        return getItem(currentPageForContainer, slotIndex);
    }

    @NotNull
    @Override
    public ItemStack removeItem(int slotIndex, int count) {
        if (slotIndex < 0 || slotIndex >= PAGE_SIZE) {
            return ItemStack.EMPTY;
        }
        NonNullList<ItemStack> currentPageItems = getPage(currentPageForContainer);
        ItemStack stack = currentPageItems.get(slotIndex);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack splitStack = stack.split(count);
        if (!splitStack.isEmpty()) {
            setChanged();
        }
        return splitStack;
    }

    @NotNull
    @Override
    public ItemStack removeItemNoUpdate(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= PAGE_SIZE) {
            return ItemStack.EMPTY;
        }
        NonNullList<ItemStack> currentPageItems = getPage(currentPageForContainer);
        ItemStack stack = currentPageItems.get(slotIndex);
        currentPageItems.set(slotIndex, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slotIndex, @NotNull ItemStack stack) {
        if (slotIndex >= 0 && slotIndex < PAGE_SIZE) {
            if (stack.getCount() > this.getMaxStackSize()) {
                stack.setCount(this.getMaxStackSize());
            }
            setItem(currentPageForContainer, slotIndex, stack);
        }
    }

    @Override
    public void setChanged() {
        if (this.dataManager != null) {
            this.dataManager.setDirty();
            FantasyTools.LOGGER.debug("PocketInventory setChanged called, marking PocketDataManager dirty for Pocket UUID: {}", this.pocketUUID != null ? this.pocketUUID : "UNKNOWN");
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        itemsByPage.values().forEach(NonNullList::clear);
        setChanged();
    }

    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
        ListTag pagesTag = new ListTag();
        FantasyTools.LOGGER.debug("Saving PocketInventory for UUID: {}. Found {} pages in memory.", this.pocketUUID, itemsByPage.size());

        itemsByPage.forEach((pageIndex, items) -> {
            // Only save pages that are not completely empty
            if (!items.stream().allMatch(ItemStack::isEmpty)) {
                CompoundTag pageTag = new CompoundTag();
                pageTag.putInt(NBT_PAGE_INDEX, pageIndex);
                ListTag itemsTag = new ListTag();
                int itemsSavedOnPage = 0;

                for (int i = 0; i < items.size(); i++) {
                    ItemStack stack = items.get(i);
                    if (!stack.isEmpty()) {
                        CompoundTag initialItemTagPlaceholder = new CompoundTag();
                        initialItemTagPlaceholder.putByte("Slot", (byte) i);

                        FantasyTools.LOGGER.debug("Saving Slot: {}, Item: {}", i, stack);
                        try {
                            CompoundTag savedItemDataTag = (CompoundTag) stack.save(registries, initialItemTagPlaceholder);
                            if (savedItemDataTag != null) {
                                FantasyTools.LOGGER.debug("After stack.save for Slot: {}, returned itemTag: {}", i, savedItemDataTag);
                                itemsTag.add(savedItemDataTag);
                                itemsSavedOnPage++;
                            } else {
                                FantasyTools.LOGGER.error("ItemStack.save returned null for non-empty stack! Pocket UUID: {}, Page: {}, Slot: {}", this.pocketUUID, pageIndex, i);
                            }
                        } catch (Exception e) {
                            FantasyTools.LOGGER.error("Failed to save ItemStack in PocketInventory (Page: {}, Slot: {}) for Pocket UUID: {}", pageIndex, i, this.pocketUUID, e);
                        }
                    }
                }
                if (itemsSavedOnPage > 0) {
                    pageTag.put(NBT_PAGE_ITEMS, itemsTag);
                    pagesTag.add(pageTag);
                    FantasyTools.LOGGER.debug("Saving Page: {} with {} items for Pocket UUID: {}", pageIndex, itemsSavedOnPage, this.pocketUUID);
                } else {
                    FantasyTools.LOGGER.debug("Skipping saving empty Page: {} for Pocket UUID: {}", pageIndex, this.pocketUUID);
                }
            }
        });
        nbt.put(NBT_PAGES, pagesTag);
        FantasyTools.LOGGER.debug("Finished saving PocketInventory for UUID: {}. Final NBT: {}", this.pocketUUID, nbt);
        return nbt;
    }

    public void load(CompoundTag nbt, HolderLookup.Provider registries) {
        itemsByPage.clear();
        maxPages = 1;
        FantasyTools.LOGGER.debug("Loading PocketInventory for UUID: {}. Incoming NBT: {}", this.pocketUUID, nbt.toString());

        ListTag pagesTag = nbt.getList(NBT_PAGES, Tag.TAG_COMPOUND);
        FantasyTools.LOGGER.debug("Found {} pages in NBT for Pocket UUID: {}", pagesTag.size(), this.pocketUUID);

        for (int i = 0; i < pagesTag.size(); i++) {
            CompoundTag pageTag = pagesTag.getCompound(i);
            int pageIndex = pageTag.getInt(NBT_PAGE_INDEX);
            NonNullList<ItemStack> items = NonNullList.withSize(PAGE_SIZE, ItemStack.EMPTY);
            ListTag itemsTag = pageTag.getList(NBT_PAGE_ITEMS, Tag.TAG_COMPOUND);
            int itemsLoadedOnPage = 0;

            for (int j = 0; j < itemsTag.size(); j++) {
                CompoundTag itemTag = itemsTag.getCompound(j);
                int slot = itemTag.getByte("Slot") & 255;
                if (slot >= 0 && slot < PAGE_SIZE) {
                    try {
                        ItemStack stack = ItemStack.parseOptional(registries, itemTag);
                        items.set(slot, stack);
                        if (!stack.isEmpty()) itemsLoadedOnPage++;
                    } catch (Exception e) {
                        FantasyTools.LOGGER.error("Failed to load ItemStack in PocketInventory (Page: {}, Slot: {}) for Pocket UUID: {}", pageIndex, slot, this.pocketUUID, e);
                    }
                } else {
                    FantasyTools.LOGGER.warn("Invalid slot index {} found in NBT for Pocket UUID: {}, Page: {}", slot, this.pocketUUID, pageIndex);
                }
            }
            itemsByPage.put(pageIndex, items);
            maxPages = Math.max(maxPages, pageIndex + 1);
            FantasyTools.LOGGER.debug("Loaded Page: {} with {} items for Pocket UUID: {}", pageIndex, itemsLoadedOnPage, this.pocketUUID);
        }
        getPage(0);
        FantasyTools.LOGGER.debug("Finished loading PocketInventory for Pocket UUID: {}", this.pocketUUID);
    }
}
