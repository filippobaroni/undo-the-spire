package undobutton;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.OverlayMenu;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.powers.BackAttackPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.BottledFlame;
import com.megacrit.cardcrawl.relics.BottledLightning;
import com.megacrit.cardcrawl.relics.BottledTornado;
import com.megacrit.cardcrawl.ui.buttons.DynamicBanner;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import com.megacrit.cardcrawl.vfx.TintEffect;
import savestate.*;
import savestate.powers.powerstates.monsters.BackAttackPowerState;
import savestate.selectscreen.CardRewardScreenState;
import savestate.selectscreen.GridCardSelectScreenState;
import savestate.selectscreen.HandSelectScreenState;
import undobutton.patches.AbstractCardPatches;
import undobutton.patches.CardStatePatches;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GameState {
    public static Class<?>[] extraStateTypes;
    public static Class<?>[] extraStateHandlers;
    private final static ArrayList<Consumer<PostLoadInfo>> postLoadRunners = new ArrayList<>();
    public final SaveState saveState;
    public final Object[] extraState;
    public Action lastAction;

    public static void addPostLoadRunner(Consumer<PostLoadInfo> runner) {
        postLoadRunners.add(runner);
    }

    public GameState(Action action) {
        lastAction = action;
        if (action.type == ActionType.FAILED) {
            saveState = null;
            extraState = null;
            return;
        }
        saveState = new SaveState();
        // Save extra state from @MakeUndoable classes
        extraState = new Object[extraStateHandlers.length];
        for (int i = 0; i < extraStateHandlers.length; i++) {
            try {
                extraState[i] = extraStateHandlers[i].getMethod("save").invoke(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // Turn is not ending in GameState
        ReflectionHacks.setPrivate(saveState, SaveState.class, "endTurnQueued", false);
        ReflectionHacks.setPrivate(saveState, SaveState.class, "isEndingTurn", false);
        // Clear card queues
        BiConsumer<Class<?>, String> clearQueue = (type, field) -> {
            Object screen = ReflectionHacks.getPrivate(saveState, SaveState.class, field);
            if (screen != null) {
                ArrayList<CardQueueItemState> queue = ReflectionHacks.getPrivate(screen, type, "cardQueueState");
                if (UndoButtonMod.DEBUG) {
                    UndoButtonMod.logger.info(
                            "Removing {} from card queue, but keeping {}.",
                            queue.stream().filter(c -> !c.autoplayCard && !c.isEndTurnAutoPlay).map(c -> CardStatePatches.ExtraFields.name.get(c.card)).toArray(),
                            queue.stream().filter(c -> c.autoplayCard || c.isEndTurnAutoPlay).map(c -> CardStatePatches.ExtraFields.name.get(c.card)).toArray()
                    );
                }
                queue.removeIf(c -> !c.autoplayCard && !c.isEndTurnAutoPlay);
            }
        };
        clearQueue.accept(CardRewardScreenState.class, "cardRewardScreenState");
        clearQueue.accept(GridCardSelectScreenState.class, "gridCardSelectScreenState");
        clearQueue.accept(HandSelectScreenState.class, "handSelectScreenState");
        // Handle surrounding (Shield and Spear fight)
        if (AbstractDungeon.getMonsters().getMonsterNames().contains("SpireShield")) {
            if (AbstractDungeon.getMonsters().getMonster("SpireSpear").isDying) {
                UndoButtonMod.controller.isPlayerFlippedHorizontally = true;
            }
            if (AbstractDungeon.getMonsters().getMonster("SpireShield").isDying) {
                UndoButtonMod.controller.isPlayerFlippedHorizontally = false;
            }
            ReflectionHacks.setPrivate(saveState.playerState, CreatureState.class, "flipHorizontal", UndoButtonMod.controller.isPlayerFlippedHorizontally);
            Consumer<String> removeBackAttack = id -> saveState.curMapNodeState.monsterData.stream().filter(m -> m.id.equals(id)).forEach(m -> m.powers.removeIf(p -> p.powerId.equals("BackAttack")));
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
        // Handle hand selection screen
        switch ((AbstractDungeon.CurrentScreen) ReflectionHacks.getPrivate(saveState, SaveState.class, "screen")) {
            case HAND_SELECT:
                HandSelectScreenState handScreen = ReflectionHacks.getPrivate(saveState, SaveState.class, "handSelectScreenState");
                CardState[] selectedCards = ReflectionHacks.getPrivate(handScreen, HandSelectScreenState.class, "selectedCards");
                CardState[] hand = saveState.playerState.hand;
                CardState[] newHand = new CardState[hand.length + selectedCards.length];
                System.arraycopy(hand, 0, newHand, 0, hand.length);
                System.arraycopy(selectedCards, 0, newHand, hand.length, selectedCards.length);
                ReflectionHacks.setPrivateFinal(saveState.playerState, PlayerState.class, "hand", newHand);
                ReflectionHacks.setPrivate(handScreen, HandSelectScreenState.class, "selectedCards", new CardState[0]);
                ReflectionHacks.setPrivate(handScreen, HandSelectScreenState.class, "numSelected", 0);
                ReflectionHacks.setPrivate(handScreen, HandSelectScreenState.class, "isDisabled", ReflectionHacks.getPrivate(handScreen, HandSelectScreenState.class, "canPickZero"));
                break;
            case GRID:
                GridCardSelectScreenState gridScreen = ReflectionHacks.getPrivate(saveState, SaveState.class, "gridCardSelectScreenState");
                ReflectionHacks.setPrivate(gridScreen, GridCardSelectScreenState.class, "selectedCards", new ArrayList<>());
                ReflectionHacks.setPrivate(gridScreen, GridCardSelectScreenState.class, "cardSelectAmount", 0);
                ReflectionHacks.setPrivate(gridScreen, GridCardSelectScreenState.class, "isConfirmButtonDisabled", !((boolean) ReflectionHacks.getPrivate(gridScreen, GridCardSelectScreenState.class, "anyNumber")));
                ReflectionHacks.setPrivate(saveState, SaveState.class, "gridSelectedCards", new ArrayList<>());
                break;
            case CARD_REWARD:
                CardRewardScreenState rewardScreen = ReflectionHacks.getPrivate(saveState, SaveState.class, "cardRewardScreenState");
                ReflectionHacks.setPrivate(rewardScreen, CardRewardScreenState.class, "discoveryCard", null);
                ReflectionHacks.setPrivate(rewardScreen, CardRewardScreenState.class, "touchCard", null);
                break;
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
        // Load extra state from @MakeUndoable classes
        for (int i = 0; i < extraStateHandlers.length; i++) {
            try {
                extraStateHandlers[i].getMethod("load", extraStateTypes[i]).invoke(null, extraStateTypes[i].cast(extraState[i]));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // Run post-load runners
        PostLoadInfo info = new PostLoadInfo();
        postLoadRunners.forEach(r -> r.accept(info));
        postLoadRunners.clear();
        // Fix screens
        fixScreens();
        // Set isPlayerFlippedHorizontally
        UndoButtonMod.controller.isPlayerFlippedHorizontally = ReflectionHacks.getPrivate(saveState.playerState, CreatureState.class, "flipHorizontal");
        // Fix bottle relics
        fixBottleRelic();
        // Reset glowing of end turn button
        AbstractDungeon.overlayMenu.endTurnButton.isGlowing = !AbstractDungeon.player.hand.canUseAnyCard();
        // Reset glowing of cards in hand
        AbstractDungeon.player.hand.glowCheck();
        // Avoid monster animations
        for (AbstractMonster m : AbstractDungeon.getCurrRoom().monsters.monsters) {
            // No health bar pulse
            m.hbAlpha = 1.0F;
            ReflectionHacks.setPrivate(m, AbstractCreature.class, "hbYOffset", 0.0F);
            ReflectionHacks.setPrivate(m, AbstractCreature.class, "hbShowTimer", 0.0F);
            ReflectionHacks.setPrivate(m, AbstractCreature.class, "healthHideTimer", 1.0F);
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
        AbstractDungeon.player.hand.refreshHandLayout();
        AbstractDungeon.player.hand.group.forEach(c -> {
            c.angle = c.targetAngle;
            c.current_x = c.target_x;
            c.current_y = c.target_y;
            c.drawScale = c.targetDrawScale;
        });
        // Avoid orbs animations
        AbstractDungeon.player.orbs.forEach(o -> {
            o.cX = o.tX;
            o.cY = o.tY;
            ReflectionHacks.setPrivate(o, AbstractOrb.class, "channelAnimTimer", 0.0F);
        });
    }

    public void fixBottleRelic() {
        for (AbstractRelic r : AbstractDungeon.player.relics) {
            switch (r.relicId) {
                case "Bottled Flame":
                    for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
                        if (c.inBottleFlame) {
                            ((BottledFlame) r).card = c;
                            ((BottledFlame) r).setDescriptionAfterLoading();
                        }
                    }
                    break;
                case "Bottled Lightning":
                    for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
                        if (c.inBottleLightning) {
                            ((BottledLightning) r).card = c;
                            ((BottledLightning) r).setDescriptionAfterLoading();
                        }
                    }
                    break;
                case "Bottled Tornado":
                    for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
                        if (c.inBottleTornado) {
                            ((BottledTornado) r).card = c;
                            ((BottledTornado) r).setDescriptionAfterLoading();
                        }
                    }
                    break;
            }
        }
    }

    public void fixScreens() {
        // No black screen animation
        ((Color) ReflectionHacks.getPrivate(AbstractDungeon.overlayMenu, OverlayMenu.class, "blackScreenColor")).a = ReflectionHacks.getPrivate(AbstractDungeon.overlayMenu, OverlayMenu.class, "blackScreenTarget");
        // No banner animation
        if (AbstractDungeon.dynamicBanner.show) {
            DynamicBanner banner = AbstractDungeon.dynamicBanner;
            banner.appearInstantly(ReflectionHacks.getPrivate(banner, DynamicBanner.class, "label"));
            Consumer<TintEffect> setToTarget = t -> t.color = ((Color) ReflectionHacks.getPrivate(t, TintEffect.class, "targetColor")).cpy();
            setToTarget.accept(ReflectionHacks.getPrivate(banner, DynamicBanner.class, "tint"));
            setToTarget.accept(ReflectionHacks.getPrivate(banner, DynamicBanner.class, "textTint"));
        }
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
                case POTION_DISCARDED:
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

    static public class PostLoadInfo {
        public HashMap<UUID, AbstractCard> allCards;

        public PostLoadInfo() {
            allCards = new HashMap<>();
            Consumer<CardGroup> addCards = cards -> cards.group.forEach(c -> allCards.put(AbstractCardPatches.ExtraFields.trulyUniqueUuid.get(c), c));
            addCards.accept(AbstractDungeon.player.drawPile);
            addCards.accept(AbstractDungeon.player.discardPile);
            addCards.accept(AbstractDungeon.player.exhaustPile);
            addCards.accept(AbstractDungeon.player.hand);
            addCards.accept(AbstractDungeon.player.limbo);
            addCards.accept(AbstractDungeon.player.masterDeck);
        }

        public AbstractCard getAbstractCard(AbstractCard card) {
            return allCards.get(AbstractCardPatches.ExtraFields.trulyUniqueUuid.get(card));
        }
    }
}
