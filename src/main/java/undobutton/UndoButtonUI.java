package undobutton;

import basemod.ClickableUIElement;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.input.InputAction;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.TutorialStrings;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.ui.buttons.PeekButton;
import undobutton.util.TextureLoader;

import static undobutton.UndoButtonMod.controller;
import static undobutton.UndoButtonMod.imagePath;

public class UndoButtonUI {
    private static final float ENERGY_X = 198F, ENERGY_Y = 190F, ENERGY_Y_OFFSET = 128F, HORIZONTAL_SPACING = 10F, TOOLTIP_OFFSET = 120F;
    private Texture undoButtonTexture, undoButtonDisabledTexture, redoButtonTexture, redoButtonDisabledTexture;
    private UndoButton undoButton;
    private RedoButton redoButton;
    public static boolean isVisibleBelowScreen = false;
    public static boolean isVisibleAboveScreen = false;

    public void initialize() {
        undoButtonTexture = TextureLoader.getTexture(imagePath("buttons/undoButton.png"));
        undoButtonDisabledTexture = TextureLoader.getTexture(imagePath("buttons/undoButtonDisabled.png"));
        redoButtonTexture = TextureLoader.getTexture(imagePath("buttons/redoButton.png"));
        redoButtonDisabledTexture = TextureLoader.getTexture(imagePath("buttons/redoButtonDisabled.png"));
        undoButton = new UndoButton();
        redoButton = new RedoButton();
    }

    public void onStartBattle(AbstractRoom room) {
        undoButton.onStartBattle(room);
        redoButton.onStartBattle(room);
    }

    public void render(SpriteBatch sb) {
        if (undoButton != null) {
            undoButton.render(sb);
        }
        if (redoButton != null) {
            redoButton.render(sb);
        }
    }

    public void update() {
        isVisibleBelowScreen = false;
        isVisibleAboveScreen = false;
        if (AbstractDungeon.overlayMenu != null && AbstractDungeon.getCurrMapNode() != null && AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && AbstractDungeon.overlayMenu.combatPanelsShown) {
            if (AbstractDungeon.isScreenUp) {
                switch (AbstractDungeon.screen) {
                    case HAND_SELECT:
                    case GRID:
                    case CARD_REWARD:
                        isVisibleAboveScreen = true;
                        break;
                }
            } else {
                isVisibleBelowScreen = true;
            }
        }
        if (undoButton != null) {
            undoButton.update();
        }
        if (redoButton != null) {
            redoButton.update();
        }
    }

    abstract static class UndoOrRedoButton extends ClickableUIElement {
        protected float width, height;
        protected TutorialStrings tutorialStrings;
        protected float alpha, targetAlpha;
        public static final Color
                BASE_TINT = new Color(1.0F, 1.0F, 1.0F, 0.0F),
                FAILED_TINT = new Color(1.0F, 0.0F, 0.0F, 0.25F);
        protected float idleAlpha;
        protected Texture enabledTexture, disabledTexture;
        protected boolean isSpireAndShieldFight = false;

        public UndoOrRedoButton(Texture enabledTexture, Texture disabledTexture, float x, float y) {
            super(enabledTexture, x, y, enabledTexture.getWidth(), enabledTexture.getHeight());
            width = image.getWidth() * Settings.scale;
            height = image.getHeight() * Settings.scale;
            this.enabledTexture = enabledTexture;
            this.disabledTexture = disabledTexture;
            setX(this.x - width / 2);
            setY(this.y - height / 2);
            setClickable(false);
        }

        public boolean isVisible() {
            return isVisibleBelowScreen || isVisibleAboveScreen;
        }

        public void onStartBattle(AbstractRoom room) {
            idleAlpha = 1.0F;
            targetAlpha = 1.0F;
            alpha = 1.0F;
            isSpireAndShieldFight = room.monsters.getMonsterNames().contains("SpireShield");
        }

        @Override
        public void render(SpriteBatch sb) {
            if (isVisible()) {
                if (hitbox.hovered) {
                    String body = null;
                    if (wasLastActionFailed()) {
                        body = getLastActionString();
                    } else if (isClickable()) {
                        body = String.format("%s (%s).", tutorialStrings.TEXT[0], getLastActionString());
                    }
                    if (body != null) {
                        TipHelper.renderGenericTip(
                                x,
                                y + height + TOOLTIP_OFFSET,
                                String.format("%s (%s)", tutorialStrings.LABEL[0], getInputAction().getKeyString()),
                                body);
                    }
                }
                super.render(sb, new Color(1.0F, 1.0F, 1.0F, alpha));
            }
        }

        public void update() {
            if (isVisibleBelowScreen) {
                image = AbstractDungeon.overlayMenu.endTurnButton.enabled ? enabledTexture : disabledTexture;
            }
            if (isVisibleAboveScreen) {
                image = enabledTexture;
            }
            if (isVisible()) {
                setClickable(isClickable() && controller.isSafeToUndo() && !AbstractDungeon.player.isDraggingCard && !AbstractDungeon.player.inSingleTargetMode && !AbstractDungeon.topPanel.potionUi.targetMode);
            } else {
                setClickable(false);
            }
            super.update();
            if (isVisible()) {
                if (isClickable() && hitbox.hovered) {
                    targetAlpha = 1.0F;
                } else if (isVisibleAboveScreen) {
                    targetAlpha = PeekButton.isPeeking ? idleAlpha : 1.0F;
                } else {
                    targetAlpha = idleAlpha;
                }
                alpha = MathUtils.lerp(alpha, targetAlpha, Gdx.graphics.getDeltaTime() * 9.0F);
            }
        }

        public void onHover() {
            if (isVisible() && isClickable()) {
                tint = getBaseTint();
                if (!InputHelper.isMouseDown) {
                    tint = tint.lerp(Color.WHITE, 0.25F);
                }
                if (hitbox.justHovered) {
                    CardCrawlGame.sound.play("UI_HOVER");
                }
            }
        }

        public void onUnhover() {
            tint = getBaseTint();
        }

        protected void onClick() {
            CardCrawlGame.sound.play("UI_CLICK_1");
            InputHelper.justClickedLeft = false;
        }

        protected Color getBaseTint() {
            if (!isClickable()) {
                return BASE_TINT.cpy();
            }
            if (wasLastActionFailed()) {
                return FAILED_TINT.cpy();
            }
            return BASE_TINT.cpy();
        }

        protected abstract InputAction getInputAction();

        protected abstract String getLastActionString();

        protected abstract boolean wasLastActionFailed();
    }

    class UndoButton extends UndoOrRedoButton {

        public UndoButton() {
            super(undoButtonTexture, undoButtonDisabledTexture, ENERGY_X - (undoButtonTexture.getWidth() + HORIZONTAL_SPACING) / 2, ENERGY_Y + ENERGY_Y_OFFSET);
            tutorialStrings = CardCrawlGame.languagePack.getTutorialString(UndoButtonMod.makeID("Undo Tip"));
        }

        @Override
        public void update() {
            setClickable(UndoButtonMod.controller.canUndo());
            super.update();
            if (!UndoButtonMod.controller.canUndo() || wasLastActionFailed()) {
                image = disabledTexture;
            }
        }

        protected void onClick() {
            super.onClick();
            UndoButtonMod.controller.undo();
        }

        @Override
        protected InputAction getInputAction() {
            return UndoButtonMod.undoInputAction;
        }

        @Override
        protected String getLastActionString() {
            return UndoButtonMod.controller.getUndoActionString();
        }

        @Override
        protected boolean wasLastActionFailed() {
            return UndoButtonMod.controller.isUndoStateFailed();
        }
    }

    class RedoButton extends UndoOrRedoButton {
        public RedoButton() {
            super(redoButtonTexture, redoButtonDisabledTexture, ENERGY_X + (redoButtonTexture.getWidth() + HORIZONTAL_SPACING) / 2, ENERGY_Y + ENERGY_Y_OFFSET);
            tutorialStrings = CardCrawlGame.languagePack.getTutorialString(UndoButtonMod.makeID("Redo Tip"));
        }

        @Override
        public void onStartBattle(AbstractRoom room) {
            super.onStartBattle(room);
            if (isSpireAndShieldFight) {
                idleAlpha = 0.6F;
                targetAlpha = idleAlpha;
                alpha = idleAlpha;
            }
        }

        @Override
        public void update() {
            setClickable(UndoButtonMod.controller.canRedo());
            super.update();
            if (!UndoButtonMod.controller.canRedo() || wasLastActionFailed()) {
                image = disabledTexture;
            }
        }

        protected void onClick() {
            super.onClick();
            UndoButtonMod.controller.redo();
        }

        @Override
        protected InputAction getInputAction() {
            return UndoButtonMod.redoInputAction;
        }

        @Override
        protected String getLastActionString() {
            return UndoButtonMod.controller.getRedoActionString();
        }

        @Override
        protected boolean wasLastActionFailed() {
            return UndoButtonMod.controller.isRedoStateFailed();
        }
    }
}
