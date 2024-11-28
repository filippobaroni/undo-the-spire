package undobutton.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.green.Eviscerate;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;


public class EvisceratePatches {
    @SpirePatch(clz = Eviscerate.class, method = "makeCopy")
    public static class MakeCopyPatch {
        @SpireInstrumentPatch
        public static ExprEditor dontIncreaseCostForTurn() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("setCostForTurn")) {
                        m.replace("$proceed(java.lang.Math.min(this.costForTurn, $$));");
                    }
                }
            };
        }
    }
}
