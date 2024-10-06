package undobutton.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import savestate.relics.RelicState;

public class RelicStatePatches {
    @SpirePatch(clz = RelicState.class, method = "loadRelic")
    public static class LoadRelicPatch {
        @SpirePostfixPatch
        public static AbstractRelic loadRelicPatch(AbstractRelic __result, RelicState __instance, boolean ___pulse) {
            if (___pulse) {
                __result.beginLongPulse();
            }
            if (ExtraFields.usedUp.get(__instance)) {
                __result.usedUp();
            }
            return __result;
        }
    }

    @SpirePatch(clz = RelicState.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractRelic.class})
    public static class ConstructorPatch {
        public static void Postfix(RelicState __instance, AbstractRelic relic) {
            ExtraFields.usedUp.set(__instance, relic.usedUp);
        }
    }

    @SpirePatch(clz = RelicState.class, method = SpirePatch.CLASS)
    public static class ExtraFields {
        public static SpireField<Boolean> usedUp = new SpireField<>(() -> false);
    }
}
