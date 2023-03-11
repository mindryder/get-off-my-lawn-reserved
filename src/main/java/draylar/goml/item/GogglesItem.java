package draylar.goml.item;

import draylar.goml.GetOffMyLawn;
import draylar.goml.api.ClaimUtils;
import draylar.goml.api.WorldParticleUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

public class GogglesItem extends ArmorItem implements PolymerItem {
    private static final BlockState[] STATES = Registries.BLOCK.stream().filter((b) -> {
        var id = Registries.BLOCK.getId(b);

        return id.getNamespace().equals("minecraft") && id.getPath().endsWith("_concrete");
    }).map((b) -> b.getDefaultState()).collect(Collectors.toList()).toArray(new BlockState[0]);

    public GogglesItem() {
        super(ArmorMaterials.IRON, Type.HELMET, new Item.Settings().maxDamage(-1));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity instanceof ServerPlayerEntity player && (selected || player.getEquippedStack(EquipmentSlot.HEAD) == stack || player.getEquippedStack(EquipmentSlot.OFFHAND) == stack)) {
            if (player.age % 70 == 0) {
                var distance = player.getServer().getPlayerManager().getViewDistance() * 16;

                ClaimUtils.getClaimsInBox(
                        world,
                        entity.getBlockPos().add(-distance, -distance, -distance),
                        entity.getBlockPos().add(distance, distance, distance)).forEach(
                        claim -> {
                            var box = claim.getKey().toBox();
                            var minPos = new BlockPos(box.x1(), Math.max(box.y1(), world.getBottomY()), box.z1());
                            var maxPos = new BlockPos(box.x2() - 1, Math.min(box.y2() - 1, world.getTopY()), box.z2() - 1);

                            WorldParticleUtils.render(player, minPos, maxPos,
                                    //new DustParticleEffect(new Vec3f(0.8f, 0.8f, 0.8f), 2)
                                    new BlockStateParticleEffect(ParticleTypes.BLOCK_MARKER, STATES[(claim.getValue().getOrigin().hashCode() & 0xFFFF) % STATES.length])
                            );
                        });
            }
        }
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.IRON_HELMET;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipContext context, @Nullable ServerPlayerEntity player) {
        var clientStack = PolymerItem.super.getPolymerItemStack(itemStack, context, player);
        clientStack.addEnchantment(Enchantments.LURE, 64);
        return clientStack;
    }
}
