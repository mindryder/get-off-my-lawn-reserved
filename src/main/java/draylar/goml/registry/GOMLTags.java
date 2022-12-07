package draylar.goml.registry;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

import static draylar.goml.GetOffMyLawn.id;

public final class GOMLTags {
    public static final TagKey<Block> ALLOWED_INTERACTIONS_BLOCKS = TagKey.of(RegistryKeys.BLOCK, id("allowed_interactions"));
    public static final TagKey<EntityType<?>> ALLOWED_INTERACTIONS_ENTITY = TagKey.of(RegistryKeys.ENTITY_TYPE, id("allowed_interactions"));

    private GOMLTags(){}
}
