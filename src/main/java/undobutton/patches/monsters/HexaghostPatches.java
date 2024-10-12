package undobutton.patches.monsters;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.Hexaghost;
import com.megacrit.cardcrawl.monsters.exordium.HexaghostBody;
import com.megacrit.cardcrawl.monsters.exordium.HexaghostOrb;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import savestate.monsters.exordium.HexaghostState;

import java.util.ArrayList;

public class HexaghostPatches {
    @SpirePatch(clz = HexaghostState.class, method = SpirePatch.CLASS)
    public static class ExtraFields {
        public static SpireField<Float> bodyRotation = new SpireField<>(() -> 0.0F);
        public static SpireField<OrbState[]> orbs = new SpireField<>(() -> new OrbState[6]);
    }

    @SpirePatch(clz = HexaghostState.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractMonster.class})
    public static class ConstructorPatch {
        @SpirePostfixPatch
        public static void setExtraFields(HexaghostState __instance, AbstractMonster monster) {
            ExtraFields.bodyRotation.set(__instance, ((HexaghostBody) ReflectionHacks.getPrivate(monster, Hexaghost.class, "body")).targetRotationSpeed);
            for (int i = 0; i < 6; i++) {
                ExtraFields.orbs.get(__instance)[i] = new OrbState(((ArrayList<HexaghostOrb>) ReflectionHacks.getPrivate(monster, Hexaghost.class, "orbs")).get(i));
            }
        }
    }

    @SpirePatch(clz = HexaghostState.class, method = "loadMonster")
    public static class LoadPatch {
        @SpirePostfixPatch
        public static AbstractMonster loadExtraFields(AbstractMonster __result, HexaghostState __instance) {
            Hexaghost hexaghost = (Hexaghost) __result;
            HexaghostBody body = ReflectionHacks.getPrivate(hexaghost, Hexaghost.class, "body");
            body.targetRotationSpeed = ExtraFields.bodyRotation.get(__instance);
            ReflectionHacks.setPrivate(body, HexaghostBody.class, "rotationSpeed", body.targetRotationSpeed);
            ArrayList<HexaghostOrb> orbs = ReflectionHacks.getPrivate(hexaghost, Hexaghost.class, "orbs");
            for (int i = 0; i < 6; i++) {
                ExtraFields.orbs.get(__instance)[i].loadOrb(orbs.get(i));
            }
            return __result;
        }

        @SpirePostfixPatch
        public static AbstractMonster fixMusic(AbstractMonster __result, HexaghostState __instance) {
            if (AbstractDungeon.getCurrRoom() instanceof MonsterRoomBoss) {
                boolean currentlyActivated = false;
                AbstractRoom currentRoom = AbstractDungeon.getCurrRoom();
                if (currentRoom != null) {
                    if (currentRoom.monsters != null && currentRoom.monsters.getMonster("Hexaghost") != null) {
                        currentlyActivated = !((ArrayList<HexaghostOrb>) ReflectionHacks.getPrivate(currentRoom.monsters.getMonster("Hexaghost"), Hexaghost.class, "orbs")).get(0).hidden;
                    }
                }
                if (ExtraFields.orbs.get(__instance)[0].hidden && currentlyActivated) {
                    CardCrawlGame.music.silenceTempBgmInstantly();
                    AbstractDungeon.scene.fadeInAmbiance();
                }
                if (!ExtraFields.orbs.get(__instance)[0].hidden && !currentlyActivated) {
                    CardCrawlGame.music.unsilenceBGM();
                    AbstractDungeon.scene.fadeOutAmbiance();
                    CardCrawlGame.music.playTempBgmInstantly("BOSS_BOTTOM");
                }
            }
            return __result;
        }
    }

    public static class OrbState {
        public boolean activated = false;
        public boolean hidden = false;

        public OrbState(HexaghostOrb orb) {
            this.activated = orb.activated;
            this.hidden = orb.hidden;
        }

        public void loadOrb(HexaghostOrb orb) {
            orb.activated = this.activated;
            orb.hidden = this.hidden;
            orb.playedSfx = true;
            ReflectionHacks.setPrivate(orb, HexaghostOrb.class, "activateTimer", 0.0F);
        }
    }
}
