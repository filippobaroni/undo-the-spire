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
        hand = StateCloner.clone(player.hand);
        drawPile = StateCloner.clone(player.drawPile);
        discardPile = StateCloner.clone(player.discardPile);
        exhaustPile = StateCloner.clone(player.exhaustPile);
        limbo = StateCloner.clone(player.limbo);
        masterDeck = StateCloner.clone(player.masterDeck);
        StateCloner.cloner.cloneClassFieldsTo(AbstractCreature.class, player, this);
    }

    public void load(AbstractPlayer player) {
        player.hand = hand;
        player.drawPile = drawPile;
        player.discardPile = discardPile;
        player.exhaustPile = exhaustPile;
        player.limbo = limbo;
        player.masterDeck = masterDeck;
        StateCloner.cloner.moveClassFieldsTo(AbstractCreature.class, this, player);

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
