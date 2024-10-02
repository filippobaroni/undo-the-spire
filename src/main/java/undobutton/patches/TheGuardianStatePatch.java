package undobutton.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.TheGuardian;
import savestate.monsters.exordium.TheGuardianState;

@SpirePatch(clz = TheGuardianState.class, method = "loadMonster")
public class TheGuardianStatePatch {
    @SpirePostfixPatch
    public static AbstractMonster setClosedOrOpenAnimation(AbstractMonster __result, TheGuardianState state) {
        TheGuardian monster = (TheGuardian) __result;
        if (ReflectionHacks.getPrivate(monster, TheGuardian.class, "isOpen")) {
            monster.state.setTimeScale(1.0F);
            monster.state.setAnimation(0, "idle", true);
            ReflectionHacks.privateMethod(AbstractMonster.class, "updateHitbox", float.class, float.class, float.class, float.class).invoke(monster, 0.0F, 95.0F, 440.0F, 350.0F);
        } else {
            monster.state.setTimeScale(2.0F);
            monster.state.setAnimation(0, "defensive", true);
            ReflectionHacks.privateMethod(AbstractMonster.class, "updateHitbox", float.class, float.class, float.class, float.class).invoke(monster, 0.0F, 95.0F, 440.0F, 250.0F);
        }
        return monster;
    }
}
