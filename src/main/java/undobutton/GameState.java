package undobutton;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.powers.BackAttackPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.BottledFlame;
import com.megacrit.cardcrawl.relics.BottledLightning;
import com.megacrit.cardcrawl.relics.BottledTornado;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import savestate.CreatureState;
import savestate.SaveState;
import savestate.powers.powerstates.monsters.BackAttackPowerState;

import java.util.Arrays;
import java.util.function.Consumer;

public class GameState {
    public final SaveState saveState;
    public Action lastAction;

    public GameState(Action action) {
        saveState = new SaveState();
        lastAction = action;
        // Turn is not ending in GameState
        ReflectionHacks.setPrivate(saveState, SaveState.class, "endTurnQueued", false);
        ReflectionHacks.setPrivate(saveState, SaveState.class, "isEndingTurn", false);
        // Handle surrounding
        if (AbstractDungeon.getMonsters().getMonsterNames().contains("SpireShield")) {
            if (AbstractDungeon.getMonsters().getMonster("SpireSpear").isDying) {
                UndoButtonMod.controller.isPlayerFlippedHorizontally = true;
            }
            if (AbstractDungeon.getMonsters().getMonster("SpireShield").isDying) {
                UndoButtonMod.controller.isPlayerFlippedHorizontally = false;
            }
            ReflectionHacks.setPrivate(saveState.playerState, CreatureState.class, "flipHorizontal", UndoButtonMod.controller.isPlayerFlippedHorizontally);
            Consumer<String> removeBackAttack = id -> saveState.curMapNodeState.monsterData.stream().filter(m -> m.id.equals(id)).forEach(m -> {
                m.powers.removeIf(p -> p.powerId.equals("BackAttack"));
            });
            Consumer<String> addBackAttack = id -> saveState.curMapNodeState.monsterData.stream().filter(m -> m.id.equals(id)).forEach(m -> {
                if (m.powers.stream().noneMatch(p -> p.powerId.equals("BackAttack"))) {
                    m.powers.add(0, new BackAttackPowerState(new BackAttackPower(AbstractDungeon.player)));
                }
            });
            if (UndoButtonMod.controller.isPlayerFlippedHorizontally) {
                removeBackAttack.accept("SpireShield");
                addBackAttack.accept("SpireSpear");
            } else {
                removeBackAttack.accept("SpireSpear");
                addBackAttack.accept("SpireShield");
            }
        }
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
        // Set isPlayerFlippedHorizontally
        UndoButtonMod.controller.isPlayerFlippedHorizontally = ReflectionHacks.getPrivate(saveState.playerState, CreatureState.class, "flipHorizontal");
        // Fix bottle relics
        fixBottleRelic();
        // Empty card queue
        AbstractDungeon.actionManager.cardQueue.clear();
        // Reset glowing of end turn button
        AbstractDungeon.overlayMenu.endTurnButton.isGlowing = !AbstractDungeon.player.hand.canUseAnyCard();
        // Avoid monster animations
        for (AbstractMonster m : AbstractDungeon.getCurrRoom().monsters.monsters) {
            // No health bar pulse
            m.hbAlpha = 1.0F;
            ReflectionHacks.setPrivate(m, AbstractCreature.class, "hbYOffset", 0.0F);
            ReflectionHacks.setPrivate(m, AbstractCreature.class, "hbShowTimer", 0.0F);
            if (m.isDead || m.halfDead) {
                for (String field : Arrays.asList("hbShadowColor", "hbBgColor", "hbTextColor", "blockOutlineColor")) {
                    ((Color) ReflectionHacks.getPrivate(m, AbstractCreature.class, field)).a = 0.0F;
                }
                ReflectionHacks.setPrivate(m, AbstractCreature.class, "healthBarAnimTimer", 0.0F);
            }
            // No intent pulse
            m.intentAlpha = m.intentAlphaTarget;
            // Faster intent animation
            ReflectionHacks.setPrivate(m, AbstractMonster.class, "intentParticleTimer", 0.0F);
            // No block pulse
            ReflectionHacks.setPrivate(m, AbstractCreature.class, "blockAnimTimer", 0.0F);
            ReflectionHacks.setPrivate(m, AbstractCreature.class, "blockOffset", 0.0F);
            ReflectionHacks.setPrivate(m, AbstractCreature.class, "blockScale", 1.0F);
            ((Color) ReflectionHacks.getPrivate(m, AbstractCreature.class, "blockColor")).a = 1.0F;
            ReflectionHacks.setPrivate(m, AbstractCreature.class, "blockTextColor", new Color(0.9F, 0.9F, 0.9F, 1.0F));
        }
        // Avoid hand animations
        for (AbstractCard c : AbstractDungeon.player.hand.group) {
            c.angle = c.targetAngle;
            c.current_x = c.target_x;
            c.current_y = c.target_y;
            c.drawScale = c.targetDrawScale;
        }
    }

    public void fixBottleRelic() {
        for (AbstractRelic r : AbstractDungeon.player.relics) {
            if (r.relicId.equals("Bottled Flame")) {
                for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
                    if (c.inBottleFlame) {
                        ((BottledFlame) r).card = c;
                        ((BottledFlame) r).setDescriptionAfterLoading();
                    }
                }
            } else if (r.relicId.equals("Bottled Lightning")) {
                for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
                    if (c.inBottleLightning) {
                        ((BottledLightning) r).card = c;
                        ((BottledLightning) r).setDescriptionAfterLoading();
                    }
                }
            } else if (r.relicId.equals("Bottled Tornado")) {
                for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
                    if (c.inBottleTornado) {
                        ((BottledTornado) r).card = c;
                        ((BottledTornado) r).setDescriptionAfterLoading();
                    }
                }
            }
        }
    }

    public enum ActionType {
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
                        card = ((AbstractCard) data).makeStatEquivalentCopy();
                        potion = null;
                    } else {
                        throw new IllegalArgumentException("Expected AbstractCard, got " + data.getClass().getName());
                    }
                    break;
                case POTION_USED:
                    if (data instanceof AbstractPotion) {
                        potion = ((AbstractPotion) data).makeCopy();
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
