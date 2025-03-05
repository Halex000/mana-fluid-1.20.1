package com.halex.manafluid.handler;

import com.halex.manafluid.index.FluidRegistry;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FogHandler {

    @SubscribeEvent
    public static void onFogDensity(ViewportEvent.RenderFog event) {
        Camera camera = event.getCamera();
        Level level = event.getCamera().getEntity().level();
        BlockPos cameraPos = camera.getBlockPosition();
        FluidState fluidState = level.getFluidState(cameraPos);

        if (fluidState.getType() == FluidRegistry.MANA_FLUID.get() || fluidState.getType() == FluidRegistry.MANA_FLUID_FLOWING.get()) {
            event.setCanceled(true);
            event.setNearPlaneDistance(1.0f);
            event.setFarPlaneDistance(10.0f);
        }
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Camera camera = event.getCamera();
        Level level = event.getCamera().getEntity().level();
        BlockPos cameraPos = camera.getBlockPosition();
        FluidState fluidState = level.getFluidState(cameraPos);

        if (fluidState.getType() == FluidRegistry.MANA_FLUID.get()) {
            event.setRed(0.21f);
            event.setGreen(0.76f);
            event.setBlue(0.88f);
        }
    }
}
