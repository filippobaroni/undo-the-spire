package undobutton.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import savestate.CreatureState;

import java.util.UUID;

public class CreatureStatePatches {
    @SpirePatch(clz = CreatureState.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractCreature.class})
    public static class ConstructorPatch {
        // If isDying, then isDead
        @SpirePostfixPatch
        public static void setIsDead(CreatureState __instance, AbstractCreature creature) {
            if (creature.isDying) {
                ReflectionHacks.setPrivate(__instance, CreatureState.class, "isDead", true);
            }
        }

        // Ignore animation of coordinates
        @SpirePostfixPatch
        public static void resetAnimationXY(CreatureState __instance, AbstractCreature creature) {
            ReflectionHacks.setPrivate(__instance, CreatureState.class, "animX", 0.0F);
            ReflectionHacks.setPrivate(__instance, CreatureState.class, "animY", 0.0F);
        }

        // Store uuid
        @SpirePostfixPatch
        public static void setUUID(CreatureState __instance, AbstractCreature creature) {
            CreatureStatePatches.ExtraFields.uuid.set(__instance, AbstractCreaturePatches.ExtraFields.uuid.get(creature));
        }
    }

    // Add uuid field to CreatureState
    @SpirePatch(clz = CreatureState.class, method = SpirePatch.CLASS)
    public static class ExtraFields {
        public static SpireField<UUID> uuid = new SpireField<>(() -> null);
    }

    // Load uuid from CreatureState
    @SpirePatch(clz = CreatureState.class, method = "loadCreature")
    public static class LoadCreaturePatch {
        public static void Postfix(CreatureState __instance, AbstractCreature creature) {
            AbstractCreaturePatches.ExtraFields.uuid.set(creature, CreatureStatePatches.ExtraFields.uuid.get(__instance));
        }
    }
}
