package undobutton;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.apache.logging.log4j.Logger;

@SpirePatch(clz = AbstractRoom.class, method = "endTurn")
public class EndTurnPatch {
    public static void Prefix(AbstractRoom __instance) {
        UndoButtonMod.logger.info("Added new state before ending turn.");
        UndoButtonMod.controller.addState();
    }
}