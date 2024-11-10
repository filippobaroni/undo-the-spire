package undobutton;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.random.Random;

public class RngState {
    public final Random monsterRng;
    public final Random mapRng;
    public final Random eventRng;
    public final Random merchantRng;
    public final Random cardRng;
    public final Random treasureRng;
    public final Random relicRng;
    public final Random potionRng;
    public final Random monsterHpRng;
    public final Random aiRng;
    public final Random shuffleRng;
    public final Random cardRandomRng;
    public final Random miscRng;

    public RngState() {
        monsterRng = AbstractDungeon.monsterRng.copy();
        mapRng = AbstractDungeon.mapRng.copy();
        eventRng = AbstractDungeon.eventRng.copy();
        merchantRng = AbstractDungeon.merchantRng.copy();
        cardRng = AbstractDungeon.cardRng.copy();
        treasureRng = AbstractDungeon.treasureRng.copy();
        relicRng = AbstractDungeon.relicRng.copy();
        potionRng = AbstractDungeon.potionRng.copy();
        monsterHpRng = AbstractDungeon.monsterHpRng.copy();
        aiRng = AbstractDungeon.aiRng.copy();
        shuffleRng = AbstractDungeon.shuffleRng.copy();
        cardRandomRng = AbstractDungeon.cardRandomRng.copy();
        miscRng = AbstractDungeon.miscRng.copy();
    }

    public void load() {
        AbstractDungeon.monsterRng = monsterRng;
        AbstractDungeon.mapRng = mapRng;
        AbstractDungeon.eventRng = eventRng;
        AbstractDungeon.merchantRng = merchantRng;
        AbstractDungeon.cardRng = cardRng;
        AbstractDungeon.treasureRng = treasureRng;
        AbstractDungeon.relicRng = relicRng;
        AbstractDungeon.potionRng = potionRng;
        AbstractDungeon.monsterHpRng = monsterHpRng;
        AbstractDungeon.aiRng = aiRng;
        AbstractDungeon.shuffleRng = shuffleRng;
        AbstractDungeon.cardRandomRng = cardRandomRng;
        AbstractDungeon.miscRng = miscRng;
    }
}
