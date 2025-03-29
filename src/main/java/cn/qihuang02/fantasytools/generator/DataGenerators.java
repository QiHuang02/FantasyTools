package cn.qihuang02.fantasytools.generator;

import cn.qihuang02.fantasytools.FantasyTools;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void getData(@NotNull GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(),
                new LangProvider(packOutput)
        );
        generator.addProvider(event.includeServer(),
                new CuriosSlotProvider(
                        packOutput,
                        existingFileHelper,
                        lookupProvider)
        );
        generator.addProvider(event.includeServer(),
                new ItemTagProvider(
                        packOutput,
                        lookupProvider,
                        generator.addProvider(
                                event.includeServer(),
                                new BlockTagProvider(packOutput, lookupProvider, existingFileHelper)
                        ).contentsGetter(),
                        existingFileHelper
                ));
    }
}
