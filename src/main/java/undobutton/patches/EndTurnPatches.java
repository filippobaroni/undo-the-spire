package undobutton.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.ui.buttons.EndTurnButton;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import undobutton.GameState;
import undobutton.UndoButtonMod;

public class EndTurnPatches {
    @SpirePatch(clz = GameActionManager.class, method = "callEndOfTurnActions")
    public static class CallEndOfTurnActionsPatch {
        public static void Prefix(GameActionManager __instance) {
            if (!UndoButtonMod.controller.isThisEndTurnForced) {
                UndoButtonMod.logger.info("Adding new state before ending turn.");
                UndoButtonMod.controller.addState(new GameState.Action(GameState.ActionType.TURN_ENDED));
            }
        }
    }

    @SpirePatch(clz = EndTurnButton.class, method = "update")
    public static class EndTurnButtonUpdatePatch {
        @SpireInsertPatch(locator = Locator.class)
        public static void SetEndTurnNotForced(EndTurnButton __instance) {
            UndoButtonMod.controller.isThisEndTurnForced = false;
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(EndTurnButton.class, "disable");
                return LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    @SpirePatch(clz = GameActionManager.class, method = "callEndTurnEarlySequence")
    public static class CallEndTurnEarlySequencePatch {
        public static void Prefix(GameActionManager __instance) {
            UndoButtonMod.controller.isThisEndTurnForced = true;
        }
    }
}