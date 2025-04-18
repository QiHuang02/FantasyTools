package cn.qihuang02.fantasytools.item.custom;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class BambooCopter extends Item implements Equipable {
    public BambooCopter(Properties properties) {
        super(
                properties.stacksTo(1)
                        .durability(1000)
                        .rarity(Rarity.valueOf("FANTASYTOOLS_LEGENDARY"))
        );
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        return super.canEquip(stack, EquipmentSlot.HEAD, entity);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }
}
