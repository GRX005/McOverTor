/*
    This file is part of the McOverTor project, licensed under the
    GNU General Public License v3.0

    Copyright (C) 2024-2026 _1ms (GRX005)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package _1ms.McOverTor.screen;

import _1ms.McOverTor.manager.RegionMgr;
import _1ms.McOverTor.manager.RegionMgr.TorRegionInfo;
import _1ms.McOverTor.manager.TorManager;
import _1ms.McOverTor.manager.TorOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static _1ms.McOverTor.Main.*;
import static _1ms.McOverTor.manager.SettingsMgr.get;

// TODO(Ravel): ambiguous static import, members with name TorRegionInfo have different new names
//

public class Region extends Screen {
    private List<TorRegionInfo> regions;
    private Set<String> usedR;
    private TorRegionList regList;
    private Set<String> snapshot;
    private final boolean blSnap;

    private boolean isLoading=true;

    public Region() {//Take a snapshot of the options, so when the menu is closed we can see what changed.
        super(Component.literal("Tor Region Selector"));
        blSnap = get(TorOption.allNodes);

        CompletableFuture.supplyAsync(() -> {
            var r = RegionMgr.getCtr();
            var used = RegionMgr.getSelCtr();
            return Map.entry(r, used); // or a small record
        }, vExec).thenAcceptAsync(entry -> {
            regions  = entry.getKey();
            usedR    = entry.getValue();
            snapshot = new HashSet<>(usedR);
            isLoading = false;
            this.rebuildWidgets();
        }, Minecraft.getInstance()); //Use static accessor as it might not be assigned here yet with this.minecraft
    }

    private void closeFunc() {
        this.minecraft.setScreenAndShow(new JoinMultiplayerScreen(new TitleScreen()));
    }
//Switch between multi or single node application, and/or apply the change of countries
    private void closeBtnF() {
        if(!usedR.equals(snapshot)) {//If the selected countries changed
            RegionMgr.modRegions(usedR).thenAcceptAsync(_->checkAndRelaunch(),this.minecraft);
            return;
        }
        //Re-Start Tor if already started so that the settings will apply, otherwise close.
        if (blSnap != get(TorOption.allNodes) && !usedR.isEmpty()) { //If the state of the tick changed
            RegionMgr.remOrAdd(get(TorOption.allNodes)).thenAcceptAsync(_->checkAndRelaunch(), this.minecraft);
            return;
        }
         closeFunc();
    }

    private void checkAndRelaunch() {
        if(TorManager.progress == 100) {
            TorManager.exitTorAsync(true).thenAcceptAsync(_ ->TorManager.startTor(), this.minecraft);
            return;
        }
        closeFunc();
    }
//TODO TEST OptionsSubScreen?, open from mod
    @Override
    protected void init() {
        super.init();
        if (isLoading)
            return;
        if(regList==null) {//Create the list UI and add the regions' names.
            regList = new TorRegionList(this.minecraft,0,0,0,20);
            regions.forEach(regList::addItem);
        }
        //We need to upd the pos of the list like this to ensure the list's entries are correctly placed after window resizing in 1.21.9&+ (This list aligns with vanilla mostly)
        //x: 130
        regList.updateSizeAndPosition(255,300, this.width/2-130, this.height/2-175);

        this.addRenderableWidget(new SettCheckBox(this.width/2-75, this.height/2+132, Component.literal("Enforce for all nodes"), TorOption.allNodes)
                .tooltip("Make the selection(s) also apply to the Entry and Middle nodes, not just the ExitNode."));

        this.addRenderableWidget(Button.builder(Component.literal("Done"), _ -> closeBtnF()).pos(this.width / 2 - 75, this.height/2+175).build());
        this.addRenderableWidget(Button.builder(Component.literal("Reset"), _ -> usedR.clear()).size(100,20).pos(this.width / 2 - 50, this.height/2+152).build());
        this.addRenderableWidget(regList);

        var txtW = this.font.width(madeByText);
        this.addRenderableWidget(new PlainTextButton(this.width-txtW-2,this.height-10,txtW,10, madeByText,
                ConfirmLinkScreen.confirmLink(this, githubUrl), this.font));
    }

    @Override
    public void onClose() {
        usedR.clear();//Restore when the user exits with ESC instead of the done btn which saves it
        usedR.addAll(snapshot);
        closeFunc();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        renderWindow(graphics, this.width/2-150, this.height/2-200, 300, 400, "McOverTor Regions");
        super.extractRenderState(graphics, mouseX,  mouseY, a);

        graphics.text(this.font, verText,2, this.height-10, 0xFFFFFFFF);

        if (isLoading) {
            graphics.centeredText(this.font, "Loading regions...", this.width / 2, this.height / 2, 0xFFFFFFFF);
            return;
        }

        if(usedR.isEmpty())
            graphics.centeredText(this.font, "none selected -> Tor decides",this.width/2 ,this.height/2-190, 0xFFFFFFFF);
    }
    //Use the default MC list widget to create our own.
    @NullMarked
    private class TorRegionList extends AbstractSelectionList<TorRegionList.TorRegion> {
        public TorRegionList(Minecraft client, int width, int height, int y, int itemsHeight) {
            super(client, width, height, y, itemsHeight);
        }
//Correctly position the scrollbar so it aligns to the list's width properly
        @Override
        protected int scrollBarX() {
            return this.getRowRight()+12;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {
            this.defaultButtonNarrationText(builder);
        }

        // Add entries to the list
        public void addItem(TorRegionInfo reg) {
            this.addEntry(new TorRegion(reg.name(), reg.code(), children().size()));
        }

        // Entry class for each item in the list
        private class TorRegion extends AbstractSelectionList.Entry<TorRegion> {
            private final Component text;
            private final String code;
            private final int ind;

            public TorRegion(String text, String code, int ind) {
                this.text = Component.literal(text);
                this.code = code;
                this.ind = ind;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
                var regList = TorRegionList.this;
                var x = regList.getRowLeft();
                var y = regList.getRowTop(ind);
                //X and Y has been replaced by the mouse versions, now we get the row coordinates from the parent, rowleft is the same for all, for the top of the row we need to know which row is it?
                graphics.text(regList.minecraft.font, text, x+2, y+6, 0xFFFFFFFF);
                if (usedR.contains(code))
                    drawTick(graphics,x-19,y);
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
                // Handle click events
                if(usedR.contains(code))
                    usedR.remove(code);
                else
                    usedR.add(code);
                regList.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            void drawTick(GuiGraphicsExtractor graphics, int x, int y) {
                // Get the current GUI scale
                double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
                // Calculate how many logical units equal exactly 1 physical screen pixel
                float screenPixelOffset = (float) (1.0 / guiScale);
                // Push the JOML matrix state
                graphics.pose().pushMatrix();
                // Translate by the exact fraction needed to move it 1 screen pixel to the left
                graphics.pose().translate(-screenPixelOffset, 0);
                // First segment: from (x+4, y+9) to (x+8, y+13)
                for (int i = 0; i <= 4; i++) {
                    int xi = x + 4 + i;
                    int yi = y + 9 + i;
                    graphics.horizontalLine(xi, xi + 1, yi, 0xFF00FF00);
                }
                // Second segment: from (x+8, y+13) to (x+16, y+5)
                for (int i = 0; i <= 8; i++) {
                    int xi = x + 8 + i;
                    int yi = y + 13 - i;
                    graphics.horizontalLine(xi, xi + 1, yi, 0xFF00FF00);
                }
                // Pop the JOML matrix state to restore previous rendering positions
                graphics.pose().popMatrix();
            }
        }
    }
}
