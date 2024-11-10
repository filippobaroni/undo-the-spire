package undobutton;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.SoulGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import undobutton.cloning.Cloner;

import java.util.Arrays;
import java.util.List;

public class StateCloner {
    public static final List<Class<?>> classesToIgnore = Arrays.asList(
            AbstractDungeon.class,
            CardCrawlGame.class);
    public static Cloner cloner = new Cloner();


    public static void initialise() {
        classesToIgnore.forEach(clz -> cloner.addIgnoredClass(clz));
        cloner.addCustomCloner(AbstractPlayer.class::isAssignableFrom, (o, cloner, clones) -> o);
        cloner.addCustomCloner(AbstractCard.class::isAssignableFrom, (o, cloner, clones) -> {
            AbstractCard c = (AbstractCard) o;
            if (clones.containsKey(c)) {
                return clones.get(c);
            }
            cloner.logCloningClass(c.getClass());
            AbstractCard result = c.makeSameInstanceOf();
            clones.put(c, result);
            result.target_x = c.target_x;
            result.target_y = c.target_y;
            result.current_x = c.target_x;
            result.current_y = c.target_y;
            result.retain = c.retain;
            result.selfRetain = c.selfRetain;
            result.block = c.block;
            result.damage = c.damage;
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
            result.ignoreEnergyOnUse = c.ignoreEnergyOnUse;
            result.dontTriggerOnUseCard = c.dontTriggerOnUseCard;

            return result;
        });
        cloner.addCustomCloner(Texture.class, (o, cloner, clones) -> {
            Texture t = (Texture) o;
            if (t.getTextureData() == null) {
                return new Texture(t.getWidth(), t.getHeight(), null);
            }
            return new Texture(t.getTextureData());
        });
        cloner.addCustomCloner(TextureAtlas.AtlasRegion.class, (o, cloner, clones) -> {
            TextureAtlas.AtlasRegion r = (TextureAtlas.AtlasRegion) o;
            return new TextureAtlas.AtlasRegion(r);
        });
        cloner.addCustomCloner(Skeleton.class, (o, cloner, clones) -> {
            Skeleton s = (Skeleton) o;
            return new Skeleton(s);
        });
        cloner.addCustomCloner(Hitbox.class, (o, cloner, clones) -> {
            Hitbox h = (Hitbox) o;
            Hitbox hb = new Hitbox(h.x, h.y, h.width, h.height);
            hb.hovered = h.hovered;
            hb.justHovered = h.justHovered;
            hb.clickStarted = h.clickStarted;
            hb.clicked = h.clicked;
            return hb;
        });
        cloner.addCustomCloner(Color.class, (o, cloner, clones) -> {
            Color c = (Color) o;
            return c.cpy();
        });
        cloner.addIgnoredClass(AnimationStateData.class);
        cloner.addCustomCloner(AnimationState.class, (o, cloner, clones) -> {
            AnimationState as = (AnimationState) o;
            AnimationState result = new AnimationState(as.getData());
            AnimationState.TrackEntry current = as.getCurrent(0);
            AnimationState.TrackEntry e = result.setAnimation(0, current.getAnimation().getName(), current.getLoop());
            result.getData().setDefaultMix(as.getData().getDefaultMix());
            e.setTimeScale(as.getTimeScale());
            return result;
        });
        cloner.addIgnoredClass(SoulGroup.class);
        // cloner.setLogCloning(UndoButtonMod.DEBUG);
        cloner.setLogCloning(false);
    }

    public static <T> T clone(T o) {
        return cloner.clone(o);
    }
}
