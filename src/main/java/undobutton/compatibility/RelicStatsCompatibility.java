package undobutton.compatibility;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.google.gson.JsonElement;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import savestate.StateFactories;
import savestate.actions.ActionState;
import undobutton.util.MakeUndoable;

import java.util.HashMap;

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

    public enum ExtraActions {
        // Not doing AoeDamageActions, might come back to bite me.
        DRAW_FOLLOWUP_ACTION("CardDrawFollowupAction", makeRelicStatsFactory("AmountAdjustmentCallback", "CardDrawFollowupAction")),
        HEALING_FOLLOWUP_ACTION("HealingFollowupAction", makeRelicStatsFactory("AmountAdjustmentCallback", "HealingFollowupAction")),
        ORANGE_PELLETS_FOLLOWUP_ACTION("OrangePelletsFollowupAction", makeRelicStatsFactory("AmountAdjustmentCallback", "OrangePelletsFollowupAction")),
        PRE_DRAW_ACTION("PreCardDrawAction", makeRelicStatsFactory("AmountAdjustmentCallback", "PreAmountAdjustmentAction")),
        PRE_GOLDEN_EYE_SCRY_ACTION("PreGoldenEyeScryAction", makeRelicStatsFactory("AmountAdjustmentCallback", "PreAmountAdjustmentAction", "baseScry")),
        PRE_HEALING_ACTION("PreHealingAction", makeRelicStatsFactory("AmountAdjustmentCallback", "PreAmountAdjustmentAction")),
        PRE_ORANGE_PELLETS_ACTION("PreOrangePelletsAction", makeRelicStatsFactory("AmountAdjustmentCallback", "PreAmountAdjustmentAction")),
        PRE_SCRY_ACTION("PreScryAction", makeRelicStatsFactory("AmountAdjustmentCallback", "PreAmountAdjustmentAction")),
        WARPED_TONGS_FOLLOWUP_ACTION("WarpedTongsFollowupAction", new ActionState.ActionFactories(action -> () -> {
            try {
                return (AbstractGameAction) Class.forName("relicstats.actions.WarpedTongsFollowupAction").newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));

        public final String actionClassName;
        public final ActionState.ActionFactories factory;

        ExtraActions(String actionClassName, ActionState.ActionFactories factory) {
            this.actionClassName = actionClassName;
            this.factory = factory;
        }

    }

    @SpirePatch(clz = StateFactories.class, method = "createActionMap")
    public static class StateFactoriesActionPatch {
        @SpirePostfixPatch
        public static HashMap<Class, ActionState.ActionFactories> addCustomActions(HashMap<Class, ActionState.ActionFactories> __result) {
            if (checkForMod()) {
                for (ExtraActions action : ExtraActions.values()) {
                    try {
                        __result.put(Class.forName("relicstats.actions." + action.actionClassName), action.factory);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Class " + action.actionClassName + " from RelicStats not found", e);
                    }
                }
            }
            return __result;
        }
    }

    private static ActionState.ActionFactories makeRelicStatsFactory(String statTrackerClassName, String parentClassName, String... extraFieldNames) {
        return new ActionState.ActionFactories(action -> new ActionState() {
            final Object[] fields;

            {
                fields = new Object[extraFieldNames.length + 1];
                try {
                    fields[0] = ReflectionHacks.getPrivate(action, Class.forName("relicstats.actions." + parentClassName), "statTracker");
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                for (int i = 0; i < extraFieldNames.length; i++) {
                    fields[i + 1] = ReflectionHacks.getPrivate(action, action.getClass(), extraFieldNames[i]);
                }
            }

            @Override
            public AbstractGameAction loadAction() {
                try {
                    Class<?>[] pTypes = new Class[fields.length];
                    pTypes[0] = Class.forName("relicstats." + statTrackerClassName);
                    for (int i = 1; i < fields.length; i++) {
                        pTypes[i] = fields[i].getClass();
                    }
                    return action.getClass().getConstructor(pTypes).newInstance(fields);
                } catch (Exception e) {
                    // Should not happen.
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static class PreHealingActionState implements ActionState {
        Object statTracker;
        Class<?> PreAmountAdjustmentAction;

        public PreHealingActionState(AbstractGameAction action) {
            try {
                PreAmountAdjustmentAction = Class.forName("relicstats.actions.PreAmountAdjustmentAction");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class PreAmountAdjustmentAction from RelicStats not found", e);
            }
            statTracker = ReflectionHacks.getPrivate(action, PreAmountAdjustmentAction, "statTracker");
        }

        public AbstractGameAction loadAction() {
            try {
                return (AbstractGameAction) PreAmountAdjustmentAction.getConstructor(statTracker.getClass()).newInstance(statTracker);
            } catch (Exception e) {
                // Should not happen.
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean checkForMod() {
        return Loader.isModLoaded("RelicStats");
    }
}
