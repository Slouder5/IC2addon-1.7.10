package com.yourname.macelator;

import ic2.api.recipe.Recipes;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotInput extends SlotMacelatorBase {

    public SlotInput(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack != null && Recipes.macerator.getOutputFor(stack, false) != null;
    }

    @Override
    public boolean isInputSlot() {
        return true;
    }
}