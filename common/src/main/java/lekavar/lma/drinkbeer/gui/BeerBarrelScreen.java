package lekavar.lma.drinkbeer.gui;

import lekavar.lma.drinkbeer.DrinkBeer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.awt.*;

public class BeerBarrelScreen extends AbstractContainerScreen<BeerBarrelMenu> {

    private static final Identifier BEER_BARREL_CONTAINER_RESOURCE = Identifier.fromNamespaceAndPath(DrinkBeer.MOD_ID, "textures/gui/container/beer_barrel.png");
    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 166;
    private final Inventory inventory;

    public BeerBarrelScreen(BeerBarrelMenu screenContainer, Inventory inv, Component title) {
        super(screenContainer, inv, title, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        this.inventory = inv;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BEER_BARREL_CONTAINER_RESOURCE,
                i, j, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int x, int y) {
        guiGraphics.centeredText(this.font, this.title, TEXTURE_WIDTH / 2, this.titleLabelY, 4210752);
        guiGraphics.text(this.font, this.inventory.getDisplayName(), this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
        String str = menu.getIsBrewing() ? convertTickToTime(menu.getRemainingBrewingTime()) : convertTickToTime(menu.getStandardBrewingTime());
        guiGraphics.text(this.font, str, 128, 54, new Color(64, 64, 64, 255).getRGB(), false);
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
