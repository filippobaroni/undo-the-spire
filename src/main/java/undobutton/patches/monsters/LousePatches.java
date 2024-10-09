package undobutton.patches.monsters;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import savestate.monsters.exordium.LouseDefensiveState;
import savestate.monsters.exordium.LouseNormalState;

public class LousePatches {
    @SpirePatch(clz = LouseNormalState.class, method = "loadMonster")
    public static class LouseNormalPatch {
        @SpirePostfixPatch
        public static AbstractMonster curlUpIfClosed(AbstractMonster __result, LouseNormalState __instance, boolean ___isOpen) {
            if (!___isOpen) {
                __result.state.setAnimation(0, "idle closed", true);
            }
            return __result;
        }
    }

    @SpirePatch(clz = LouseDefensiveState.class, method = "loadMonster")
    public static class LouseDefensivePatch {
        @SpirePostfixPatch
        public static AbstractMonster curlUpIfClosed(AbstractMonster __result, LouseDefensiveState __instance, boolean ___isOpen) {
            if (!___isOpen) {
                __result.state.setAnimation(0, "idle closed", true);
            }
            return __result;
        }
    }
}
