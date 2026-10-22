package io.github.jesperandersson.vildmark.world.level.block.plants.tree.grower;

import io.github.jesperandersson.vildmark.Vildmark;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

/** What a pine sapling grows into. */
public class PineTreeGrower {
    // No 2x2 mega tree and no flowering variant, just the normal pine
    public static final TreeGrower PINE = new TreeGrower(
            "vildmark_pine",
            /* megaTree= */ Optional.empty(),
            /* tree= */ Optional.of(Vildmark.PINE_CONFIGURED),
            /* flowers= */ Optional.empty()
    );
}
