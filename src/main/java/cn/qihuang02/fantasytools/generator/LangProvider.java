package cn.qihuang02.fantasytools.generator;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.FTItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class LangProvider extends LanguageProvider {
    public LangProvider(PackOutput output) {
        super(output, FantasyTools.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        this.add("itemgroup.fantasytools.tab", "FantasyTools");
        this.add("effect.fantasytools.stasis", "Stasis");
        this.add("item.fantasytools.zhongya.bound", "The item has been bound to you.");
        this.add("item.fantasytools.zhongya.activated", "You have activated the Zhongya's Hourglass.");
        this.add("item.fantasytools.zhongya.not_owner", "You are not the owner of the item and cannot use it.");
        this.add(FTItems.ZHONGYAHOURGLASS.get(), "Zhongya's Hourglass");
    }
}
