package undobutton.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.optionCards.ChooseCalm;
import com.megacrit.cardcrawl.cards.optionCards.ChooseWrath;
import com.megacrit.cardcrawl.helpers.CardLibrary;

public class CardLibraryPatches {
    @SpirePatch2(clz = CardLibrary.class, method = "addColorlessCards", paramtypez = {})
    public static class AddStanceChoicesPatch {
        @SpirePostfixPatch
        public static void Postfix() {
            CardLibrary.add(new ChooseCalm());
            CardLibrary.add(new ChooseWrath());
        }
    }
}
