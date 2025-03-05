package com.halex.manafluid.handler;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import com.halex.manafluid.index.FluidRegistry;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

public class ManaPoolFluidHandler implements IFluidHandler {

    private final ManaPoolBlockEntity pool;
    // Capacity in millibuckets: 1 mB = 100 mana.
    private final int capacity;

    public ManaPoolFluidHandler(ManaPoolBlockEntity pool) {
        this.pool = pool;
        this.capacity = pool.getMaxMana() / 100;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if (tank != 0) return FluidStack.EMPTY;
        // Convert mana to mB: pool.getCurrentMana() / 100.
        return new FluidStack(FluidRegistry.MANA_FLUID.get(), pool.getCurrentMana() / 100);
    }

    @Override
    public int getTankCapacity(int tank) {
        if (tank != 0) return 0;
        return capacity;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        if (tank != 0) return false;
        return stack.getFluid().equals(FluidRegistry.MANA_FLUID.get());
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty() || !resource.getFluid().equals(FluidRegistry.MANA_FLUID.get())) {
            return 0;
        }
        int currentMB = pool.getCurrentMana() / 100;
        int available = capacity - currentMB;
        int fillAmount = Math.min(resource.getAmount(), available);
        if (fillAmount > 0 && action.execute()) {
            // Convert mB to mana (1 mB = 100 mana)
            pool.receiveMana(fillAmount * 100);
        }
        return fillAmount;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty() || !resource.getFluid().equals(FluidRegistry.MANA_FLUID.get())) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        int currentMB = pool.getCurrentMana() / 100;
        int drainAmount = Math.min(maxDrain, currentMB);
        if (drainAmount > 0 && action.execute()) {
            pool.receiveMana(-drainAmount * 100);
        }
        return new FluidStack(FluidRegistry.MANA_FLUID.get(), drainAmount);
    }
}
