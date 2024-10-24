package undobutton.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import savestate.PlayerState;


public class PlayerStatePatches {
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
    }
}