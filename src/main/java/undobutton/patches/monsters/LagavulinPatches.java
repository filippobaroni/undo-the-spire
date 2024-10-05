package undobutton.patches.monsters;

import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.monsters.exordium.Lagavulin;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.NewExpr;
import savestate.monsters.exordium.LagavulinState;

public class LagavulinPatches {
    @SpirePatch(clz = LagavulinState.class, method = "loadMonster")
    public static class loadMonsterPatch {
        @SpireInstrumentPatch
        public static ExprEditor modifyLoadMonster() {
            return new ExprEditor() {
                @Override
                public void edit(NewExpr e) throws CannotCompileException {
                    if (e.getClassName().equals(Lagavulin.class.getName())) {
                        e.replace("$_ = $proceed(!this.isOutTriggered);");
                    }
                }
            };
        }
    }
}
