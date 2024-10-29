package undobutton;

import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.util.ArrayDeque;

public class UndoButtonController {
    private final ArrayDeque<GameState> pastGameStates = new ArrayDeque<>();
    private final ArrayDeque<GameState> futureGameStates = new ArrayDeque<>();
    private int nonFailedPastStates = 0;
    private int nonFailedFutureStates = 0;
    public boolean isThisEndTurnForced = false;
    public boolean isPlayerFlippedHorizontally = false;


    public void onStartBattle(AbstractRoom room) {
        clearStates();
        isThisEndTurnForced = false;
        isPlayerFlippedHorizontally = false;
    }

    public boolean canUndo() {
        return nonFailedPastStates > 0;
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
        return nonFailedFutureStates > 0;
    }

    public void addState(GameState.Action action) {
        // Clear future states
        futureGameStates.clear();
        nonFailedFutureStates = 0;
        // Add new state
        try {
            pastGameStates.addFirst(new GameState(action));
            nonFailedPastStates++;
        } catch (Exception e) {
            failedToAddState(e);
            pastGameStates.addFirst(new GameState(new GameState.Action(GameState.ActionType.FAILED)));
        }
        // Remove the oldest state if queue is too long
        if (pastGameStates.size() > UndoButtonMod.getMaxStates()) {
            GameState state = pastGameStates.removeLast();
            if (state.lastAction.type != GameState.ActionType.FAILED) {
                nonFailedPastStates--;
            }
            UndoButtonMod.logger.info("More than {} states in queue, removing oldest state.", UndoButtonMod.getMaxStates());
        }
    }

    public void undo() {
        while (!pastGameStates.isEmpty() && pastGameStates.getFirst().lastAction.type == GameState.ActionType.FAILED) {
            pastGameStates.removeFirst();
            UndoButtonMod.logger.info("Skipping undoing a failed state.");
        }
        if (pastGameStates.isEmpty()) {
            UndoButtonMod.logger.info("Undo queue is empty.");
            return;
        }
        UndoButtonMod.logger.info("Undoing.");
        GameState state = pastGameStates.removeFirst();
        --nonFailedPastStates;
        try {
            futureGameStates.addFirst(new GameState(state.lastAction));
            nonFailedFutureStates++;
        } catch (Exception e) {
            failedToAddState(e);
            futureGameStates.addFirst(new GameState(new GameState.Action(GameState.ActionType.FAILED)));
        }
        state.apply();
    }

    public void redo() {
        while (!futureGameStates.isEmpty() && futureGameStates.getFirst().lastAction.type == GameState.ActionType.FAILED) {
            futureGameStates.removeFirst();
            UndoButtonMod.logger.info("Skipping redoing a failed state.");
        }
        if (futureGameStates.isEmpty()) {
            UndoButtonMod.logger.info("Redo queue is empty.");
            return;
        }
        UndoButtonMod.logger.info("Redoing.");
        GameState state = futureGameStates.removeFirst();
        --nonFailedFutureStates;
        try {
            pastGameStates.addFirst(new GameState(state.lastAction));
            nonFailedPastStates++;
        } catch (Exception e) {
            failedToAddState(e);
            pastGameStates.addFirst(new GameState(new GameState.Action(GameState.ActionType.FAILED)));
        }
        state.apply();
    }

    public void clearStates() {
        pastGameStates.clear();
        futureGameStates.clear();
        nonFailedPastStates = 0;
        nonFailedFutureStates = 0;
        UndoButtonMod.logger.info("Cleared undo/redo queue.");
    }

    public boolean isUndoStateFailed() {
        return !pastGameStates.isEmpty() && pastGameStates.getFirst().lastAction.type == GameState.ActionType.FAILED;
    }

    public boolean isRedoStateFailed() {
        return !futureGameStates.isEmpty() && futureGameStates.getFirst().lastAction.type == GameState.ActionType.FAILED;
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

    public void failedToAddState(Exception e) {
        if (UndoButtonMod.DEBUG) {
            throw new RuntimeException("Failed to save the current state", e);
        } else {
            UndoButtonMod.logger.error("Failed to save the current state");
            e.printStackTrace();
        }
    }

}
