package undobutton.patches.relics;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import savestate.relics.RedSkullState;

public class RedSkullPatches {
    @SpirePatch(clz = RedSkullState.class, method = "loadRelic")
    public static class loadRelicPatch {
        @SpirePostfixPatch
        public static AbstractRelic fixPulse(AbstractRelic __result, RedSkullState __instance, boolean ___isActive) {
            if (___isActive) {
                __result.beginLongPulse();
            }
            return __result;
        }
    }
}
