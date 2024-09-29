package undobutton;

import basemod.ClickableUIElement;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import undobutton.util.TextureLoader;

import static undobutton.UndoButtonMod.imagePath;

public class UndoButtonUI {
    private static final float ENERGY_X = 198F, ENERGY_Y = 190F, ENERGY_Y_OFFSET = 100F, HORIZONTAL_SPACING = 20F;
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

    abstract class UndoOrRedoButton extends ClickableUIElement {
        protected boolean isHidden;

        public UndoOrRedoButton(Texture texture, float x, float y) {
            super(texture, x, y, texture.getWidth(), texture.getHeight());
            hide();
        }

        public void update() {
            if (AbstractDungeon.overlayMenu == null || AbstractDungeon.getCurrMapNode() == null || AbstractDungeon.getCurrRoom() == null || AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMBAT || !AbstractDungeon.overlayMenu.combatPanelsShown || AbstractDungeon.isScreenUp) {
                hide();
            }
            if (!isHidden && isClickable()) {
                setClickable(AbstractDungeon.actionManager.phase == GameActionManager.Phase.WAITING_ON_USER);
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
    }

    class UndoButton extends UndoOrRedoButton {
        public UndoButton() {
            super(undoButtonTexture, ENERGY_X - (undoButtonTexture.getWidth() + HORIZONTAL_SPACING) / 2, ENERGY_Y + ENERGY_Y_OFFSET);
            setX(x - undoButtonTexture.getWidth() * Settings.scale / 2);
            setY(y - undoButtonTexture.getHeight() * Settings.scale / 2);
        }

        @Override
        public void render(SpriteBatch sb) {
            if (!isHidden) {
                super.render(sb);
            }
        }

        @Override
        public void update() {
            if(UndoButtonMod.controller.canUndo()) {
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
    }

    class RedoButton extends UndoOrRedoButton {
        public RedoButton() {
            super(redoButtonTexture, ENERGY_X + (redoButtonTexture.getWidth() + HORIZONTAL_SPACING) / 2, ENERGY_Y + ENERGY_Y_OFFSET);
            setX(x - redoButtonTexture.getWidth() * Settings.scale / 2);
            setY(y - redoButtonTexture.getHeight() * Settings.scale / 2);
        }

        @Override
        public void render(SpriteBatch sb) {
            if (!isHidden) {
                super.render(sb);
            }
        }

        @Override
        public void update() {
            if(UndoButtonMod.controller.canRedo()) {
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
    }
}
