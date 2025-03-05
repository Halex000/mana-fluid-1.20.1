package com.halex.manafluid;

import com.halex.manafluid.handler.FogHandler;
import com.halex.manafluid.index.BlockRegistry;
import com.halex.manafluid.index.FluidInteractionsRegistry;
import com.halex.manafluid.index.FluidRegistry;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModMain.MODID)
public class ModMain {

  public static final String MODID = "manafluid";
  public static final Logger LOGGER = LogManager.getLogger();

  public ModMain() {
    IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

    BlockRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    FluidRegistry.register();

    eventBus.addListener(this::addCreative);

    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
  }

  private void setup(final FMLCommonSetupEvent event) {
    event.enqueueWork(FluidInteractionsRegistry::register);
  }

  private void addCreative(BuildCreativeModeTabContentsEvent event) {
    if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
      event.accept(FluidRegistry.MANA_BUCKET);
    }
  }

  private void setupClient(final FMLClientSetupEvent event) {
    ItemBlockRenderTypes.setRenderLayer(FluidRegistry.MANA_FLUID.get(), RenderType.translucent());
    ItemBlockRenderTypes.setRenderLayer(FluidRegistry.MANA_FLUID_FLOWING.get(), RenderType.translucent());
    MinecraftForge.EVENT_BUS.register(FogHandler.class);

  }
}
