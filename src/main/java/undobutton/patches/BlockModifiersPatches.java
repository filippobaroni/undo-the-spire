package undobutton.patches;

import com.evacipated.cardcrawl.mod.stslib.blockmods.BlockInstance;
import com.evacipated.cardcrawl.mod.stslib.blockmods.BlockModifierManager;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import savestate.CreatureState;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class BlockModifiersPatches {

    @SpirePatch(clz = CreatureState.class, method = SpirePatch.CLASS)
    public static class CreatureStateExtraFields {
        public static SpireField<ArrayList<BlockInstanceState>> blockInstances = new SpireField<>(ArrayList::new);
    }

    @SpirePatch(clz = CreatureState.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractCreature.class})
    public static class CreatureStateConstructorPatch {
        @SpirePostfixPatch
        public static void saveBlockInstances(CreatureState __instance, AbstractCreature creature) {
            CreatureStateExtraFields.blockInstances.set(__instance, BlockModifierManager.blockInstances(creature).stream().map(BlockInstanceState::new).collect(Collectors.toCollection(ArrayList::new)));
        }
    }

    @SpirePatch(clz = CreatureState.class, method = "loadCreature")
    public static class CreatureStateLoadPatch {
        @SpirePostfixPatch
        public static void loadBlockInstances(CreatureState __instance, AbstractCreature creature) {
            BlockModifierManager.removeAllBlockInstances(creature);
            CreatureStateExtraFields.blockInstances.get(__instance).forEach(state -> BlockModifierManager.addBlockInstance(creature, state.loadBlockInstance(creature)));
        }
    }

    public static class BlockInstanceState {
        public final int blockAmount;

        public BlockInstanceState(BlockInstance blockInstance) {
            this.blockAmount = blockInstance.getBlockAmount();
            if (!blockInstance.getBlockTypes().isEmpty()) {
                throw new IllegalArgumentException("Unsupported block instance with block types");
            }
        }

        public BlockInstance loadBlockInstance(AbstractCreature owner) {
            return new BlockInstance(owner, this.blockAmount, new ArrayList<>());
        }
    }
}
