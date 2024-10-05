package undobutton.patches.monsters;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.AwakenedOne;
import savestate.monsters.beyond.AwakenedOneState;

public class AwakenedOnePatches {
    @SpirePatch(clz = AwakenedOneState.class, method = "loadMonster")
    public static class loadMonsterPatch {
        @SpirePostfixPatch
        public static AbstractMonster setState(AbstractMonster __result, AwakenedOneState __instance, boolean ___form1) {
            if (!___form1 && !__result.halfDead) {
                __result.state.setAnimation(0, "Idle_2", true);
                ReflectionHacks.setPrivate(__result, AwakenedOne.class, "animateParticles", true);
            }
            return __result;
        }
    }
}
