package undobutton;

import basemod.ModLabel;
import basemod.ModPanel;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.screens.options.DropdownMenuListener;

import java.util.Arrays;

public class UndoButtonSettingsPanel extends ModPanel implements DropdownMenuListener {
    public static final int[] UNDO_STATES_NUMBERS = {10, 20, 50, 100, 200, 500, 1000};
    public static float LABEL_X = 380.0F, LABEL_Y = 792.0F;
    public static float DROPDOWN_REL_X = 380.0F, DROPDOWN_REL_Y = 22.0F;
    private static DropdownMenu undoStatesDropdown;
    private static UIStrings numStatesStrings;

    public UndoButtonSettingsPanel() {
        super(me -> {
            numStatesStrings = CardCrawlGame.languagePack.getUIString(UndoButtonMod.makeID("Settings Num States"));
            undoStatesDropdown.setSelectedIndex(Arrays.binarySearch(UNDO_STATES_NUMBERS, UndoButtonMod.getMaxStates()));
            try {
                DROPDOWN_REL_X = Float.parseFloat(numStatesStrings.TEXT_DICT.get("width"));
            } catch (NumberFormatException ignored) {
            }
        });
        ModLabel numStatesLabel = new ModLabel("", LABEL_X, LABEL_Y, Settings.CREAM_COLOR, FontHelper.charDescFont, this, me -> me.text = numStatesStrings.TEXT[0]);
        this.addUIElement(numStatesLabel);
        undoStatesDropdown = new DropdownMenu(this, Arrays.stream(UNDO_STATES_NUMBERS).mapToObj(String::valueOf).toArray(String[]::new), FontHelper.tipBodyFont, Settings.CREAM_COLOR);
    }

    public void update() {
        super.update();
        undoStatesDropdown.update();
    }

    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {
        if (dropdownMenu == undoStatesDropdown) {
            UndoButtonMod.setMaxStates(UNDO_STATES_NUMBERS[i]);
        }
    }

    public void render(SpriteBatch sb) {
        super.render(sb);
        undoStatesDropdown.render(sb, (LABEL_X + DROPDOWN_REL_X) * Settings.scale, (LABEL_Y + DROPDOWN_REL_Y) * Settings.scale);
    }
}
