package org.pega.metrics.prpc;

import com.google.inject.*;
import com.google.inject.Module;
import org.pega.metrics.prpc.cache.Meters;
import org.pega.metrics.prpc.cache.Registries;

import java.util.ArrayList;

public class MetricHarness {

    private Injector injector;

    private MetricHarness() {
        injector = Guice.createInjector(getModules());
    }

    public static MetricHarness getInstance() {
        return Holder.instance;
    }

    Registries registryHolder() {
        return injector.getInstance(Registries.class);
    }

    Meters multiMeters() {
        return injector.getInstance(Meters.class);
    }

    private Module[] getModules() {
        ArrayList<Module> modules =  new ArrayList<>();
//        modules.add(new AbstractModule() {
//            @Override
//            protected void configure() {
//                bind(Registries.class).in();
//            }
//        });
        return modules.toArray(new Module[0]);
    }

    private static class Holder {
        private static MetricHarness instance = new MetricHarness();
    }

}
