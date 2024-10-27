package undobutton.patches.monsters;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.Reptomancer;
import savestate.monsters.beyond.ReptomancerState;
import undobutton.GameState;
import undobutton.patches.AbstractCreaturePatches;

import java.util.UUID;

public class ReptomancerPatches {
    @SpirePatch(clz = ReptomancerState.class, method = SpirePatch.CLASS)
    public static class ExtraFields {
        public static SpireField<UUID[]> daggers = new SpireField<>(() -> new UUID[4]);
    }

    @SpirePatch(clz = ReptomancerState.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractMonster.class})
    public static class ConstructorPatch {
        @SpirePostfixPatch
        public static void setDaggers(ReptomancerState __instance, AbstractMonster monster) {
            AbstractMonster[] daggers = ReflectionHacks.getPrivate(monster, Reptomancer.class, "daggers");
            for (int i = 0; i < 4; i++) {
                if (daggers[i] != null) {
                    ExtraFields.daggers.get(__instance)[i] = AbstractCreaturePatches.ExtraFields.uuid.get(daggers[i]);
                }
            }
        }
    }

    @SpirePatch(clz = ReptomancerState.class, method = "loadMonster")
    public static class LoadPatch {
        @SpirePostfixPatch
        public static AbstractMonster addLoadDaggersAction(AbstractMonster __result, ReptomancerState __instance) {
            GameState.addPostLoadRunner((info) -> {
                AbstractMonster[] daggers = ReflectionHacks.getPrivate(__result, Reptomancer.class, "daggers");
                for (int i = 0; i < 4; i++) {
                    if (ExtraFields.daggers.get(__instance)[i] != null) {
                        UUID uuid = ExtraFields.daggers.get(__instance)[i];
                        daggers[i] = AbstractCreaturePatches.getMonsterFromUUID(uuid);
                    }
                }
            });
            return __result;
        }
    }
}
