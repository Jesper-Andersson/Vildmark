package io.github.jesperandersson.vildmark.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SlantedFenceBlock extends FenceBlock {
    public SlantedFenceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // Unlike a normal fence, only connects to other slanted fences, not to solid blocks or gates
    @Override
    public boolean connectsTo(BlockState state, boolean isSideSolid, Direction direction) {
        return state.is(this);
    }
}
