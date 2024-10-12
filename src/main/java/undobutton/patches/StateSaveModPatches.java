package undobutton.patches;


import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import savestate.CardState;
import savestate.SaveStateMod;
import savestate.monsters.MonsterState;
import savestate.relics.RelicState;


public class StateSaveModPatches {
    // There is a bug in MonsterState where a method tried to set the block colour to a float (instead of Color).
    // This patch fixes that.
    @SpirePatch(requiredModId = "SaveStateMod", clz = MonsterState.class, method = "populateSharedFields")
    public static class MonsterPopulateSharedFieldsPatch {
        @SpireInstrumentPatch
        public static ExprEditor modifyPopulateSharedFields() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("setPrivate")) {
                        m.replace("if (!$3.equals(\"blockTextColor\")) { $proceed($$); }");
                    }
                }
            };
        }
    }

    // Patch SaveStateMod to not add a config panel.
    @SpirePatch(requiredModId = "SaveStateMod", clz = SaveStateMod.class, method = "receivePostInitialize")
    public static class receivePostInitializePatch {
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

    // Patch SaveStateMod's CardState to ignore the (arbitrary) limit of 100 on free cards.
    @SpirePatch(requiredModId = "SaveStateMod", clz = CardState.class, method = "freeCard")
    public static class freeCardPatch {
        @SpireInstrumentPatch
        public static ExprEditor modifyFreeCard() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("size")) {
                        m.replace("$_ = 0;");
                    }
                }
            };
        }
    }

    // Patch SaveStateMod's RelicState to ignore the (arbitrary) limit of 100 on free relics.
    @SpirePatch(clz = RelicState.class, method = "freeRelic")
    public static class freeRelicPatch {
        @SpireInstrumentPatch
        public static ExprEditor modifyFreeCard() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("size")) {
                        m.replace("$_ = 0;");
                    }
                }
            };
        }
    }
}
