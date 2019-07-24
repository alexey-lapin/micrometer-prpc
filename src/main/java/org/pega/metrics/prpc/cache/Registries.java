package org.pega.metrics.prpc.cache;

import io.micrometer.core.instrument.MeterRegistry;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class encapsulates a cache store for {@code MeterRegistry} objects.
 * It could be used in some cases:
 *  - at node startup time to remember created registries
 *  - at some time after startup to publish metrics (recurring)
 *
 * @author Alexey Lapin
 */
@Singleton
public class Registries {

    private Map<String, MeterRegistry> cache = new ConcurrentHashMap<>();

    Registries() {
    }

    public void put(String name, MeterRegistry registry) {
        cache.put(name, registry);
    }

    public MeterRegistry get(String name) {
        return cache.get(name);
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }
}
