package draylar.goml.registry;

import com.google.common.collect.HashBiMap;
import draylar.goml.api.Augment;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GOMLAugments {
    private static final Map<Identifier, Augment> AUGMENTS = new HashMap<>();
    private static final Map<Augment, Identifier> AUGMENT_IDS = new Object2ObjectOpenCustomHashMap<>(Util.identityHashStrategy());


    @Nullable
    public static Augment get(Identifier identifier) {
        return AUGMENTS.get(identifier);
    }

    public static Augment register(Identifier identifier, Augment augment) {
        AUGMENT_IDS.put(augment, identifier);
        return AUGMENTS.put(identifier, augment);
    }

    public static Identifier getId(Augment augment) {
        return AUGMENT_IDS.get(augment);
    }
}
