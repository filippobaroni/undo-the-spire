package undobutton.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;
import savestate.selectscreen.HandSelectScreenState;
import undobutton.GameState;
import undobutton.UndoButtonMod;

public class ScreensPatches {
    @SpirePatch(clz = AbstractDungeon.class, method = "closeCurrentScreen")
    public static class CloseCurrentScreenPatch {
        @SpirePrefixPatch
        public static void addState() {
            AbstractRoom room = AbstractDungeon.getCurrRoom();
            if (room != null && room.phase == AbstractRoom.RoomPhase.COMBAT) {
                switch (AbstractDungeon.screen) {
                    case HAND_SELECT:
                    case GRID:
                        UndoButtonMod.controller.addState(new GameState.Action(GameState.ActionType.CARD_SELECTED));
                        UndoButtonMod.logger.info("Added new state before selecting cards.");
                        break;
                }
            }
        }
    }

    @SpirePatch(clz = HandSelectScreenState.class, method = "loadHandSelectScreenState")
    public static class LoadHandSelectScreenStatePatch {
        @SpirePrefixPatch
        public static void prep(HandSelectScreenState __instance) {
            AbstractDungeon.handCardSelectScreen.upgradePreviewCard = null;
            AbstractDungeon.handCardSelectScreen.hoveredCard = null;
            ReflectionHacks.privateMethod(HandCardSelectScreen.class, "prep").invoke(AbstractDungeon.handCardSelectScreen);
        }
    }
}
