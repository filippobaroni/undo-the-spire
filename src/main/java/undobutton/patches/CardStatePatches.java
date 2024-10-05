package undobutton.patches;


import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import savestate.CardState;

public class CardStatePatches {
    // Patch CardState to store the name and description of the card.
    @SpirePatch(clz = CardState.class, method = SpirePatch.CLASS)
    public static class CardStateExtraFields {
        public static SpireField<String> name = new SpireField<>(() -> null);
        public static SpireField<String> description = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = CardState.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractCard.class})
    public static class CardStateConstructorPatch {
        @SpirePostfixPatch
        public static void Postfix(CardState __instance, AbstractCard card) {
            CardStatePatches.CardStateExtraFields.name.set(__instance, card.name);
            CardStatePatches.CardStateExtraFields.description.set(__instance, card.rawDescription);
        }
    }

    @SpirePatch(clz = CardState.class, method = "loadCard")
    public static class LoadCardPatch {
        @SpirePostfixPatch
        public static AbstractCard Postfix(AbstractCard __result, CardState __instance) {
            __result.name = CardStatePatches.CardStateExtraFields.name.get(__instance);
            __result.rawDescription = CardStatePatches.CardStateExtraFields.description.get(__instance);
            __result.initializeDescription();
            return __result;
        }
    }
}
