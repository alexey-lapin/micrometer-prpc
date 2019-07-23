package org.pega.metrics.prpc.cache;

import org.pega.metrics.prpc.meter.AbstractPrpcMultiMeter;

import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Singleton
public class Meters {

    private Set<AbstractPrpcMultiMeter> cache = new CopyOnWriteArraySet<>();

    Meters() {
    }

    public void add(AbstractPrpcMultiMeter meter) {
        cache.add(meter);
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    public void register() {
        cache.forEach(meter -> {
            meter.register(true);
        });
    }
}
