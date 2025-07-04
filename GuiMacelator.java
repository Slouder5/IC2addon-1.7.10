package com.yourname.macelator;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiMacelator extends GuiContainer {
    private static final ResourceLocation texture = new ResourceLocation("macelator", "textures/gui/macelator.png");
    private final TileEntityMacelator tile;

    public GuiMacelator(InventoryPlayer playerInv, TileEntityMacelator tile) {
        super(new ContainerMacelator(playerInv, tile));
        this.tile = tile;
        this.xSize = 256;
        this.ySize = 256;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString("Сингулярный Дробитель", 8, 6, 4210752);
        fontRendererObj.drawString("Инвентарь", 8, ySize - 94, 4210752);
        String energyText = String.format("%.0f/%.0f EU", tile.getEnergy(), 10000.0);
        fontRendererObj.drawString(energyText, 28, 100, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        mc.getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        int energyWidth = (int) (tile.getEnergy() / 10000.0 * 100);
        if (energyWidth > 0) {
            drawTexturedModalRect(guiLeft + 28, guiTop + 110, 256, 0, energyWidth, 8);
        }
    }
}