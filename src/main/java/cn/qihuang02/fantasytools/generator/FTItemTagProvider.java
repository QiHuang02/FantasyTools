package cn.qihuang02.fantasytools.generator;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.FTItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class FTItemTagProvider extends ItemTagsProvider {

    private static final TagKey<Item> CURIOS_HOURGLASS =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "hourglass"));
    private static final TagKey<Item> CURIOS_HEAD =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "head"));

    private static final TagKey<Item> INGOTS =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots"));
    private static final TagKey<Item> GEMS =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems"));

    private static final TagKey<Item> FULL_METAL_1 =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/full_metal"));
    private static final TagKey<Item> FULL_METAL_2 =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "gems/full_metal"));

    public FTItemTagProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagLookup<Block>> blockTags,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, FantasyTools.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this
                .tag(CURIOS_HOURGLASS)
                .add(FTItems.ZHONGYA_HOURGLASS.get());
        this
                .tag(CURIOS_HEAD)
                .add(FTItems.BAMBOO_COPTER.get());
        this
                .tag(FULL_METAL_1)
                .add(FTItems.FULL_METAL.get());
        this
                .tag(FULL_METAL_2)
                .add(FTItems.FULL_METAL.get());
        this
                .tag(INGOTS)
                .add(FTItems.FULL_METAL.get());
        this
                .tag(GEMS)
                .add(FTItems.FULL_METAL.get());
    }
}
