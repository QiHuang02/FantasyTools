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
        this.add("item.fantasytools.zhongya.owner", "Owner: %s");
        this.add("item.fantasytools.zhongya.key", "Press [ %s ] to activate Zhongya's Hourglass");

        this.add("item.fantasytools.inv_cloak.no_owner", "No owner");
        this.add("item.fantasytools.inv_cloak.owner", "Owner: %s");

        this.add("enchantment.fantasytools.pierce", "Pierce");

        this.add("tag.item.curios.hourglass", "Hourglass");
        this.add("tag.item.curios.head", "Head");
        this.add("tag.item.c.ingots.full_metal", "Full Metal");
        this.add("tag.item.c.gems.full_metal", "Full Metal");

        this.add("emi.category.fantasytools.portal_transform", "Portal Transform");
        this.add("tooltip.fantasytools.portal_transform.byproduct", "Byproduct");
        this.add("tooltip.fantasytools.portal_transform.byproduct.chance", "Chance: %s");
        this.add("tooltip.fantasytools.portal_transform.byproduct.min_count", "Min Count: %s");
        this.add("tooltip.fantasytools.portal_transform.byproduct.max_count", "Max Count: %s");
        this.add("tooltip.fantasytools.pocket.pageinfo", "Page: %s / %s");

        this.add("tooltip.fantasytools.portal_transform.dimensions", "Dimension Requirement");
        this.add("tooltip.fantasytools.portal_transform.unknown_dimension", "Unknown dimensions");
        this.add("tooltip.fantasytools.portal_transform.no_requirement", "No requirement");

        this.add("tooltip.jade.spear.count", "Spear Count: %s");
        this.add("config.jade.plugin_fantasytools.spear_data", "Spear Data");

        this.add("key.categories.fantasytools", "FantasyTools");
        this.add("key.fantasytools.activate_zhongya", "Activate Zhongya's Hourglass");

        this.add(FTItems.DEMIGUISE_FUR.get(), "Demiguise fur");
        this.add(FTItems.ZHONGYA_HOURGLASS.get(), "Zhongya's Hourglass");
        this.add(FTItems.INVIS_CLOAK.get(), "Invisibility Cloak");
        this.add(FTItems.BAMBOO_COPTER.get(), "Bamboo Copter");
        this.add(FTItems.FULL_METAL.get(), "Full Metal");
        this.add(FTItems.FOUR_DIMENSIONAL_POCKET.get(), "Four Dimensional Pocket");
    }
}
