package undobutton.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import savestate.PlayerState;
import savestate.orbs.OrbState;

import java.util.ArrayList;
import java.util.stream.Collectors;


public class PlayerStatePatches {
    @SpirePatch(clz = PlayerState.class, method = SpirePatch.CLASS)
    public static class ExtraFields {
        public static SpireField<ArrayList<OrbState>> orbsChanneledThisTurn = new SpireField<>(ArrayList::new);
    }

    @SpirePatch(clz = PlayerState.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractPlayer.class})
    public static class ConstructorPatch {
        @SpirePostfixPatch
        public static void saveOrbsChanneledThisTurn(PlayerState __instance, AbstractPlayer player) {
            ExtraFields.orbsChanneledThisTurn.set(__instance, AbstractDungeon.actionManager.orbsChanneledThisTurn.stream().map(OrbState::forOrb).collect(Collectors.toCollection(ArrayList::new)));
        }
    }

    @SpirePatch(clz = PlayerState.class, method = "loadPlayer")
    public static class LoadPlayerPatch {
        // Patch PlayerState.loadPlayer to set EnergyPanel.totalCount instead of calling setEnergy.
        // This prevents the (in my opinion) excessive flashing animation of the energy panel.
        @SpireInstrumentPatch
        public static ExprEditor modifySetEnergy() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("setEnergy")) {
                        m.replace(EnergyPanel.class.getName() + ".totalCount = $1;");
                    }
                }
            };
        }

        // Set the health bar to the correct width
        @SpirePostfixPatch
        public static AbstractPlayer updateHealthBar(AbstractPlayer __result, PlayerState __instance) {
            __result.healthBarUpdatedEvent();
            ReflectionHacks.setPrivate(__result, AbstractCreature.class, "healthBarWidth", ReflectionHacks.getPrivate(__result, AbstractCreature.class, "targetHealthBarWidth"));
            ReflectionHacks.setPrivate(__result, AbstractCreature.class, "healthBarAnimTimer", 0.0F);
            return __result;
        }

        // Load the orbs channeled this turn
        @SpirePostfixPatch
        public static AbstractPlayer loadOrbsChanneledThisTurn(AbstractPlayer __result, PlayerState __instance) {
            AbstractDungeon.actionManager.orbsChanneledThisTurn = ExtraFields.orbsChanneledThisTurn.get(__instance).stream().map(OrbState::loadOrb).collect(Collectors.toCollection(ArrayList::new));
            return __result;
        }

        // Set Watcher's eye on the staff to match current stance
        @SpirePostfixPatch
        public static AbstractPlayer setWatcherEye(AbstractPlayer __result, PlayerState __instance) {
            if (__result.chosenClass == AbstractPlayer.PlayerClass.WATCHER) {
                __result.onStanceChange(__result.stance.ID);
            }
            return __result;
        }
    }
}