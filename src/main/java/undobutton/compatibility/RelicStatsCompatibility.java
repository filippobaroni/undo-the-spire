package undobutton.compatibility;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.google.gson.JsonElement;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import relicstats.AmountAdjustmentCallback;
import relicstats.AmountIncreaseCallback;
import relicstats.actions.*;
import savestate.StateFactories;
import savestate.actions.ActionState;
import undobutton.patches.AbstractCreaturePatches;
import undobutton.util.MakeUndoable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

@MakeUndoable(statetype = JsonElement.class)
public class RelicStatsCompatibility {
    public static JsonElement save() {
        if (checkForMod()) {
            try {
                return (JsonElement) Class.forName("relicstats.StatsSaver").getMethod("saveRelics").invoke(null);
            } catch (Exception e) {
                // Should not happen.
                throw new RuntimeException("Failed to save state for RelicStats", e);
            }
        }
        return null;
    }

    public static void load(JsonElement element) {
        if (checkForMod()) {
            try {
                Class.forName("relicstats.StatsSaver").getMethod("loadRelics", JsonElement.class).invoke(null, element);
            } catch (Exception e) {
                // Should not happen.
                throw new RuntimeException("Failed to load state for RelicStats", e);
            }
        }
    }

    private static boolean checkForMod() {
        return Loader.isModLoaded("RelicStats");
    }

    public enum ExtraActions {
        AOE_DAMAGE_FOLLOWUP_ACTION(AoeDamageFollowupAction.class, new ActionState.ActionFactories(action -> new AoeDamageFollowupActionState((AoeDamageFollowupAction) action))),
        DRAW_FOLLOWUP_ACTION(CardDrawFollowupAction.class),
        HEALING_FOLLOWUP_ACTION(HealingFollowupAction.class),
        ORANGE_PELLETS_FOLLOWUP_ACTION(OrangePelletsFollowupAction.class),
        PRE_AOE_DAMAGE_ACTION(PreAoeDamageAction.class
                , new ActionState.ActionFactories(action -> new PreAoeDamageActionState((PreAoeDamageAction) action))),
        PRE_DRAW_ACTION(PreCardDrawAction.class, PreAmountAdjustmentAction.class),
        PRE_GOLDEN_EYE_SCRY_ACTION(PreGoldenEyeScryAction.class, PreAmountAdjustmentAction.class, "baseScry"),
        PRE_HEALING_ACTION(PreHealingAction.class, PreAmountAdjustmentAction.class),
        PRE_ORANGE_PELLETS_ACTION(PreOrangePelletsAction.class, PreAmountAdjustmentAction.class),
        PRE_SCRY_ACTION(PreScryAction.class, PreAmountAdjustmentAction.class),
        WARPED_TONGS_FOLLOWUP_ACTION(WarpedTongsFollowupAction.class, new ActionState.ActionFactories(action -> WarpedTongsFollowupAction::new));

        public final Class<? extends AbstractGameAction> actionClass;
        public final ActionState.ActionFactories factory;

        ExtraActions(Class<? extends AbstractGameAction> actionClass, ActionState.ActionFactories factory) {
            this.actionClass = actionClass;
            this.factory = factory;
        }

        ExtraActions(Class<? extends AbstractGameAction> actionClass, Class<? extends AbstractGameAction> parentClass, String extraFieldName) {
            this.actionClass = actionClass;
            this.factory = new ActionState.ActionFactories(action -> new ActionState() {
                final AmountAdjustmentCallback statTracker;
                Object extraField = null;

                {
                    statTracker = ReflectionHacks.getPrivate(action, parentClass, "statTracker");
                    if (extraFieldName != null) {
                        extraField = ReflectionHacks.getPrivate(action, actionClass, extraFieldName);
                    }
                }

                @Override
                public AbstractGameAction loadAction() {
                    if (extraField == null) {
                        try {
                            return actionClass.getConstructor(AmountAdjustmentCallback.class).newInstance(statTracker);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                 NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Class<?> extraFieldType = extraField.getClass();
                        if (extraFieldType == Integer.class) {
                            extraFieldType = int.class;
                        }
                        try {
                            return actionClass.getConstructor(AmountAdjustmentCallback.class, extraFieldType).newInstance(statTracker, extraField);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                 NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }

        ExtraActions(Class<? extends AbstractGameAction> actionClass, Class<? extends AbstractGameAction> parentClass) {
            this(actionClass, parentClass, null);
        }

        ExtraActions(Class<? extends AbstractGameAction> actionClass, String extraFieldName) {
            this(actionClass, actionClass, extraFieldName);
        }

        ExtraActions(Class<? extends AbstractGameAction> actionClass) {
            this(actionClass, actionClass, null);
        }

    }

    @SpirePatch(clz = StateFactories.class, method = "createActionMap")
    public static class StateFactoriesActionPatch {
        @SpirePostfixPatch
        public static HashMap<Class, ActionState.ActionFactories> addCustomActions(HashMap<Class, ActionState.ActionFactories> __result) {
            if (checkForMod()) {
                for (ExtraActions action : ExtraActions.values()) {
                    __result.put(action.actionClass, action.factory);
                }
            }
            return __result;
        }
    }

    public static class PreAoeDamageActionState implements ActionState {
        public ArrayList<UUID> affectedMonsters;

        public PreAoeDamageActionState(PreAoeDamageAction action) {
            affectedMonsters = action.getAffectedMonsters().stream().map(m -> AbstractCreaturePatches.ExtraFields.uuid.get(m)).collect(Collectors.toCollection(ArrayList::new));
        }

        public AbstractGameAction loadAction() {
            PreAoeDamageAction result = new PreAoeDamageAction();//(AbstractGameAction) PreAoeDamageAction.newInstance();
            ReflectionHacks.setPrivate(result, PreAoeDamageAction.class, "affectedMonsters", affectedMonsters.stream().map(AbstractCreaturePatches::getMonsterFromUUID).collect(Collectors.toCollection(ArrayList::new)));
            return result;
        }
    }

    @SpirePatch(requiredModId = "RelicStats", clz = PreAoeDamageAction.class, method = "update")
    public static class PreAoeDamageActionPatch {
        @SpirePostfixPatch
        public static void updateFollowUpAction(PreAoeDamageAction __instance) {
            for (AbstractGameAction action : AbstractDungeon.actionManager.actions) {
                if (action instanceof AoeDamageFollowupAction) {
                    ReflectionHacks.setPrivate(action, AoeDamageFollowupAction.class, "preAction", __instance);
                    break;
                }
            }
        }
    }

    public static class AoeDamageFollowupActionState implements ActionState {
        public AmountIncreaseCallback statTracker;

        public AoeDamageFollowupActionState(AoeDamageFollowupAction action) {
            statTracker = ReflectionHacks.getPrivate(action, AoeDamageFollowupAction.class, "statTracker");
        }

        public AbstractGameAction loadAction() {
            return new AoeDamageFollowupAction(statTracker, new PreAoeDamageAction());
        }
    }
}
