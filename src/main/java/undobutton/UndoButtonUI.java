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
import undobutton.util.TextureLoader;

import static undobutton.UndoButtonMod.controller;
import static undobutton.UndoButtonMod.imagePath;

public class UndoButtonUI {
    private static final float ENERGY_X = 198F, ENERGY_Y = 190F, ENERGY_Y_OFFSET = 128F, HORIZONTAL_SPACING = 10F, TOOLTIP_OFFSET = 120F;
    private Texture undoButtonTexture, undoButtonDisabledTexture, redoButtonTexture, redoButtonDisabledTexture;
    private UndoButton undoButton;
    private RedoButton redoButton;

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
        if (undoButton != null) {
            undoButton.update();
        }
        if (redoButton != null) {
            redoButton.update();
        }
    }

    abstract static class UndoOrRedoButton extends ClickableUIElement {
        protected boolean isHidden;
        protected float width, height;
        protected TutorialStrings tutorialStrings;
        protected boolean isClicked;
        protected float alpha, targetAlpha;
        protected float idleAlpha;
        protected Texture enabledTexture, disabledTexture;

        public UndoOrRedoButton(Texture enabledTexture, Texture disabledTexture, float x, float y) {
            super(enabledTexture, x, y, enabledTexture.getWidth(), enabledTexture.getHeight());
            width = image.getWidth() * Settings.scale;
            height = image.getHeight() * Settings.scale;
            this.enabledTexture = enabledTexture;
            this.disabledTexture = disabledTexture;
            isClicked = false;
            setX(this.x - width / 2);
            setY(this.y - height / 2);
            hide();
        }

        public void onStartBattle(AbstractRoom room) {
            idleAlpha = 1.0F;
            targetAlpha = 1.0F;
            alpha = 1.0F;
        }

        @Override
        public void render(SpriteBatch sb) {
            if (!isHidden) {
                if (isClickable() && hitbox.hovered) {
                    TipHelper.renderGenericTip(x,
                            y + height + TOOLTIP_OFFSET,
                            String.format("%s (%s)", tutorialStrings.LABEL[0], getInputAction().getKeyString()),
                            String.format("%s (%s).", tutorialStrings.TEXT[0], getLastActionString()));
                }
                super.render(sb, new Color(1.0F, 1.0F, 1.0F, alpha));
            }
        }

        public void update() {
            if (AbstractDungeon.overlayMenu == null || AbstractDungeon.getCurrMapNode() == null || AbstractDungeon.getCurrRoom() == null || AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMBAT || !AbstractDungeon.overlayMenu.combatPanelsShown) {
                hide();
            } else {
                show();
            }
            if (!isHidden && isClickable()) {
                setClickable(controller.isSafeToUndo() && !AbstractDungeon.isScreenUp && !AbstractDungeon.player.isDraggingCard && !AbstractDungeon.player.inSingleTargetMode && !AbstractDungeon.topPanel.potionUi.targetMode);
            }
            super.update();
            if (AbstractDungeon.overlayMenu != null && AbstractDungeon.overlayMenu.endTurnButton.enabled) {
                image = enabledTexture;
            } else {
                image = disabledTexture;
            }
            if (!isHidden) {
                if (isClickable() && hitbox.hovered) {
                    targetAlpha = 1.0F;
                } else {
                    targetAlpha = idleAlpha;
                }
                alpha = MathUtils.lerp(alpha, targetAlpha, Gdx.graphics.getDeltaTime() * 9.0F);
            }
        }

        public void show() {
            isHidden = false;
            tint.a = 0.0F;
        }

        public void hide() {
            isHidden = true;
            setClickable(false);
            isClicked = false;
        }

        public void onHover() {
            if (!isHidden && isClickable()) {
                if (InputHelper.isMouseDown) {
                    tint.a = 0.0F;
                    isClicked = true;
                } else {
                    tint.a = 0.25F;
                    isClicked = false;
                }
                if (hitbox.justHovered) {
                    CardCrawlGame.sound.play("UI_HOVER");
                }
            }
        }

        public void onUnhover() {
            if (!isHidden) {
                tint.a = 0.0F;
            }
            isClicked = false;
        }

        protected void onClick() {
            CardCrawlGame.sound.play("UI_CLICK_1");
        }

        protected abstract InputAction getInputAction();
        protected abstract String getLastActionString();
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
            if (!UndoButtonMod.controller.canUndo()) {
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
    }

    class RedoButton extends UndoOrRedoButton {
        public RedoButton() {
            super(redoButtonTexture, redoButtonDisabledTexture, ENERGY_X + (redoButtonTexture.getWidth() + HORIZONTAL_SPACING) / 2, ENERGY_Y + ENERGY_Y_OFFSET);
            tutorialStrings = CardCrawlGame.languagePack.getTutorialString(UndoButtonMod.makeID("Redo Tip"));
        }

        @Override
        public void onStartBattle(AbstractRoom room) {
            super.onStartBattle(room);
            if (room.monsters.getMonsterNames().contains("SpireShield")) {
                idleAlpha = 0.6F;
                targetAlpha = idleAlpha;
                alpha = idleAlpha;
            }
        }

        @Override
        public void update() {
            setClickable(UndoButtonMod.controller.canRedo());
            super.update();
            if (!UndoButtonMod.controller.canRedo()) {
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
    }
}
