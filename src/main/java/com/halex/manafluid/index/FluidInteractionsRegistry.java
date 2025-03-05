package com.halex.manafluid.index;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import vazkii.botania.common.block.BotaniaBlocks;


public class FluidInteractionsRegistry {
    public static void register() {
        FluidInteractionRegistry.addInteraction(
                Fluids.LAVA.getFluidType(),
                new FluidInteractionRegistry.InteractionInformation(
                        FluidRegistry.MANA_TYPE.get(),
                        (fluidState) -> fluidState.isSource() ? Blocks.OBSIDIAN.defaultBlockState() : BotaniaBlocks.livingrock.defaultBlockState()
                )
        );
    }
}
