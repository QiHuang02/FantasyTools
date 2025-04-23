package cn.qihuang02.fantasytools.menu.slot;

import cn.qihuang02.fantasytools.data.PocketInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PocketSlot extends Slot {
    private final PocketInventory pocketInventory;
    private final int pageIndex;
    private final int actualSlotIndex;

    public PocketSlot(PocketInventory pocketInventory, int pageIndex, int actualSlotIndex, int xDisplayPosition, int yDisplayPosition) {
        super(pocketInventory, actualSlotIndex, xDisplayPosition, yDisplayPosition);
        this.pocketInventory = pocketInventory;
        this.pageIndex = pageIndex;
        this.actualSlotIndex = actualSlotIndex;
    }

    /**
     * Returns the maximum stack size allowed in this slot.
     */
    @Override
    public int getMaxStackSize() {
        return PocketInventory.STACK_LIMIT;
    }

    /**
     * Returns the maximum stack size allowed for a specific ItemStack in this slot.
     * Useful if different items have different limits within the same slot type.
     */
    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return Math.min(PocketInventory.STACK_LIMIT, stack.getMaxStackSize());
    }

    /**
     * Check if the stack is allowed to be placed in this slot.
     * For now, allow any item. Could add checks here later (e.g., prevent placing other pockets).
     */
    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return true;
    }

    /**
     * Retrieves the ItemStack in this slot from the PocketInventory.
     */
    @NotNull
    @Override
    public ItemStack getItem() {
        return pocketInventory.getItem(this.pageIndex, this.actualSlotIndex);
    }

    /**
     * Sets the ItemStack in this slot within the PocketInventory.
     * Also marks the PocketDataManager as dirty.
     */
    @Override
    public void set(@NotNull ItemStack stack) {
        if (stack.getCount() > this.getMaxStackSize(stack)) {
            stack.setCount(this.getMaxStackSize(stack));
        }
        pocketInventory.setItem(this.pageIndex, this.actualSlotIndex, stack);
        this.setChanged();
    }

    /**
     * Called when the stack in this slot changes.
     * Marks the PocketDataManager as dirty to ensure data is saved.
     */
    @Override
    public void setChanged() {
        super.setChanged();
    }

    /**
     * Called when an item is picked up from the slot.
     */
    @Override
    public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
        this.setChanged();
        super.onTake(player, stack);
    }

    @Override
    public boolean hasItem() {
        return !getItem().isEmpty();
    }

    @Override
    public boolean mayPickup(@NotNull Player player) {
        return true;
    }

    @Override
    public boolean isSameInventory(@NotNull Slot other) {
        if (other instanceof PocketSlot otherPocketSlot) {
            return otherPocketSlot.pocketInventory == this.pocketInventory;
        }
        return super.isSameInventory(other);
    }
}
