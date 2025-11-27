package io.th0rgal.oraxen.block;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class VisualBlockStateAllocator {
    private final Map<String, Integer> cachedBlockStates = new LinkedHashMap<>();
    private final Map<String, CompletableFuture<Integer>> pendingAllocations = new LinkedHashMap<>();
    private final AutoStateGroup[] autoStateGroups;
    private int nextAutoVisualId = 0;

    public VisualBlockStateAllocator(int vanillaBlockStateCount) {
        this.nextAutoVisualId = vanillaBlockStateCount;
        this.autoStateGroups = new AutoStateGroup[]{
                new AutoStateGroup("solid", new int[]{1, 2, 3}),
                new AutoStateGroup("open", new int[]{4, 5, 6}),
                new AutoStateGroup("liquid", new int[]{7, 8, 9})
        };
    }

    public CompletableFuture<Integer> assignFixedBlockState(@NotNull String key, int visualId) {
        if (cachedBlockStates.containsKey(key) && cachedBlockStates.get(key) != visualId) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Key " + key + " already has a different visual ID"));
        }

        cachedBlockStates.put(key, visualId);
        return CompletableFuture.completedFuture(visualId);
    }

    public CompletableFuture<Integer> requestAutoState(@NotNull String key, @NotNull AutoStateGroup group) {
        if (cachedBlockStates.containsKey(key)) {
            return CompletableFuture.completedFuture(cachedBlockStates.get(key));
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

            if (cachedBlockStates.containsKey(key)) {
                future.complete(cachedBlockStates.get(key));
            } else {
                int visualId = nextAutoVisualId++;
                cachedBlockStates.put(key, visualId);
                future.complete(visualId);
            }
        }

        pendingAllocations.clear();
    }

    @NotNull
    public Map<String, Integer> getCachedBlockStates() {
        return new HashMap<>(cachedBlockStates);
    }

    public void reset() {
        cachedBlockStates.clear();
        pendingAllocations.clear();
        nextAutoVisualId = 0;
    }

    public static class AutoStateGroup {
        private final String id;
        private final int[] candidates;

        public AutoStateGroup(@NotNull String id, int[] candidates) {
            this.id = id;
            this.candidates = candidates;
        }

        @NotNull
        public String getId() {
            return id;
        }

        public int[] getCandidates() {
            return candidates;
        }
    }
}
