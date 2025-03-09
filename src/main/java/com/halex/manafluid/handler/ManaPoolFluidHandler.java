package com.halex.manafluid.handler;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import com.halex.manafluid.index.FluidRegistry;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

public class ManaPoolFluidHandler implements IFluidHandler {

    private final ManaPoolBlockEntity pool;
    private double accumulatedMB;

    public ManaPoolFluidHandler(ManaPoolBlockEntity pool) {
        this.pool = pool;
        this.accumulatedMB = pool.getCurrentMana() / 100.0;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if (tank != 0) {
            return new FluidStack(FluidRegistry.MANA_FLUID.get(), 0);
        }
        resyncFromPool();

        int wholeMB = (int) Math.floor(accumulatedMB);
        return new FluidStack(FluidRegistry.MANA_FLUID.get(), wholeMB);
    }

    @Override
    public int getTankCapacity(int tank) {
        return (tank == 0) ? pool.getMaxMana() / 100 : 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        if (tank != 0) return false;
        return stack.getFluid().equals(FluidRegistry.MANA_FLUID.get());
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !resource.getFluid().equals(FluidRegistry.MANA_FLUID.get())) {
            return 0;
        }
        resyncFromPool();

        int requestedMB = resource.getAmount();
        double oldMB = accumulatedMB;
        double capacity = pool.getMaxMana() / 100.0;
        double newMB = Math.min(oldMB + requestedMB, capacity);

        int wholeDelta = (int) Math.floor(newMB) - (int) Math.floor(oldMB);

        if (action == FluidAction.EXECUTE) {
            if (wholeDelta > 0) {
                pool.receiveMana(wholeDelta * 100);
                pool.setChanged();
            }
            accumulatedMB = newMB;
        }
        return wholeDelta;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !resource.getFluid().equals(FluidRegistry.MANA_FLUID.get())) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        resyncFromPool();

        double oldMB = accumulatedMB;
        double newMB = Math.max(oldMB - maxDrain, 0);
        int wholeDelta = (int) Math.floor(oldMB) - (int) Math.floor(newMB);

        if (action == FluidAction.EXECUTE) {
            if (wholeDelta > 0) {
                pool.receiveMana(-wholeDelta * 100);
                pool.setChanged();
            }
            if (newMB < 1) {
                newMB = 0;
            }
            accumulatedMB = newMB;
        }

        return new FluidStack(FluidRegistry.MANA_FLUID.get(), wholeDelta);
    }

    private void resyncFromPool() {
        accumulatedMB = pool.getCurrentMana() / 100.0;
    }
}
