package com.yourname.macelator;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

@Mod(modid = MacelatorMod.MODID, name = "Macelator v1", version = "1.0")
public class MacelatorMod {

    public static final String MODID = "macelator";

    @Mod.Instance(MODID)
    public static MacelatorMod instance;

    public static Block macelatorBlock;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        macelatorBlock = new BlockMacelator();
        GameRegistry.registerBlock(macelatorBlock, "macelatorBlock");
        GameRegistry.registerTileEntity(TileEntityMacelator.class, "TileEntityMacelator");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandlerMacelator());
    }
}