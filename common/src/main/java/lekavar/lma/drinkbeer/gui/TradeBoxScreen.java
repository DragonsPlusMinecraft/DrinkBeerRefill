package lekavar.lma.drinkbeer.gui;

import lekavar.lma.drinkbeer.DrinkBeer;
import lekavar.lma.drinkbeer.managers.TradeBoxManager;
import lekavar.lma.drinkbeer.networking.NetWorking;
import lekavar.lma.drinkbeer.utils.Convert;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.awt.*;

public class TradeBoxScreen extends AbstractContainerScreen<TradeBoxMenu> {
    private static final Identifier TRADE_BOX_GUI = Identifier.fromNamespaceAndPath(DrinkBeer.MOD_ID, "textures/gui/container/trade_box.png");
    private static final Identifier REFRESH_WIDGET = Identifier.fromNamespaceAndPath(DrinkBeer.MOD_ID, "container/reroll");
    private static final Identifier REFRESH_WIDGET_BLUE = Identifier.fromNamespaceAndPath(DrinkBeer.MOD_ID, "container/reroll_blue");
    private static final WidgetSprites REFRESH_WIDGET_SPRITE = new WidgetSprites(REFRESH_WIDGET,REFRESH_WIDGET_BLUE);
    private final int textureWidth = 176;
    private final int textureHeight = 166;
    TradeBoxMenu container;

    public TradeBoxScreen(TradeBoxMenu screenContainer, Inventory inv, Component title) {
        super(screenContainer, inv, title);
        this.imageWidth = textureWidth;
        this.imageHeight = textureHeight;

        this.container = screenContainer;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        int backgroundWidth = this.imageWidth;
        int backgroundHeight = this.imageHeight;
        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TRADE_BOX_GUI,
                x, y, 0.0F, 0.0F, backgroundWidth, backgroundHeight, 256, 256);
        if (container.isCooling()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TRADE_BOX_GUI,
                    x + 84, y + 25, 178.0F, 38.0F, 72, 36, 256, 256);
            String timeStr = Convert.tickToTime(container.getCoolingTime());
            guiGraphics.drawString(font, timeStr, x + 114, y + 39, new Color(64, 64, 64, 255).getRGB());
        } else if (container.isTrading()) {
            if (isHovering(157, 6, 13, 13, (double) mouseX, (double) mouseY)) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TRADE_BOX_GUI,
                        x + 155, y + 4, 178.0F, 19.0F, 16, 16, 256, 256);
            } else {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TRADE_BOX_GUI,
                        x + 155, y + 4, 178.0F, 0.0F, 16, 16, 256, 256);
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
        renderBackground(guiGraphics,mouseX,mouseY,partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected boolean isHovering(int xPosition, int yPosition, int width, int height, double pointX, double pointY) {
        return super.isHovering(xPosition, yPosition, width, height, pointX, pointY);
    }

    @Override
    protected void init() {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        this.addRenderableWidget(new ImageButton(x + 156, y + 5, 16, 16, REFRESH_WIDGET_SPRITE, (buttonWidget) -> {
            if (container.isTrading()) {
                NetWorking.sendRefreshTradebox(container.getBlockPos());
            }
        }));
        super.init();
    }
}
