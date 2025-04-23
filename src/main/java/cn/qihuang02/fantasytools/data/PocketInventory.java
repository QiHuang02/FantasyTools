package cn.qihuang02.fantasytools.data;

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
    public static final int PAGE_SIZE = 27; // 3x9
    public static final int STACK_LIMIT = 99;
    private static final String NBT_PAGES = "Pages";
    private static final String NBT_PAGE_INDEX = "PageIndex";
    private static final String NBT_PAGE_ITEMS = "Items";

    // Use ConcurrentHashMap? HashMap should be fine if access is synchronized externally (e.g., via SavedData)
    private final Map<Integer, NonNullList<ItemStack>> itemsByPage;
    private PocketDataManager dataManager; // Reference to manager for setDirty
    private UUID pocketUUID; // UUID of the pocket this inventory belongs to
    private int maxPages; // Tracks highest page number accessed + 1, used in canAddPage logic
    private int currentPageForContainer = 0; // Tracks which page the Container methods operate on

    // Default constructor, initializes page 0
    public PocketInventory() {
        this.itemsByPage = new HashMap<>();
        this.maxPages = 1; // Start with at least 1 page capacity conceptually
        getPage(0); // Ensure page 0 list exists
    }

    // Setter for PocketDataManager reference
    public void setDataManager(PocketDataManager manager) {
        this.dataManager = manager;
    }

    // Setter for the pocket's UUID
    public void setPocketUUID(UUID uuid) {
        this.pocketUUID = uuid;
    }

    // Gets or creates the NonNullList for a specific page
    public NonNullList<ItemStack> getPage(int page) {
        if (page < 0) {
            page = 0; // Ensure page index is non-negative
        }
        // Update maxPages tracked if a higher page is accessed
        maxPages = Math.max(maxPages, page + 1);
        // Compute if absent: if page key doesn't exist, create a new list and put it in the map
        return itemsByPage.computeIfAbsent(page, k -> NonNullList.withSize(PAGE_SIZE, ItemStack.EMPTY));
    }

    // Get item from specific page and slot
    public ItemStack getItem(int page, int slot) {
        if (slot < 0 || slot >= PAGE_SIZE) {
            return ItemStack.EMPTY; // Invalid slot index
        }
        return getPage(page).get(slot); // Get from the correct page list
    }

    // Set item in specific page and slot
    public void setItem(int page, int slot, ItemStack stack) {
        if (slot >= 0 && slot < PAGE_SIZE) {
            // Enforce stack limit for the slot
            if (stack.getCount() > getSlotStackLimit(slot)) {
                stack.setCount(getSlotStackLimit(slot));
            }
            getPage(page).set(slot, stack); // Set in the correct page list
            setChanged(); // Mark data as dirty
        }
    }

    // Calculates the highest page index + 1 that actually exists in the map
    public int getMaxPages() {
        // Find the maximum key (page index) present in the map, add 1. Default to 1 if map is empty.
        return itemsByPage.keySet().stream().mapToInt(i -> i + 1).max().orElse(1);
    }

    // Finds the index of the highest page that contains at least one item
    public int getHighestPageIndex() {
        int highest = 0;
        for (Map.Entry<Integer, NonNullList<ItemStack>> entry : itemsByPage.entrySet()) {
            // If the page is not empty and its index is higher than current highest
            if (entry.getKey() > highest && !isPageEmpty(entry.getKey())) {
                highest = entry.getKey();
            }
        }
        return highest;
    }

    // Checks if a specific page is full (no empty slots)
    public boolean isPageFull(int page) {
        // Check if *none* of the items on the page are empty
        return getPage(page).stream().noneMatch(ItemStack::isEmpty);
    }

    // Checks if a specific page is empty (all slots are empty)
    public boolean isPageEmpty(int page) {
        // Check if *all* items on the page are empty
        return getPage(page).stream().allMatch(ItemStack::isEmpty);
    }

    // Determines if a new page can be added (original logic)
    public boolean canAddPage() {
        // Logic: Can add a new page only if the highest non-empty page is full
        int highestNonEmptyPage = getHighestPageIndex();
        return isPageFull(highestNonEmptyPage);
    }

    public int getPageSize() {
        return PAGE_SIZE;
    }

    // --- Container Implementation ---

    // Overall max stack size for the inventory (used by some vanilla logic)
    @Override
    public int getMaxStackSize() {
        return STACK_LIMIT;
    }

    // Max stack size for a specific slot index (respects our limit)
    public int getSlotStackLimit(int slotIndex) {
        // For PocketInventory, all slots have the same custom limit
        return STACK_LIMIT;
    }

    /**
     * Sets the target page for the standard Container interface methods.
     * Called by PocketMenu when the page changes.
     */
    public void setCurrentPageForContainer(int page) {
        this.currentPageForContainer = Math.max(0, page);
        // Ensure the page list exists when switching
        getPage(this.currentPageForContainer);
    }

    // Returns the size of the *current* page being accessed via Container interface
    @Override
    public int getContainerSize() {
        return PAGE_SIZE; // Always the size of one page
    }

    // Checks if the *current* page being accessed via Container interface is empty
    @Override
    public boolean isEmpty() {
        // Check if all items on the currently targeted page are empty
        return getPage(currentPageForContainer).stream().allMatch(ItemStack::isEmpty);
    }

    // Gets item from the *current* page at the given slot index
    @NotNull
    @Override
    public ItemStack getItem(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= PAGE_SIZE) {
            return ItemStack.EMPTY;
        }
        // Delegate to the specific page/slot getter using currentPageForContainer
        return getItem(currentPageForContainer, slotIndex);
    }

    // Removes items from the *current* page at the given slot index
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
        // Use ItemStack.split to remove items and return the removed part
        ItemStack splitStack = stack.split(count);
        // If the split resulted in changes, mark as dirty
        if (!splitStack.isEmpty()) {
            setChanged();
        }
        return splitStack;
    }

    // Removes the entire stack from the *current* page at the given slot index without updating listeners immediately
    @NotNull
    @Override
    public ItemStack removeItemNoUpdate(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= PAGE_SIZE) {
            return ItemStack.EMPTY;
        }
        NonNullList<ItemStack> currentPageItems = getPage(currentPageForContainer);
        ItemStack stack = currentPageItems.get(slotIndex);
        // Set the slot to empty
        currentPageItems.set(slotIndex, ItemStack.EMPTY);
        // Return the original stack (or empty if it was already empty)
        return stack;
        // Note: setChanged() is typically called *after* this by the calling code (e.g., Slot)
    }

    // Sets the item in the *current* page at the given slot index
    @Override
    public void setItem(int slotIndex, @NotNull ItemStack stack) {
        if (slotIndex >= 0 && slotIndex < PAGE_SIZE) {
            // Enforce max stack size based on *this* inventory's limit, not necessarily the item's default
            if (stack.getCount() > this.getMaxStackSize()) {
                stack.setCount(this.getMaxStackSize());
            }
            // Delegate to the specific page/slot setter using currentPageForContainer
            setItem(currentPageForContainer, slotIndex, stack);
            // setChanged() is called within the specific setItem method
        }
    }

    // Marks the data manager as dirty, indicating changes need saving
    @Override
    public void setChanged() {
        if (this.dataManager != null) {
            this.dataManager.setDirty();
            // Original log message
            FantasyTools.LOGGER.debug("PocketInventory setChanged called, marking PocketDataManager dirty for Pocket UUID: {}", this.pocketUUID != null ? this.pocketUUID : "UNKNOWN");
        } else {
            // Original empty else block
            // FantasyTools.LOGGER.warn("PocketInventory setChanged called but dataManager is null! Pocket UUID: {}", this.pocketUUID); // Suggested log
        }
    }

    // Checks if the player can still interact with this inventory (always true for pocket)
    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    // Clears all items from all pages
    @Override
    public void clearContent() {
        itemsByPage.values().forEach(NonNullList::clear); // Clear each page list
        setChanged(); // Mark data as dirty
    }

    // --- NBT Saving and Loading ---

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
                        // Create a placeholder tag with the slot index
                        CompoundTag initialItemTagPlaceholder = new CompoundTag();
                        initialItemTagPlaceholder.putByte("Slot", (byte) i);

                        FantasyTools.LOGGER.debug("Saving Slot: {}, Item: {}", i, stack);
                        try {
                            // Save the item stack, potentially modifying the placeholder tag
                            CompoundTag savedItemDataTag = (CompoundTag) stack.save(registries, initialItemTagPlaceholder);
                            // Ensure the tag wasn't nulled out (shouldn't happen with non-empty stack)
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
                // Only add the page tag if it actually contained items
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
        itemsByPage.clear(); // Clear existing data before loading
        maxPages = 1; // Reset max pages tracked
        FantasyTools.LOGGER.debug("Loading PocketInventory for UUID: {}. Incoming NBT: {}", this.pocketUUID, nbt.toString());

        ListTag pagesTag = nbt.getList(NBT_PAGES, Tag.TAG_COMPOUND);
        FantasyTools.LOGGER.debug("Found {} pages in NBT for Pocket UUID: {}", pagesTag.size(), this.pocketUUID);

        for (int i = 0; i < pagesTag.size(); i++) {
            CompoundTag pageTag = pagesTag.getCompound(i);
            int pageIndex = pageTag.getInt(NBT_PAGE_INDEX);
            // Create the list for this page
            NonNullList<ItemStack> items = NonNullList.withSize(PAGE_SIZE, ItemStack.EMPTY);
            ListTag itemsTag = pageTag.getList(NBT_PAGE_ITEMS, Tag.TAG_COMPOUND);
            int itemsLoadedOnPage = 0; // Counter for logging

            for (int j = 0; j < itemsTag.size(); j++) {
                CompoundTag itemTag = itemsTag.getCompound(j);
                // Get slot index, ensuring it's treated as unsigned byte
                int slot = itemTag.getByte("Slot") & 255;
                if (slot >= 0 && slot < PAGE_SIZE) { // Validate slot index
                    try {
                        // Parse the ItemStack from the NBT tag
                        ItemStack stack = ItemStack.parseOptional(registries, itemTag);
                        items.set(slot, stack); // Set the loaded stack in the list
                        if (!stack.isEmpty()) itemsLoadedOnPage++;
                    } catch (Exception e) {
                        FantasyTools.LOGGER.error("Failed to load ItemStack in PocketInventory (Page: {}, Slot: {}) for Pocket UUID: {}", pageIndex, slot, this.pocketUUID, e);
                    }
                } else {
                    FantasyTools.LOGGER.warn("Invalid slot index {} found in NBT for Pocket UUID: {}, Page: {}", slot, this.pocketUUID, pageIndex);
                }
            }
            // Store the loaded page items in the map
            itemsByPage.put(pageIndex, items);
            // Update maxPages based on loaded page indices
            maxPages = Math.max(maxPages, pageIndex + 1);
            FantasyTools.LOGGER.debug("Loaded Page: {} with {} items for Pocket UUID: {}", pageIndex, itemsLoadedOnPage, this.pocketUUID);
        }
        // Ensure page 0 exists even if NBT was empty or didn't contain page 0
        getPage(0);
        FantasyTools.LOGGER.debug("Finished loading PocketInventory for Pocket UUID: {}", this.pocketUUID);
    }
}
