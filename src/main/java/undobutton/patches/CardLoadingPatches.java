package undobutton.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.unique.ExhumeAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import savestate.CardQueueItemState;
import savestate.actions.ExhumeActionState;
import savestate.actions.UseCardActionState;
import savestate.fastobjects.actions.UpdateOnlyUseCardAction;
import undobutton.GameState;

import java.util.ArrayList;

public class CardLoadingPatches {
    @SpirePatch(clz = CardQueueItemState.class, method = "loadItem")
    public static class CardQueueItemPatch {
        @SpirePostfixPatch
        static public CardQueueItem linkCard(CardQueueItem __result, CardQueueItemState __instance) {
            GameState.addPostLoadRunner((info) -> {
                AbstractCard c = info.getAbstractCard(__result.card);
                if (c != null) {
                    __result.card = c;
                }
            });
            return __result;
        }
    }

    @SpirePatch(clz = UseCardActionState.class, method = "loadAction", paramtypez = {})
    public static class UseCardActionPatch {
        @SpirePostfixPatch
        static public UpdateOnlyUseCardAction linkCard(UpdateOnlyUseCardAction __result, UseCardActionState __instance) {
            GameState.addPostLoadRunner((info) -> {
                AbstractCard c0 = ReflectionHacks.getPrivate(__result, UpdateOnlyUseCardAction.class, "targetCard");
                AbstractCard c = info.getAbstractCard(c0);
                if (c != null) {
                    ReflectionHacks.setPrivate(__result, UpdateOnlyUseCardAction.class, "targetCard", c);
                }
            });
            return __result;
        }
    }

    @SpirePatch(clz = ExhumeActionState.class, method = "loadCurrentAction", paramtypez = {})
    public static class ExhumeActionPatch {
        @SpirePostfixPatch
        public static AbstractGameAction linkCard(AbstractGameAction __result, ExhumeActionState __instance) {
            GameState.addPostLoadRunner((info) -> {
                ((ArrayList<AbstractCard>) ReflectionHacks.getPrivate(__result, ExhumeAction.class, "exhumes")).replaceAll(c0 -> {
                    AbstractCard c = info.getAbstractCard(c0);
                    return c == null ? c0 : c;
                });
            });
            return __result;
        }
    }
}
