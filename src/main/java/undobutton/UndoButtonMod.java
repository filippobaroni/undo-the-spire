package undobutton;

import basemod.BaseMod;
import basemod.DevConsole;
import basemod.ReflectionHacks;
import basemod.interfaces.*;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.Patcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.evacipated.cardcrawl.modthespire.lib.SpireSideload;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.input.InputAction;
import com.megacrit.cardcrawl.localization.TutorialStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import javassist.CannotCompileException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scannotation.AnnotationDB;
import savestate.CardState;
import savestate.relics.RelicState;
import undobutton.util.GeneralUtils;
import undobutton.util.MakeUndoable;
import undobutton.util.TextureLoader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.reflect.Modifier.isStatic;

@SpireInitializer
@SpireSideload(modIDs = {"SaveStateMod"})
public class UndoButtonMod implements EditStringsSubscriber, PostInitializeSubscriber, OnStartBattleSubscriber, RenderSubscriber, PostUpdateSubscriber, PrePotionUseSubscriber {
    public static final boolean DEBUG = false;

    private static final String resourcesFolder = checkResourcesPath();
    private static final String defaultLanguage = "eng";
    public static ModInfo info;
    public static String modID;
    public static final Logger logger = LogManager.getLogger(modID); //Used to output to the console.
    public static UndoButtonController controller;
    public static UndoButtonUI ui;
    public static InputAction undoInputAction, redoInputAction;
    private static SpireConfig optionsConfig;

    static {
        loadModInfo();
    }

    //This is used to prefix the IDs of various objects like cards and relics,
    //to avoid conflicts between different mods using the same name for things.

    public UndoButtonMod() {
        Properties defaults = new Properties();
        defaults.setProperty("maxStates", "100");
        try {
            optionsConfig = new SpireConfig(modID, "config", defaults);
        } catch (IOException e) {
            e.printStackTrace();
        }
        controller = new UndoButtonController();
        ui = new UndoButtonUI();
        BaseMod.subscribe(this); //This will make BaseMod trigger all the subscribers at their appropriate times.
        logger.info("{} subscribed to BaseMod.", modID);
        // Find all @MakeUndoable classes
        ArrayList<Class<?>> handlers = findAllClassesWithAnnotation(MakeUndoable.class.getName()).stream().map(name -> {
            try {
                return (Class<?>) Loader.getClassPool().getOrNull(name).toClass();
            } catch (CannotCompileException e) {
                // This should never happen.
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toCollection(ArrayList::new));
        GameState.extraStateHandlers = new Class<?>[handlers.size()];
        GameState.extraStateTypes = new Class<?>[handlers.size()];
        for (int i = 0; i < handlers.size(); i++) {
            Class<?> h = handlers.get(i);
            GameState.extraStateTypes[i] = h.getAnnotation(MakeUndoable.class).statetype();
            try {
                if (!isStatic(h.getMethod("save").getModifiers())) {
                    throw new RuntimeException("Method save in class " + h.getName() + " must be static");
                }
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Class " + h.getName() + " does not have a save method");
            }
            try {
                if (!isStatic(h.getMethod("load", GameState.extraStateTypes[i]).getModifiers())) {
                    throw new RuntimeException("Method load in class " + h.getName() + " must be static");
                }
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Class " + h.getName() + " does not have a load method");
            }
            GameState.extraStateHandlers[i] = h;
        }
    }

    public static int getMaxStates() {
        return optionsConfig.getInt("maxStates");
    }

    public static void setMaxStates(int numStates) {
        optionsConfig.setInt("maxStates", numStates);
        try {
            optionsConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String makeID(String id) {
        return modID + ":" + id;
    }

    //This will be called by ModTheSpire because of the @SpireInitializer annotation at the top of the class.
    public static void initialize() {
        new UndoButtonMod();
    }

    //This is used to load the appropriate localization files based on language.
    private static String getLangString() {
        return Settings.language.name().toLowerCase();
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
        if (separator > 0) name = name.substring(0, separator);

        FileHandle resources = new LwjglFileHandle(name, Files.FileType.Internal);

        if (!resources.exists()) {
            throw new RuntimeException("\n\tFailed to find resources folder; expected it to be named \"" + name + "\"." + " Either make sure the folder under resources has the same name as your mod's package, or change the line\n" + "\t\"private static final String resourcesFolder = checkResourcesPath();\"\n" + "\tat the top of the " + UndoButtonMod.class.getSimpleName() + " java file.");
        }
        if (!resources.child("images").exists()) {
            throw new RuntimeException("\n\tFailed to find the 'images' folder in the mod's 'resources/" + name + "' folder; Make sure the " + "images folder is in the correct location.");
        }
        if (!resources.child("localization").exists()) {
            throw new RuntimeException("\n\tFailed to find the 'localization' folder in the mod's 'resources/" + name + "' folder; Make sure the " + "localization folder is in the correct location.");
        }

        return name;
    }

    static private List<String> findAllClassesWithAnnotation(String annotationName) {
        return Patcher.annotationDBMap.values().stream().map(db -> db.getAnnotationIndex().getOrDefault(annotationName, Collections.emptySet())).flatMap(Set::stream).collect(Collectors.toList());
    }


    @Override
    public void receivePostInitialize() {
        //This loads the image used as an icon in the in-game mods menu.
        Texture badgeTexture = TextureLoader.getTexture(imagePath("badge.png"));
        //Set up the mod information displayed in the in-game mods menu.
        //The information used is taken from your pom.xml file.

        //If you want to set up a config panel, that will be done here.
        //The Mod Badges page has a basic example of this, but setting up config is overall a bit complex.
        BaseMod.registerModBadge(badgeTexture, info.Name, GeneralUtils.arrToString(info.Authors), info.Description, new UndoButtonSettingsPanel());

        // Initialise the UI
        ui.initialize();

        // Set up the undo and redo input actions
        undoInputAction = new InputAction(Input.Keys.U);
        redoInputAction = new InputAction(Input.Keys.R);
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom room) {
        controller.onStartBattle(room);
        CardState.resetFreeCards();
        RelicState.resetFreeRelics();
        ui.onStartBattle(room);
    }

    @Override
    public void receiveRender(SpriteBatch sb) {
        if (UndoButtonUI.isVisibleAboveScreen) {
            ui.render(sb);
        }
    }

    @Override
    public void receivePostUpdate() {
        ui.update();
        if (!controller.isSafeToUndo() || DevConsole.visible) {
            return;
        }
        if (undoInputAction.isJustPressed()) {
            controller.undo();
        } else if (redoInputAction.isJustPressed()) {
            controller.redo();
        }
    }

    @Override
    public void receivePrePotionUse(AbstractPotion potion) {
        if (AbstractDungeon.getCurrMapNode() == null) {
            return;
        }
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (room == null || room.phase != AbstractRoom.RoomPhase.COMBAT) {
            return;
        }
        controller.addState(new GameState.Action(GameState.ActionType.POTION_USED, potion));
        logger.info("Added new state before using potion {}", potion.name);
        if (AbstractDungeon.getMonsters().getMonsterNames().contains("SpireShield")) {
            AbstractMonster m = ReflectionHacks.getPrivate(AbstractDungeon.topPanel.potionUi, PotionPopUp.class, "hoveredMonster");
            if (m != null) {
                UndoButtonMod.controller.isPlayerFlippedHorizontally = m.drawX < AbstractDungeon.player.drawX;
            }
        }
    }

    /*----------Localization----------*/

    /**
     * This determines the mod's ID based on information stored by ModTheSpire.
     */
    private static void loadModInfo() {
        Optional<ModInfo> infos = Arrays.stream(Loader.MODINFOS).filter((modInfo) -> {
            AnnotationDB annotationDB = Patcher.annotationDBMap.get(modInfo.jarURL);
            if (annotationDB == null) return false;
            Set<String> initializers = annotationDB.getAnnotationIndex().getOrDefault(SpireInitializer.class.getName(), Collections.emptySet());
            return initializers.contains(UndoButtonMod.class.getName());
        }).findFirst();
        if (infos.isPresent()) {
            info = infos.get();
            modID = info.ID;
        } else {
            throw new RuntimeException("Failed to determine mod info/ID based on initializer.");
        }
    }

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
            } catch (GdxRuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadLocalization(String lang) {
        //While this does load every type of localization, most of these files are just outlines so that you can see how they're formatted.
        //Feel free to comment out/delete any that you don't end up using.
        BaseMod.loadCustomStringsFile(UIStrings.class, localizationPath(lang, "UIStrings.json"));
        BaseMod.loadCustomStringsFile(TutorialStrings.class, localizationPath(lang, "TutorialStrings.json"));
    }
}
