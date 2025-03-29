package cn.qihuang02.fantasytools.generator;

import cn.qihuang02.fantasytools.FantasyTools;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.concurrent.CompletableFuture;

public class CuriosSlotProvider extends CuriosDataProvider {
    public CuriosSlotProvider(PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<HolderLookup.Provider> registries) {
        super(FantasyTools.MODID, output, fileHelper, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
        this
                .createSlot("hourglass")
                .size(1)
                .operation("SET")
                .order(999)
                .icon(ResourceLocation.fromNamespaceAndPath("curios", "empty_curios_slot"))
                .addCosmetic(false)
                .dropRule(ICurio.DropRule.ALWAYS_KEEP);
        this
                .createEntities("hourglass")
                .replace(false)
                .addPlayer()
                .addSlots("hourglass");
    }
}
