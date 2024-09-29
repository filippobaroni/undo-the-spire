package undobutton;

import java.util.ArrayDeque;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import org.apache.logging.log4j.Logger;
import savestate.SaveState;

public class UndoButtonController {
    private final Logger logger;
    public static int MAX_UNDO = 0;
    private final ArrayDeque<SaveState> pastGameStates = new ArrayDeque<>();
    private final ArrayDeque<SaveState> futureGameStates = new ArrayDeque<>();

    public UndoButtonController(Logger logger) {
        this.logger = logger;
    }

    public boolean canUndo() {
        return !pastGameStates.isEmpty();
    }

    public boolean canRedo() {
        return !futureGameStates.isEmpty();
    }

    public void addState() {
        // Clear future states
        futureGameStates.clear();
        // Add new state
        pastGameStates.addFirst(makeState());
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
        futureGameStates.addFirst(makeState());
        applyState(pastGameStates.removeFirst());
    }

    public void redo() {
        if (futureGameStates.isEmpty()) {
            logger.info("Redo queue is empty.");
            return;
        }
        logger.info("Redoing.");
        pastGameStates.addFirst(makeState());
        applyState(futureGameStates.removeFirst());
    }

    private SaveState makeState() {
        SaveState state = new SaveState();
        // Purge end turn traces from newState
        ReflectionHacks.setPrivate(state, SaveState.class, "endTurnQueued", false);
        ReflectionHacks.setPrivate(state, SaveState.class, "isEndingTurn", false);
        // Return
        return state;
    }

    private void applyState(SaveState state) {
        // Release selected/hovered card
        AbstractDungeon.player.releaseCard();
        ReflectionHacks.setPrivate(AbstractDungeon.player, AbstractPlayer.class, "hoveredMonster", null);
        // Release potion
        AbstractDungeon.topPanel.potionUi.close();
        AbstractDungeon.topPanel.potionUi.targetMode = false;
        ReflectionHacks.setPrivate(AbstractDungeon.topPanel.potionUi, PotionPopUp.class, "hoveredMonster", null);
        // Load state
        state.loadState();
    }

    public void clearStates() {
        pastGameStates.clear();
        futureGameStates.clear();
        logger.info("Cleared undo/redo queue.");
    }
}
