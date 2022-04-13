package com.victorbrndls.gianttools;

import com.mojang.logging.LogUtils;
import com.victorbrndls.gianttools.item.GiantPickaxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

@Mod(GiantTools.MOD_ID)
public class GiantTools {

    public static final String MOD_ID = "gianttools";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public GiantTools() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::setup);
        ITEMS.register(modEventBus);

        ITEMS.register("stone_pickaxe", () -> new GiantPickaxeItem(Tiers.STONE));
        ITEMS.register("iron_pickaxe", () -> new GiantPickaxeItem(Tiers.IRON));
        ITEMS.register("gold_pickaxe", () -> new GiantPickaxeItem(Tiers.GOLD));
        ITEMS.register("diamond_pickaxe", () -> new GiantPickaxeItem(Tiers.DIAMOND));
        ITEMS.register("netherite_pickaxe", () -> new GiantPickaxeItem(Tiers.NETHERITE));
    }

    private void setup(final FMLCommonSetupEvent event) {

    }

}
