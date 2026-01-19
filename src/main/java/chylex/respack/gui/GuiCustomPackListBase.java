package chylex.respack.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.ResourcePackListEntry;

import java.util.List;

public abstract class GuiCustomPackListBase extends GuiListExtended {

    protected final Minecraft mc;
    protected final List<ResourcePackListEntry> entries;

    public GuiCustomPackListBase(Minecraft mc, int width, int height, int top, int bottom, int slotHeight, List<ResourcePackListEntry> entries) {
        super(mc, width, height, top, bottom, slotHeight);
        this.mc = mc;
        this.entries = entries;
    }

    @Override
    public IGuiListEntry getListEntry(int index) {
        return (IGuiListEntry) entries.get(index);
    }

    @Override
    protected int getSize() {
        return entries.size();
    }

    @Override
    protected void drawBackground() {
        Gui.drawRect(this.left, this.top, this.right, this.bottom, 0x454d4d4d);
    }

    @Override
    protected void drawContainerBackground(Tessellator tess) {
        Gui.drawRect(this.left, this.top, this.right, this.bottom, 0x454d4d4d);
    }

    @Override
    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
        // disable vanilla gradients
    }

    @Override
    protected int getScrollBarX() {
        return this.right - 6;
    }

    @Override
    public int getListWidth() {
        return this.width;
    }
}
