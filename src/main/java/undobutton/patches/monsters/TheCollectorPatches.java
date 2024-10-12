package undobutton.patches.monsters;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.TheCollector;
import savestate.monsters.city.TheCollectorState;
import undobutton.patches.AbstractCreaturePatches;

import java.util.HashMap;
import java.util.UUID;

public class TheCollectorPatches {
    @SpirePatch(clz = TheCollectorState.class, method = SpirePatch.CLASS)
    public static class ExtraFields {
        public static SpireField<HashMap<Integer, UUID>> enemySlots = new SpireField<>(HashMap::new);
    }

    @SpirePatch(clz = TheCollectorState.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractMonster.class})
    public static class ConstructorPatch {
        @SpirePostfixPatch
        public static void setEnemySlots(TheCollectorState __instance, AbstractMonster monster) {
            HashMap<Integer, AbstractMonster> enemySlots = ReflectionHacks.getPrivate(monster, TheCollector.class, "enemySlots");
            for (int i = 1; i <= 2; i++) {
                AbstractMonster g = enemySlots.get(i);
                if (g != null) {
                    ExtraFields.enemySlots.get(__instance).put(i, AbstractCreaturePatches.ExtraFields.uuid.get(g));
                }
            }
        }
    }

    @SpirePatch(clz = TheCollectorState.class, method = "loadMonster")
    public static class LoadPatch {
        @SpirePostfixPatch
        public static AbstractMonster addLoadMinionsAction(AbstractMonster __result, TheCollectorState __instance) {
            AbstractDungeon.actionManager.addToTop(new AbstractGameAction() {
                @Override
                public void update() {
                    HashMap<Integer, AbstractMonster> enemySlots = ReflectionHacks.getPrivate(__result, TheCollector.class, "enemySlots");
                    enemySlots.clear();
                    for (int i = 1; i <= 2; i++) {
                        if (ExtraFields.enemySlots.get(__instance).containsKey(i)) {
                            UUID uuid = ExtraFields.enemySlots.get(__instance).get(i);
                            enemySlots.put(i, AbstractDungeon.getMonsters().monsters.stream().filter(m -> AbstractCreaturePatches.ExtraFields.uuid.get(m).equals(uuid)).findFirst().get());
                        }
                    }
                    this.isDone = true;
                }
            });
            return __result;
        }
    }
}
