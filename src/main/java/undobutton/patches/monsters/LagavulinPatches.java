package undobutton.patches.monsters;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.Lagavulin;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.NewExpr;
import savestate.monsters.exordium.LagavulinState;

public class LagavulinPatches {
    @SpirePatch(clz = LagavulinState.class, method = "loadMonster")
    public static class loadMonsterPatch {
        @SpireInstrumentPatch
        public static ExprEditor fixSetAsleep() {
            return new ExprEditor() {
                @Override
                public void edit(NewExpr e) throws CannotCompileException {
                    if (e.getClassName().equals(Lagavulin.class.getName())) {
                        e.replace("$_ = $proceed(!this.isOutTriggered);");
                    }
                }
            };
        }

        @SpirePostfixPatch
        public static AbstractMonster fixMusic(AbstractMonster __result, LagavulinState __instance, boolean ___isOutTriggered) {
            boolean currentlyOpen = false;
            AbstractRoom currentRoom = AbstractDungeon.getCurrRoom();
            if (currentRoom != null) {
                if (currentRoom.monsters != null && currentRoom.monsters.getMonster("Lagavulin") != null) {
                    currentlyOpen = ReflectionHacks.getPrivate(currentRoom.monsters.getMonster("Lagavulin"), Lagavulin.class, "isOutTriggered");
                }
            }
            if (currentlyOpen && !___isOutTriggered) {
                CardCrawlGame.music.silenceTempBgmInstantly();
                CardCrawlGame.music.unsilenceBGM();
                AbstractDungeon.scene.fadeInAmbiance();
            }
            if (!currentlyOpen && ___isOutTriggered) {
                CardCrawlGame.music.unsilenceBGM();
                AbstractDungeon.scene.fadeOutAmbiance();
                CardCrawlGame.music.playTempBgmInstantly("ELITE");
            }
            return __result;
        }

    }
}
