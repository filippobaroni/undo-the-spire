package undobutton.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import savestate.CreatureState;

public class CreatureStatePatches {
    @SpirePatch(clz = CreatureState.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractCreature.class})
    public static class ConstructorPatch {
        @SpirePostfixPatch
        public static void setIsDead(CreatureState __instance, AbstractCreature creature) {
            if (creature.isDying) {
                ReflectionHacks.setPrivate(__instance, CreatureState.class, "isDead", true);
            }
        }
    }
}
