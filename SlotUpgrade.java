package com.yourname.macelator;

import ic2.core.item.block.ItemBlockPipe;
import ic2.core.item.upgrade.ItemUpgradeModule;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import ic2.core.item.ItemEjector;

public class SlotUpgrade extends SlotMacelatorBase {

    public SlotUpgrade(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if (stack == null) return false;
        return stack.getItem() instanceof ItemUpgradeModule || // Ускоритель
                stack.getItem() instanceof ItemBlockPipe || // Затягиватель
                stack.getItem() instanceof ItemEjector; // Выталкиватель
    }

    @Override
    public boolean isUpgradeSlot() {
        return true;
    }
}