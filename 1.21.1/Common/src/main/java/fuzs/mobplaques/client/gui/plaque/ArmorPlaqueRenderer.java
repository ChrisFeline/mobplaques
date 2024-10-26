package fuzs.mobplaques.client.gui.plaque;

import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class ArmorPlaqueRenderer extends MobPlaqueRenderer {
    private static final ResourceLocation ARMOR_FULL_SPRITE = ResourceLocationHelper.withDefaultNamespace("hud/armor_full");

    @Override
    public int getValue(LivingEntity entity) {
        if (entity.getType() == EntityType.WOLF && entity instanceof TamableAnimal animal && animal.isTame()) {
            return getWolfArmorDurability(entity);
        }

        return entity.getArmorValue();
    }

    @Override
    protected ResourceLocation getSprite(LivingEntity entity) {
        return ARMOR_FULL_SPRITE;
    }

    @Override
    protected Component getComponent(LivingEntity entity) {
        if (entity.getType() == EntityType.WOLF && entity instanceof TamableAnimal animal && animal.isTame()) {
            return Component.literal("+" + this.getValue(entity));
        }

        return super.getComponent(entity);
    }

    public int getWolfArmorDurability(LivingEntity entity) {
        int totalDurability = 0;

        for (ItemStack armorPiece : entity.getAllSlots()) {
            int id = Item.getId(armorPiece.getItem());
            if (id == 797) { // Wolf armor ID
                int maxDurability = armorPiece.getMaxDamage();
                int damage = armorPiece.getDamageValue();
                int durability = maxDurability - damage;

                totalDurability += durability;
            }
        }

        return totalDurability;
    }
}
