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

import java.util.List;
import java.util.Set;

import static _1ms.McOverTor.manager.LocationMgr.TorRegionInfo;

public class Region extends Screen {
    private static final List<TorRegionInfo> regions = LocationMgr.getCtr();
    private static final Set<String> usedR = LocationMgr.getSelCtr();
    //static final SettCheckBox multipleSel = new SettCheckBox(0,0,Text.literal("Select multiple"), TorOption.multipleSel);
    static final SettCheckBox strictRegion = new SettCheckBox(0,0,Text.literal("Strict nodes enforcement"), TorOption.strictRegion);
    static final ButtonWidget closeBtn = ButtonWidget.builder(Text.literal("Done"), btn-> closeBtnF()).build();
    private static TorRegionList regList;
    public Region() {
        super(Text.literal("Tor Region Selector"));
    }

    private static void closeFunc() {
        MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));
    }

    private static void closeBtnF() {
        LocationMgr.modRegions(usedR);
        if(TorManager.progress == 100) {
            TorManager.exitTor(true);
            TorManager.startTor();
            return;
        }
        closeFunc();
    }

    @Override
    protected void init() {//List lenght repsonsive
        super.init();
        closeBtn.setFocused(false);
        if(regList==null) {//250
            regList = new TorRegionList(this.client, 260,300,0,20);
            regions.forEach(regList::addItem);
        }
        regList.setPosition(this.width/2-130, this.height/2-175);

        //multipleSel.setPosition(this.width/2, this.height/2+200);
        strictRegion.setPosition(this.width/2-75, this.height/2+135);
        closeBtn.setPosition(this.width / 2 - 75, this.height/2+170);

        strictRegion.setTooltip(Tooltip.of(Text.literal("Ensures Tor only connects through the location(s) that you've selected, even if there are no available nodes, or they are very slow.")));

        //this.addSelectableChild(multipleSel);
        this.addSelectableChild(strictRegion);
        this.addSelectableChild(closeBtn);
        this.addSelectableChild(regList);
    }

    @Override
    public void close() {
        closeFunc();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context,mouseX,mouseY,deltaTicks);

        Main.renderWindow(context, (this.width - 200) / 2-50, this.height/2-200, 300, 400, "McOverTor Regions");
        regList.render(context,mouseX,mouseY,deltaTicks);
        //multipleSel.render(context,mouseX,mouseY,deltaTicks);
        strictRegion.render(context,mouseX,mouseY,deltaTicks);
        closeBtn.render(context,mouseX,mouseY,deltaTicks);
    }

    private static class TorRegionList extends EntryListWidget<TorRegionList.TorRegion> {
        public TorRegionList(MinecraftClient client, int width, int height, int y, int itemsHeight) {
            super(client, width, height, y, itemsHeight);

        }

        @Override
        protected int getScrollbarX() {
            return this.getRowRight()+12;
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }

        // Add entries to your list
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


