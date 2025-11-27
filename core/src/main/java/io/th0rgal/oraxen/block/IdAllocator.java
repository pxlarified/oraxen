package io.th0rgal.oraxen.block;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class IdAllocator {
    private final BiMap<String, Integer> forcedIdMap = HashBiMap.create(128);
    private final BitSet occupiedIdSet = new BitSet();
    private final Map<String, CompletableFuture<Integer>> pendingAllocations = new LinkedHashMap<>();
    private final Map<String, Integer> cachedIdMap = new LinkedHashMap<>();
    private int nextAutoId = 0;

    public IdAllocator(int startingId) {
        this.nextAutoId = startingId;
    }

    public CompletableFuture<Integer> assignFixedId(@NotNull String key, int id) {
        if (forcedIdMap.containsKey(key) && forcedIdMap.get(key) != id) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Key " + key + " already has a different fixed ID"));
        }

        if (occupiedIdSet.get(id)) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("ID " + id + " is already occupied"));
        }

        forcedIdMap.put(key, id);
        occupiedIdSet.set(id);
        CompletableFuture<Integer> future = CompletableFuture.completedFuture(id);
        cachedIdMap.put(key, id);
        return future;
    }

    public CompletableFuture<Integer> requestAutoId(@NotNull String key) {
        if (cachedIdMap.containsKey(key)) {
            return CompletableFuture.completedFuture(cachedIdMap.get(key));
        }

        if (pendingAllocations.containsKey(key)) {
            return pendingAllocations.get(key);
        }

        CompletableFuture<Integer> future = new CompletableFuture<>();
        pendingAllocations.put(key, future);
        return future;
    }

    public void processPendingAllocations() {
        for (Map.Entry<String, CompletableFuture<Integer>> entry : pendingAllocations.entrySet()) {
            String key = entry.getKey();
            CompletableFuture<Integer> future = entry.getValue();

            if (cachedIdMap.containsKey(key)) {
                future.complete(cachedIdMap.get(key));
            } else {
                int id = findNextAvailableId();
                occupiedIdSet.set(id);
                cachedIdMap.put(key, id);
                future.complete(id);
            }
        }

        pendingAllocations.clear();
    }

    private int findNextAvailableId() {
        while (occupiedIdSet.get(nextAutoId)) {
            nextAutoId++;
        }
        return nextAutoId++;
    }

    public boolean isOccupied(int id) {
        return occupiedIdSet.get(id);
    }

    public void reset() {
        forcedIdMap.clear();
        occupiedIdSet.clear();
        pendingAllocations.clear();
        cachedIdMap.clear();
        nextAutoId = 0;
    }

    @NotNull
    public Map<String, Integer> getCachedIdMap() {
        return new HashMap<>(cachedIdMap);
    }
}
