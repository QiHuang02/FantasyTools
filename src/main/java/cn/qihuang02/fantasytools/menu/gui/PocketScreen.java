package cn.qihuang02.fantasytools.menu.gui;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.menu.PocketMenu;
import cn.qihuang02.fantasytools.network.packet.ChangePocketPagePacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class PocketScreen extends AbstractContainerScreen<PocketMenu> {
    private static final ResourceLocation TEXTURE = FantasyTools.getRL("textures/gui/pocket.png");

    private static final int BUTTON_WIDTH = 18;
    private static final int BUTTON_HEIGHT = 18;

    private static final int BUTTON_LEFT_X = -18;
    private static final int PREV_BUTTON_Y = 18;
    private static final int NEXT_BUTTON_Y = PREV_BUTTON_Y + BUTTON_HEIGHT;

    private Button prevButton;
    private Button nextButton;

    public PocketScreen(PocketMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;

        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        updateButtonStates();

        renderPageNumber(guiGraphics);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    private void updateButtonStates() {
        int currentPage = this.menu.getCurrentPage();
        int maxPages = this.menu.getPocketInventory().getMaxPages();

        this.prevButton.active = currentPage > 0;
        this.prevButton.visible = currentPage > 0;

        boolean canGotoNext = menu.getPocketInventory().canAddPage() || (currentPage + 1) < maxPages;
        this.nextButton.active = canGotoNext;
        this.nextButton.visible = canGotoNext;
    }

    private void renderPageNumber(GuiGraphics guiGraphics) {
        int currentPage = this.menu.getCurrentPage() + 1;
        int maxPages = this.menu.getClientMaxPages();
        Component pageInfoText = Component.translatable("tooltip.fantasytools.pocket.pageinfo", currentPage, maxPages);

        int textWidth = this.font.width(pageInfoText);
        int x = this.leftPos + (this.imageWidth / 2) - (textWidth / 2);
        int y = this.topPos + (6 * 18) + 20;

        guiGraphics.drawString(this.font, pageInfoText, x, y, 0x404040, false);
    }

    @Override
    protected void init() {
        super.init();

        int buttonStartX = this.leftPos;

        this.prevButton = Button.builder(Component.literal("<"), this::handlePrevButtonClick)
                .bounds(buttonStartX + BUTTON_LEFT_X, this.topPos + PREV_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT) // 使用 BUTTON_LEFT_X 和 PREV_BUTTON_Y
                .build();
        this.addRenderableWidget(this.prevButton);

        this.nextButton = Button.builder(Component.literal(">"), this::handleNextButtonClick)
                .bounds(buttonStartX + BUTTON_LEFT_X, this.topPos + NEXT_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT) // 使用 BUTTON_LEFT_X 和 NEXT_BUTTON_Y
                .build();
        this.addRenderableWidget(this.nextButton);

        updateButtonStates();
    }

    private void handlePrevButtonClick(Button button) {
        int currentPage = this.menu.getCurrentPage();
        if (currentPage > 0) {
            requestPageChange(currentPage - 1);
        }
    }

    private void handleNextButtonClick(Button button) {
        int currentPage = this.menu.getCurrentPage();
        if (menu.getPocketInventory().canAddPage() || (currentPage + 1 < menu.getPocketInventory().getMaxPages())) {
            requestPageChange(currentPage + 1);
        }
    }

    private void requestPageChange(int newPage) {
        ChangePocketPagePacket packet = new ChangePocketPagePacket(newPage);
        PacketDistributor.sendToServer(packet);
        FantasyTools.LOGGER.debug("Client sent ChangePocketPagePacket for page: {}", newPage);
    }
}