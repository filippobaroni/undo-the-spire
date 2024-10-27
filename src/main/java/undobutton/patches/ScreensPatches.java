package undobutton.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;
import com.megacrit.cardcrawl.ui.buttons.GridSelectConfirmButton;
import com.megacrit.cardcrawl.ui.buttons.PeekButton;
import savestate.selectscreen.CardRewardScreenState;
import savestate.selectscreen.GridCardSelectScreenState;
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
                    case CARD_REWARD:
                        UndoButtonMod.controller.addState(new GameState.Action(GameState.ActionType.CARD_SELECTED));
                        UndoButtonMod.logger.info("Added new state before selecting cards.");
                        break;
                }
            }
        }
    }

    @SpirePatch(clz = HandSelectScreenState.class, method = SpirePatch.CLASS)
    public static class HandExtraFields {
        public static SpireField<String> selectionReason = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = HandSelectScreenState.class, method = SpirePatch.CONSTRUCTOR)
    public static class HandConstructorPatch {
        @SpirePostfixPatch
        public static void setSelectionReason(HandSelectScreenState __instance) {
            HandExtraFields.selectionReason.set(__instance, AbstractDungeon.handCardSelectScreen.selectionReason);
        }
    }

    @SpirePatch(clz = HandSelectScreenState.class, method = "loadHandSelectScreenState")
    public static class LoadHandSelectScreenStatePatch {
        @SpirePrefixPatch
        public static void prep(HandSelectScreenState __instance) {
            AbstractDungeon.handCardSelectScreen.upgradePreviewCard = null;
            ReflectionHacks.privateMethod(HandCardSelectScreen.class, "prep").invoke(AbstractDungeon.handCardSelectScreen);
        }

        @SpirePostfixPatch
        public static void post(HandSelectScreenState __instance) {
            AbstractDungeon.handCardSelectScreen.selectionReason = HandExtraFields.selectionReason.get(__instance);
            ReflectionHacks.privateMethod(HandCardSelectScreen.class, "updateMessage").invoke(AbstractDungeon.handCardSelectScreen);
        }
    }

    @SpirePatch(clz = GridCardSelectScreenState.class, method = SpirePatch.CLASS)
    public static class GridExtraFields {
        public static SpireField<String> tipMsg = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = GridCardSelectScreenState.class, method = SpirePatch.CONSTRUCTOR)
    public static class GridConstructorPatch {
        @SpirePostfixPatch
        public static void setTipMsg(GridCardSelectScreenState __instance) {
            GridExtraFields.tipMsg.set(__instance, ReflectionHacks.getPrivate(AbstractDungeon.gridSelectScreen, GridCardSelectScreen.class, "tipMsg"));
        }
    }

    @SpirePatch(clz = GridCardSelectScreenState.class, method = "loadGridSelectScreen")
    public static class LoadGridSelectScreenStatePatch {
        @SpirePrefixPatch
        public static void prep(GridCardSelectScreenState __instance) {
            AbstractDungeon.gridSelectScreen.upgradePreviewCard = null;
            ReflectionHacks.privateMethod(GridCardSelectScreen.class, "callOnOpen").invoke(AbstractDungeon.gridSelectScreen);
        }

        @SpirePostfixPatch
        public static void post(GridCardSelectScreenState __instance) {
            GridCardSelectScreen screen = AbstractDungeon.gridSelectScreen;
            ReflectionHacks.setPrivate(screen, GridCardSelectScreen.class, "tipMsg", GridExtraFields.tipMsg.get(__instance));
            screen.peekButton.hideInstantly();
            screen.peekButton.show();
            ReflectionHacks.setPrivate(screen.peekButton, PeekButton.class, "current_x", ReflectionHacks.getPrivate(screen.peekButton, PeekButton.class, "target_x"));
            screen.confirmButton.hideInstantly();
            if (!screen.confirmButton.isDisabled) {
                screen.confirmButton.show();
                ReflectionHacks.setPrivate(screen.confirmButton, GridSelectConfirmButton.class, "current_x", ReflectionHacks.getPrivate(screen.confirmButton, GridSelectConfirmButton.class, "target_x"));
            }
            ReflectionHacks.privateMethod(GridCardSelectScreen.class, "calculateScrollBounds").invoke(screen);
            ReflectionHacks.privateMethod(GridCardSelectScreen.class, "updateCardPositionsAndHoverLogic").invoke(screen);
            screen.targetGroup.group.forEach(c -> {
                c.current_x = c.target_x;
                c.current_y = c.target_y;
            });
        }
    }

    @SpirePatch(clz = CardRewardScreenState.class, method = SpirePatch.CLASS)
    public static class CardRewardExtraFields {
        public static SpireField<String> header = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = CardRewardScreenState.class, method = SpirePatch.CONSTRUCTOR)
    public static class CardRewardConstructorPatch {
        @SpirePostfixPatch
        public static void setHeader(CardRewardScreenState __instance) {
            CardRewardExtraFields.header.set(__instance, ReflectionHacks.getPrivate(AbstractDungeon.cardRewardScreen, CardRewardScreen.class, "header"));
        }
    }

    @SpirePatch(clz = CardRewardScreenState.class, method = "loadCardRewardScreen")
    public static class LoadCardRewardScreenStatePatch {
        @SpirePostfixPatch
        public static void post(CardRewardScreenState __instance) {
            ReflectionHacks.setPrivate(AbstractDungeon.cardRewardScreen, CardRewardScreen.class, "header", CardRewardExtraFields.header.get(__instance));
            AbstractDungeon.cardRewardScreen.reopen();
            AbstractDungeon.cardRewardScreen.discoveryCard = null;
            AbstractDungeon.overlayMenu.showBlackScreen();
        }
    }
}
