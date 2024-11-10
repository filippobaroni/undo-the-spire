package undobutton.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.OverlayMenu;
import undobutton.UndoButtonMod;
import undobutton.UndoButtonUI;

public class RenderButtonsOnOverlayMenuPatches {
    @SpirePatch(clz = OverlayMenu.class, method = "render")
    public static class RenderPatch {
        @SpirePrefixPatch
        public static void renderUndoButtons(OverlayMenu __instance, SpriteBatch sb) {
            if (UndoButtonUI.isVisibleBelowScreen) {
                UndoButtonMod.ui.render(sb);
            }
        }
    }

    @SpirePatch(clz = OverlayMenu.class, method = "showCombatPanels")
    public static class ShowCombatPanelsPatch {
        @SpirePostfixPatch
        public static void showUndoButton(OverlayMenu __instance) {
            UndoButtonMod.ui.show();
        }
    }

    @SpirePatch(clz = OverlayMenu.class, method = "hideCombatPanels")
    public static class HideCombatPanelsPatch {
        @SpirePostfixPatch
        public static void hideUndoButton(OverlayMenu __instance) {
            UndoButtonMod.ui.hide();
        }
    }
}