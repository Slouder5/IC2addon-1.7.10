package com.yourname.macelator;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockMacelator extends BlockContainer {

    public BlockMacelator() {
        super(Material.iron);
        setBlockName("macelatorBlock");
        setBlockTextureName("macelator:macelator");
        setCreativeTab(CreativeTabs.tabDecorations);
        setHardness(3.0F);
        setResistance(5.0F);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(MacelatorMod.instance, 0, world, x, y, z);
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityMacelator();
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityMacelator) {
            TileEntityMacelator macelator = (TileEntityMacelator) tile;
            for (int i = 0; i < macelator.getSizeInventory(); i++) {
                ItemStack stack = macelator.getStackInSlot(i);
                if (stack != null) {
                    dropBlockAsItem(world, x, y, z, stack);
                    macelator.setInventorySlotContents(i, null);
                }
            }
            world.removeTileEntity(x, y, z);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}