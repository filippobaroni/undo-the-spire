package undobutton.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.ChooseOneColorless;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import savestate.StateFactories;
import savestate.actions.CurrentActionState;

import java.util.HashMap;

public class ChooseOneColorlessActionPatches {
    @SpirePatch(clz = StateFactories.class, method = "createCurrentActionMap")
    public static class StateFactoriesPatch {
        @SpirePostfixPatch
        public static HashMap<Class, CurrentActionState.CurrentActionFactories> addColorlessFactory(HashMap<Class, CurrentActionState.CurrentActionFactories> __result) {
            __result.put(ChooseOneColorless.class, new CurrentActionState.CurrentActionFactories(ActionState::new));
            return __result;
        }
    }

    public static class ActionState implements CurrentActionState {
        ActionState(AbstractGameAction action) {
            this((ChooseOneColorless) action);
        }

        ActionState(ChooseOneColorless action) {

        }

        @Override
        public AbstractGameAction loadCurrentAction() {
            ChooseOneColorless result = new ChooseOneColorless();
            ReflectionHacks.setPrivate(result, AbstractGameAction.class, "duration", 0.0F);
            return result;
        }

        @SpirePatch(clz = ChooseOneColorless.class, paramtypez = {}, method = "update")
        public static class NoDoubleTriggerActionPatch {
            @SpirePostfixPatch
            public static void noDoubleTrigger(ChooseOneColorless __instance) {
                if (AbstractDungeon.isScreenUp) {
                    __instance.isDone = false;
                }
            }
        }
    }
}
