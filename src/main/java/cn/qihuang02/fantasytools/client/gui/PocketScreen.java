package cn.qihuang02.fantasytools.client.gui;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.data.PocketInventory;
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

    private static final int BUTTON_WIDTH = 50;
    private static final int BUTTON_HEIGHT = 18;
    private static final int PREV_BUTTON_X = 10;
    private static final int NEXT_BUTTON_X = 116;
    private static final int BUTTON_Y = 64; // Original Y position

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

        // Original button state update logic
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

    // Original button state logic
    private void updateButtonStates() {
        int currentPage = this.menu.getCurrentPage();
        // Original logic relies on client-side dummy inventory state, which is unreliable
        int maxPages = this.menu.getPocketInventory().getMaxPages(); // Total number of existing pages (on client dummy)

        this.prevButton.active = currentPage > 0;
        this.prevButton.visible = currentPage > 0; // Hide if on page 0

        boolean canGotoNext = menu.getPocketInventory().canAddPage() || (currentPage + 1) < maxPages;
        this.nextButton.active = canGotoNext;
        this.nextButton.visible = canGotoNext; // Hide if cannot go next based on client state
    }


    // Original page number rendering
    private void renderPageNumber(GuiGraphics guiGraphics) {
        int currentPage = this.menu.getCurrentPage() + 1; // Display as 1-based index
        String pageText = "Page " + currentPage; // Originally just displayed current page

        int textWidth = this.font.width(pageText);
        int x = this.leftPos + (this.imageWidth / 2) - (textWidth / 2);
        // Original Y position calculation
        int y = this.topPos + (PocketInventory.PAGE_SIZE / 9 * 18) + 5; // Below pocket slots

        guiGraphics.drawString(this.font, pageText, x, y, 0x404040, false); // Dark gray color
    }

    @Override
    protected void init() {
        super.init();

        int buttonStartX = this.leftPos;
        int buttonStartY = this.topPos + BUTTON_Y; // Use original BUTTON_Y

        this.prevButton = Button.builder(Component.literal("< Prev"), this::handlePrevButtonClick)
                .bounds(buttonStartX + PREV_BUTTON_X, buttonStartY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(this.prevButton);

        this.nextButton = Button.builder(Component.literal("Next >"), this::handleNextButtonClick)
                .bounds(buttonStartX + NEXT_BUTTON_X, buttonStartY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(this.nextButton);

        // Initial state update based on potentially incorrect client data
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
        // Original logic, relies on client dummy inventory state
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