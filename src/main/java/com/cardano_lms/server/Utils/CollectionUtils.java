package com.cardano_lms.server.Utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CollectionUtils {

    private CollectionUtils() {}

    public static <K, V> Map<K, V> toLookupMap(Collection<V> items, Function<V, K> keyExtractor) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }
        return items.stream().collect(Collectors.toMap(
                keyExtractor,
                Function.identity(),
                (existing, replacement) -> existing
        ));
    }

    public static <K, V, R> Map<K, R> toLookupMap(
            Collection<V> items,
            Function<V, K> keyExtractor,
            Function<V, R> valueExtractor
    ) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }
        return items.stream().collect(Collectors.toMap(
                keyExtractor,
                valueExtractor,
                (existing, replacement) -> existing
        ));
    }

    public static <K, V> Map<K, List<V>> groupBy(Collection<V> items, Function<V, K> keyExtractor) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }
        return items.stream().collect(Collectors.groupingBy(keyExtractor));
    }

    public static <T> Set<T> toSet(Collection<T> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(items);
    }

    public static <T, K> Set<K> toSet(Collection<T> items, Function<T, K> keyExtractor) {
        if (items == null || items.isEmpty()) {
            return Collections.emptySet();
        }
        return items.stream().map(keyExtractor).collect(Collectors.toSet());
    }

    public static <T> boolean equalElements(Collection<T> a, Collection<T> b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.size() != b.size()) return false;
        return new HashSet<>(a).equals(new HashSet<>(b));
    }

    public static <T> Optional<T> findById(Map<Long, T> lookupMap, Long id) {
        return Optional.ofNullable(lookupMap.get(id));
    }

    public static <T> List<T> findByIds(Map<Long, T> lookupMap, Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .map(lookupMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static <T> List<List<T>> partition(List<T> items, int chunkSize) {
        if (items == null || items.isEmpty() || chunkSize <= 0) {
            return Collections.emptyList();
        }
        
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < items.size(); i += chunkSize) {
            result.add(items.subList(i, Math.min(i + chunkSize, items.size())));
        }
        return result;
    }

    public static <T> T firstOrDefault(Collection<T> items, T defaultValue) {
        if (items == null || items.isEmpty()) {
            return defaultValue;
        }
        return items.iterator().next();
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
}
