package io.micrometer.core.instrument;

import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import ru.sbrf.pegi18.mon.prpc.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 *
 */
public class PrpcPrometheusRegistry extends PrometheusMeterRegistry {

    public static final String PROP_NAME_TAG = "Tag";

    private final Map<Meter.Id, AbstractPrpcMeter> map;

    public PrpcPrometheusRegistry() {
        this(PrometheusConfig.DEFAULT);
    }

    public PrpcPrometheusRegistry(PrometheusConfig config) {
        this(config, new CollectorRegistry(), Clock.SYSTEM);
    }

    public PrpcPrometheusRegistry(PrometheusConfig config, CollectorRegistry registry, Clock clock) {
        super(config, registry, clock);
        map = new ConcurrentHashMap<>();
    }

    public void gauge(String name, PrpcSource source, String valuePropName) {
        gauge(name, Tags.empty(), source, valuePropName);
    }

    public void gauge(String name, Iterable<Tag> tags, PrpcSource source, String valuePropName) {
        gauge(name, tags, source, PROP_NAME_TAG, valuePropName);
    }

    public void gauge(String name, Iterable<Tag> tags, PrpcSource source, String tagsPropName, String valuePropName) {
        Meter.Id id = new Meter.Id(name, Tags.of(tags), null, null, Meter.Type.GAUGE, null);
        AbstractPrpcMeter meter = PrpcGauge.builder()
            .id(id)
            .source(source)
            .config(config())
            .tagsPropName(tagsPropName)
            .valuePropName(valuePropName)
            .build();
        map.put(id, meter);
    }

    public void counter(String name, PrpcSource source, String totalValuePropName) {
        counter(name, Tags.empty(), source, totalValuePropName);
    }

    public void counter(String name, Iterable<Tag> tags, PrpcSource source, String totalValuePropName) {
        counter(name, tags, source, PROP_NAME_TAG, totalValuePropName);
    }

    public void counter(String name, Iterable<Tag> tags, PrpcSource source, String tagsPropName, String totalValuePropName) {
        Meter.Id id = new Meter.Id(name, Tags.of(tags), null, null, Meter.Type.COUNTER, null);
        AbstractPrpcMeter meter = PrpcFunctionCounter.builder()
            .id(id)
            .source(source)
            .config(config())
            .tagsPropName(tagsPropName)
            .totalPropName(totalValuePropName)
            .build();
        map.put(id, meter);
    }

    public void timer(String name, PrpcSource source, String countValuePropName, String sumValuePropName) {
        timer(name, Tags.empty(), source, countValuePropName, sumValuePropName);
    }

    public void timer(String name, Iterable<Tag> tags, PrpcSource source, String countValuePropName, String sumValuePropName) {
        timer(name, tags, source, PROP_NAME_TAG, countValuePropName, sumValuePropName);
    }

    public void timer(String name, Iterable<Tag> tags, PrpcSource source, String tagsPropName, String countValuePropName, String sumValuePropName) {
        Meter.Id id = new Meter.Id(name, Tags.of(tags), null, null, Meter.Type.TIMER, null);
        AbstractPrpcMeter meter = PrpcFunctionTimer.builder()
            .id(id)
            .source(source)
            .config(config())
            .tagsPropName(tagsPropName)
            .countValuePropName(countValuePropName)
            .sumValuePropName(sumValuePropName)
            .build();
        map.put(id, meter);
    }

    @Override
    public String scrape() {

//        return super.scrape();
        String scraped = map.entrySet()
            .stream()
            .parallel()
            .map(Map.Entry::getValue)
            .map(AbstractPrpcMeter::promify)
            .collect(Collectors.joining("\n"));

        return scraped;
    }
}
