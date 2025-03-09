package com.halex.manafluid.handler;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

public class ManaPoolFluidHandlerProvider implements ICapabilitySerializable<CompoundTag> {
    private final LazyOptional<IFluidHandler> holder;

    public ManaPoolFluidHandlerProvider(ManaPoolBlockEntity pool) {
        this.holder = LazyOptional.of(() -> new ManaPoolFluidHandler(pool));
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return holder.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
    }

    public void invalidate() {
        holder.invalidate();
    }
}
