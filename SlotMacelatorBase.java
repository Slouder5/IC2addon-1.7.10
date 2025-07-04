package com.yourname.macelator;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public abstract class SlotMacelatorBase extends Slot {

    public SlotMacelatorBase(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    public boolean isInputSlot() {
        return false;
    }

    public boolean isOutputSlot() {
        return false;
    }

    public boolean isUpgradeSlot() {
        return false;
    }

    @Override
    public abstract boolean isItemValid(ItemStack stack);
}