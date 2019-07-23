package org.pega.metrics.prpc.meter;

import com.google.common.collect.ImmutableMap;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.pega.metrics.prpc.source.AbstractPrpcSource;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pega.metrics.prpc.PropertyMock.TagPropDef.tagPropDef;
import static org.pega.metrics.prpc.PropertyMock.ValuePropDef.valuePropDef;
import static org.pega.metrics.prpc.PropertyMock.mockSourcePropBuilder;

class PrpcMultiGaugeTest {

    @Test
    void test2() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        ClipboardProperty prop = mockSourcePropBuilder().group()
                .addItem(tagPropDef("Tag", Tags.of("n1", "m1")), valuePropDef("Value", ImmutableMap.of("v", 10.0)))
                .addItem(tagPropDef("Tag", Tags.of("n1", "m2")), valuePropDef("Value", ImmutableMap.of("v", 12.0)))
                .build();

        AbstractPrpcSource source = mock(AbstractPrpcSource.class);
        when(source.get()).thenReturn(Optional.of(prop));
        when(source.tagsPropName()).thenReturn("Tag");

        PrpcMultiGauge mg = PrpcMultiGauge.builder("name")
                .source(source)
                .registry(registry)
                .valuePropName("Value(v)")
                .build();
        mg.register();

        System.out.println(registry.getMeters().size());
        System.out.println(registry.get("name").meters().stream().map(Meter::getId).collect(Collectors.toList()));
        System.out.println(registry.get("name").gauges().stream().map(Gauge::value).collect(Collectors.toList()));
        System.out.println();

        ClipboardProperty prop2 = mockSourcePropBuilder().group()
                .addItem(tagPropDef("Tag", Tags.of("n1", "m1")), valuePropDef("Value", ImmutableMap.of("v", 5.0)))
                .addItem(tagPropDef("Tag", Tags.of("n1", "m3")), valuePropDef("Value", ImmutableMap.of("v", 6.0)))
                .build();

        when(source.get()).thenReturn(Optional.of(prop2));
        mg.register();

        System.out.println(registry.getMeters().size());
        System.out.println(registry.get("name").meters().stream().map(Meter::getId).collect(Collectors.toList()));
        System.out.println(registry.get("name").gauges().stream().map(Gauge::value).collect(Collectors.toList()));

    }
}