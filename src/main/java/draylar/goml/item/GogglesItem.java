package draylar.goml.item;

import draylar.goml.GetOffMyLawn;
import draylar.goml.api.ClaimUtils;
import draylar.goml.api.WorldParticleUtils;
import eu.pb4.polymer.api.item.PolymerItem;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GogglesItem extends ArmorItem implements PolymerItem {

    public GogglesItem() {
        super(ArmorMaterials.IRON, EquipmentSlot.HEAD, new Item.Settings().group(GetOffMyLawn.GROUP).maxDamage(-1));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity instanceof ServerPlayerEntity player && (selected || player.getEquippedStack(EquipmentSlot.HEAD) == stack || player.getEquippedStack(EquipmentSlot.OFFHAND) == stack)) {
            if (player.age % 20 == 0) {
                var distance = player.getServer().getPlayerManager().getViewDistance() * 16;

                ClaimUtils.getClaimsInBox(
                        world,
                        entity.getBlockPos().add(-distance, -distance, -distance),
                        entity.getBlockPos().add(distance, distance, distance)).forEach(
                                claim -> {
                    BlockPos claimPos = claim.getKey().getOrigin();
                    int radius = claim.getKey().getRadius();

                    WorldParticleUtils.render(player, claimPos.add(-radius, -radius, -radius), claimPos.add(radius, radius, radius), 0.8f, 0.8f, 0.8f);
                });
            }
        }
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.IRON_HELMET;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        var clientStack = PolymerItem.super.getPolymerItemStack(itemStack, player);
        clientStack.addEnchantment(Enchantments.LURE, 64);
        return clientStack;
    }
}
