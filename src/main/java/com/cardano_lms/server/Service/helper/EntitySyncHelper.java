package com.cardano_lms.server.Service.helper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class EntitySyncHelper {

    private EntitySyncHelper() {}

    public static <T, R> void sync(
            List<T> existingList,
            List<R> requestList,
            Function<T, Long> existingIdExtractor,
            Function<R, Long> requestIdExtractor,
            Consumer<R> onUpdate,
            Consumer<R> onCreate,
            Consumer<T> onRemove
    ) {
        Map<Long, T> existingMap = existingList.stream()
                .filter(e -> existingIdExtractor.apply(e) != null)
                .collect(Collectors.toMap(existingIdExtractor, e -> e));

        Set<Long> requestIds = requestList.stream()
                .filter(r -> requestIdExtractor.apply(r) != null)
                .map(requestIdExtractor)
                .collect(Collectors.toSet());

        existingList.stream()
                .filter(e -> existingIdExtractor.apply(e) != null && !requestIds.contains(existingIdExtractor.apply(e)))
                .forEach(onRemove);

        existingList.removeIf(e -> existingIdExtractor.apply(e) != null && !requestIds.contains(existingIdExtractor.apply(e)));

        for (R request : requestList) {
            Long id = requestIdExtractor.apply(request);
            if (id != null && existingMap.containsKey(id)) {
                onUpdate.accept(request);
            } else {
                onCreate.accept(request);
            }
        }
    }

    public static <T, R> void syncSimple(
            List<T> existingList,
            List<R> requestList,
            Function<T, Long> existingIdExtractor,
            Function<R, Long> requestIdExtractor,
            java.util.function.BiConsumer<T, R> onUpdate,
            Function<R, T> onCreate
    ) {
        Map<Long, T> existingMap = existingList.stream()
                .filter(e -> existingIdExtractor.apply(e) != null)
                .collect(Collectors.toMap(existingIdExtractor, e -> e));

        Set<Long> requestIds = requestList.stream()
                .filter(r -> requestIdExtractor.apply(r) != null)
                .map(requestIdExtractor)
                .collect(Collectors.toSet());

        existingList.removeIf(e -> existingIdExtractor.apply(e) != null && !requestIds.contains(existingIdExtractor.apply(e)));

        for (R request : requestList) {
            Long id = requestIdExtractor.apply(request);
            if (id != null && existingMap.containsKey(id)) {
                onUpdate.accept(existingMap.get(id), request);
            } else {
                T newEntity = onCreate.apply(request);
                existingList.add(newEntity);
            }
        }
    }

    public static <T> Map<Long, T> toIdMap(List<T> list, Function<T, Long> idExtractor) {
        return list.stream()
                .filter(e -> idExtractor.apply(e) != null)
                .collect(Collectors.toMap(idExtractor, e -> e));
    }

    public static <R> Set<Long> extractIds(List<R> list, Function<R, Long> idExtractor) {
        return list.stream()
                .filter(r -> idExtractor.apply(r) != null)
                .map(idExtractor)
                .collect(Collectors.toSet());
    }
}

