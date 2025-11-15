package undobutton.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.exordium.DeadAdventurer;
import com.megacrit.cardcrawl.events.exordium.Mushrooms;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.EventRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import javassist.CannotCompileException;
import javassist.CtBehavior;
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

    public enum ActualRoomType {
        BOSS, ELITE, MONSTER, EVENT
    }

    @SpirePatch(clz = MapRoomNodeState.class, method = SpirePatch.CLASS)
    public static class ExtraFields {
        public static SpireField<ArrayList<RewardItem>> rewards = new SpireField<>(() -> null);
        public static SpireField<ActualRoomType> actualRoomType = new SpireField<>(() -> null);
        public static SpireField<AbstractEvent> event = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = MapRoomNodeState.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {MapRoomNode.class})
    public static class ConstructorPatch {
        @SpirePostfixPatch
        public static void PopulateRewards(MapRoomNodeState __instance, MapRoomNode node) {
            AbstractRoom room = node.room;
            ExtraFields.rewards.set(__instance, room.rewards.stream().map(RoomStatePatches::copyRewardItem).collect(Collectors.toCollection(ArrayList::new)));
            ExtraFields.actualRoomType.set(__instance,
                    room instanceof MonsterRoomBoss ? ActualRoomType.BOSS :
                            room instanceof MonsterRoomElite ? ActualRoomType.ELITE :
                                    room instanceof EventRoom ? ActualRoomType.EVENT : ActualRoomType.MONSTER);
            if (room.event != null) {
                ExtraFields.event.set(__instance, room.event);
            }
        }
    }

    @SpirePatch(clz = MapRoomNodeState.class, method = "loadMapRoomNode")
    public static class LoadPatch {
        @SpireInsertPatch(locator = Locator.class)
        public static void checkIfEventRoom(MapRoomNodeState __instance, MapRoomNode node, @ByRef AbstractRoom[] ___room) {
            if (ExtraFields.actualRoomType.get(__instance) == ActualRoomType.EVENT) {
                ___room[0] = new EventRoom();
            }
            if (ExtraFields.event.get(__instance) != null) {
                ___room[0].event = ExtraFields.event.get(__instance);
            }
        }

        @SpirePostfixPatch
        public static MapRoomNode postfix(MapRoomNode __result, MapRoomNodeState __instance) {
            __result.room.rewards = ExtraFields.rewards.get(__instance).stream().map(RoomStatePatches::copyRewardItem).collect(Collectors.toCollection(ArrayList::new));
            if (__result.room.event instanceof DeadAdventurer) {
                ReflectionHacks.setPrivate(__result.room.event, DeadAdventurer.class, "adventurerImg", ImageMaster.loadImage("images/npcs/nopants.png"));
            } else if (__result.room.event instanceof Mushrooms) {
                ReflectionHacks.setPrivate(__result.room.event, Mushrooms.class, "fgImg", ImageMaster.loadImage("images/events/fgShrooms.png"));
                ReflectionHacks.setPrivate(__result.room.event, Mushrooms.class, "bgImg", ImageMaster.loadImage("images/events/bgShrooms.png"));
            }
            return __result;
        }

        public static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(MapRoomNodeState.class, "getRoomForType");
                int line = LineFinder.findInOrder(ctMethodToPatch, finalMatcher)[0];
                return new int[]{line + 1};
            }
        }
    }
}
