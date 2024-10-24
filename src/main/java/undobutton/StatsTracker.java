package undobutton;

import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import undobutton.util.MakeUndoable;

@MakeUndoable(statetype = StatsTracker.StatsState.class)
public class StatsTracker {
    public static class StatsState {
        public final int goldGained;
        public final int damageReceivedThisCombat;
        public final int hpLossThisCombat;

        public StatsState() {
            goldGained = CardCrawlGame.goldGained;
            damageReceivedThisCombat = GameActionManager.damageReceivedThisCombat;
            hpLossThisCombat = GameActionManager.hpLossThisCombat;
        }
    }

    public static StatsState save() {
        return new StatsState();
    }

    public static void load(StatsState state) {
        CardCrawlGame.goldGained = state.goldGained;
    }
}
