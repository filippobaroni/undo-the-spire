package undobutton.patches;


import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import savestate.CardState;

public class CardStatePatches {
    @SpirePatch(clz = CardState.class, method = SpirePatch.CLASS)
    public static class ExtraFields {
        public static SpireField<String> name = new SpireField<>(() -> null);
        public static SpireField<String> description = new SpireField<>(() -> null);
        public static SpireField<Float> target_x = new SpireField<>(() -> 0.0F);
        public static SpireField<Float> target_y = new SpireField<>(() -> 0.0F);
    }

    @SpirePatch(clz = CardState.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractCard.class})
    public static class CardStateConstructorPatch {
        @SpirePostfixPatch
        public static void Postfix(CardState __instance, AbstractCard card) {
            ExtraFields.name.set(__instance, card.name);
            ExtraFields.description.set(__instance, card.rawDescription);
            ExtraFields.target_x.set(__instance, card.target_x);
            ExtraFields.target_y.set(__instance, card.target_y);
        }
    }

    @SpirePatch(clz = CardState.class, method = "loadCard")
    public static class LoadCardPatch {
        @SpirePostfixPatch
        public static AbstractCard Postfix(AbstractCard __result, CardState __instance) {
            __result.name = ExtraFields.name.get(__instance);
            __result.rawDescription = ExtraFields.description.get(__instance);
            __result.initializeDescription();
            __result.target_x = ExtraFields.target_x.get(__instance);
            __result.target_y = ExtraFields.target_y.get(__instance);
            __result.current_x = __result.target_x;
            __result.current_y = __result.target_y;
            return __result;
        }
    }
}
