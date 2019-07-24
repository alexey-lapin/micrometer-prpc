package org.pega.metrics.prpc;

import com.google.inject.*;
import com.google.inject.Module;
import org.pega.metrics.prpc.cache.Meters;
import org.pega.metrics.prpc.cache.Registries;

import java.util.ArrayList;

public class Metrics {

    private Injector injector;

    private Metrics() {
        injector = Guice.createInjector();
    }

    public static Metrics getInstance() {
        return Holder.instance;
    }

    public Registries registries() {
        return injector.getInstance(Registries.class);
    }

    public Meters meters() {
        return injector.getInstance(Meters.class);
    }

    private static class Holder {
        private static Metrics instance = new Metrics();
    }

}
