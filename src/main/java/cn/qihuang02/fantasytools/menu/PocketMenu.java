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
    private static final int PLAYER_INV_START_Y = 140;
    private static final int PLAYER_HOTBAR_START_Y = 198;

    private static final int POCKET_INV_START_X = 8;
    private static final int POCKET_INV_START_Y = 18;
    private static final int SLOTS_PER_ROW = 9;

    private static final int POCKET_ROWS = 6;
    private static final int POCKET_SLOTS_END = PocketInventory.PAGE_SIZE;

    private final UUID pocketUUID;
    private final PocketInventory pocketInventory;
    private final Inventory playerInventory;
    private int currentPage;

    private int clientMaxPages = 1;
    private boolean clientCanGoNext;

    public PocketMenu(int containerId, Inventory playerInv, UUID pocketUUID, int initialPage) {
        this(containerId, playerInv, pocketUUID, initialPage, getPocketInventoryFromServer(playerInv.player, pocketUUID));
    }

    private PocketMenu(int containerId, Inventory playerInv, UUID pocketUUID, int initialPage, PocketInventory inventory) {
        super(FTMenuTypes.POCKET_MENU_TYPE.get(), containerId);
        this.pocketUUID = pocketUUID;
        this.playerInventory = playerInv;
        this.pocketInventory = inventory;
        this.currentPage = initialPage;

        addSlots();
    }

    public static PocketMenu createClientSide(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        UUID uuid = extraData.readUUID();
        int page = extraData.readVarInt();
        // Client gets a dummy inventory
        return new PocketMenu(containerId, playerInv, uuid, page, new PocketInventory());
    }

    private static PocketInventory getPocketInventoryFromServer(Player player, UUID pocketId) {
        if (player.level() instanceof ServerLevel serverLevel) {
            PocketDataManager manager = PocketDataManager.get(serverLevel);
            return manager.getOrCreateInventory(pocketId);
        } else {
            FantasyTools.LOGGER.warn("PocketMenu created on client using server-side constructor logic?");
            return new PocketInventory();
        }
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (slotId >= 0 && slotId < POCKET_SLOTS_END) {
            Slot slot = this.slots.get(slotId);
            if (slot instanceof PocketSlot pocketSlot) {
                if (button == 1 && (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE)) {
                    ItemStack carried = this.getCarried();
                    ItemStack slotStack = pocketSlot.getItem();

                    if (!slotStack.isEmpty() && carried.isEmpty()) {
                        int amountToTake = (slotStack.getCount() + 1) / 2;
                        ItemStack takenStack = slotStack.split(amountToTake);
                        this.setCarried(takenStack);
                        pocketSlot.setChanged();
                        this.broadcastChanges();
                        return;
                    } else if (!carried.isEmpty() && pocketSlot.mayPlace(carried)) {
                        if (slotStack.isEmpty()) {
                            ItemStack singleItem = carried.split(1);
                            pocketSlot.set(singleItem);
                            this.broadcastChanges();
                            return;
                        } else if (ItemStack.isSameItemSameComponents(slotStack, carried) && slotStack.getCount() < pocketSlot.getMaxStackSize(slotStack)) {
                            if (slotStack.getCount() < PocketInventory.STACK_LIMIT) {
                                carried.shrink(1);
                                slotStack.grow(1);
                                pocketSlot.setChanged();
                                this.broadcastChanges();
                                return;
                            }
                        }
                    }
                }
                if (clickType == ClickType.PICKUP && button == 0) {
                    ItemStack carried = this.getCarried();
                    ItemStack slotStack = pocketSlot.getItem();

                    if (slotStack.isEmpty() && !carried.isEmpty() && pocketSlot.mayPlace(carried)) {
                        pocketSlot.set(carried.copy());
                        this.setCarried(ItemStack.EMPTY);
                        this.broadcastChanges();
                        return;
                    } else if (!slotStack.isEmpty() && carried.isEmpty()) {
                        this.setCarried(slotStack.copy());
                        pocketSlot.set(ItemStack.EMPTY);
                        this.broadcastChanges();
                        return;
                    } else if (!slotStack.isEmpty() && !carried.isEmpty()) {
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
                        } else if (pocketSlot.mayPlace(carried)) {
                            this.setCarried(slotStack.copy());
                            pocketSlot.set(carried.copy());
                            this.broadcastChanges();
                            return;
                        }
                    }
                    return;
                }
            }
        }
        super.clicked(slotId, button, clickType, player);
    }


    private void addSlots() {
        this.slots.clear();
        pocketInventory.getPage(this.currentPage);

        for (int row = 0; row < POCKET_ROWS; ++row) {
            for (int col = 0; col < SLOTS_PER_ROW; ++col) {
                int index = col + row * SLOTS_PER_ROW;
                this.addSlot(new PocketSlot(
                        this.pocketInventory,
                        this.currentPage,
                        index,
                        POCKET_INV_START_X + col * 18,
                        POCKET_INV_START_Y + row * 18
                ));
            }
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_INV_START_X + col * 18, PLAYER_INV_START_Y + row * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, PLAYER_INV_START_X + i * 18, PLAYER_HOTBAR_START_Y));
        }
    }

    private void syncPocketSlots() {
        if (!this.playerInventory.player.level().isClientSide) {
            NonNullList<ItemStack> items = pocketInventory.getPage(currentPage);
            for (int i = 0; i < PocketInventory.PAGE_SIZE; i++) {
                if (i < this.slots.size()) {
                    Slot currentSlot = this.slots.get(i);
                    if (currentSlot instanceof PocketSlot) {
                        ItemStack currentItem = items.get(i);
                        currentSlot.set(currentItem.copy());
                    }
                }
            }
            this.broadcastChanges();
        }
    }


    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            final int pocketSlotsEnd = PocketInventory.PAGE_SIZE;
            final int hotbarSlotsStart = pocketSlotsEnd + 27;
            final int hotbarSlotsEnd = hotbarSlotsStart + 9;

            if (index < pocketSlotsEnd) {
                if (!this.moveItemStackTo(slotStack, pocketSlotsEnd, hotbarSlotsEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (slotStack.getCount() > PocketInventory.STACK_LIMIT) {
                    return ItemStack.EMPTY;
                }
                if (!this.moveItemStackTo(slotStack, 0, pocketSlotsEnd, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }


    @Override
    public boolean stillValid(@NotNull Player player) {
        return player.isAlive();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public PocketInventory getPocketInventory() {
        return pocketInventory;
    }

    public UUID getPocketUUID() {
        return pocketUUID;
    }

    public void setCurrentPageClient(int page) {
        if (this.playerInventory.player.level().isClientSide) {
            this.currentPage = page;
            FantasyTools.LOGGER.debug("PocketMenu Client: Synced newPage to {}", this.currentPage);
        }
    }

    public void changePageServer(int requestedPage) {
        if (this.playerInventory.player.level().isClientSide) return;

        if (requestedPage < 0) {
            requestedPage = 0;
        }

        int maxPages = this.pocketInventory.getMaxPages();
        if (requestedPage >= maxPages && !this.pocketInventory.canAddPage()) {
            requestedPage = maxPages - 1;
            if (requestedPage < 0) requestedPage = 0;
        }

        this.currentPage = requestedPage;
        this.pocketInventory.setCurrentPageForContainer(this.currentPage);
        addSlots();
        syncPocketSlots();

        if (playerInventory.player instanceof ServerPlayer sp) {
            sp.containerMenu = this;
        }

        int newMaxPages = this.pocketInventory.getMaxPages();
        boolean newCanAddPage = this.pocketInventory.canAddPage();
        boolean canGoNextAfterChange = newCanAddPage || (this.currentPage + 1) < newMaxPages;

        if (playerInventory.player instanceof ServerPlayer serverPlayer) {
            SyncPocketPagePacket syncPacket = new SyncPocketPagePacket(this.currentPage, canGoNextAfterChange, newMaxPages);
            PacketDistributor.sendToPlayer(serverPlayer, syncPacket);
            FantasyTools.LOGGER.debug("PocketMenu Server: Changed page. Sent sync: page={}, canGoNext={}, maxPages={}", this.currentPage, canGoNextAfterChange, newMaxPages);
        }
    }

    public int getClientMaxPages() {
        return this.clientMaxPages;
    }

    public void syncClientState(int page, boolean canGoNext, int maxPages) {
        if (this.playerInventory.player.level().isClientSide) {
            this.currentPage = page;
            this.clientCanGoNext = canGoNext;
            this.clientMaxPages = maxPages;
            FantasyTools.LOGGER.debug("PocketMenu Client: Synced newPage={}, canGoNext={}, maxPages={}", this.currentPage, this.clientCanGoNext, this.clientMaxPages);
        }
    }
}