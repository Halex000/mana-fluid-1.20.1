package com.halex.manafluid.handler;

import com.halex.manafluid.ModMain;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.common.block.mana.ManaPoolBlock;

import java.util.List;

@Mod.EventBusSubscriber(modid = ModMain.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ManaPoolBlock) {
            List<Component> tooltip = event.getToolTip();
            Component line = Component.translatable("tooltip.manafluid.mana_pool")
                    .withStyle(ChatFormatting.GRAY);

            int index = tooltip.size();
            if (event.getFlags().isAdvanced()) {
                String id = String.valueOf(ForgeRegistries.ITEMS.getKey(blockItem));
                for (int i = tooltip.size() - 1; i >= 0; i--) {
                    if (tooltip.get(i).getString().equals(id)) {
                        index = i;
                        break;
                    }
                }
            }
            tooltip.add(index, line);
        }
    }
}
