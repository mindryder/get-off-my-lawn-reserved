package draylar.goml.api;

import draylar.goml.block.augment.HeavenWingsAugmentBlock;
import net.minecraft.nbt.*;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public record DataKey<T>(String key, T defaultValue, Function<T, NbtElement> serializer, Function<NbtElement, T> deserializer) {
    private static final Map<String, DataKey<?>> REGISTRY = new HashMap<>();

    public DataKey {
        if (REGISTRY.containsKey(key)) {
            throw new RuntimeException("Duplicate key " + key + "! You can't register the same key twice!");
        }

        REGISTRY.put(key, this);
    }

    public static <T, C extends Collection<T>> DataKey<C> ofCollection(String key, Supplier<C> collectionCreator, Function<T, NbtElement> serializer, Function<NbtElement, T> deserializer) {
        return new DataKey<>(key, collectionCreator.get(), (list) -> {
            var nbt = new NbtList();

            for (var i : list) {
                if (i != null) {
                    nbt.add(serializer.apply(i));
                }
            }

            return nbt;
        }, (nbt) -> {
            var list = collectionCreator.get();

            if (nbt instanceof AbstractNbtList<?> nbtList)
            for (var i : nbtList) {
                if (i != null) {
                    list.add(deserializer.apply(i));
                }
            }

            return list;
        });
    }

    public static DataKey<String> ofString(String key, String defaultValue) {
        return new DataKey<>(key, defaultValue, (s) -> NbtString.of(s), (nbt) -> nbt instanceof NbtString nbtString ? nbtString.asString() : defaultValue);
    }

    public static DataKey<Boolean> ofBoolean(String key, boolean defaultValue) {
        return new DataKey<>(key, defaultValue, (i) -> NbtByte.of(i), (nbt) -> nbt instanceof AbstractNbtNumber nbtNumber ? nbtNumber.byteValue() > 0 : defaultValue);
    }

    public static DataKey<Integer> ofInt(String key, int defaultValue) {
        return new DataKey<>(key, defaultValue, (i) -> NbtInt.of(i), (nbt) -> nbt instanceof AbstractNbtNumber nbtNumber ? nbtNumber.intValue() : defaultValue);
    }

    public static DataKey<UUID> ofUuid(String key) {
        return new DataKey<>(key, Util.NIL_UUID, (i) -> NbtHelper.fromUuid(i), (nbt) -> NbtHelper.toUuid(nbt));
    }

    public static DataKey<Set<UUID>> ofUuidSet(String key) {
        return ofCollection(key, HashSet::new, (i) -> NbtHelper.fromUuid(i), (nbt) -> NbtHelper.toUuid(nbt));
    }

    public static DataKey<Double> ofDouble(String key, double defaultValue) {
        return new DataKey<>(key, defaultValue, (i) -> NbtDouble.of(i), (nbt) -> nbt instanceof AbstractNbtNumber nbtNumber ? nbtNumber.doubleValue() : defaultValue);
    }

    public static DataKey<BlockPos> ofPos(String key) {
        return new DataKey<>(key, null, (i) -> NbtHelper.fromBlockPos(i), (nbt) -> nbt instanceof NbtCompound compound ? NbtHelper.toBlockPos(compound) : null);
    }

    @Nullable
    public static DataKey<?> getKey(String key) {
        return REGISTRY.get(key);
    }

    public static Collection<String> keys() {
        return REGISTRY.keySet();
    }

    public static <T extends Enum<T>> DataKey<T> ofEnum(String key, Class<T> tClass, T defaultValue) {
        return new DataKey<>(key, defaultValue, (i) -> NbtString.of(i.name()), (nbt) -> {
            var value = nbt instanceof NbtString string ? Enum.valueOf(tClass, string.asString()) : null;
            return value != null ? value : defaultValue;
        });
    }
}
