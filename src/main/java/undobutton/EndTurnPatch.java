package undobutton;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.GameActionManager;

@SpirePatch(clz = GameActionManager.class, method = "callEndOfTurnActions")
public class EndTurnPatch {
    public static void Prefix(GameActionManager __instance) {
        UndoButtonMod.logger.info("Added new state before ending turn.");
        UndoButtonMod.controller.addState(new GameState.Action(GameState.ActionType.TURN_ENDED));
    }
}