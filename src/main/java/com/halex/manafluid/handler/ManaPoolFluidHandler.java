package com.halex.manafluid.handler;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import com.halex.manafluid.index.FluidRegistry;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

public class ManaPoolFluidHandler implements IFluidHandler {

    private final ManaPoolBlockEntity pool;
    // The maximum capacity in millibuckets (1 mB = 100 mana).
    private final int capacity;
    // Tracks the “effective” fluid level (in mB), including fractional amounts.
    private double accumulatedMB;

    public ManaPoolFluidHandler(ManaPoolBlockEntity pool) {
        this.pool = pool;
        this.capacity = pool.getMaxMana() / 100;
        // Initialize from the pool’s current mana.
        this.accumulatedMB = pool.getCurrentMana() / 100.0;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if (tank != 0) {
            return FluidStack.EMPTY;
        }
        // Re-sync from the pool in case Botania changed the mana externally.
        resyncFromPool();

        // Only whole mB are reported. If less than 1, show empty so pipes don’t get stuck.
        int wholeMB = (int) Math.floor(accumulatedMB);
        return wholeMB > 0
                ? new FluidStack(FluidRegistry.MANA_FLUID.get(), wholeMB)
                : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return (tank == 0) ? capacity : 0;
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
        // Re-sync from the pool first.
        resyncFromPool();

        // How many mB are being requested?
        int requestedMB = resource.getAmount();

        double oldMB = accumulatedMB;
        // Don’t exceed capacity.
        double newMB = Math.min(oldMB + requestedMB, capacity);

        // Count how many whole mB boundaries we cross.
        int wholeDelta = (int) Math.floor(newMB) - (int) Math.floor(oldMB);

        if (action == FluidAction.EXECUTE) {
            // Only if we cross at least 1 whole mB do we add actual mana to the pool.
            if (wholeDelta > 0) {
                pool.receiveMana(wholeDelta * 100);
                pool.setChanged();  // Mark tile entity as dirty for saving / syncing
            }
            // Update our local accumulator even if no whole mB was added.
            accumulatedMB = newMB;
        }

        // The “amount filled” is how many whole mB were actually committed.
        return wholeDelta;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        // Validate fluid type
        if (resource.isEmpty() || !resource.getFluid().equals(FluidRegistry.MANA_FLUID.get())) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        // Re-sync from the pool first.
        resyncFromPool();

        double oldMB = accumulatedMB;
        // Subtract up to maxDrain, not going below zero.
        double newMB = Math.max(oldMB - maxDrain, 0);
        // How many whole mB do we actually remove?
        int wholeDelta = (int) Math.floor(oldMB) - (int) Math.floor(newMB);

        if (action == FluidAction.EXECUTE) {
            if (wholeDelta > 0) {
                // Remove that many mB from the actual pool (1 mB = 100 mana).
                pool.receiveMana(-wholeDelta * 100);
                pool.setChanged();
            }
            // If leftover is less than 1, forcibly snap to 0 so pipes see it as empty.
            if (newMB < 1) {
                newMB = 0;
            }
            accumulatedMB = newMB;
        }

        // Return the number of whole mB we drained.
        return new FluidStack(FluidRegistry.MANA_FLUID.get(), wholeDelta);
    }

    /**
     * Re-sync our accumulator with the pool’s current mana in case Botania or another mod
     * changed it directly. Prevents the fluid handler from reporting stale data.
     */
    private void resyncFromPool() {
        accumulatedMB = pool.getCurrentMana() / 100.0;
    }
}
