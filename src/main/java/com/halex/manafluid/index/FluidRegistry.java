package com.halex.manafluid.index;

import com.halex.manafluid.ModMain;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import java.awt.Color;
import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.function.Consumer;

public class FluidRegistry {
    private static Supplier<ForgeFlowingFluid> manaFluidSupplier = () -> {
        throw new IllegalStateException("MANA_FLUID not initialized yet!");
    };
    private static Supplier<ForgeFlowingFluid> manaFluidFlowingSupplier = () -> {
        throw new IllegalStateException("MANA_FLUID_FLOWING not initialized yet!");
    };

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, ModMain.MODID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.Keys.FLUIDS, ModMain.MODID);
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.Keys.BLOCKS, ModMain.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.Keys.ITEMS, ModMain.MODID);

    public static final RegistryObject<FluidType> MANA_TYPE = FLUID_TYPES.register(
            "mana_type",
            () -> new TransparentRenderedPlaceableFluidType(
                    FluidType.Properties.create().density(500).viscosity(1500).canExtinguish(true).sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL).sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY).supportsBoating(true),
                    new ResourceLocation(ModMain.MODID, "block/mana_still"),
                    new ResourceLocation(ModMain.MODID, "block/mana_flow")
            )
    );

    public static final RegistryObject<LiquidBlock> MANA = BLOCKS.register(
            "mana_block",
            () -> new ManaFluidBlock(
                    () -> manaFluidSupplier.get(),
                    BlockBehaviour.Properties.copy(Blocks.WATER)
            )
    );

    public static final RegistryObject<Item> MANA_BUCKET = ITEMS.register(
            "mana_bucket",
            () -> new BucketItem(
                    () -> manaFluidSupplier.get(),
                    new Item.Properties()
                            .stacksTo(1)
                            .craftRemainder(net.minecraft.world.item.Items.BUCKET)
            )
    );

    public static final ForgeFlowingFluid.Properties MANA_PROPERTIES =
            new ForgeFlowingFluid.Properties(
                    MANA_TYPE,
                    () -> manaFluidSupplier.get(),
                    () -> manaFluidFlowingSupplier.get()
            )
                    .slopeFindDistance(5)
                    .levelDecreasePerBlock(1)
                    .tickRate(5)
                    .explosionResistance(100f)
                    .bucket(MANA_BUCKET)
                    .block(MANA);

    public static final RegistryObject<ForgeFlowingFluid> MANA_FLUID = FLUIDS.register(
            "mana",
            () -> {
                ForgeFlowingFluid fluid = new ForgeFlowingFluid.Source(MANA_PROPERTIES);
                manaFluidSupplier = () -> fluid;
                return fluid;
            }
    );

    public static final RegistryObject<ForgeFlowingFluid> MANA_FLUID_FLOWING = FLUIDS.register(
            "mana_flowing",
            () -> {
                ForgeFlowingFluid fluid = new ForgeFlowingFluid.Flowing(MANA_PROPERTIES);
                manaFluidFlowingSupplier = () -> fluid;
                return fluid;
            }
    );

    public static void register() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        FLUID_TYPES.register(bus);
        FLUIDS.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ManaFluidBlock.register();
    }

    public static class ManaFluidBlock extends LiquidBlock {
        public ManaFluidBlock(Supplier<? extends ForgeFlowingFluid> fluidSupplier, BlockBehaviour.Properties properties) {
            super(fluidSupplier, properties);
        }

        @Override
        public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
            super.onPlace(state, level, pos, oldState, isMoving);
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                level.neighborChanged(neighbor, this, pos);
            }
        }

        @Override
        public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
            super.tick(state, level, pos, random);
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                level.neighborChanged(neighbor, this, pos);
            }
        }

        public static void register() {}
    }

    public static abstract class TintedFluidType extends FluidType {
        protected static final int NO_TINT = 0xffffffff;
        private final ResourceLocation stillTexture;
        private final ResourceLocation flowingTexture;

        public TintedFluidType(Properties properties,
                               ResourceLocation stillTexture,
                               ResourceLocation flowingTexture) {
            super(properties);
            this.stillTexture = stillTexture;
            this.flowingTexture = flowingTexture;
        }

        @Override
        public void initializeClient(Consumer<net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
            consumer.accept(new net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return stillTexture;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return flowingTexture;
                }

                @Override
                public int getTintColor(FluidStack stack) {
                    return TintedFluidType.this.getTintColor(stack);
                }

                @Override
                public int getTintColor(FluidState state, net.minecraft.world.level.BlockAndTintGetter getter, BlockPos pos) {
                    return TintedFluidType.this.getTintColor(state, getter, pos);
                }

                @Override
                public @NotNull Vector3f modifyFogColor(
                        Camera camera,
                        float partialTick,
                        ClientLevel level,
                        int renderDistance,
                        float darkenWorldAmount,
                        Vector3f fluidFogColor
                ) {
                    Vector3f custom = TintedFluidType.this.getCustomFogColor();
                    return custom == null ? fluidFogColor : custom;
                }
            });
        }

        protected abstract int getTintColor(FluidStack stack);

        protected abstract int getTintColor(FluidState state, net.minecraft.world.level.BlockAndTintGetter getter, BlockPos pos);

        protected Vector3f getCustomFogColor() {
            return null;
        }
    }

    private static class TransparentRenderedPlaceableFluidType extends TintedFluidType {

        private final Vector3f fogColor;

        public TransparentRenderedPlaceableFluidType(Properties properties,
                                                     ResourceLocation stillTexture,
                                                     ResourceLocation flowingTexture) {
            super(properties, stillTexture, flowingTexture);
            Color c = new Color(0x36C2E0);
            this.fogColor = new Vector3f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
        }

        @Override
        public String getDescriptionId() {
            return "fluid_type.manafluid.mana_fluid";
        }

        @Override
        protected int getTintColor(FluidStack stack) {
            return NO_TINT;
        }

        @Override
        public int getTintColor(FluidState state, net.minecraft.world.level.BlockAndTintGetter world, BlockPos pos) {
            return 0xEE2FD0FF;
        }

        @Override
        protected Vector3f getCustomFogColor() {
            return fogColor;
        }
    }
}