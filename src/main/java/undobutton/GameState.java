package undobutton;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import savestate.SaveState;

public class GameState {
    private SaveState saveState;
    public Action lastAction;

    public GameState(Action action) {
        saveState = new SaveState();
        ReflectionHacks.setPrivate(saveState, SaveState.class, "endTurnQueued", false);
        ReflectionHacks.setPrivate(saveState, SaveState.class, "isEndingTurn", false);
        lastAction = action;
    }

    public void apply() {
        // Release selected/hovered card
        AbstractDungeon.player.releaseCard();
        ReflectionHacks.setPrivate(AbstractDungeon.player, AbstractPlayer.class, "hoveredMonster", null);
        // Release potion
        AbstractDungeon.topPanel.potionUi.close();
        AbstractDungeon.topPanel.potionUi.targetMode = false;
        ReflectionHacks.setPrivate(AbstractDungeon.topPanel.potionUi, PotionPopUp.class, "hoveredMonster", null);
        // Load saveState
        saveState.loadState();
    }

    public static enum ActionType {
        CARD_PLAYED,
        POTION_USED,
        TURN_ENDED
    }

    public static class Action {
        private final ActionType type;
        private final AbstractCard card;
        private final AbstractPotion potion;

        public Action(ActionType type) {
            this.type = type;
            switch (type) {
                case TURN_ENDED:
                    card = null;
                    potion = null;
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
                        card = (AbstractCard) data;
                        potion = null;
                    } else {
                        throw new IllegalArgumentException("Expected AbstractCard, got " + data.getClass().getName());
                    }
                    break;
                case POTION_USED:
                    if (data instanceof AbstractPotion) {
                        potion = (AbstractPotion) data;
                        card = null;
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
                case CARD_PLAYED:
                    return "play " + card.name;
                case POTION_USED:
                    return "use " + potion.name;
                case TURN_ENDED:
                    return "end turn";
                default:
                    return "UNKNOWN ACTION TYPE";
            }
        }
    }
}
