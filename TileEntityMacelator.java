package com.yourname.macelator;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMacelator extends TileEntity implements IInventory, ISidedInventory, IEnergySink {

    private ItemStack[] inventory = new ItemStack[28]; // 0-11 вход, 12-23 выход, 24-27 апгрейды
    private double energy = 0;
    private final double capacity = 10000;
    private final double EU_PER_TICK = 32;
    private boolean addedToEnergyNet = false;

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;

        if (!addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
            markDirty();
        }

        // Автозатягивание
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity neighbor = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
            if (neighbor instanceof IInventory) {
                extractIntoInputs((IInventory) neighbor, dir.getOpposite());
            }
        }

        // Обработка
        for (int i = 0; i < 12; i++) {
            ItemStack input = inventory[i];
            if (input == null || energy < EU_PER_TICK) continue;

            RecipeOutput output = Recipes.macerator.getOutputFor(input, false);
            if (output == null || output.items == null || output.items.isEmpty()) continue;

            ItemStack result = output.items.get(0).copy();

            while (input.stackSize > 0 && energy >= EU_PER_TICK) {
                boolean inserted = false;
                for (int j = 12; j < 24; j++) {
                    if (canInsertOutput(j, result)) {
                        if (inventory[j] == null) {
                            inventory[j] = result.copy();
                        } else if (inventory[j].isItemEqual(result)) {
                            inventory[j].stackSize += result.stackSize;
                        }
                        input.stackSize--;
                        if (input.stackSize <= 0) inventory[i] = null;
                        energy -= EU_PER_TICK;
                        inserted = true;
                        markDirty();
                        break;
                    }
                }
                if (!inserted) break;
            }
        }

        // Автовыталкивание
        for (int i = 12; i < 24; i++) {
            ItemStack stack = inventory[i];
            if (stack == null) continue;
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                TileEntity neighbor = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
                if (neighbor instanceof IInventory) {
                    ItemStack remaining = insertStackIntoInventory((IInventory) neighbor, stack.copy(), dir.getOpposite());
                    if (remaining == null || remaining.stackSize < stack.stackSize) {
                        inventory[i] = remaining;
                        markDirty();
                        break;
                    }
                }
            }
        }
    }

    private boolean canInsertOutput(int slot, ItemStack output) {
        ItemStack current = inventory[slot];
        if (current == null) return true;
        return current.isItemEqual(output) && current.stackSize + output.stackSize <= current.getMaxStackSize();
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return slot >= 0 && slot < 12 && Recipes.macerator.getOutputFor(stack, false) != null;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot >= 12 && slot < 24;
    }

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
        return true;
    }

    @Override
    public double getDemandedEnergy() {
        return Math.min(capacity - energy, 32);
    }

    @Override
    public int getSinkTier() {
        return 1;
    }

    @Override
    public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage) {
        double canAccept = getDemandedEnergy();
        double accepted = Math.min(amount, canAccept);
        energy += accepted;
        if (energy > capacity) energy = capacity;
        markDirty();
        return amount - accepted;
    }

    public double getEnergy() {
        return energy;
    }

    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return inventory[i];
    }

    @Override
    public ItemStack decrStackSize(int i, int count) {
        if (inventory[i] != null) {
            if (inventory[i].stackSize <= count) {
                ItemStack itemstack = inventory[i];
                inventory[i] = null;
                markDirty();
                return itemstack;
            } else {
                ItemStack itemstack = inventory[i].splitStack(count);
                if (inventory[i].stackSize == 0) inventory[i] = null;
                markDirty();
                return itemstack;
            }
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i) {
        if (inventory[i] != null) {
            ItemStack itemstack = inventory[i];
            inventory[i] = null;
            markDirty();
            return itemstack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack stack) {
        inventory[i] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "Сингулярный Дробитель";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this &&
                player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        if (stack == null) return false;
        if (i >= 24 && i < 28) {
            return stack.getItem() instanceof ic2.core.upgrade.IUpgradeItem;
        }
        if (i >= 0 && i < 12) {
            return Recipes.macerator.getOutputFor(stack, false) != null;
        }
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(itemTag);
                list.appendTag(itemTag);
            }
        }
        tag.setTag("Items", list);
        tag.setDouble("Energy", energy);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagList list = tag.getTagList("Items", 10);
        inventory = new ItemStack[getSizeInventory()];
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
        energy = tag.getDouble("Energy");
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
    }

    public static ItemStack insertStackIntoInventory(IInventory inventory, ItemStack stack, ForgeDirection fromSide) {
        if (inventory instanceof ISidedInventory) {
            ISidedInventory sided = (ISidedInventory) inventory;
            int[] slots = sided.getAccessibleSlotsFromSide(fromSide.ordinal());
            for (int slot : slots) {
                if (sided.canInsertItem(slot, stack, fromSide.ordinal())) {
                    ItemStack target = inventory.getStackInSlot(slot);
                    if (target == null) {
                        inventory.setInventorySlotContents(slot, stack);
                        inventory.markDirty();
                        return null;
                    } else if (target.isItemEqual(stack) && target.stackSize < target.getMaxStackSize()) {
                        int transfer = Math.min(stack.stackSize, target.getMaxStackSize() - target.stackSize);
                        target.stackSize += transfer;
                        stack.stackSize -= transfer;
                        inventory.markDirty();
                        if (stack.stackSize <= 0) return null;
                    }
                }
            }
            return stack;
        } else {
            for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                ItemStack target = inventory.getStackInSlot(slot);
                if (target == null) {
                    inventory.setInventorySlotContents(slot, stack);
                    inventory.markDirty();
                    return null;
                } else if (target.isItemEqual(stack) && target.stackSize < target.getMaxStackSize()) {
                    int transfer = Math.min(stack.stackSize, target.getMaxStackSize() - target.stackSize);
                    target.stackSize += transfer;
                    stack.stackSize -= transfer;
                    inventory.markDirty();
                    if (stack.stackSize <= 0) return null;
                }
            }
            return stack;
        }
    }

    private void extractIntoInputs(IInventory from, ForgeDirection side) {
        int[] slots;
        if (from instanceof ISidedInventory) {
            slots = ((ISidedInventory) from).getAccessibleSlotsFromSide(side.ordinal());
        } else {
            slots = new int[from.getSizeInventory()];
            for (int i = 0; i < slots.length; i++) slots[i] = i;
        }
        for (int slot : slots) {
            ItemStack extract = from.getStackInSlot(slot);
            if (extract == null || !isItemValidForSlot(0, extract)) continue;
            for (int i = 0; i < 12; i++) {
                if (inventory[i] == null) {
                    inventory[i] = extract.copy();
                    from.setInventorySlotContents(slot, null);
                    markDirty();
                    from.markDirty();
                    return;
                } else if (inventory[i].isItemEqual(extract) && inventory[i].stackSize < inventory[i].getMaxStackSize()) {
                    int transfer = Math.min(extract.stackSize, inventory[i].getMaxStackSize() - inventory[i].stackSize);
                    inventory[i].stackSize += transfer;
                    extract.stackSize -= transfer;
                    if (extract.stackSize <= 0) {
                        from.setInventorySlotContents(slot, null);
                        markDirty();
                        from.markDirty();
                        return;
                    }
                }
            }
        }
    }
}