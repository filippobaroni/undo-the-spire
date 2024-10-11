package undobutton.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rewards.RewardItem;
import savestate.MapRoomNodeState;
import undobutton.UndoButtonMod;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class RoomStatePatches {
    public static RewardItem copyRewardItem(RewardItem rewardItem) {
        switch (rewardItem.type) {
            case RELIC:
                return new RewardItem(rewardItem.relic.makeCopy());
            case GOLD:
                return new RewardItem(rewardItem.goldAmt);
            case STOLEN_GOLD:
                return new RewardItem(rewardItem.goldAmt, true);
            default:
                UndoButtonMod.logger.error("Unsupported reward type: {}", rewardItem.type);
                throw new UnsupportedOperationException("Unsupported reward type: " + rewardItem.type);
        }
    }

    @SpirePatch(clz = MapRoomNodeState.class, method = SpirePatch.CLASS)
    public static class RewardsField {
        public static SpireField<ArrayList<RewardItem>> rewards = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = MapRoomNodeState.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {MapRoomNode.class})
    public static class ConstructorPatch {
        @SpirePostfixPatch
        public static void PopulateRewards(MapRoomNodeState __instance, MapRoomNode node) {
            RewardsField.rewards.set(__instance, node.room.rewards.stream().map(RoomStatePatches::copyRewardItem).collect(Collectors.toCollection(ArrayList::new)));
        }
    }

    @SpirePatch(clz = MapRoomNodeState.class, method = "loadMapRoomNode")
    public static class LoadPatch {
        @SpirePostfixPatch
        public static MapRoomNode Postfix(MapRoomNode __result, MapRoomNodeState __instance) {
            __result.room.rewards = RewardsField.rewards.get(__instance).stream().map(RoomStatePatches::copyRewardItem).collect(Collectors.toCollection(ArrayList::new));
            return __result;
        }
    }
}
