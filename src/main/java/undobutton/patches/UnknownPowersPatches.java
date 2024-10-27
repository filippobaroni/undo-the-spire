package undobutton.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import savestate.powers.PowerState;
import undobutton.UndoButtonMod;

import java.lang.reflect.InvocationTargetException;

public class UnknownPowersPatches {
    @SpirePatch(clz = PowerState.class, method = "forPower")
    public static class forPowerPatch {
        @SpireInsertPatch(locator = Locator.class)
        public static SpireReturn<PowerState> tryToSaveUnknownPower(AbstractPower power) {
            if (!UndoButtonMod.DEBUG) {
                return SpireReturn.Return(new PowerState(power) {
                    @Override
                    public AbstractPower loadPower(AbstractCreature targetAndSource) {
                        try {
                            return power.getClass().getConstructor(AbstractCreature.class, int.class).newInstance(targetAndSource, amount);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                 NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
            return SpireReturn.Continue();
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.NewExprMatcher(IllegalStateException.class);
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }
}
