package undobutton;


import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import savestate.SaveStateMod;

// Patch SaveStateMod to not add a config panel.
@SpirePatch(clz = SaveStateMod.class, method = "receivePostInitialize")
public class StateSaveModPanelPatch {
    @SpireInstrumentPatch
    public static ExprEditor modifyRegisterModBadge() {
        return new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if (m.getMethodName().equals("registerModBadge")) {
                    m.replace("$proceed($1, $2, $3, $4, null);");
                }
            }
        };
    }
}
