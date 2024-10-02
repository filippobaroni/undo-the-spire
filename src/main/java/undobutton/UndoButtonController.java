package undobutton;

import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;

public class UndoButtonController {
    private final Logger logger;
    public static int MAX_UNDO = 0;
    private final ArrayDeque<GameState> pastGameStates = new ArrayDeque<>();
    private final ArrayDeque<GameState> futureGameStates = new ArrayDeque<>();

    public UndoButtonController(Logger logger) {
        this.logger = logger;
    }

    public boolean canUndo() {
        return !pastGameStates.isEmpty();
    }

    public boolean isSafeToUndo() {
        if(AbstractDungeon.getCurrMapNode() == null) {
            return false;
        }
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (room == null || room.phase != AbstractRoom.RoomPhase.COMBAT) {
            return false;
        }
        if (AbstractDungeon.isScreenUp || !AbstractDungeon.actionManager.isEmpty() || AbstractDungeon.actionManager.phase != GameActionManager.Phase.WAITING_ON_USER) {
            return false;
        }
        return true;
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
        if (pastGameStates.size() > MAX_UNDO) {
            pastGameStates.removeLast();
            logger.info("More than {} states in queue, removing oldest state.", MAX_UNDO);
        }
    }

    public void undo() {
        if (pastGameStates.isEmpty()) {
            logger.info("Undo queue is empty.");
            return;
        }
        logger.info("Undoing.");
        GameState state = pastGameStates.removeFirst();
        futureGameStates.addFirst(new GameState(state.lastAction));
        state.apply();
    }

    public void redo() {
        if (futureGameStates.isEmpty()) {
            logger.info("Redo queue is empty.");
            return;
        }
        logger.info("Redoing.");
        GameState state= futureGameStates.removeFirst();
        pastGameStates.addFirst(new GameState(state.lastAction));
        state.apply();
    }

    public void clearStates() {
        pastGameStates.clear();
        futureGameStates.clear();
        logger.info("Cleared undo/redo queue.");
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
