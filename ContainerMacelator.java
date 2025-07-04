package com.yourname.macelator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMacelator extends Container {
    private final TileEntityMacelator tile;

    public ContainerMacelator(InventoryPlayer playerInv, TileEntityMacelator tile) {
        this.tile = tile;

        // ==== Апгрейды (слева вертикально 4 слота)
        for (int i = 0; i < 4; i++) {
            addSlotToContainer(new SlotUpgrade(tile, 24 + i, 170, 28 + i * 18));
        }

        // ==== Входы (4 ряда по 3 слота слева)
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                addSlotToContainer(new SlotInput(tile, index, 26 + col * 18, 28 + row * 18));
            }
        }

        // ==== Выходы (4 ряда по 3 слота справа)
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 3; col++) {
                int index = 12 + row * 3 + col;
                addSlotToContainer(new SlotOutput(tile, index, 106 + col * 18, 28 + row * 18));
            }
        }

        // ==== Инвентарь игрока (3 строки × 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int x = 26 + col * 18;
                int y = 132 + row * 18;
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, x, y));
            }
        }

        // ==== Хотбар игрока
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 26 + col * 18, 190));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack itemstack = null;
        Slot slot = (Slot) inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            itemstack = slotStack.copy();

            if (slotIndex < 28) { // Мацелатор слоты (0-27)
                if (!mergeItemStack(slotStack, 28, 64, true)) {
                    return null;
                }
            } else { // Инвентарь игрока (28-63)
                if (slotIndex < 55) { // Главный инвентарь (28-54)
                    if (!mergeItemStack(slotStack, 0, 12, false)) { // Входные слоты
                        return null;
                    }
                } else { // Хотбар (55-63)
                    if (!mergeItemStack(slotStack, 0, 12, false)) { // Входные слоты
                        return null;
                    }
                }
            }

            if (slotStack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (slotStack.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(player, slotStack);
        }

        return itemstack;
    }
}