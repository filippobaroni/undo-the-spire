package undobutton.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.AbstractCreature;

import java.util.UUID;

public class AbstractCreaturePatches {
    // Add UUID to AbstractCreature
    @SpirePatch(clz = AbstractCreature.class, method = SpirePatch.CLASS)
    public static class ExtraFields {
        public static SpireField<UUID> uuid = new SpireField<>(UUID::randomUUID);
    }
}
