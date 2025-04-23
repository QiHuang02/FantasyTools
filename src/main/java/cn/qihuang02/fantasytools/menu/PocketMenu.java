package cn.qihuang02.fantasytools.menu;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.data.PocketDataManager;
import cn.qihuang02.fantasytools.data.PocketInventory;
import cn.qihuang02.fantasytools.menu.slot.PocketSlot;
import cn.qihuang02.fantasytools.network.packet.SyncPocketPagePacket;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PocketMenu extends AbstractContainerMenu {
    private static final int PLAYER_INV_START_X = 8;
    private static final int PLAYER_INV_START_Y = 86; // Adjusted Y for pocket slots
    private static final int PLAYER_HOTBAR_START_Y = 144; // Adjusted Y
    private static final int POCKET_INV_START_X = 8;
    private static final int POCKET_INV_START_Y = 18;
    private static final int SLOTS_PER_ROW = 9;
    private static final int POCKET_ROWS = 3;
    private static final int POCKET_SLOTS_END = PocketInventory.PAGE_SIZE; // 27 slots

    private final UUID pocketUUID;
    private final PocketInventory pocketInventory;
    private final Inventory playerInventory;
    private int currentPage; // Only stores current page, no other sync state

    // Server-side constructor
    public PocketMenu(int containerId, Inventory playerInv, UUID pocketUUID, int initialPage) {
        this(containerId, playerInv, pocketUUID, initialPage, getPocketInventoryFromServer(playerInv.player, pocketUUID));
    }

    // Private constructor used by both client and server factories
    private PocketMenu(int containerId, Inventory playerInv, UUID pocketUUID, int initialPage, PocketInventory inventory) {
        super(FTMenuTypes.POCKET_MENU_TYPE.get(), containerId);
        this.pocketUUID = pocketUUID;
        this.playerInventory = playerInv;
        this.pocketInventory = inventory; // Can be real (server) or dummy (client)
        this.currentPage = initialPage;

        addSlots(); // Add initial slots
    }

    // Client-side factory
    public static PocketMenu createClientSide(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        UUID uuid = extraData.readUUID();
        int page = extraData.readVarInt();
        // Client gets a dummy inventory
        return new PocketMenu(containerId, playerInv, uuid, page, new PocketInventory());
    }

    // Helper to get server inventory
    private static PocketInventory getPocketInventoryFromServer(Player player, UUID pocketId) {
        if (player.level() instanceof ServerLevel serverLevel) {
            PocketDataManager manager = PocketDataManager.get(serverLevel);
            return manager.getOrCreateInventory(pocketId);
        } else {
            // Should not happen in server constructor call path
            FantasyTools.LOGGER.warn("PocketMenu created on client using server-side constructor logic?");
            return new PocketInventory(); // Return dummy as fallback
        }
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        // --- Original custom click logic ---
        if (slotId >= 0 && slotId < POCKET_SLOTS_END) { // Check if click is within pocket slots
            Slot slot = this.slots.get(slotId);
            if (slot instanceof PocketSlot pocketSlot) { // Ensure it's our custom slot
                // Handle right-click splitting/placing single item
                if (button == 1 && (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE)) {
                    ItemStack carried = this.getCarried(); // Item on cursor
                    ItemStack slotStack = pocketSlot.getItem(); // Item in slot

                    // Right-click empty hand on non-empty slot: pick up half
                    if (!slotStack.isEmpty() && carried.isEmpty()) {
                        int amountToTake = (slotStack.getCount() + 1) / 2;
                        ItemStack takenStack = slotStack.split(amountToTake); // Modifies slotStack
                        this.setCarried(takenStack);
                        pocketSlot.setChanged(); // Mark inventory as changed
                        this.broadcastChanges(); // Sync changes
                        return; // Action handled
                    }
                    // Right-click item on slot (potentially non-empty): place one item
                    else if (!carried.isEmpty() && pocketSlot.mayPlace(carried)) {
                        // Place one into empty slot
                        if (slotStack.isEmpty()) {
                            ItemStack singleItem = carried.split(1); // Take one from cursor
                            pocketSlot.set(singleItem); // Place it in slot
                            this.broadcastChanges();
                            return; // Action handled
                        }
                        // Place one onto existing compatible stack (if not full)
                        else if (ItemStack.isSameItemSameComponents(slotStack, carried) && slotStack.getCount() < pocketSlot.getMaxStackSize(slotStack)) {
                            // Check against PocketInventory limit specifically
                            if (slotStack.getCount() < PocketInventory.STACK_LIMIT) {
                                carried.shrink(1); // Remove one from cursor
                                slotStack.grow(1); // Add one to slot
                                pocketSlot.setChanged();
                                this.broadcastChanges();
                                return; // Action handled
                            }
                        }
                    }
                }

                // Handle left-click pickup/place/swap (added for completeness, might need adjustments)
                if (clickType == ClickType.PICKUP && button == 0) { // Left-click
                    ItemStack carried = this.getCarried();
                    ItemStack slotStack = pocketSlot.getItem();

                    // Click empty slot with item: place all
                    if (slotStack.isEmpty() && !carried.isEmpty() && pocketSlot.mayPlace(carried)) {
                        pocketSlot.set(carried.copy()); // Set slot to cursor item
                        this.setCarried(ItemStack.EMPTY); // Clear cursor
                        this.broadcastChanges();
                        return;
                    }
                    // Click non-empty slot with empty hand: pick up all
                    else if (!slotStack.isEmpty() && carried.isEmpty()) {
                        this.setCarried(slotStack.copy()); // Set cursor to slot item
                        pocketSlot.set(ItemStack.EMPTY); // Clear slot
                        this.broadcastChanges();
                        return;
                    }
                    // Click non-empty slot with item
                    else if (!slotStack.isEmpty() && !carried.isEmpty()) {
                        // If items match: try to merge into slot
                        if (ItemStack.isSameItemSameComponents(slotStack, carried)) {
                            int canAccept = pocketSlot.getMaxStackSize(slotStack) - slotStack.getCount();
                            if (canAccept > 0) {
                                int transferAmount = Math.min(carried.getCount(), canAccept);
                                carried.shrink(transferAmount);
                                slotStack.grow(transferAmount);
                                pocketSlot.setChanged();
                                this.broadcastChanges();
                                return;
                            }
                        }
                        // If items don't match but slot allows placing: swap
                        else if (pocketSlot.mayPlace(carried)) {
                            this.setCarried(slotStack.copy()); // Cursor takes slot item
                            pocketSlot.set(carried.copy()); // Slot takes cursor item
                            this.broadcastChanges();
                            return;
                        }
                    }
                    // Prevent default handling if we handled the click
                    return; // Explicitly return here if any custom logic was hit
                }
            }
        }
        // If custom logic didn't handle it, fall back to default behavior
        super.clicked(slotId, button, clickType, player);
    }


    private void addSlots() {
        this.slots.clear(); // Clear existing slots before adding new ones
        // Ensure the target page exists in the inventory (getPage creates if absent)
        pocketInventory.getPage(this.currentPage); // Make sure the list for the current page is initialized

        // Add Pocket Slots for the current page
        for (int row = 0; row < POCKET_ROWS; ++row) {
            for (int col = 0; col < SLOTS_PER_ROW; ++col) {
                int index = col + row * SLOTS_PER_ROW; // Index within the page (0-26)
                this.addSlot(new PocketSlot(
                        this.pocketInventory,
                        this.currentPage,     // Pass the current page index
                        index,                // Pass the slot index within the page
                        POCKET_INV_START_X + col * 18,
                        POCKET_INV_START_Y + row * 18
                ));
            }
        }

        // Add Player Inventory Slots
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_INV_START_X + col * 18, PLAYER_INV_START_Y + row * 18));
            }
        }

        // Add Player Hotbar Slots
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, PLAYER_INV_START_X + i * 18, PLAYER_HOTBAR_START_Y));
        }
    }


    // Original method for syncing slots (called by changePageServer)
    private void syncPocketSlots() {
        if (!this.playerInventory.player.level().isClientSide) { // Ensure server side
            NonNullList<ItemStack> items = pocketInventory.getPage(currentPage);
            for (int i = 0; i < PocketInventory.PAGE_SIZE; i++) {
                if (i < this.slots.size()) {
                    // Get the slot expected to be the pocket slot
                    Slot currentSlot = this.slots.get(i);
                    // It's safer to check the type, although indices 0-26 should be PocketSlots
                    if (currentSlot instanceof PocketSlot) {
                        ItemStack currentItem = items.get(i);
                        // Set the item in the server-side slot representation.
                        // broadcastChanges() will handle sending this to the client.
                        currentSlot.set(currentItem.copy());
                    }
                }
            }
            // Broadcast all changes made to the slots
            this.broadcastChanges();
        }
        // Client side doesn't need to do anything here, it receives updates via broadcastChanges
    }


    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            final int pocketSlotsEnd = PocketInventory.PAGE_SIZE; // 0-26
            final int playerInvSlotsStart = pocketSlotsEnd; // 27
            final int playerInvSlotsEnd = playerInvSlotsStart + 27; // 27-53
            final int hotbarSlotsStart = playerInvSlotsEnd; // 54
            final int hotbarSlotsEnd = hotbarSlotsStart + 9; // 54-62

            // Moving from Pocket to Player Inv/Hotbar
            if (index < pocketSlotsEnd) {
                // Try merge into player inventory (indices 27-53), then hotbar (54-62)
                if (!this.moveItemStackTo(slotStack, playerInvSlotsStart, hotbarSlotsEnd, true)) {
                    return ItemStack.EMPTY; // Failed to move
                }
            }
            // Moving from Player Inv/Hotbar to Pocket
            else {
                // Crucial check: Do not allow moving items that exceed the pocket's stack limit
                if (slotStack.getCount() > PocketInventory.STACK_LIMIT) {
                    // Maybe just move up to the limit? Or prevent entirely? Preventing is safer.
                    return ItemStack.EMPTY; // Prevent moving oversized stacks into the pocket
                }

                // Try merge into pocket slots (indices 0-26)
                if (!this.moveItemStackTo(slotStack, 0, pocketSlotsEnd, false)) {
                    return ItemStack.EMPTY; // Failed to move
                }
            }

            // Standard logic after move attempt
            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                // No item moved (count is same as original)
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack); // Trigger take event
        }

        return itemstack; // Return the item stack that was moved (or copy of original if failed?)
    }


    @Override
    public boolean stillValid(@NotNull Player player) {
        // Basic check, can be expanded (e.g., check if player still has the pocket item)
        return player.isAlive();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public PocketInventory getPocketInventory() {
        // On client, this is the dummy inventory
        // On server, this is the real inventory
        return pocketInventory;
    }

    public UUID getPocketUUID() {
        return pocketUUID;
    }

    // Original client page update method (only sets page number)
    public void setCurrentPageClient(int page) {
        if (this.playerInventory.player.level().isClientSide) {
            this.currentPage = page;
            FantasyTools.LOGGER.debug("PocketMenu Client: Synced newPage to {}", this.currentPage);
            // Note: This original version doesn't re-add slots on client, relying on server sync
        }
    }


    // Original server page change logic
    public void changePageServer(int requestedPage) {
        if (this.playerInventory.player.level().isClientSide) return; // Only run on server

        if (requestedPage < 0) {
            requestedPage = 0;
        }

        // Basic check against max *existing* pages and possibility to add based on dummy inventory logic
        // This relies on the server's actual pocketInventory instance
        int maxPages = this.pocketInventory.getMaxPages();
        if (requestedPage >= maxPages && !this.pocketInventory.canAddPage()) {
            // If requested page is beyond existing and cannot add, clamp to last page
            requestedPage = maxPages - 1;
            if (requestedPage < 0) requestedPage = 0; // Handle case where maxPages was 0 or 1
        }

        // Update current page on server
        this.currentPage = requestedPage;

        // **Crucially, update the inventory's internal page pointer BEFORE syncing slots**
        this.pocketInventory.setCurrentPageForContainer(this.currentPage);

        // Rebuild slots for the new page
        addSlots(); // This needs to use the new newPage

        // Sync the contents of the new slots to the client
        syncPocketSlots();

        // Update the player's container instance on the server
        if (playerInventory.player instanceof ServerPlayer sp) {
            sp.containerMenu = this;
        }

        // Send packet to tell client the new page number
        if (playerInventory.player instanceof ServerPlayer serverPlayer) {
            // Original packet only sent the new page number
            SyncPocketPagePacket syncPacket = new SyncPocketPagePacket(this.currentPage);
            PacketDistributor.sendToPlayer(serverPlayer, syncPacket);
            FantasyTools.LOGGER.debug("PocketMenu Server: Changed to page {} and sent SyncPocketPagePacket", this.currentPage);
        } else {
            FantasyTools.LOGGER.debug("PocketMenu Server: Changed to page {}", this.currentPage);
        }
    }
}