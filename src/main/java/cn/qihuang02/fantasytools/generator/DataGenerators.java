package cn.qihuang02.fantasytools.generator;

import cn.qihuang02.fantasytools.FantasyTools;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void getData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(),
                new FTLangProvider(packOutput)
        );
        generator.addProvider(event.includeServer(),
                new CuriosSlotProvider(
                        packOutput,
                        existingFileHelper,
                        lookupProvider)
        );
        generator.addProvider(event.includeServer(),
                new FTItemTagProvider(
                        packOutput,
                        lookupProvider,
                        generator.addProvider(
                                event.includeServer(),
                                new FTBlockTagProvider(packOutput, lookupProvider, existingFileHelper)
                        ).contentsGetter(),
                        existingFileHelper
                ));
        generator.addProvider(event.includeServer(),
                new FTDatapackProvider(packOutput, lookupProvider));
    }
}
