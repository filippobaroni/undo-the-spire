package undobutton.patches.monsters;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.GremlinLeader;
import savestate.monsters.city.GremlinLeaderState;
import undobutton.GameState;
import undobutton.patches.AbstractCreaturePatches;

import java.util.UUID;

public class GremlinLeaderPatches {
    // Store gremlin list
    @SpirePatch(clz = GremlinLeaderState.class, method = SpirePatch.CLASS)
    public static class ExtraFields {
        public static SpireField<UUID[]> gremlins = new SpireField<>(() -> new UUID[3]);
    }

    @SpirePatch(clz = GremlinLeaderState.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractMonster.class})
    public static class ConstructorPatch {
        @SpirePostfixPatch
        public static void setGremlins(GremlinLeaderState __instance, AbstractMonster monster) {
            for (int i = 0; i < 3; i++) {
                AbstractMonster g = ((GremlinLeader) monster).gremlins[i];
                if (g != null) {
                    ExtraFields.gremlins.get(__instance)[i] = AbstractCreaturePatches.ExtraFields.uuid.get(g);
                }
            }
        }
    }

    @SpirePatch(clz = GremlinLeaderState.class, method = "loadMonster")
    public static class LoadPatch {
        @SpirePostfixPatch
        public static AbstractMonster addLoadGremlinsRunner(AbstractMonster __result, GremlinLeaderState __instance) {
            GameState.addPostLoadRunner(() -> {
                for (int i = 0; i < 3; i++) {
                    if (ExtraFields.gremlins.get(__instance)[i] != null) {
                        UUID uuid = ExtraFields.gremlins.get(__instance)[i];
                        ((GremlinLeader) __result).gremlins[i] = AbstractDungeon.getMonsters().monsters.stream().filter(m -> AbstractCreaturePatches.ExtraFields.uuid.get(m).equals(uuid)).findFirst().get();
                    }
                }
            });
            return __result;
        }
    }
}
