package undobutton;

import basemod.BaseMod;
import basemod.DevConsole;
import basemod.interfaces.*;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.input.InputAction;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import undobutton.util.GeneralUtils;
import undobutton.util.TextureLoader;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.Patcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.localization.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scannotation.AnnotationDB;

import java.util.*;

@SpireInitializer
public class UndoButtonMod implements
        EditStringsSubscriber,
        PostInitializeSubscriber,
        OnStartBattleSubscriber,
        RenderSubscriber,
        PostUpdateSubscriber,
        OnCardUseSubscriber,
        PrePotionUseSubscriber {
    public static ModInfo info;
    public static String modID;
    static { loadModInfo(); }
    private static final String resourcesFolder = checkResourcesPath();
    public static final Logger logger = LogManager.getLogger(modID); //Used to output to the console.
    public static UndoButtonController controller;
    public static UndoButtonUI ui;
    public static InputAction undoInputAction, redoInputAction;


    //This is used to prefix the IDs of various objects like cards and relics,
    //to avoid conflicts between different mods using the same name for things.
    public static String makeID(String id) {
        return modID + ":" + id;
    }

    //This will be called by ModTheSpire because of the @SpireInitializer annotation at the top of the class.
    public static void initialize() {
        new UndoButtonMod();
    }

    public UndoButtonMod() {
        controller = new UndoButtonController(logger);
        ui = new UndoButtonUI();
        UndoButtonController.MAX_UNDO = 100;
        BaseMod.subscribe(this); //This will make BaseMod trigger all the subscribers at their appropriate times.
        logger.info("{} subscribed to BaseMod.", modID);
    }

    @Override
    public void receivePostInitialize() {
        //This loads the image used as an icon in the in-game mods menu.
        Texture badgeTexture = TextureLoader.getTexture(imagePath("badge.png"));
        //Set up the mod information displayed in the in-game mods menu.
        //The information used is taken from your pom.xml file.

        //If you want to set up a config panel, that will be done here.
        //The Mod Badges page has a basic example of this, but setting up config is overall a bit complex.
        BaseMod.registerModBadge(badgeTexture, info.Name, GeneralUtils.arrToString(info.Authors), info.Description, null);

        // Initialise the UI
        ui.initialize();

        // Set up the undo and redo input actions
        undoInputAction = new InputAction(Input.Keys.U);
        redoInputAction = new InputAction(Input.Keys.R);
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom room) {
        controller.clearStates();
    }

    @Override
    public void receiveRender(SpriteBatch sb) {
        ui.render(sb);
    }

    @Override
    public void receivePostUpdate() {
        ui.update();
        if(!controller.isSafeToUndo()) {
            return;
        }
        if (undoInputAction.isJustPressed()) {
            controller.undo();
        } else if (redoInputAction.isJustPressed()) {
            controller.redo();
        }
    }

    @Override
    public void receiveCardUsed(AbstractCard card) {
        if (card.isInAutoplay) {
            return;
        }
        controller.addState(new GameState.Action(GameState.ActionType.CARD_PLAYED, card));
        logger.info("Added new state before playing card {}", card.name);
    }

    @Override
    public void receivePrePotionUse(AbstractPotion potion) {
        if(AbstractDungeon.getCurrMapNode() == null) {
            return;
        }
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (room == null || room.phase != AbstractRoom.RoomPhase.COMBAT) {
            return;
        }
        controller.addState(new GameState.Action(GameState.ActionType.POTION_USED, potion));
        logger.info("Added new state before using potion {}", potion.name);
    }

    /*----------Localization----------*/

    //This is used to load the appropriate localization files based on language.
    private static String getLangString()
    {
        return Settings.language.name().toLowerCase();
    }
    private static final String defaultLanguage = "eng";

    @Override
    public void receiveEditStrings() {
        /*
            First, load the default localization.
            Then, if the current language is different, attempt to load localization for that language.
            This results in the default localization being used for anything that might be missing.
            The same process is used to load keywords slightly below.
        */
        loadLocalization(defaultLanguage); //no exception catching for default localization; you better have at least one that works.
        if (!defaultLanguage.equals(getLangString())) {
            try {
                loadLocalization(getLangString());
            }
            catch (GdxRuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadLocalization(String lang) {
        //While this does load every type of localization, most of these files are just outlines so that you can see how they're formatted.
        //Feel free to comment out/delete any that you don't end up using.
        BaseMod.loadCustomStringsFile(UIStrings.class,
                localizationPath(lang, "UIStrings.json"));
        BaseMod.loadCustomStringsFile(TutorialStrings.class,
                localizationPath(lang, "TutorialStrings.json"));
    }

    //These methods are used to generate the correct filepaths to various parts of the resources folder.
    public static String localizationPath(String lang, String file) {
        return resourcesFolder + "/localization/" + lang + "/" + file;
    }

    public static String imagePath(String file) {
        return resourcesFolder + "/images/" + file;
    }

    /**
     * Checks the expected resources path based on the package name.
     */
    private static String checkResourcesPath() {
        String name = UndoButtonMod.class.getName(); //getPackage can be iffy with patching, so class name is used instead.
        int separator = name.indexOf('.');
        if (separator > 0)
            name = name.substring(0, separator);

        FileHandle resources = new LwjglFileHandle(name, Files.FileType.Internal);

        if (!resources.exists()) {
            throw new RuntimeException("\n\tFailed to find resources folder; expected it to be named \"" + name + "\"." +
                    " Either make sure the folder under resources has the same name as your mod's package, or change the line\n" +
                    "\t\"private static final String resourcesFolder = checkResourcesPath();\"\n" +
                    "\tat the top of the " + UndoButtonMod.class.getSimpleName() + " java file.");
        }
        if (!resources.child("images").exists()) {
            throw new RuntimeException("\n\tFailed to find the 'images' folder in the mod's 'resources/" + name + "' folder; Make sure the " +
                    "images folder is in the correct location.");
        }
        if (!resources.child("localization").exists()) {
            throw new RuntimeException("\n\tFailed to find the 'localization' folder in the mod's 'resources/" + name + "' folder; Make sure the " +
                    "localization folder is in the correct location.");
        }

        return name;
    }

    /**
     * This determines the mod's ID based on information stored by ModTheSpire.
     */
    private static void loadModInfo() {
        Optional<ModInfo> infos = Arrays.stream(Loader.MODINFOS).filter((modInfo)->{
            AnnotationDB annotationDB = Patcher.annotationDBMap.get(modInfo.jarURL);
            if (annotationDB == null)
                return false;
            Set<String> initializers = annotationDB.getAnnotationIndex().getOrDefault(SpireInitializer.class.getName(), Collections.emptySet());
            return initializers.contains(UndoButtonMod.class.getName());
        }).findFirst();
        if (infos.isPresent()) {
            info = infos.get();
            modID = info.ID;
        }
        else {
            throw new RuntimeException("Failed to determine mod info/ID based on initializer.");
        }
    }
}
