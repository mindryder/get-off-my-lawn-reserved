package draylar.goml.registry;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

import static draylar.goml.GetOffMyLawn.id;

public final class GOMLTags {
    public static final TagKey<Block> ALLOWED_INTERACTIONS_BLOCKS = TagKey.of(Registry.BLOCK_KEY, id("allowed_interactions"));
    public static final TagKey<EntityType<?>> ALLOWED_INTERACTIONS_ENTITY = TagKey.of(Registry.ENTITY_TYPE_KEY, id("allowed_interactions"));

    private GOMLTags(){}
}
