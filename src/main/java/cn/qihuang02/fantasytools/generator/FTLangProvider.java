package cn.qihuang02.fantasytools.generator;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.FTItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class FTLangProvider extends LanguageProvider {
    public FTLangProvider(PackOutput output) {
        super(output, FantasyTools.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        this.add("item.fantasytools.tab", "FantasyTools");
        this.add("effect.fantasytools.stasis", "Stasis");
        this.add("item.fantasytools.zhongya.not_owner", "You are not the owner of the item and cannot use it.");
        this.add("item.fantasytools.zhongya.no_owner", "Owner: Null");
        this.add("item.fantasytools.zhongya.owner", "Owner: %s");
        this.add("item.fantasytools.inv_cloak.no_owner", "No owner");
        this.add("item.fantasytools.inv_cloak.owner", "Owner: %s");
        this.add("enchantment.fantasytools.pierce", "Pierce");
        this.add(FTItems.DEMIGUISE_FUR.get(), "Demiguise fur");
        this.add(FTItems.ZHONGYAHOURGLASS.get(), "Zhongya's Hourglass");
        this.add(FTItems.INVIS_CLOAK.get(), "Invisibility Cloak");
    }
}
