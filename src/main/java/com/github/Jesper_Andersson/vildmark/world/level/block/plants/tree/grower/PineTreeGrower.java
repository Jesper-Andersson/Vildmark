package com.github.Jesper_Andersson.vildmark.world.level.block.plants.tree.grower;

import com.github.Jesper_Andersson.vildmark.Vildmark;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public class PineTreeGrower {
    public static final TreeGrower PINE = new TreeGrower(
            "vildmark_pine",
            Optional.empty(),
            Optional.of(Vildmark.PINE_CONFIGURED),
            Optional.empty()
    );
}
