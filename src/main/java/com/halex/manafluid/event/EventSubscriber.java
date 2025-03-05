package com.halex.manafluid.event;

import com.halex.manafluid.handler.ManaPoolFluidHandler;
import com.halex.manafluid.index.FluidRegistry;
import com.halex.manafluid.ModMain;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

@Mod.EventBusSubscriber(modid = ModMain.MODID)
public class EventSubscriber {

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof ManaPoolBlockEntity) {
            ResourceLocation id = new ResourceLocation(ModMain.MODID, "mana_fluid");
            event.addCapability(id, new ICapabilitySerializable<net.minecraft.nbt.CompoundTag>() {

                private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() ->
                        new ManaPoolFluidHandler((ManaPoolBlockEntity) event.getObject()));

                @Override
                public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, net.minecraft.core.Direction side) {
                    if (cap == ForgeCapabilities.FLUID_HANDLER) {
                        return fluidHandler.cast();
                    }
                    return LazyOptional.empty();
                }

                @Override
                public net.minecraft.nbt.CompoundTag serializeNBT() {
                    return new net.minecraft.nbt.CompoundTag();
                }

                @Override
                public void deserializeNBT(net.minecraft.nbt.CompoundTag nbt) {
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();

        BlockEntity tile = level.getBlockEntity(event.getPos());
        if (!(tile instanceof ManaPoolBlockEntity pool)) {
            return;
        }
        Player player = event.getEntity();
        ItemStack held = event.getItemStack();
        BlockPos pos = event.getPos();
        // 1000 mB equals 100,000 mana.
        final int manaDelta = 100000;
        boolean didAction = false;

        if(!level.isClientSide()) {
            if ((held.getItem() == FluidRegistry.MANA_BUCKET.get()) && !player.isCrouching()) {
                if(pool.getMaxMana()-pool.getCurrentMana() >= manaDelta) {
                    pool.receiveMana(manaDelta);
                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    if (!player.isCreative()) {
                        held.shrink(1);
                        if (held.isEmpty()) {
                            player.setItemInHand(event.getHand(), new ItemStack(Items.BUCKET));
                        } else if (!player.getInventory().add(new ItemStack(Items.BUCKET))) {
                            player.drop(new ItemStack(Items.BUCKET), false);
                        }
                    }
                }
                didAction = true;
            }
            else if (held.getItem() == Items.BUCKET) {
                if (pool.getCurrentMana() >= manaDelta) {
                    pool.receiveMana(-manaDelta);
                    level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    if (!player.isCreative()) {
                        held.shrink(1);
                        if (held.isEmpty()) {
                            player.setItemInHand(event.getHand(), new ItemStack(FluidRegistry.MANA_BUCKET.get()));
                        } else if (!player.getInventory().add(new ItemStack(FluidRegistry.MANA_BUCKET.get()))) {
                            player.drop(new ItemStack(FluidRegistry.MANA_BUCKET.get()), false);
                        }
                    }
                    didAction = true;
                }
            }
        }
        else {
            if(held.getItem() == FluidRegistry.MANA_BUCKET.get() && !player.isCrouching()) {
                didAction = true;
            }
            if(held.getItem() == Items.BUCKET) {
                didAction = true;
            }
        }

        if (didAction) {
            player.swing(event.getHand());
            pool.setChanged();
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setUseItem(PlayerInteractEvent.Result.DENY);
            event.setUseBlock(PlayerInteractEvent.Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Level level = event.getLevel();
        if (level.isClientSide()) {
            return;
        }
        Player player = event.getEntity();
        HitResult ray = player.pick(5.0D, 0.0F, false);
        if (ray.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = ((BlockHitResult) ray).getBlockPos();
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof ManaPoolBlockEntity) {
            ItemStack held = event.getItemStack();
            if(held.getItem() == FluidRegistry.MANA_BUCKET.get() && !player.isCrouching()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.CONSUME);
            }
            if(held.getItem() == Items.BUCKET) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.CONSUME);
            }
        }
    }
}
