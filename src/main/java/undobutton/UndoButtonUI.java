package undobutton;

import basemod.ClickableUIElement;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.input.InputAction;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.TutorialStrings;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import undobutton.util.TextureLoader;

import static undobutton.UndoButtonMod.imagePath;

public class UndoButtonUI {
    private static float ENERGY_X = 198F, ENERGY_Y = 190F, ENERGY_Y_OFFSET = 128F, HORIZONTAL_SPACING = 10F, TOOLTIP_OFFSET = 120F;
    private Texture undoButtonTexture, redoButtonTexture;
    private UndoButton undoButton;
    private RedoButton redoButton;

    public void initialize() {
        undoButtonTexture = TextureLoader.getTexture(imagePath("buttons/undoButton.png"));
        redoButtonTexture = TextureLoader.getTexture(imagePath("buttons/redoButton.png"));
        undoButton = new UndoButton();
        redoButton = new RedoButton();
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

        public UndoOrRedoButton(Texture texture, float x, float y) {
            super(texture, x, y, texture.getWidth(), texture.getHeight());
            width = texture.getWidth()* Settings.scale;
            height = texture.getHeight() * Settings.scale;
            setX(this.x - width / 2);
            setY(this.y - height / 2);
            hide();
        }

        @Override
        public void render(SpriteBatch sb) {
            if (!isHidden) {
                if (hitbox.hovered) {
                    TipHelper.renderGenericTip(x,
                            y + height + TOOLTIP_OFFSET,
                            String.format("%s (%s)", tutorialStrings.LABEL[0], getInputAction().getKeyString()),
                            String.format("%s (%s).", tutorialStrings.TEXT[0], getLastActionString()));
                }
                super.render(sb);
            }
        }

        public void update() {
            if (AbstractDungeon.overlayMenu == null || AbstractDungeon.getCurrMapNode() == null || AbstractDungeon.getCurrRoom() == null || AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMBAT || !AbstractDungeon.overlayMenu.combatPanelsShown || AbstractDungeon.isScreenUp) {
                hide();
            }
            if (!isHidden && isClickable()) {
                setClickable(UndoButtonMod.controller.isSafeToUndo());
            }
            super.update();
        }

        public void show() {
            isHidden = false;
        }

        public void hide() {
            isHidden = true;
            setClickable(false);
        }

        public void onHover() {
            if (!isHidden && isClickable()) {
                if (InputHelper.isMouseDown) {
                    tint.a = 0.0F;
                } else {
                    tint.a = 0.25F;
                }
            }
        }

        public void onUnhover() {
            if (!isHidden && isClickable()) {
                tint.a = 0.0F;
            }
        }

        protected abstract InputAction getInputAction();
        protected abstract String getLastActionString();
    }

    class UndoButton extends UndoOrRedoButton {

        public UndoButton() {
            super(undoButtonTexture, ENERGY_X - (undoButtonTexture.getWidth() + HORIZONTAL_SPACING) / 2, ENERGY_Y + ENERGY_Y_OFFSET);
            tutorialStrings = CardCrawlGame.languagePack.getTutorialString(UndoButtonMod.makeID("Undo Tip"));
        }

        @Override
        public void update() {
            if (UndoButtonMod.controller.canUndo()) {
                show();
                setClickable(true);
            } else {
                hide();
            }
            super.update();
        }

        protected void onClick() {
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
            super(redoButtonTexture, ENERGY_X + (redoButtonTexture.getWidth() + HORIZONTAL_SPACING) / 2, ENERGY_Y + ENERGY_Y_OFFSET);
            tutorialStrings = CardCrawlGame.languagePack.getTutorialString(UndoButtonMod.makeID("Redo Tip"));
        }

        @Override
        public void update() {
            if (UndoButtonMod.controller.canRedo()) {
                show();
                setClickable(true);
            } else {
                hide();
            }
            super.update();
        }

        protected void onClick() {
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
