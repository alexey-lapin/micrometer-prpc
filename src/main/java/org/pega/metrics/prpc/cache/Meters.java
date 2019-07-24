package org.pega.metrics.prpc.cache;

import org.pega.metrics.prpc.meter.AbstractPrpcMultiMeter;

import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class encapsulates a cache store for {@code AbstractPrpcMultiMeter} objects.
 * It could be used in some cases:
 *  - at node startup time to remember meters which dimensions may vary during runtime
 *  - at some time after startup to re-register dimensions (recurring)
 *
 * @author Alexey Lapin
 */
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
