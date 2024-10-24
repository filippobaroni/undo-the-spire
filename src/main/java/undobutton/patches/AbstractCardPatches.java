package undobutton.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;

import java.util.UUID;

public class AbstractCardPatches {
    // Add a truly unique UUID to AbstractCard
    @SpirePatch(clz = AbstractCard.class, method = SpirePatch.CLASS)
    public static class ExtraFields {
        public static SpireField<UUID> trulyUniqueUuid = new SpireField<>(UUID::randomUUID);
    }
}
