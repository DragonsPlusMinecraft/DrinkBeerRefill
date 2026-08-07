package lekavar.lma.drinkbeer.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.managers.TradeBoxManager;
import lekavar.lma.drinkbeer.networking.NetWorking;
import lekavar.lma.drinkbeer.utils.Convert;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.awt.*;

public class TradeBoxScreen extends AbstractContainerScreen<TradeBoxMenu> {
    private static final ResourceLocation TRADE_BOX_GUI = new ResourceLocation(DrinkBeer.MOD_ID, "textures/gui/container/trade_box.png");
    private final int textureWidth = 176;
    private final int textureHeight = 166;
    private final TradeBoxMenu container;

    public TradeBoxScreen(TradeBoxMenu screenContainer, Inventory inv, Component title) {
        super(screenContainer, inv, title);
        this.imageWidth = textureWidth;
        this.imageHeight = textureHeight;

        this.container = screenContainer;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TRADE_BOX_GUI);
        int backgroundWidth = this.getXSize();
        int backgroundHeight = this.getYSize();
        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;

        guiGraphics.blit(TRADE_BOX_GUI, x, y, 0, 0, backgroundWidth, backgroundHeight);
        if (container.isCooling()) {
            guiGraphics.blit(TRADE_BOX_GUI, x + 84, y + 25, 178, 38, 72, 36);
            String timeStr = Convert.tickToTime(container.getCoolingTime());
            guiGraphics.drawString(font, timeStr, x + 114, y + 39, new Color(64, 64, 64, 255).getRGB());
        } else if (container.isTrading()) {
            if (isHovering(157, 6, 13, 13, (double) mouseX, (double) mouseY)) {
                guiGraphics.blit(TRADE_BOX_GUI, x + 155, y + 4, 178, 19, 16, 16);
            } else {
                guiGraphics.blit(TRADE_BOX_GUI, x + 155, y + 4, 178, 0, 16, 16);
            }
        }
        if (!container.isCooling()) {
            Language language = Language.getInstance();
            String youStr = language.getOrDefault("drinkbeer.resident.you");
            guiGraphics.drawString(font, youStr, x + 85, y + 16, new Color(64, 64, 64, 255).getRGB());
            String locationAndResidentStr =
                    language.getOrDefault(TradeBoxManager.getLocationTranslationKey(container.getLocationId()))
                            + "-" +
                            language.getOrDefault(TradeBoxManager.getResidentTranslationKey(container.getResidentId()));
            guiGraphics.drawString(font, locationAndResidentStr, x + 85, y + 63, new Color(64, 64, 64, 255).getRGB());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - getXSize()) / 2;
        int y = (height - getYSize()) / 2;
        this.addRenderableWidget(new ImageButton(x + 156, y + 5, 15, 15, 210, 0, 0, TRADE_BOX_GUI, (buttonWidget) -> {
            if (container.isTrading()) {
                NetWorking.sendRefreshTradebox(container.getBlockPos());
            }
        }));
    }
}
