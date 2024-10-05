package undobutton.patches.relics;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import savestate.relics.ArtOfWarState;

public class ArtOfWarPatches {
    @SpirePatch(clz = ArtOfWarState.class, method = "loadRelic")
    public static class loadRelicPatch {
        @SpirePostfixPatch
        public static AbstractRelic fixPulse(AbstractRelic __result, ArtOfWarState __instance, boolean ___gainEnergyNext) {
            if (___gainEnergyNext) {
                __result.beginLongPulse();
            }
            return __result;
        }
    }
}
