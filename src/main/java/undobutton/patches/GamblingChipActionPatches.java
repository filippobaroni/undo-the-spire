package undobutton.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.unique.GamblingChipAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import savestate.StateFactories;
import savestate.actions.CurrentActionState;

import java.util.HashMap;

public class GamblingChipActionPatches {
    @SpirePatch(clz = StateFactories.class, method = "createCurrentActionMap")
    public static class StateFactoriesPatch {
        @SpirePostfixPatch
        public static HashMap<Class, CurrentActionState.CurrentActionFactories> addGamblingFactory(HashMap<Class, CurrentActionState.CurrentActionFactories> __result) {
            __result.put(GamblingChipAction.class, new CurrentActionState.CurrentActionFactories(GamblingChipActionPatches.ActionState::new));
            return __result;
        }
    }

    public static class ActionState implements CurrentActionState, savestate.actions.ActionState {
        final boolean notchip;

        public ActionState(AbstractGameAction action) {
            this((GamblingChipAction) action);
        }

        public ActionState(GamblingChipAction action) {
            notchip = ReflectionHacks.getPrivate(action, GamblingChipAction.class, "notchip");
        }

        @Override
        public AbstractGameAction loadCurrentAction() {
            AbstractGameAction result = new GamblingChipAction(AbstractDungeon.player, notchip);
            ReflectionHacks.setPrivate(result, AbstractGameAction.class, "duration", 0);
            return result;
        }

        @Override
        public AbstractGameAction loadAction() {
            return new GamblingChipAction(AbstractDungeon.player, notchip);
        }

        @SpirePatch(clz = GamblingChipAction.class, paramtypez = {}, method = "update")
        public static class NoDoubleTriggerActionPatch {
            @SpirePostfixPatch
            public static void noDoubleTrigger(GamblingChipAction __instance) {
                if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved && AbstractDungeon.isScreenUp) {
                    __instance.isDone = false;
                }
            }
        }
    }
}
