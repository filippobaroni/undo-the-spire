package undobutton.patches.relics;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import savestate.relics.CentennialPuzzleState;

public class CentennialPuzzlePatches {
    @SpirePatch(clz = CentennialPuzzleState.class, method = "loadRelic")
    public static class loadRelicPatch {
        @SpirePostfixPatch
        public static AbstractRelic fixDisplay(AbstractRelic __result, CentennialPuzzleState __instance, boolean ___usedThisCombat) {
            if (___usedThisCombat) {
                __result.grayscale = true;
            } else {
                __result.beginLongPulse();
            }
            return __result;
        }
    }
}
