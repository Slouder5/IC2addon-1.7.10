package com.yourname.macelator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandlerMacelator implements cpw.mods.fml.common.network.IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityMacelator) {
            return new ContainerMacelator(player.inventory, (TileEntityMacelator) tile);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityMacelator) {
            return new GuiMacelator(player.inventory, (TileEntityMacelator) tile);
        }
        return null;
    }
}