package lekavar.lma.drinkbeer.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.awt.*;

public class BeerBarrelScreen extends AbstractContainerScreen<BeerBarrelMenu> {

    private final ResourceLocation BEER_BARREL_CONTAINER_RESOURCE = ResourceLocation.fromNamespaceAndPath(DrinkBeer.MOD_ID, "textures/gui/container/beer_barrel.png");
    private final int textureWidth = 176;
    private final int textureHeight = 166;
    private Inventory inventory;

    public BeerBarrelScreen(BeerBarrelMenu screenContainer, Inventory inv, Component title) {
        super(screenContainer, inv, title);
        this.imageWidth = textureWidth;
        this.imageHeight = textureHeight;

        this.inventory = inv;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, BEER_BARREL_CONTAINER_RESOURCE);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(BEER_BARREL_CONTAINER_RESOURCE, i, j, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.drawCenteredString(this.font, this.title, (int) this.textureWidth / 2, (int) this.titleLabelY, 4210752);
        guiGraphics.drawString(this.font, this.inventory.getDisplayName(), this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
        String str = menu.getIsBrewing() ? convertTickToTime(menu.getRemainingBrewingTime()) : convertTickToTime(menu.getStandardBrewingTime());
        guiGraphics.drawString(this.font, str, 128, 54, new Color(64, 64, 64, 255).getRGB(), false);
    }

    public String convertTickToTime(int tick) {
        String result;
        if (tick > 0) {
            double time = tick / 20;
            int m = (int) (time / 60);
            int s = (int) (time % 60);
            result = m + ":" + s;
        } else result = "";
        return result;
    }
}
