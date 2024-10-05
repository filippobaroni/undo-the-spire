package undobutton.patches.monsters;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import savestate.monsters.city.ByrdState;

public class ByrdPatches {
    @SpirePatch(clz = ByrdState.class, method = "loadMonster")
    public static class loadMonsterPatch {
        @SpirePostfixPatch
        public static AbstractMonster setState(AbstractMonster __result, ByrdState __instance, boolean ___isFlying) {
            __result.changeState(___isFlying ? "FLYING" : "GROUNDED");
            return __result;
        }
    }
}
