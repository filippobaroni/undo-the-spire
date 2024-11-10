package undobutton;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.esotericsoftware.spine.Skeleton;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.rits.cloning.*;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class StateCloner {
    public static final List<Class<?>> classesToIgnore = Arrays.asList(
            AbstractPlayer.class,
            AbstractDungeon.class,
            CardCrawlGame.class,
            AbstractRoom.class);
    public static Cloner cloner = new Cloner();


    public static void initialise() {
        ClassPool.getDefault().appendClassPath(new LoaderClassPath(Cloner.class.getClassLoader()));
        patchCloner();
        cloner.registerCloningStrategy((o, field) -> {
            if (classesToIgnore.stream().anyMatch(c -> c.isAssignableFrom(field.getType()))) {
                UndoButtonMod.logger.info("Ignoring {} in field {} of class {}", field.getType().getName(), field.getName(), o.getClass().getName());
                return ICloningStrategy.Strategy.SAME_INSTANCE_INSTEAD_OF_CLONE;
            }
            return ICloningStrategy.Strategy.IGNORE;
        });
        cloner.registerFastCloner(AbstractCard.class, (o, cloner, clones) -> {
            AbstractCard c = (AbstractCard) o;
            if (c == null) {
                return null;
            }
            AbstractCard result = c.makeSameInstanceOf();
            result.target_x = c.target_x;
            result.target_y = c.target_y;
            result.current_x = c.target_x;
            result.current_y = c.target_y;
            result.cost = c.cost;
            result.costForTurn = c.costForTurn;
            result.isCostModified = c.isCostModified;
            result.isCostModifiedForTurn = c.isCostModifiedForTurn;
            result.retain = c.retain;
            result.selfRetain = c.selfRetain;
            result.upgraded = c.upgraded;
            result.timesUpgraded = c.timesUpgraded;
            result.baseBlock = c.baseBlock;
            result.block = c.block;
            result.baseDamage = c.baseDamage;
            result.damage = c.damage;
            result.baseMagicNumber = c.baseMagicNumber;
            result.magicNumber = c.magicNumber;
            result.baseHeal = c.baseHeal;
            result.heal = c.heal;
            result.baseDraw = c.baseDraw;
            result.draw = c.draw;
            result.baseDiscard = c.baseDiscard;
            result.discard = c.discard;
            result.purgeOnUse = c.purgeOnUse;
            result.exhaustOnUseOnce = c.exhaustOnUseOnce;
            result.exhaust = c.exhaust;
            result.isEthereal = c.isEthereal;
            result.isInnate = c.isInnate;
            result.shuffleBackIntoDrawPile = c.shuffleBackIntoDrawPile;
            result.freeToPlayOnce = c.freeToPlayOnce;
            result.misc = c.misc;
            result.ignoreEnergyOnUse = c.ignoreEnergyOnUse;
            result.dontTriggerOnUseCard = c.dontTriggerOnUseCard;
            result.inBottleFlame = c.inBottleFlame;
            result.inBottleLightning = c.inBottleLightning;
            result.inBottleTornado = c.inBottleTornado;

            return result;
        });
        cloner.registerFastCloner(Texture.class, (o, cloner, clones) -> {
            Texture t = (Texture) o;
            if (t == null) {
                return null;
            }
            if (t.getTextureData() == null) {
                return new Texture(t.getWidth(), t.getHeight(), null);
            }
            return new Texture(t.getTextureData());
        });
        cloner.registerFastCloner(TextureAtlas.AtlasRegion.class, (o, cloner, clones) -> {
            TextureAtlas.AtlasRegion r = (TextureAtlas.AtlasRegion) o;
            if (r == null) {
                return null;
            }
            return new TextureAtlas.AtlasRegion(r);
        });
        cloner.registerFastCloner(Skeleton.class, (o, cloner, clones) -> {
            Skeleton s = (Skeleton) o;
            if (s == null) {
                return null;
            }
            Skeleton cloned = new Skeleton(s);
            if (clones != null) {
                clones.put(s.getData(), cloned.getData());
            }
            return cloned;
        });
        cloner.registerFastCloner(Hitbox.class, (o, cloner, clones) -> {
            Hitbox h = (Hitbox) o;
            if (h == null) {
                return null;
            }
            Hitbox hb = new Hitbox(h.x, h.y, h.width, h.height);
            hb.hovered = h.hovered;
            hb.justHovered = h.justHovered;
            hb.clickStarted = h.clickStarted;
            hb.clicked = h.clicked;
            return hb;
        });
        cloner.registerFastCloner(Color.class, (o, cloner, clones) -> {
            Color c = (Color) o;
            if (c == null) {
                return null;
            }
            return c.cpy();
        });
        cloner.setDumpClonedClasses(UndoButtonMod.DEBUG);
    }

    public static <T> T deepClone(T o) {
        return cloner.deepClone(o);
    }

    public static void cloneAllFieldsTo(Class<?> clazz, Object from, Object to, Map<Object, Object> clones) {
        IDumpCloned dumpCloned = ReflectionHacks.getPrivate(cloner, Cloner.class, "dumpCloned");
        IDeepCloner classCloner = getClassCloner(clazz);
        int numFields = ReflectionHacks.getPrivate(classCloner, classCloner.getClass(), "numFields");
        Field[] fields = ReflectionHacks.getPrivate(classCloner, classCloner.getClass(), "fields");
        boolean[] shouldClone = ReflectionHacks.getPrivate(classCloner, classCloner.getClass(), "shouldClone");
        if (dumpCloned != null) {
            dumpCloned.startCloning(from.getClass());
        }
        try {
            for (int i = 0; i < numFields; i++) {
                Field field = fields[i];
                Object fieldObject = field.get(from);
                Object fieldObjectClone = shouldClone[i] ? ReflectionHacks.privateMethod(cloner.getClass(), "applyCloningStrategy", Map.class, Object.class, Object.class, Field.class).invoke(cloner, clones, from, fieldObject, field) : fieldObject;
                field.set(to, fieldObjectClone);
                if (dumpCloned != null && fieldObjectClone != fieldObject) {
                    dumpCloned.cloning(field, from.getClass());
                }
            }
        } catch (IllegalAccessException e) {
            throw new CloningException(e);
        }
    }

    public static void moveAllFieldsTo(Class<?> clazz, Object from, Object to) {
        IDeepCloner classCloner = getClassCloner(clazz);
        int numFields = ReflectionHacks.getPrivate(classCloner, classCloner.getClass(), "numFields");
        Field[] fields = ReflectionHacks.getPrivate(classCloner, classCloner.getClass(), "fields");
        try {
            for (int i = 0; i < numFields; i++) {
                Field field = fields[i];
                Object fieldObject = field.get(from);
                field.set(to, fieldObject);
            }
        } catch (IllegalAccessException e) {
            throw new CloningException(e);
        }
    }

    public static IDeepCloner getClassCloner(Class<?> clazz) {
        Map<Class, IDeepCloner> cloners = ReflectionHacks.getPrivate(cloner, Cloner.class, "cloners");
        IDeepCloner classCloner = cloners.get(clazz);
        if (classCloner == null) {
            classCloner = ReflectionHacks.privateMethod(Cloner.class, "findDeepCloner", Class.class).invoke(cloner, clazz);
            cloners.put(clazz, classCloner);
        }
        return classCloner;
    }

    private static void patchCloner() {
        try {
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClonerClass = classPool.get(Cloner.class.getName());
            // Patch Cloner's deepClone to use GameState clones
            CtMethod deepCloneMethod = ctClonerClass.getDeclaredMethod("deepClone");
            deepCloneMethod.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("cloneInternal")) {
                        String gameStateCurrentlyCreating = GameState.class.getName() + ".currentlyCreating";
                        m.replace("if (this == " + StateCloner.class.getName() + ".cloner && " + gameStateCurrentlyCreating + " != null) {" +
                                "$_ = $proceed($1, " + gameStateCurrentlyCreating + ".objects);" +
                                "} else {" +
                                "$_ = $proceed($$);" +
                                "}");
                    }
                }
            });
        } catch (NotFoundException | CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

//    @SpirePatch(clz = Cloner.class, method = SpirePatch.CLASS)
//    public static class ClonerExtraFields {
//        public static SpireField<Boolean> isUndoTheSpireClone = new SpireField<>(() -> false);
//    }
//    @SpirePatch(clz = Cloner.class, method = "deepClone")
//    public static class DeepClonePatch {
//        @SpireInsertPatch(locator = Locator.class)
//        public static <T> void useGameStateClones(Cloner __instance, T o, @ByRef Map<Object, Object>[] clones) {
//            if (ClonerExtraFields.isUndoTheSpireClone.get(__instance)) {
//                if (GameState.currentlyCreating != null) {
//                    clones[0] = GameState.currentlyCreating.objects;
//                }
//            }
//        }
//
//        public static class Locator extends SpireInsertLocator {
//            @Override
//            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
//                Matcher finalMatcher = new Matcher.FieldAccessMatcher(Cloner.class, "cloneInternal");
//                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
//            }
//        }
//    }
}
