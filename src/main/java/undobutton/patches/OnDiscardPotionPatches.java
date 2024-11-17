package undobutton.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import undobutton.GameState;
import undobutton.UndoButtonMod;

public class OnDiscardPotionPatches {
    @SpirePatch(clz = PotionPopUp.class, method = "updateInput")
    public static class UpdateInputPatch {
        @SpireInsertPatch(locator = Locator.class)
        public static void addStateOnDiscard(PotionPopUp __instance, AbstractPotion ___potion) {
            if (AbstractDungeon.getCurrMapNode() == null) {
                return;
            }
            AbstractRoom room = AbstractDungeon.getCurrRoom();
            if (room == null || room.phase != AbstractRoom.RoomPhase.COMBAT) {
                return;
            }
            UndoButtonMod.controller.addState(new GameState.Action(GameState.ActionType.POTION_DISCARDED, ___potion));
            UndoButtonMod.logger.info("Added new state before discarding potion {}.", ___potion.name);
        }

        public static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(TopPanel.class, "destroyPotion");
                int[] allMatches = LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher);
                return new int[]{allMatches[allMatches.length - 1]};
            }
        }
    }
}
