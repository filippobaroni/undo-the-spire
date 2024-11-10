package undobutton;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class PlayerState extends AbstractCreature {
    CardGroup hand;
    CardGroup drawPile;
    CardGroup discardPile;
    CardGroup exhaustPile;
    CardGroup limbo;
    CardGroup masterDeck;

    public PlayerState(AbstractPlayer player) {
        if (GameState.currentlyCreating == null) {
            throw new IllegalStateException("PlayerState can only be created during GameState creation");
        }
        hand = StateCloner.deepClone(player.hand);
        drawPile = StateCloner.deepClone(player.drawPile);
        discardPile = StateCloner.deepClone(player.discardPile);
        exhaustPile = StateCloner.deepClone(player.exhaustPile);
        limbo = StateCloner.deepClone(player.limbo);
        masterDeck = StateCloner.deepClone(player.masterDeck);
        StateCloner.cloneAllFieldsTo(AbstractCreature.class, player, this, GameState.currentlyCreating.objects);
    }

    public void load(AbstractPlayer player) {
        player.hand = hand;
        player.drawPile = drawPile;
        player.discardPile = discardPile;
        player.exhaustPile = exhaustPile;
        player.limbo = limbo;
        player.masterDeck = masterDeck;
        StateCloner.moveAllFieldsTo(AbstractCreature.class, this, player);

        // Refresh hand layout
        player.hand.refreshHandLayout();
        player.hand.group.forEach(c -> {
            c.current_x = c.target_x;
            c.current_y = c.target_y;
        });
    }

    @Override
    public void damage(DamageInfo damageInfo) {

    }

    @Override
    public void render(SpriteBatch spriteBatch) {

    }
}
