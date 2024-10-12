package undobutton.patches.monsters;

import basemod.ReflectionHacks;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import savestate.monsters.city.ByrdState;

public class ByrdPatches {
    @SpirePatch(clz = ByrdState.class, method = "loadMonster")
    public static class loadMonsterPatch {
        @SpirePostfixPatch
        public static AbstractMonster setState(AbstractMonster __result, ByrdState __instance, boolean ___isFlying) {
            if (!___isFlying) {
                ReflectionHacks.privateMethod(AbstractCreature.class, "loadAnimation", String.class, String.class, float.class).invoke(__result, "images/monsters/theCity/byrd/grounded.atlas", "images/monsters/theCity/byrd/grounded.json", 1.0F);
                AnimationState.TrackEntry e = __result.state.setAnimation(0, "idle", true);
                e.setTime(e.getEndTime() * MathUtils.random());
                ReflectionHacks.privateMethod(AbstractMonster.class, "updateHitbox", float.class, float.class, float.class, float.class).invoke(__result, 10.0F, -50.0F, 240.0F, 180.0F);
            }
            return __result;
        }
    }
}
