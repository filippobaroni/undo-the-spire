package undobutton;

import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.util.ArrayDeque;

public class UndoButtonController {
    private final ArrayDeque<GameState> pastGameStates = new ArrayDeque<>();
    private final ArrayDeque<GameState> futureGameStates = new ArrayDeque<>();
    public boolean isThisEndTurnForced = false;
    public boolean isPlayerFlippedHorizontally = false;


    public void onStartBattle(AbstractRoom room) {
        clearStates();
        isThisEndTurnForced = false;
        isPlayerFlippedHorizontally = false;
    }

    public boolean canUndo() {
        return !pastGameStates.isEmpty();
    }

    public boolean isSafeToUndo() {
        if (AbstractDungeon.getCurrMapNode() == null) {
            return false;
        }
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (room == null || room.phase != AbstractRoom.RoomPhase.COMBAT) {
            return false;
        }
        if (AbstractDungeon.isScreenUp) {
            switch (AbstractDungeon.screen) {
                case HAND_SELECT:
                case GRID:
                case CARD_REWARD:
                    return true;
                default:
                    return false;
            }
        } else {
            return AbstractDungeon.actionManager.isEmpty() && AbstractDungeon.actionManager.phase == GameActionManager.Phase.WAITING_ON_USER && AbstractDungeon.overlayMenu.endTurnButton.enabled;
        }
    }

    public boolean canRedo() {
        return !futureGameStates.isEmpty();
    }

    public void addState(GameState.Action action) {
        // Clear future states
        futureGameStates.clear();
        // Add new state
        pastGameStates.addFirst(new GameState(action));
        // Remove the oldest state if queue is too long
        if (pastGameStates.size() > UndoButtonMod.getMaxStates()) {
            pastGameStates.removeLast();
            UndoButtonMod.logger.info("More than {} states in queue, removing oldest state.", UndoButtonMod.getMaxStates());
        }
    }

    public void undo() {
        if (pastGameStates.isEmpty()) {
            UndoButtonMod.logger.info("Undo queue is empty.");
            return;
        }
        UndoButtonMod.logger.info("Undoing.");
        GameState state = pastGameStates.removeFirst();
        futureGameStates.addFirst(new GameState(state.lastAction));
        state.apply();
    }

    public void redo() {
        if (futureGameStates.isEmpty()) {
            UndoButtonMod.logger.info("Redo queue is empty.");
            return;
        }
        UndoButtonMod.logger.info("Redoing.");
        GameState state = futureGameStates.removeFirst();
        pastGameStates.addFirst(new GameState(state.lastAction));
        state.apply();
    }

    public void clearStates() {
        pastGameStates.clear();
        futureGameStates.clear();
        UndoButtonMod.logger.info("Cleared undo/redo queue.");
    }

    public String getUndoActionString() {
        if (pastGameStates.isEmpty()) {
            return "";
        }
        return pastGameStates.getFirst().lastAction.toString();
    }

    public String getRedoActionString() {
        if (futureGameStates.isEmpty()) {
            return "";
        }
        return futureGameStates.getFirst().lastAction.toString();
    }

}
