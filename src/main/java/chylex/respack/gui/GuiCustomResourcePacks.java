package chylex.respack.gui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiResourcePackAvailable;
import net.minecraft.client.gui.GuiResourcePackSelected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.ResourcePackListEntry;
import net.minecraft.client.resources.ResourcePackListEntryFound;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.ResourcePackRepository.Entry;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

import chylex.respack.packs.ResourcePackListEntryFolder;
import chylex.respack.packs.ResourcePackListProcessor;

import com.google.common.collect.Lists;



@SideOnly(Side.CLIENT)
public class GuiCustomResourcePacks extends GuiScreenResourcePacks {

    private final GuiScreen parentScreen;

    private GuiTextField searchField;
    private GuiCustomPackListBase guiPacksAvailable;
    private GuiCustomPackListBase guiPacksSelected;

    private List<ResourcePackListEntry> listPacksAvailable, listPacksAvailableProcessed, listPacksDummy;
    private List<ResourcePackListEntry> listPacksSelected;

    private ResourcePackListProcessor listProcessor;

    private File currentFolder;
    private GuiButton selectedButton;
    private boolean hasUpdated, requiresReload;

    private Comparator<ResourcePackListEntry> currentSorter;
    private List<Entry> originalSelectedPacks;

    public GuiCustomResourcePacks(GuiScreen parentScreen) {
        super(parentScreen);
        this.parentScreen = parentScreen;
    }

    public void moveToFolder(File folder) {
        currentFolder = folder;
        refreshSelectedPacks();
        refreshAvailablePacks();
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
originalSelectedPacks = Lists.newArrayList();
for (Entry entry : mc.getResourcePackRepository().getRepositoryEntries()) {
    originalSelectedPacks.add(entry);
}

        int selectedCenter = (width * 3) / 4 + 10;
buttonList.add(new GuiOptionButton(
    1,
    selectedCenter - 75,
    height - 26,
    I18n.format("gui.done")
));

        buttonList.add(new GuiOptionButton(2, width / 2 - 244 - 114, height - 26, I18n.format("resourcePack.openFolder")));
        buttonList.add(new GuiOptionButton(10, width / 2 - 204, height - 26, 40, 20, "A-Z"));
        buttonList.add(new GuiOptionButton(11, width / 2 - 204 + 44, height - 26, 40, 20, "Z-A"));
        buttonList.add(new GuiOptionButton(20, width / 2 - 74, height - 26, 70, 20, "Refresh"));

        String prevText = searchField == null ? "" : searchField.getText();
        searchField = new GuiTextField(30, fontRendererObj, width / 2 - 110, 10, 220, 20);
        searchField.setText(prevText);

        if (!requiresReload) {
            listPacksAvailable = Lists.newArrayListWithCapacity(8);
            listPacksAvailableProcessed = Lists.newArrayListWithCapacity(8);
            listPacksDummy = Lists.newArrayListWithCapacity(1);
            listPacksSelected = Lists.newArrayListWithCapacity(8);

            ResourcePackRepository repository = mc.getResourcePackRepository();
            repository.updateRepositoryEntriesAll();

            currentFolder = repository.getDirResourcepacks();
            listPacksAvailable.addAll(createAvailablePackList(repository));

            for (Entry entry : Lists.reverse(repository.getRepositoryEntries())) {
                listPacksSelected.add(new ResourcePackListEntryFound(this, entry));
            }
        }

        guiPacksAvailable = new GuiCustomPackListAvailable(mc, width / 2, height, listPacksAvailableProcessed);
        guiPacksAvailable.setSlotXBoundsFromLeft(width / 2 - 470);
        guiPacksAvailable.registerScrollButtons(7, 8);
        guiPacksAvailable.top = 35;

        guiPacksSelected  = new GuiCustomPackListSelected(mc, width / 2, height, listPacksSelected);
        guiPacksSelected.setSlotXBoundsFromLeft(width / 2 + 10);
        guiPacksSelected.registerScrollButtons(7, 8);
        guiPacksSelected.top = 35;

        listProcessor = new ResourcePackListProcessor(listPacksAvailable, listPacksAvailableProcessed);
        listProcessor.setSorter(currentSorter == null ? (currentSorter = ResourcePackListProcessor.sortAZ) : currentSorter);
        listProcessor.setFilter(searchField.getText().trim());
    }

@Override
public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawCustom(mouseX, mouseY, partialTicks);
}
private void drawCustom(int mouseX, int mouseY, float partialTicks) {
    this.drawDefaultBackground();

    int topY = 0;
    int topBarHeight = 35;
    int bottomBarHeight = 35;
    int bottomY = height - bottomBarHeight;

    int listWidth = width / 2;
    int leftX = 0;
    int rightX = listWidth;

    guiPacksAvailable.setSlotXBoundsFromLeft(leftX);
    guiPacksAvailable.bottom = bottomY;
    guiPacksAvailable.top = topBarHeight;

    guiPacksSelected.top = topBarHeight;
    guiPacksSelected.setSlotXBoundsFromLeft(rightX);
    guiPacksSelected.bottom = bottomY;

    guiPacksAvailable.drawScreen(mouseX, mouseY, partialTicks);
    guiPacksSelected.drawScreen(mouseX, mouseY, partialTicks);

    // Draw bars
    drawTexturedBar(topY, topBarHeight);
    drawGradientRect(0, topY, width, topY + topBarHeight, 0xC0101010, 0x00010101);

    drawTexturedBar(bottomY, bottomBarHeight);
    drawGradientRect(0, bottomY, width, height, 0x00010101, 0xC0101010);

    searchField.drawTextBox();

    // Custom titles
    int titleY = 18;
    int leftListCenter = leftX + (listWidth / 2);
    int rightListCenter = rightX + (listWidth / 2);

    String leftTitle = "Available Resource Packs";
    String rightTitle = "Selected Resource Packs";

    fontRendererObj.drawString(leftTitle, leftListCenter - fontRendererObj.getStringWidth(leftTitle) / 2, titleY, 0xFFFFFF);
    fontRendererObj.drawString(rightTitle, rightListCenter - fontRendererObj.getStringWidth(rightTitle) / 2, titleY, 0xFFFFFF);

    for (GuiButton button : buttonList) {
        button.drawButton(mc, mouseX, mouseY);
    }
}

private boolean havePacksChanged() {
    List<Entry> current = refreshSelectedPacks();

    if (current.size() != originalSelectedPacks.size()) {
        return true;
    }

    for (int i = 0; i < current.size(); i++) {
        if (current.get(i) != originalSelectedPacks.get(i)) {
            return true;
        }
    }

    return false;
}
private void saveSelectedPacksToGameSettings(List<Entry> selected) {
    List<String> enabled = Lists.newArrayList();

    for (Entry entry : selected) {
        if (entry.getResourcePackName() != null) {
            enabled.add(entry.getResourcePackName());
        }
    }

    mc.gameSettings.resourcePacks = enabled;
    mc.gameSettings.saveOptions();
}


private void drawTexturedBar(int y, int height) {
    mc.getTextureManager().bindTexture(Gui.optionsBackground);

    Tessellator tess = Tessellator.getInstance();
    WorldRenderer wr = tess.getWorldRenderer();

    wr.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX_COLOR);

    wr.pos(0, y + height, 0).tex(0, (y + height) / 32.0F).color(128, 128, 255, 128).endVertex();
    wr.pos(width, y + height, 0).tex(width / 32.0F, (y + height) / 32.0F).color(128, 128, 128, 128).endVertex();
    wr.pos(width, y, 0).tex(width / 32.0F, y / 32.0F).color(128, 128, 128, 128).endVertex();
    wr.pos(0, y, 0).tex(0, y / 32.0F).color(128, 128, 128, 128).endVertex();

    tess.draw();
}

    @Override
    public void handleMouseInput() throws IOException {
        try {
            super.handleMouseInput();
        } catch (NullPointerException ignored) {}

        guiPacksAvailable.handleMouseInput();
        guiPacksSelected.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int buttonId) {
        if (buttonId == 0) {
            for (GuiButton button : buttonList) {
                if (button.mousePressed(mc, mouseX, mouseY)) {
    selectedButton = button;
    button.playPressSound(mc.getSoundHandler());
    try {
        actionPerformed(button);
    } catch (IOException e) {
        e.printStackTrace();
    }
}
            }
        }

        guiPacksAvailable.mouseClicked(mouseX, mouseY, buttonId);
        guiPacksSelected.mouseClicked(mouseX, mouseY, buttonId);
        searchField.mouseClicked(mouseX, mouseY, buttonId);

        listProcessor.refresh();
    }
@Override
protected void actionPerformed(GuiButton button) throws IOException {
    if (button.id == 1) {
    boolean changed = havePacksChanged();

    if (changed) {
        List<Entry> selected = refreshSelectedPacks();
        saveSelectedPacksToGameSettings(selected);
        mc.refreshResources();
    }

    requiresReload = false;
    mc.displayGuiScreen(parentScreen);
    return;
}


    super.actionPerformed(button);
}


    @Override
    protected void mouseReleased(int mouseX, int mouseY, int eventType) {
        if (eventType == 0 && selectedButton != null) {
            selectedButton.mouseReleased(mouseX, mouseY);
            selectedButton = null;
        }
    }



    @Override
    protected void keyTyped(char keyChar, int keyCode) throws IOException {
        super.keyTyped(keyChar, keyCode);

        if (searchField.isFocused()) {
            searchField.textboxKeyTyped(keyChar, keyCode);
            listProcessor.setFilter(searchField.getText().trim());
        }
    }

    @Override
public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);

    // restore original pack order
    if (requiresReload) {
        mc.getResourcePackRepository().setRepositories(originalSelectedPacks);
        saveSelectedPacksToGameSettings(originalSelectedPacks);
    }
}


    @Override
    public void updateScreen() {
        searchField.updateCursorCounter();

        if (hasUpdated) {
            hasUpdated = false;
            refreshSelectedPacks();
            refreshAvailablePacks();
        }
    }

    public void refreshAvailablePacks() {
        listPacksAvailable.clear();
        listPacksAvailable.addAll(createAvailablePackList(mc.getResourcePackRepository()));
        listProcessor.refresh();
    }

    public List<Entry> refreshSelectedPacks() {
        List<Entry> selected = Lists.newArrayListWithCapacity(listPacksSelected.size());

        for (ResourcePackListEntry entry : listPacksSelected) {
            if (entry instanceof ResourcePackListEntryFound) {
                ResourcePackListEntryFound packEntry = (ResourcePackListEntryFound) entry;
                if (packEntry.func_148318_i() != null) {
                    selected.add(packEntry.func_148318_i());
                }
            }
        }

        Collections.reverse(selected);
        mc.getResourcePackRepository().setRepositories(selected);
        return selected;
    }

    private List<ResourcePackListEntryFound> createAvailablePackList(ResourcePackRepository repository) {
        final List<ResourcePackListEntryFound> list = Lists.newArrayList();

        if (!repository.getDirResourcepacks().equals(currentFolder)) {
            list.add(new ResourcePackListEntryFolder(this, currentFolder.getParentFile(), true));
        }

        final File[] files = currentFolder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && !new File(file, "pack.mcmeta").isFile()) {
                    list.add(new ResourcePackListEntryFolder(this, file));
                } else {
                    try {
                        Constructor<Entry> constructor = Entry.class.getDeclaredConstructor(ResourcePackRepository.class, File.class);
                        constructor.setAccessible(true);

                        Entry entry = constructor.newInstance(repository, file);
                        entry.updateResourcePack();
                        list.add(new ResourcePackListEntryFound(this, entry));
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }

        List<Entry> repositoryEntries = repository.getRepositoryEntries();

        for (Iterator<ResourcePackListEntryFound> iter = list.iterator(); iter.hasNext();) {
            ResourcePackListEntryFound listEntry = iter.next();

            if (listEntry.func_148318_i() != null && repositoryEntries.contains(listEntry.func_148318_i())) {
                iter.remove();
            }
        }

        return list;
    }

    @Override
    public boolean hasResourcePackEntry(ResourcePackListEntry entry) {
        return listPacksSelected.contains(entry);
    }

    @Override
    public List getListContaining(ResourcePackListEntry entry) {
        return hasResourcePackEntry(entry) ? listPacksSelected : listPacksAvailable;
    }

    @Override
    public List<ResourcePackListEntry> getAvailableResourcePacks() {
        hasUpdated = true;
        listPacksDummy.clear();
        return listPacksDummy;
    }

    @Override
    public List<ResourcePackListEntry> getSelectedResourcePacks() {
        hasUpdated = true;
        return listPacksSelected;
    }

    @Override
    public void markChanged() {
        requiresReload = true;
    }
}
