package undobutton.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.OverlayMenu;
import undobutton.UndoButtonMod;
import undobutton.UndoButtonUI;

public class OverlayMenuPatches {
    @SpirePatch(clz = OverlayMenu.class, method = "render")
    public static class RenderPatch {
        @SpirePrefixPatch
        public static void renderUndoButtons(OverlayMenu __instance, SpriteBatch sb) {
            if (UndoButtonUI.isVisibleBelowScreen) {
                UndoButtonMod.ui.render(sb);
            }
        }
    }
}
