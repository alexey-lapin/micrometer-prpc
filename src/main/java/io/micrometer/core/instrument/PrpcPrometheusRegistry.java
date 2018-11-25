package io.micrometer.core.instrument;

import io.micrometer.core.instrument.prpc.PrpcSource;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by sp00x on 15-Nov-18.
 * Project: pg-micrometer
 */
public class PrpcPrometheusRegistry extends PrometheusMeterRegistry {

    private final Map<Meter.Id, PrpcSource> map;
    private final Map<Meter.Id, PrpcMeter> map2;

    public PrpcPrometheusRegistry() {
        this(PrometheusConfig.DEFAULT);
    }

    public PrpcPrometheusRegistry(PrometheusConfig config) {
        this(config, new CollectorRegistry(), Clock.SYSTEM);
    }

    public PrpcPrometheusRegistry(PrometheusConfig config, CollectorRegistry registry, Clock clock) {
        super(config, registry, clock);
        map = new ConcurrentHashMap<>();
        map2 = new ConcurrentHashMap<>();
    }

    public void gauge(String name, PrpcSource source) {
        gauge(name, Tags.empty(), source);
    }

    public void gauge(String name, Iterable<Tag> tags, PrpcSource source) {
        Meter.Id id = new Meter.Id(name, Tags.of(tags), null, null, Meter.Type.GAUGE, null);
        map.put(id, source.id(id));
    }

    public void gauge(String name, Iterable<Tag> tags, String valuePropName, PrpcSource source) {
        Meter.Id id = new Meter.Id(name, Tags.of(tags), null, null, Meter.Type.GAUGE, null);
        PrpcMeter meter = new PrpcGauge(id, source, "Tag", valuePropName);
        map2.put(id, meter);
    }

    public void timer(String name, Iterable<Tag> tags, String coutnValuePropName, String sumValuePropName, PrpcSource source) {
        Meter.Id id = new Meter.Id(name, Tags.of(tags), null, null, Meter.Type.GAUGE, null);
        map.put(id, source.id(id));
    }


    @Override
    public String scrape() {

//        return super.scrape();
        PrpcFormatter formatter = new PrpcFormatter(this);

        String scraped = map.entrySet()
                .stream()
                .parallel()
                .map(Map.Entry::getValue)
                .map(formatter::promify)
                .collect(Collectors.joining("\n"));

        return scraped;
    }
}
