package com.github.Jesper_Andersson.nordic.world.level.block.plants.tree.grower;

import com.github.Jesper_Andersson.nordic.Nordic;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public class PineTreeGrower {
    public static final TreeGrower PINE = new TreeGrower(
            "nordic_pine",
            Optional.empty(),
            Optional.of(Nordic.PINE_CONFIGURED),
            Optional.empty()
    );
}
