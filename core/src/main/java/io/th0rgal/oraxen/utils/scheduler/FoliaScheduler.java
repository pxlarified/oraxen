package io.th0rgal.oraxen.utils.scheduler;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.utils.VersionUtil;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Folia-specific scheduler implementation using region-based scheduling.
 * Uses reflection to access Folia's API without requiring Folia as a dependency.
 */
class FoliaScheduler {

    private static final boolean IS_FOLIA = VersionUtil.isFoliaServer();
    private static volatile boolean FOLIA_AVAILABLE = false;
    private static volatile Class<?> REGIONIZED_SERVER_CLASS;
    private static volatile Class<?> REGION_SCHEDULER_CLASS;
    private static volatile Class<?> ENTITY_SCHEDULER_CLASS;
    private static volatile Class<?> GLOBAL_REGION_CLASS;
    private static volatile Class<?> ASYNC_SCHEDULER_CLASS;
    private static final Object INIT_LOCK = new Object();

    private static Class<?> loadClass(String className) {
        try {
            // Try with current classloader first
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            try {
                // Try with Bukkit server's classloader
                ClassLoader serverClassLoader = Bukkit.getServer().getClass().getClassLoader();
                return Class.forName(className, true, serverClassLoader);
            } catch (Exception e2) {
                try {
                    // Try with plugin's classloader if available
                    OraxenPlugin plugin = OraxenPlugin.get();
                    if (plugin != null) {
                        ClassLoader pluginClassLoader = plugin.getClass().getClassLoader();
                        return Class.forName(className, true, pluginClassLoader);
                    }
                } catch (Exception ignored) {
                    // Ignore - will return null
                }
                return null;
            }
        }
    }

    private static void ensureInitialized() {
        if (FOLIA_AVAILABLE || !IS_FOLIA) {
            return;
        }
        
        synchronized (INIT_LOCK) {
            if (FOLIA_AVAILABLE) {
                return;
            }
            
            REGIONIZED_SERVER_CLASS = loadClass("io.papermc.paper.threadedregions.RegionizedServer");
            REGION_SCHEDULER_CLASS = loadClass("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            ENTITY_SCHEDULER_CLASS = loadClass("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            // Try different possible names for GlobalRegion inner class
            GLOBAL_REGION_CLASS = loadClass("io.papermc.paper.threadedregions.RegionizedServer$GlobalRegion");
            if (GLOBAL_REGION_CLASS == null) {
                GLOBAL_REGION_CLASS = loadClass("io.papermc.paper.threadedregions.RegionizedServer.GlobalRegion");
            }
            ASYNC_SCHEDULER_CLASS = loadClass("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            
            // GlobalRegion is optional - we can get the global scheduler via getGlobalRegion() method
            if (REGIONIZED_SERVER_CLASS != null && REGION_SCHEDULER_CLASS != null && ENTITY_SCHEDULER_CLASS != null 
                    && ASYNC_SCHEDULER_CLASS != null) {
                FOLIA_AVAILABLE = true;
                if (GLOBAL_REGION_CLASS == null) {
                    Logs.logWarning("[FoliaScheduler] GlobalRegion class not found, will use reflection to access global region");
                } else {
                    Logs.logInfo("[FoliaScheduler] Successfully loaded all Folia scheduler classes");
                }
            } else {
                Logs.logError("[FoliaScheduler] Folia detected but required classes not accessible!");
                Logs.logError("[FoliaScheduler] RegionizedServer: " + (REGIONIZED_SERVER_CLASS != null ? "OK" : "FAILED"));
                Logs.logError("[FoliaScheduler] RegionScheduler: " + (REGION_SCHEDULER_CLASS != null ? "OK" : "FAILED"));
                Logs.logError("[FoliaScheduler] EntityScheduler: " + (ENTITY_SCHEDULER_CLASS != null ? "OK" : "FAILED"));
                Logs.logError("[FoliaScheduler] GlobalRegion: " + (GLOBAL_REGION_CLASS != null ? "OK" : "FAILED (optional)"));
                Logs.logError("[FoliaScheduler] AsyncScheduler: " + (ASYNC_SCHEDULER_CLASS != null ? "OK" : "FAILED"));
            }
        }
    }

    private static Object getGlobalRegionScheduler() throws Exception {
        Object server = Bukkit.getServer();
        
        // Approach 1: Try to get global region scheduler directly from server
        try {
            return server.getClass().getMethod("getGlobalRegionScheduler").invoke(server);
        } catch (NoSuchMethodException ignored) {
            // Approach 2: Try to get the underlying server instance
            try {
                java.lang.reflect.Field serverField = server.getClass().getDeclaredField("server");
                serverField.setAccessible(true);
                Object regionizedServer = serverField.get(server);
                Object globalRegion = regionizedServer.getClass().getMethod("getGlobalRegion").invoke(regionizedServer);
                return globalRegion.getClass().getMethod("getRegionScheduler").invoke(globalRegion);
            } catch (Exception e2) {
                // Approach 3: Try to cast server to RegionizedServer and access directly
                if (REGIONIZED_SERVER_CLASS != null && REGIONIZED_SERVER_CLASS.isInstance(server)) {
                    Object globalRegion = REGIONIZED_SERVER_CLASS.getMethod("getGlobalRegion").invoke(server);
                    return globalRegion.getClass().getMethod("getRegionScheduler").invoke(globalRegion);
                }
                throw new RuntimeException("Could not access global region scheduler", e2);
            }
        }
    }

    private static java.lang.reflect.Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        // Try exact match first
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            // Try to find method by name and parameter count
            for (java.lang.reflect.Method method : clazz.getMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == parameterTypes.length) {
                    // Check if parameter types are compatible
                    Class<?>[] actualParams = method.getParameterTypes();
                    boolean compatible = true;
                    for (int i = 0; i < actualParams.length; i++) {
                        if (!actualParams[i].isAssignableFrom(parameterTypes[i])) {
                            compatible = false;
                            break;
                        }
                    }
                    if (compatible) {
                        return method;
                    }
                }
            }
            // Log available methods for debugging
            Logs.logError("[FoliaScheduler] Method " + methodName + " not found. Available methods:");
            for (java.lang.reflect.Method method : clazz.getMethods()) {
                if (method.getName().equals(methodName)) {
                    StringBuilder sig = new StringBuilder("  - ").append(methodName).append("(");
                    Class<?>[] params = method.getParameterTypes();
                    for (int i = 0; i < params.length; i++) {
                        if (i > 0) sig.append(", ");
                        sig.append(params[i].getSimpleName());
                    }
                    sig.append(")");
                    Logs.logError(sig.toString());
                }
            }
            throw new NoSuchMethodException("Method " + methodName + " with compatible signature not found in " + clazz.getName());
        }
    }

    private static BukkitTask castToBukkitTask(Object task) {
        // Folia's task types implement BukkitTask but may be in different classloader
        // Create a wrapper that implements BukkitTask and delegates to the Folia task
        if (task == null) {
            return null;
        }
        if (task instanceof BukkitTask) {
            return (BukkitTask) task;
        }
        // Create a wrapper adapter that implements BukkitTask
        return new FoliaTaskAdapter(task);
    }

    /**
     * Adapter class that wraps Folia's task objects to implement BukkitTask.
     * This is necessary because Folia's task classes are in a different classloader
     * and cannot be directly cast to BukkitTask.
     */
    private static class FoliaTaskAdapter implements BukkitTask {
        private final Object foliaTask;

        FoliaTaskAdapter(Object foliaTask) {
            this.foliaTask = foliaTask;
        }

        @Override
        public int getTaskId() {
            try {
                java.lang.reflect.Method method = foliaTask.getClass().getMethod("getTaskId");
                return (Integer) method.invoke(foliaTask);
            } catch (Exception e) {
                return -1;
            }
        }

        @Override
        public boolean isSync() {
            try {
                java.lang.reflect.Method method = foliaTask.getClass().getMethod("isSync");
                return (Boolean) method.invoke(foliaTask);
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public Plugin getOwner() {
            try {
                java.lang.reflect.Method method = foliaTask.getClass().getMethod("getOwner");
                return (Plugin) method.invoke(foliaTask);
            } catch (Exception e) {
                return OraxenPlugin.get();
            }
        }

        @Override
        public boolean isCancelled() {
            try {
                java.lang.reflect.Method method = foliaTask.getClass().getMethod("isCancelled");
                return (Boolean) method.invoke(foliaTask);
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public void cancel() {
            try {
                java.lang.reflect.Method method = foliaTask.getClass().getMethod("cancel");
                method.invoke(foliaTask);
            } catch (Exception e) {
                Logs.logError("[FoliaScheduler] Failed to cancel task: " + e.getMessage());
            }
        }

        Object getFoliaTask() {
            return foliaTask;
        }
    }

    private static BukkitTask invokeSchedulerMethod(Object scheduler, Class<?> schedulerClass, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        // Try with Plugin first, then without
        java.lang.reflect.Method method = null;
        Object[] invokeArgs = args;
        
        // Try to find method with Plugin as first parameter
        try {
            Class<?>[] withPlugin = new Class<?>[paramTypes.length + 1];
            withPlugin[0] = Plugin.class;
            System.arraycopy(paramTypes, 0, withPlugin, 1, paramTypes.length);
            method = findMethod(schedulerClass, methodName, withPlugin);
            // Prepend plugin to args
            Object[] newArgs = new Object[args.length + 1];
            newArgs[0] = OraxenPlugin.get();
            System.arraycopy(args, 0, newArgs, 1, args.length);
            invokeArgs = newArgs;
        } catch (NoSuchMethodException e1) {
            // Try without Plugin parameter
            try {
                method = findMethod(schedulerClass, methodName, paramTypes);
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException("Could not find " + methodName + " method in " + schedulerClass.getName(), e1);
            }
        }
        
        Object result = method.invoke(scheduler, invokeArgs);
        return castToBukkitTask(result);
    }

    @NotNull
    static BukkitTask runTask(@NotNull Runnable task) {
        if (!IS_FOLIA) {
            return Bukkit.getScheduler().runTask(OraxenPlugin.get(), task);
        }
        ensureInitialized();
        if (!FOLIA_AVAILABLE) {
            throw new UnsupportedOperationException("Folia detected but scheduler classes not available. Plugin cannot run on Folia!");
        }
        try {
            // Folia's RegionScheduler requires a Location, so we use the spawn location of the first world
            // This maintains synchronous execution while working with Folia's API
            Location spawnLocation = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0).getSpawnLocation();
            if (spawnLocation == null) {
                // Fallback: use async scheduler if no worlds are loaded yet
                return runTaskAsynchronously(task);
            }
            Object server = Bukkit.getServer();
            Object scheduler = server.getClass().getMethod("getRegionScheduler").invoke(server);
            // RegionScheduler.run(Plugin, Location, Consumer<BukkitTask>) - wrap Runnable in Consumer
            // Use raw Consumer to avoid classloader casting issues - Folia passes LocationScheduledTask which can't be cast to BukkitTask
            @SuppressWarnings("unchecked")
            Consumer<Object> consumer = (t) -> task.run();
            Object result = REGION_SCHEDULER_CLASS.getMethod("run", Plugin.class, Location.class, Consumer.class)
                    .invoke(scheduler, OraxenPlugin.get(), spawnLocation, consumer);
            return castToBukkitTask(result);
        } catch (Exception e) {
            Logs.logError("[FoliaScheduler] Failed to schedule global task: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to schedule task on Folia", e);
        }
    }

    @NotNull
    static BukkitTask runTask(@NotNull Entity entity, @NotNull Runnable task) {
        if (!IS_FOLIA) {
            return Bukkit.getScheduler().runTask(OraxenPlugin.get(), task);
        }
        ensureInitialized();
        if (!FOLIA_AVAILABLE) {
            throw new UnsupportedOperationException("Folia detected but scheduler classes not available. Plugin cannot run on Folia!");
        }
        try {
            Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
            // EntityScheduler uses Consumer-based API: run(Plugin, Consumer)
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Object> consumer = (t) -> task.run();
            Object result = ENTITY_SCHEDULER_CLASS.getMethod("run", Plugin.class, java.util.function.Consumer.class)
                    .invoke(scheduler, OraxenPlugin.get(), consumer);
            return castToBukkitTask(result);
        } catch (Exception e) {
            Logs.logError("[FoliaScheduler] Failed to schedule entity task: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to schedule entity task on Folia", e);
        }
    }

    @NotNull
    static BukkitTask runTask(@NotNull Location location, @NotNull Runnable task) {
        if (!IS_FOLIA) {
            return Bukkit.getScheduler().runTask(OraxenPlugin.get(), task);
        }
        ensureInitialized();
        if (!FOLIA_AVAILABLE) {
            throw new UnsupportedOperationException("Folia detected but scheduler classes not available. Plugin cannot run on Folia!");
        }
        try {
            Object server = Bukkit.getServer();
            Object scheduler = server.getClass().getMethod("getRegionScheduler").invoke(server);
            // RegionScheduler.run(Plugin, Location, Consumer<BukkitTask>) - wrap Runnable in Consumer
            // Use raw Consumer to avoid classloader casting issues - Folia passes LocationScheduledTask which can't be cast to BukkitTask
            @SuppressWarnings("unchecked")
            Consumer<Object> consumer = (t) -> task.run();
            Object result = REGION_SCHEDULER_CLASS.getMethod("run", Plugin.class, Location.class, Consumer.class)
                    .invoke(scheduler, OraxenPlugin.get(), location, consumer);
            return castToBukkitTask(result);
        } catch (Exception e) {
            Logs.logError("[FoliaScheduler] Failed to schedule location task: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to schedule location task on Folia", e);
        }
    }

    @NotNull
    static BukkitTask runTaskLater(@NotNull Runnable task, long delay) {
        if (!IS_FOLIA) {
            return Bukkit.getScheduler().runTaskLater(OraxenPlugin.get(), task, delay);
        }
        ensureInitialized();
        if (!FOLIA_AVAILABLE) {
            throw new UnsupportedOperationException("Folia detected but scheduler classes not available. Plugin cannot run on Folia!");
        }
        try {
            // Folia's RegionScheduler requires a Location, so we use the spawn location of the first world
            Location spawnLocation = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0).getSpawnLocation();
            if (spawnLocation == null) {
                // Fallback: use async scheduler if no worlds are loaded yet
                Object server = Bukkit.getServer();
                Object scheduler = server.getClass().getMethod("getAsyncScheduler").invoke(server);
                Object result = ASYNC_SCHEDULER_CLASS.getMethod("runDelayed", Plugin.class, Runnable.class, long.class, java.util.concurrent.TimeUnit.class)
                        .invoke(scheduler, OraxenPlugin.get(), task, delay * 50L, java.util.concurrent.TimeUnit.MILLISECONDS);
                return castToBukkitTask(result);
            }
            Object server = Bukkit.getServer();
            Object scheduler = server.getClass().getMethod("getRegionScheduler").invoke(server);
            // RegionScheduler.runDelayed(Plugin, Location, Consumer<BukkitTask>, long) - wrap Runnable in Consumer
            // Use raw Consumer to avoid classloader casting issues - Folia passes LocationScheduledTask which can't be cast to BukkitTask
            @SuppressWarnings("unchecked")
            Consumer<Object> consumer = (t) -> task.run();
            Object result = REGION_SCHEDULER_CLASS.getMethod("runDelayed", Plugin.class, Location.class, Consumer.class, long.class)
                    .invoke(scheduler, OraxenPlugin.get(), spawnLocation, consumer, delay);
            return castToBukkitTask(result);
        } catch (Exception e) {
            Logs.logError("[FoliaScheduler] Failed to schedule delayed global task: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to schedule delayed task on Folia", e);
        }
    }

    @NotNull
    static BukkitTask runTaskLater(@NotNull Entity entity, @NotNull Runnable task, long delay) {
        if (!IS_FOLIA) {
            return Bukkit.getScheduler().runTaskLater(OraxenPlugin.get(), task, delay);
        }
        ensureInitialized();
        if (!FOLIA_AVAILABLE) {
            throw new UnsupportedOperationException("Folia detected but scheduler classes not available. Plugin cannot run on Folia!");
        }
        try {
            Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
            // EntityScheduler.runDelayed(Plugin, Consumer, Runnable, long)
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Object> consumer = (t) -> {};
            Object result = ENTITY_SCHEDULER_CLASS.getMethod("runDelayed", Plugin.class, java.util.function.Consumer.class, Runnable.class, long.class)
                    .invoke(scheduler, OraxenPlugin.get(), consumer, task, delay);
            return castToBukkitTask(result);
        } catch (Exception e) {
            Logs.logError("[FoliaScheduler] Failed to schedule delayed entity task: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to schedule delayed entity task on Folia", e);
        }
    }

    @NotNull
    static BukkitTask runTaskLater(@NotNull Location location, @NotNull Runnable task, long delay) {
        if (!IS_FOLIA) {
            return Bukkit.getScheduler().runTaskLater(OraxenPlugin.get(), task, delay);
        }
        ensureInitialized();
        if (!FOLIA_AVAILABLE) {
            throw new UnsupportedOperationException("Folia detected but scheduler classes not available. Plugin cannot run on Folia!");
        }
        try {
            Object server = Bukkit.getServer();
            Object scheduler = server.getClass().getMethod("getRegionScheduler").invoke(server);
            // RegionScheduler.runDelayed(Plugin, Location, Consumer<BukkitTask>, long) - wrap Runnable in Consumer
            // Use raw Consumer to avoid classloader casting issues - Folia passes LocationScheduledTask which can't be cast to BukkitTask
            @SuppressWarnings("unchecked")
            Consumer<Object> consumer = (t) -> task.run();
            Object result = REGION_SCHEDULER_CLASS.getMethod("runDelayed", Plugin.class, Location.class, Consumer.class, long.class)
                    .invoke(scheduler, OraxenPlugin.get(), location, consumer, delay);
            return castToBukkitTask(result);
        } catch (Exception e) {
            Logs.logError("[FoliaScheduler] Failed to schedule delayed location task: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to schedule delayed location task on Folia", e);
        }
    }

    @NotNull
    static BukkitTask runTaskTimer(@NotNull Runnable task, long delay, long period) {
        if (!IS_FOLIA) {
            return Bukkit.getScheduler().runTaskTimer(OraxenPlugin.get(), task, delay, period);
        }
        ensureInitialized();
        if (!FOLIA_AVAILABLE) {
            throw new UnsupportedOperationException("Folia detected but scheduler classes not available. Plugin cannot run on Folia!");
        }
        // Folia requires delay > 0, so if delay is 0, we run the task immediately and then schedule with delay 1
        if (delay <= 0) {
            delay = 1;
            // Run task immediately if delay was 0
            TaskScheduler.runTask(task);
        }
        try {
            // Folia's RegionScheduler requires a Location, so we use the spawn location of the first world
            Location spawnLocation = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0).getSpawnLocation();
            if (spawnLocation == null) {
                // Fallback: use async scheduler if no worlds are loaded yet
                Object server = Bukkit.getServer();
                Object scheduler = server.getClass().getMethod("getAsyncScheduler").invoke(server);
                Object result = ASYNC_SCHEDULER_CLASS.getMethod("runAtFixedRate", Plugin.class, Runnable.class, long.class, long.class, java.util.concurrent.TimeUnit.class)
                        .invoke(scheduler, OraxenPlugin.get(), task, delay * 50L, period * 50L, java.util.concurrent.TimeUnit.MILLISECONDS);
                return castToBukkitTask(result);
            }
            Object server = Bukkit.getServer();
            Object scheduler = server.getClass().getMethod("getRegionScheduler").invoke(server);
            // RegionScheduler.runAtFixedRate(Plugin, Location, Consumer<BukkitTask>, long, long) - wrap Runnable in Consumer
            // Use raw Consumer to avoid classloader casting issues - Folia passes LocationScheduledTask which can't be cast to BukkitTask
            @SuppressWarnings("unchecked")
            Consumer<Object> consumer = (t) -> task.run();
            Object result = REGION_SCHEDULER_CLASS.getMethod("runAtFixedRate", Plugin.class, Location.class, Consumer.class, long.class, long.class)
                    .invoke(scheduler, OraxenPlugin.get(), spawnLocation, consumer, delay, period);
            return castToBukkitTask(result);
        } catch (Exception e) {
            Logs.logError("[FoliaScheduler] Failed to schedule repeating global task: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to schedule repeating task on Folia", e);
        }
    }

    @NotNull
    static BukkitTask runTaskTimer(@NotNull Entity entity, @NotNull Runnable task, long delay, long period) {
        if (!IS_FOLIA) {
            return Bukkit.getScheduler().runTaskTimer(OraxenPlugin.get(), task, delay, period);
        }
        ensureInitialized();
        if (!FOLIA_AVAILABLE) {
            throw new UnsupportedOperationException("Folia detected but scheduler classes not available. Plugin cannot run on Folia!");
        }
        // Folia requires delay > 0, so if delay is 0, we run the task immediately and then schedule with delay 1
        if (delay <= 0) {
            delay = 1;
            // Run task immediately if delay was 0
            TaskScheduler.runTask(entity, task);
        }
        try {
            Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
            // EntityScheduler.runAtFixedRate(Plugin, Consumer, Runnable, long, long)
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Object> consumer = (t) -> {};
            Object result = ENTITY_SCHEDULER_CLASS.getMethod("runAtFixedRate", Plugin.class, java.util.function.Consumer.class, Runnable.class, long.class, long.class)
                    .invoke(scheduler, OraxenPlugin.get(), consumer, task, delay, period);
            return castToBukkitTask(result);
        } catch (Exception e) {
            Logs.logError("[FoliaScheduler] Failed to schedule repeating entity task: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to schedule repeating entity task on Folia", e);
        }
    }

    @NotNull
    static BukkitTask runTaskTimer(@NotNull Location location, @NotNull Runnable task, long delay, long period) {
        if (!IS_FOLIA) {
            return Bukkit.getScheduler().runTaskTimer(OraxenPlugin.get(), task, delay, period);
        }
        ensureInitialized();
        if (!FOLIA_AVAILABLE) {
            throw new UnsupportedOperationException("Folia detected but scheduler classes not available. Plugin cannot run on Folia!");
        }
        // Folia requires delay > 0, so if delay is 0, we run the task immediately and then schedule with delay 1
        if (delay <= 0) {
            delay = 1;
            // Run task immediately if delay was 0
            TaskScheduler.runTask(location, task);
        }
        try {
            Object server = Bukkit.getServer();
            Object scheduler = server.getClass().getMethod("getRegionScheduler").invoke(server);
            // RegionScheduler.runAtFixedRate(Plugin, Location, Consumer<BukkitTask>, long, long) - wrap Runnable in Consumer
            // Use raw Consumer to avoid classloader casting issues - Folia passes LocationScheduledTask which can't be cast to BukkitTask
            @SuppressWarnings("unchecked")
            Consumer<Object> consumer = (t) -> task.run();
            Object result = REGION_SCHEDULER_CLASS.getMethod("runAtFixedRate", Plugin.class, Location.class, Consumer.class, long.class, long.class)
                    .invoke(scheduler, OraxenPlugin.get(), location, consumer, delay, period);
            return castToBukkitTask(result);
        } catch (Exception e) {
            Logs.logError("[FoliaScheduler] Failed to schedule repeating location task: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to schedule repeating location task on Folia", e);
        }
    }

    @NotNull
    static BukkitTask runTaskTimer(@NotNull Consumer<BukkitTask> task, long delay, long period) {
        if (!IS_FOLIA) {
            AtomicReference<BukkitTask> taskRef = new AtomicReference<>();
            org.bukkit.scheduler.BukkitRunnable runnable = new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    BukkitTask currentTask = taskRef.get();
                    if (currentTask != null) {
                        task.accept(currentTask);
                    }
                }
            };
            BukkitTask scheduledTask = runnable.runTaskTimer(OraxenPlugin.get(), delay, period);
            taskRef.set(scheduledTask);
            return scheduledTask;
        }
        ensureInitialized();
        if (!FOLIA_AVAILABLE) {
            throw new UnsupportedOperationException("Folia detected but scheduler classes not available. Plugin cannot run on Folia!");
        }
        // Folia requires delay > 0, so if delay is 0, set it to 1
        // Note: For Consumer<BukkitTask>, we can't easily run immediately since we need the actual task reference
        if (delay <= 0) {
            delay = 1;
        }
        try {
            // Folia's RegionScheduler requires a Location, so we use the spawn location of the first world
            Location spawnLocation = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0).getSpawnLocation();
            if (spawnLocation == null) {
                // Fallback: use async scheduler if no worlds are loaded yet
                Object server = Bukkit.getServer();
                Object scheduler = server.getClass().getMethod("getAsyncScheduler").invoke(server);
                AtomicReference<BukkitTask> taskRef = new AtomicReference<>();
                Runnable runnable = () -> {
                    BukkitTask currentTask = taskRef.get();
                    if (currentTask != null) {
                        task.accept(currentTask);
                    }
                };
                Object result = ASYNC_SCHEDULER_CLASS.getMethod("runAtFixedRate", Plugin.class, Runnable.class, long.class, long.class, java.util.concurrent.TimeUnit.class)
                        .invoke(scheduler, OraxenPlugin.get(), runnable, delay * 50L, period * 50L, java.util.concurrent.TimeUnit.MILLISECONDS);
                BukkitTask scheduledTask = castToBukkitTask(result);
                taskRef.set(scheduledTask);
                return scheduledTask;
            }
            Object server = Bukkit.getServer();
            Object scheduler = server.getClass().getMethod("getRegionScheduler").invoke(server);
            
            // Create a wrapper that captures the task reference
            // Use raw Consumer to avoid classloader casting issues - Folia passes LocationScheduledTask which can't be cast to BukkitTask
            AtomicReference<BukkitTask> taskRef = new AtomicReference<>();
            @SuppressWarnings("unchecked")
            Consumer<Object> consumerWrapper = (t) -> {
                BukkitTask wrappedTask = castToBukkitTask(t);
                taskRef.set(wrappedTask);
                task.accept(wrappedTask);
            };
            
            Object result = REGION_SCHEDULER_CLASS.getMethod("runAtFixedRate", Plugin.class, Location.class, Consumer.class, long.class, long.class)
                    .invoke(scheduler, OraxenPlugin.get(), spawnLocation, consumerWrapper, delay, period);
            BukkitTask scheduledTask = castToBukkitTask(result);
            taskRef.set(scheduledTask);
            return scheduledTask;
        } catch (Exception e) {
            Logs.logError("[FoliaScheduler] Failed to schedule repeating consumer task: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to schedule repeating consumer task on Folia", e);
        }
    }

    @NotNull
    static BukkitTask runTaskAsynchronously(@NotNull Runnable task) {
        if (!IS_FOLIA) {
            return Bukkit.getScheduler().runTaskAsynchronously(OraxenPlugin.get(), task);
        }
        ensureInitialized();
        if (!FOLIA_AVAILABLE) {
            throw new UnsupportedOperationException("Folia detected but scheduler classes not available. Plugin cannot run on Folia!");
        }
        try {
            Object server = Bukkit.getServer();
            Object scheduler = server.getClass().getMethod("getAsyncScheduler").invoke(server);
            // Folia AsyncScheduler uses Consumer-based API: runNow(Plugin, Consumer)
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Object> consumer = (t) -> task.run();
            Object result = ASYNC_SCHEDULER_CLASS.getMethod("runNow", Plugin.class, java.util.function.Consumer.class)
                    .invoke(scheduler, OraxenPlugin.get(), consumer);
            return castToBukkitTask(result);
        } catch (Exception e) {
            Logs.logError("[FoliaScheduler] Failed to schedule async task: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to schedule async task on Folia", e);
        }
    }

    @NotNull
    static BukkitTask runTaskLaterAsynchronously(@NotNull Runnable task, long delay) {
        if (!IS_FOLIA) {
            return Bukkit.getScheduler().runTaskLaterAsynchronously(OraxenPlugin.get(), task, delay);
        }
        ensureInitialized();
        if (!FOLIA_AVAILABLE) {
            throw new UnsupportedOperationException("Folia detected but scheduler classes not available. Plugin cannot run on Folia!");
        }
        try {
            Object server = Bukkit.getServer();
            Object scheduler = server.getClass().getMethod("getAsyncScheduler").invoke(server);
            // Folia AsyncScheduler uses Consumer-based API: runDelayed(Plugin, Consumer, long, TimeUnit)
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Object> consumer = (t) -> task.run();
            Object result = ASYNC_SCHEDULER_CLASS.getMethod("runDelayed", Plugin.class, java.util.function.Consumer.class, long.class, java.util.concurrent.TimeUnit.class)
                    .invoke(scheduler, OraxenPlugin.get(), consumer, delay * 50L, java.util.concurrent.TimeUnit.MILLISECONDS);
            return castToBukkitTask(result);
        } catch (Exception e) {
            Logs.logError("[FoliaScheduler] Failed to schedule delayed async task: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to schedule delayed async task on Folia", e);
        }
    }

    @NotNull
    static BukkitTask runTaskTimerAsynchronously(@NotNull Runnable task, long delay, long period) {
        if (!IS_FOLIA) {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(OraxenPlugin.get(), task, delay, period);
        }
        ensureInitialized();
        if (!FOLIA_AVAILABLE) {
            throw new UnsupportedOperationException("Folia detected but scheduler classes not available. Plugin cannot run on Folia!");
        }
        try {
            Object server = Bukkit.getServer();
            Object scheduler = server.getClass().getMethod("getAsyncScheduler").invoke(server);
            // Folia AsyncScheduler uses Consumer-based API: runAtFixedRate(Plugin, Consumer, long, long, TimeUnit)
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Object> consumer = (t) -> task.run();
            Object result = ASYNC_SCHEDULER_CLASS.getMethod("runAtFixedRate", Plugin.class, java.util.function.Consumer.class, long.class, long.class, java.util.concurrent.TimeUnit.class)
                    .invoke(scheduler, OraxenPlugin.get(), consumer, delay * 50L, period * 50L, java.util.concurrent.TimeUnit.MILLISECONDS);
            return castToBukkitTask(result);
        } catch (Exception e) {
            Logs.logError("[FoliaScheduler] Failed to schedule repeating async task: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to schedule repeating async task on Folia", e);
        }
    }
}

