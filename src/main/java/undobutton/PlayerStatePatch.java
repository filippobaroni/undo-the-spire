package undobutton;

import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import savestate.PlayerState;


// Patch PlayerState.loadPlayer to set EnergyPanel.totalCount instead of calling setEnergy.
// This prevents the (in my opinion) excessive flashing animation of the energy panel.
@SpirePatch(clz = PlayerState.class, method = "loadPlayer")
public class PlayerStatePatch {
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
}