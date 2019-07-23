package org.pega.metrics.prpc.cache;

import io.micrometer.core.instrument.MeterRegistry;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
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
