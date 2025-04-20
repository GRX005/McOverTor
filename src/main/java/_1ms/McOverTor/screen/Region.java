/*
    This file is part of the McOverTor project, licensed under the
    GNU General Public License v3.0

    Copyright (C) 2024-2025 _1ms

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

import _1ms.McOverTor.Main;
import _1ms.McOverTor.manager.LocationMgr;
import _1ms.McOverTor.manager.TorManager;
import _1ms.McOverTor.manager.TorOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static _1ms.McOverTor.manager.LocationMgr.TorRegionInfo;
import static _1ms.McOverTor.manager.SettingsMgr.get;

public class Region extends Screen {
    private static final List<TorRegionInfo> regions = LocationMgr.getCtr();
    private static final Set<String> usedR = LocationMgr.getSelCtr();
    static final SettCheckBox multiRegion = new SettCheckBox(0,0,Text.literal("Enforce for all nodes"), TorOption.allNodes);
    static final ButtonWidget closeBtn = ButtonWidget.builder(Text.literal("Done"), btn-> closeBtnF()).build();
    static final ButtonWidget resetBtn = ButtonWidget.builder(Text.literal("Reset"), btn-> usedR.clear()).size(100,20).build();
    private static TorRegionList regList;
    private static Set<String> snapshot;
    private static boolean blSnap;
    public Region() {//Take a snapshot of the options, so when the menu is closed we can see what changed.
        super(Text.literal("Tor Region Selector"));
        snapshot = new HashSet<>(usedR);
        blSnap = get(TorOption.allNodes);
    }

    private static void closeFunc() {
        MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));
    }
//Switch between multi or single node application, and/or apply the change of countries
    private static void closeBtnF() {
        if(!usedR.equals(snapshot)) {//If the selected countries changed
            LocationMgr.modRegions(usedR);
            checkAndRelaunch();
            return;
        }
        //Re-Start Tor if already started so that the settings will apply, otherwise close.
        if (blSnap != get(TorOption.allNodes) && !usedR.isEmpty()) { //If the state of the tick changed
            LocationMgr.remOrAdd(get(TorOption.allNodes));
            checkAndRelaunch();
            return;
        }
         closeFunc();
    }

    private static void checkAndRelaunch() {
        if(TorManager.progress == 100) {
            TorManager.exitTor(true);
            TorManager.startTor();
            return;
        }
        closeFunc();
    }

    @Override
    protected void init() {
        super.init();
        closeBtn.setFocused(false);
        resetBtn.setFocused(false);
        if(regList==null) {//250. Create the list UI and add the regions' names.
            regList = new TorRegionList(this.client, 260,300,0,20);
            regions.forEach(regList::addItem);
        }
        regList.setPosition(this.width/2-130, this.height/2-175);

        multiRegion.setPosition(this.width/2-75, this.height/2+132);//135
        closeBtn.setPosition(this.width / 2 - 75, this.height/2+175);
        resetBtn.setPosition(this.width / 2 - 50, this.height/2+152);

        multiRegion.setTooltip(Tooltip.of(Text.literal("Make the selection(s) also apply to the Entry and Middle nodes, not just the ExitNode.")));

        this.addSelectableChild(multiRegion);
        this.addSelectableChild(closeBtn);
        this.addSelectableChild(resetBtn);
        this.addSelectableChild(regList);
    }

    @Override
    public void close() {
        usedR.clear();//Restore when the user exits with ESC instead of the done btn
        usedR.addAll(snapshot);
        closeFunc();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context,mouseX,mouseY,deltaTicks);

        Main.renderWindow(context, this.width/2-150, this.height/2-200, 300, 400, "McOverTor Regions");
        regList.render(context,mouseX,mouseY,deltaTicks);

        multiRegion.render(context,mouseX,mouseY,deltaTicks);
        closeBtn.render(context,mouseX,mouseY,deltaTicks);
        resetBtn.render(context,mouseX,mouseY,deltaTicks);

        if(usedR.isEmpty())
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, "none selected -> Tor decides",this.width/2 ,this.height/2-190, 0xFFFFFF);
    }
//Use the default MC list widget to create our own.
    private static class TorRegionList extends EntryListWidget<TorRegionList.TorRegion> {
        public TorRegionList(MinecraftClient client, int width, int height, int y, int itemsHeight) {
            super(client, width, height, y, itemsHeight);
        }
//Correctly position the scrollbar so it aligns to the list's width properly
        @Override
        protected int getScrollbarX() {
            return this.getRowRight()+12;
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }

        // Add entries to the list
        public void addItem(TorRegionInfo reg) {
            this.addEntry(new TorRegion(reg.name(), reg.code()));
        }

        // Entry class for each item in the list
        private static class TorRegion extends EntryListWidget.Entry<TorRegion> {
            private final Text text;
            private final String code;

            public TorRegion(String text, String code) {
                this.text = Text.literal(text);
                this.code = code;
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.drawText(MinecraftClient.getInstance().textRenderer, text, x, y + 4, 0xFFFFFF, true);
                if (usedR.contains(code))
                    drawTick(context,x-22,y-2);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                // Handle click events
                if(usedR.contains(code))
                    usedR.remove(code);
                else
                    usedR.add(code);

                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            void drawTick(DrawContext context, int x, int y) {
                final int[][] segments = {
                        {x + 4, y + 9, x + 8, y + 13},  // Left segment
                        {x + 8, y + 13, x + 16, y + 5}  // Right segment
                };

                for (int[] seg : segments) {
                    int dx = seg[2] - seg[0];
                    int dy = seg[3] - seg[1];
                    int steps = Math.max(Math.abs(dx), Math.abs(dy));

                    for (int i = 0; i <= steps; i++) {
                        int xi = seg[0] + i * dx / steps;
                        int yi = seg[1] + i * dy / steps;

                        for (int t = 0; t < 2; t++) {  //thickness=2
                            context.drawVerticalLine(xi + t, yi, yi, 0xFF00FF00);
                        }
                    }
                }
            }
        }
    }
}


