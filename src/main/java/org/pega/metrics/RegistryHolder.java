package org.pega.metrics;

import io.micrometer.core.instrument.MeterRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Alexey Lapin
 */
public class RegistryHolder {

    private static Map<String, MeterRegistry> holder = new ConcurrentHashMap<>();

    private RegistryHolder() {
    }

    public static RegistryHolder getInstance() {
        return Holder.instance;
    }

    public void put(String name, MeterRegistry registry) {
        holder.put(name, registry);
    }

    public MeterRegistry get(String name) {
        return holder.get(name);
    }

    public void clear() {
        holder.clear();
    }

    public int size() {
        return holder.size();
    }

    private static class Holder {
        private static RegistryHolder instance = new RegistryHolder();
    }
}
