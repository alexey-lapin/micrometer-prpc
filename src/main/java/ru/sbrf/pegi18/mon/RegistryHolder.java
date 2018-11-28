package ru.sbrf.pegi18.mon;

import io.micrometer.core.instrument.MeterRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lapin2-aa
 */
public class RegistryHolder {

    private static RegistryHolder instance;
    private static Map<String, MeterRegistry> holder = Collections.synchronizedMap(new HashMap<>());

    private RegistryHolder() {
    }

    public static RegistryHolder getInstance() {
        if (instance == null) {
            synchronized (RegistryHolder.class) {
                if (instance == null) {
                    instance = new RegistryHolder();
                }
            }
        }
        return instance;
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

}
