package undobutton.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.EndTurnAction;
import com.megacrit.cardcrawl.actions.common.MonsterStartTurnAction;
import com.megacrit.cardcrawl.actions.common.PlayTopCardAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.MayhemPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.RedSkull;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import savestate.StateFactories;
import savestate.actions.ActionState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TemporaryActionsPatches {
    public static List<Class<? extends AbstractGameAction>> extraActions = Arrays.asList(
            MayhemAction.class,
            RedSkullAction.class,
            AbstractRoomEndTurnAction.class
    );

    public static class MayhemAction extends AbstractGameAction {
        public void update() {
            addToBot(new PlayTopCardAction(AbstractDungeon.getCurrRoom().monsters.getRandomMonster(null, true, AbstractDungeon.cardRandomRng), false));
            isDone = true;
        }
    }

    public static class RedSkullAction extends AbstractGameAction {
        public void update() {
            RedSkull relic = (RedSkull) AbstractDungeon.player.getRelic("Red Skull");
            if (!((boolean) ReflectionHacks.getPrivate(relic, RedSkull.class, "isActive")) && AbstractDungeon.player.isBloodied) {
                relic.flash();
                ReflectionHacks.setPrivate(relic, AbstractRelic.class, "pulse", true);
                AbstractDungeon.player.addPower(new StrengthPower(AbstractDungeon.player, 3));
                addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, relic));
                ReflectionHacks.setPrivate(relic, AbstractRelic.class, "isActive", true);
                AbstractDungeon.onModifyPower();
            }
            isDone = true;
        }
    }

    public static class AbstractRoomEndTurnAction extends AbstractGameAction {
        public void update() {
            addToBot(new EndTurnAction());
            addToBot(new WaitAction(1.2F));
            if (!AbstractDungeon.getCurrRoom().skipMonsterTurn) {
                addToBot(new MonsterStartTurnAction());
            }
            AbstractDungeon.actionManager.monsterAttacksQueued = false;
            isDone = true;
        }
    }

    @SpirePatch(clz = StateFactories.class, method = "createActionMap")
    public static class StateFactoriesPatch {
        @SpirePostfixPatch
        public static HashMap<Class, ActionState.ActionFactories> addCustomActions(HashMap<Class, ActionState.ActionFactories> __result) {
            for (Class<? extends AbstractGameAction> actionClass : extraActions) {
                __result.put(actionClass, new ActionState.ActionFactories(action -> () -> {
                    try {
                        return actionClass.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }
            return __result;
        }
    }

    @SpirePatch(clz = MayhemPower.class, method = "atStartOfTurn")
    public static class MayhemPowerPatch {
        @SpireInstrumentPatch
        public static ExprEditor replaceTemporaryAction() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("addToBot")) {
                        m.replace("addToBot(new " + MayhemAction.class.getName() + "());");
                    }
                }
            };
        }
    }

    @SpirePatch(clz = RedSkull.class, method = "atBattleStart")
    public static class RedSkullPatch {
        @SpireInstrumentPatch
        public static ExprEditor replaceTemporaryAction() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("addToBot")) {
                        m.replace("addToBot(new " + RedSkullAction.class.getName() + "());");
                    }
                }
            };
        }
    }

    @SpirePatch(clz = AbstractRoom.class, method = "endTurn")
    public static class AbstractRoomEndTurnPatch {
        @SpireInstrumentPatch
        public static ExprEditor replaceTemporaryAction() {
            return new ExprEditor() {
                private int count = 0;

                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("addToBottom") && m.getClassName().equals(GameActionManager.class.getName())) {
                        count++;
                        if (count == 3) {
                            m.replace("$proceed(new " + AbstractRoomEndTurnAction.class.getName() + "());");
                        }
                    }
                }
            };
        }
    }
}
