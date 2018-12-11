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
public class PrpcPrometheusMeterRegistry extends PrometheusMeterRegistry {

    private final Map<Meter.Id, AbstractPrpcMeter> map;

    public PrpcPrometheusMeterRegistry() {
        this(PrometheusConfig.DEFAULT);
    }

    public PrpcPrometheusMeterRegistry(PrometheusConfig config) {
        this(config, new CollectorRegistry(), Clock.SYSTEM);
    }

    public PrpcPrometheusMeterRegistry(PrometheusConfig config, CollectorRegistry registry, Clock clock) {
        super(config, registry, clock);
        map = new ConcurrentHashMap<>();
    }

    public PrpcGauge gauge(String name, PrpcSource source, String valuePropName) {
        return gauge(name, Tags.empty(), source, valuePropName);
    }

    public PrpcGauge gauge(String name, Iterable<Tag> tags, PrpcSource source, String valuePropName) {
        return gauge(name, tags, source, AbstractPrpcMeter.PROP_NAME_TAG, valuePropName);
    }

    public PrpcGauge gauge(String name, Iterable<Tag> tags, PrpcSource source, String tagsPropName, String valuePropName) {
        Meter.Id id = new Meter.Id(name, Tags.of(tags), null, null, Meter.Type.GAUGE, null);
        PrpcGauge meter = PrpcGauge.builder()
            .id(id)
            .source(source)
            .config(config())
            .tagsPropName(tagsPropName)
            .valuePropName(valuePropName)
            .build();
        map.put(id, meter);
        return meter;
    }

    public PrpcFunctionCounter counter(String name, PrpcSource source, String totalValuePropName) {
        return counter(name, Tags.empty(), source, totalValuePropName);
    }

    public PrpcFunctionCounter counter(String name, Iterable<Tag> tags, PrpcSource source, String totalValuePropName) {
        return counter(name, tags, source, AbstractPrpcMeter.PROP_NAME_TAG, totalValuePropName);
    }

    public PrpcFunctionCounter counter(String name, Iterable<Tag> tags, PrpcSource source, String tagsPropName, String totalValuePropName) {
        Meter.Id id = new Meter.Id(name, Tags.of(tags), null, null, Meter.Type.COUNTER, null);
        PrpcFunctionCounter meter = PrpcFunctionCounter.builder()
            .id(id)
            .source(source)
            .config(config())
            .tagsPropName(tagsPropName)
            .totalPropName(totalValuePropName)
            .build();
        map.put(id, meter);
        return meter;
    }

    public PrpcFunctionTimer timer(String name, PrpcSource source, String countValuePropName, String sumValuePropName) {
        return timer(name, Tags.empty(), source, countValuePropName, sumValuePropName);
    }

    public PrpcFunctionTimer timer(String name, Iterable<Tag> tags, PrpcSource source, String countValuePropName, String sumValuePropName) {
        return timer(name, tags, source, AbstractPrpcMeter.PROP_NAME_TAG, countValuePropName, sumValuePropName);
    }

    public PrpcFunctionTimer timer(String name, Iterable<Tag> tags, PrpcSource source, String tagsPropName, String countValuePropName, String sumValuePropName) {
        Meter.Id id = new Meter.Id(name, Tags.of(tags), null, null, Meter.Type.TIMER, null);
        PrpcFunctionTimer meter = PrpcFunctionTimer.builder()
            .id(id)
            .source(source)
            .config(config())
            .tagsPropName(tagsPropName)
            .countValuePropName(countValuePropName)
            .sumValuePropName(sumValuePropName)
            .build();
        map.put(id, meter);
        return meter;
    }

    @Override
    public String scrape() {
        StringBuffer buf = new StringBuffer();
        String scraped = map.entrySet()
            .stream()
//            .parallel()
            .map(Map.Entry::getValue)
            .map(AbstractPrpcMeter::promify)
            .collect(Collectors.joining());
        buf.append(super.scrape());
        buf.append(scraped);
        return buf.toString();
    }
}
