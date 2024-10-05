package undobutton.patches.relics;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import savestate.relics.PocketwatchState;

public class PocketwatchPatches {
    @SpirePatch(clz = PocketwatchState.class, method = "loadRelic")
    public static class LoadRelicPatch {
        public static AbstractRelic Postfix(AbstractRelic __result, PocketwatchState __instance) {
            if (__result.counter >= 0 && __result.counter <= 3) {
                __result.beginLongPulse();
            }
            return __result;
        }
    }
}
