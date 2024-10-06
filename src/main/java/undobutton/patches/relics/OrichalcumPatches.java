package undobutton.patches.relics;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.Orichalcum;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import savestate.relics.OrichalcumState;

public class OrichalcumPatches {
    @SpirePatch(clz = OrichalcumState.class, method = "loadRelic")
    public static class loadRelicPatch {
        @SpirePostfixPatch
        public static AbstractRelic fixPulse(AbstractRelic __result, OrichalcumState __instance, boolean ___trigger) {
            AbstractDungeon.actionManager.addToBottom(new UpdateOrichalcumPulseAction());
            return __result;
        }
    }

    public static class UpdateOrichalcumPulseAction extends AbstractGameAction {
        @Override
        public void update() {
            if (AbstractDungeon.isPlayerInDungeon() && AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                for (AbstractRelic r : AbstractDungeon.player.relics) {
                    if (r instanceof Orichalcum) {
                        if (AbstractDungeon.player.currentBlock == 0 || ((Orichalcum) r).trigger) {
                            r.beginLongPulse();
                        }
                    }
                }
            }
            this.isDone = true;
        }
    }
}
