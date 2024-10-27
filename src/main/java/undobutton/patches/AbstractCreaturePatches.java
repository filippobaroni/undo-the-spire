package undobutton.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.UUID;

public class AbstractCreaturePatches {
    // Add UUID to AbstractCreature
    @SpirePatch(clz = AbstractCreature.class, method = SpirePatch.CLASS)
    public static class ExtraFields {
        public static SpireField<UUID> uuid = new SpireField<>(UUID::randomUUID);
    }

    public static AbstractMonster getMonsterFromUUID(UUID uuid) {
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (AbstractCreaturePatches.ExtraFields.uuid.get(m).equals(uuid)) {
                return m;
            }
        }
        return null;
    }
}
