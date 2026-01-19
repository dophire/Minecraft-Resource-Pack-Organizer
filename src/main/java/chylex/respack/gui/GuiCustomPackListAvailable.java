package chylex.respack.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ResourcePackListEntry;

import java.util.List;

public class GuiCustomPackListAvailable extends GuiCustomPackListBase {

    public GuiCustomPackListAvailable(Minecraft mc, int width, int height, List<ResourcePackListEntry> entries) {
        super(mc, width, height, 35, height - 51, 36, entries);
    }
}
