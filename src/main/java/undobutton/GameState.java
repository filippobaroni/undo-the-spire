package undobutton;

import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.util.ArrayList;

public class GameState {
    static GameState currentlyCreating;

    public Action lastAction;

    public PlayerState playerState;
    public AbstractRoom room;
    public GameActionManager actionManager;
    public RngState rngState;


    public int totalDiscardedThisTurn = 0;
    public int damageReceivedThisTurn = 0;
    public int damageReceivedThisCombat = 0;
    public int hpLossThisCombat = 0;
    public int playerHpLastTurn;
    public int energyGainedThisCombat;
    public int turn = 0;

    public ArrayList<AbstractCard> drawnCards;

    public GameState(Action action) {
        lastAction = action;
        if (action.type == ActionType.FAILED) {
            return;
        }
        StateCloner.cloner.initCloningBatch();

        rngState = new RngState();
        playerState = new PlayerState(AbstractDungeon.player);
        room = StateCloner.clone(AbstractDungeon.getCurrRoom());
        actionManager = StateCloner.clone(AbstractDungeon.actionManager);

        totalDiscardedThisTurn = GameActionManager.totalDiscardedThisTurn;
        damageReceivedThisTurn = GameActionManager.damageReceivedThisTurn;
        damageReceivedThisCombat = GameActionManager.damageReceivedThisCombat;
        hpLossThisCombat = GameActionManager.hpLossThisCombat;
        playerHpLastTurn = GameActionManager.playerHpLastTurn;
        energyGainedThisCombat = GameActionManager.energyGainedThisCombat;
        turn = GameActionManager.turn;

        StateCloner.cloner.endCloningBatch();

        // Clear card queue
        actionManager.cardQueue.removeIf(c -> !c.autoplayCard && !c.isEndTurnAutoPlay);
    }

    public void apply() {
        rngState.load();
        playerState.load(AbstractDungeon.player);
        AbstractDungeon.getCurrMapNode().room = room;
        AbstractDungeon.actionManager = actionManager;

        GameActionManager.totalDiscardedThisTurn = totalDiscardedThisTurn;
        GameActionManager.damageReceivedThisTurn = damageReceivedThisTurn;
        GameActionManager.damageReceivedThisCombat = damageReceivedThisCombat;
        GameActionManager.hpLossThisCombat = hpLossThisCombat;
        GameActionManager.playerHpLastTurn = playerHpLastTurn;
        GameActionManager.energyGainedThisCombat = energyGainedThisCombat;
        GameActionManager.turn = turn;
    }


    public enum ActionType {
        FAILED, CARD_PLAYED, POTION_USED, POTION_DISCARDED, CARD_SELECTED, TURN_ENDED
    }

    public static class Action {
        public final ActionType type;
        private final AbstractCard card;
        private final AbstractPotion potion;
        private final UIStrings uiStrings;

        public Action(ActionType type) {
            this.type = type;
            switch (type) {
                case FAILED:
                    card = null;
                    potion = null;
                    uiStrings = CardCrawlGame.languagePack.getUIString(UndoButtonMod.makeID("Failed Action"));
                    break;
                case TURN_ENDED:
                    card = null;
                    potion = null;
                    uiStrings = CardCrawlGame.languagePack.getUIString(UndoButtonMod.makeID("End Turn Action"));
                    break;
                case CARD_SELECTED:
                    card = null;
                    potion = null;
                    uiStrings = CardCrawlGame.languagePack.getUIString(UndoButtonMod.makeID("Select Cards Action"));
                    break;
                default:
                    throw new IllegalArgumentException("Wrong argument type for ActionType " + type);
            }
        }

        public Action(ActionType type, Object data) {
            this.type = type;
            switch (type) {
                case CARD_PLAYED:
                    if (data instanceof AbstractCard) {
                        card = ((AbstractCard) data).makeStatEquivalentCopy();
                        potion = null;
                        uiStrings = CardCrawlGame.languagePack.getUIString(UndoButtonMod.makeID("Card Action"));
                    } else {
                        throw new IllegalArgumentException("Expected AbstractCard, got " + data.getClass().getName());
                    }
                    break;
                case POTION_USED:
                    if (data instanceof AbstractPotion) {
                        potion = ((AbstractPotion) data).makeCopy();
                        card = null;
                        uiStrings = CardCrawlGame.languagePack.getUIString(UndoButtonMod.makeID("Potion Action"));
                    } else {
                        throw new IllegalArgumentException("Expected AbstractPotion, got " + data.getClass().getName());
                    }
                    break;
                case POTION_DISCARDED:
                    if (data instanceof AbstractPotion) {
                        potion = ((AbstractPotion) data).makeCopy();
                        card = null;
                        uiStrings = CardCrawlGame.languagePack.getUIString(UndoButtonMod.makeID("Potion Discard Action"));
                    } else {
                        throw new IllegalArgumentException("Expected AbstractPotion, got " + data.getClass().getName());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Wrong argument type for ActionType " + type);
            }
        }

        public String toString() {
            switch (type) {
                case FAILED:
                    return uiStrings.TEXT[0];
                case CARD_PLAYED:
                    return uiStrings.TEXT[0] + card.name + uiStrings.TEXT[1];
                case POTION_USED:
                    return uiStrings.TEXT[0] + potion.name + uiStrings.TEXT[1];
                case TURN_ENDED:
                    return uiStrings.TEXT[0];
                case CARD_SELECTED:
                    return uiStrings.TEXT[0];
                default:
                    return "UNKNOWN ACTION TYPE";
            }
        }
    }
}
