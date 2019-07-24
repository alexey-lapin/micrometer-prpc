package org.pega.metrics.prpc.meter;

import com.google.common.collect.ImmutableMap;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.pega.metrics.prpc.source.AbstractPrpcSource;
import org.pega.metrics.prpc.source.PrpcSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pega.metrics.prpc.PropertyMock.TagPropDef.tagPropDef;
import static org.pega.metrics.prpc.PropertyMock.ValuePropDef.valuePropDef;
import static org.pega.metrics.prpc.PropertyMock.mockSourcePropBuilder;

class PrpcMultiGaugeTest {

    @Test
    void should_registerCorrectTagsAndValues() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        Tags tags1 = Tags.of("n1", "m1");
        Tags tags2 = Tags.of("n1", "m2");
        ClipboardProperty prop = mockSourcePropBuilder().group()
                .addItem(tagPropDef("Tag", tags1),
                        valuePropDef("Value", ImmutableMap.of("v", 10.0)))
                .addItem(tagPropDef("Tag", tags2),
                        valuePropDef("Value", ImmutableMap.of("v", 12.0)))
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

        assertThat(registry.getMeters().size()).isEqualTo(2);

        List<Iterable<Tag>> registredTags1 = registry
                .get("name")
                .meters()
                .stream()
                .map(Meter::getId).map(Meter.Id::getTagsAsIterable)
                .collect(toList());
        assertThat(registredTags1).containsOnly(tags1, tags2);

        List<Double> values1 = registry.get("name").gauges().stream().map(Gauge::value).collect(toList());
        assertThat(values1).containsOnly(10.0, 12.0);

        Tags tags3 = Tags.of("n1", "m3");
        ClipboardProperty prop2 = mockSourcePropBuilder().group()
                .addItem(tagPropDef("Tag", tags1),
                        valuePropDef("Value", ImmutableMap.of("v", 5.0)))
                .addItem(tagPropDef("Tag", tags3),
                        valuePropDef("Value", ImmutableMap.of("v", 6.0)))
                .build();

        when(source.get()).thenReturn(Optional.of(prop2));

        mg.register(true);

        assertThat(registry.getMeters().size()).isEqualTo(2);

        List<Iterable<Tag>> registredTags2 = registry
                .get("name")
                .meters()
                .stream()
                .map(Meter::getId).map(Meter.Id::getTagsAsIterable)
                .collect(toList());
        assertThat(registredTags2).containsOnly(tags1, tags3);

        List<Double> values2 = registry.get("name").gauges().stream().map(Gauge::value).collect(toList());
        assertThat(values2).containsOnly(5.0, 6.0);
    }

    @Test
    void should_equalityBeCorrect() {
        PrpcMultiGauge mg1 = PrpcMultiGauge.builder("name")
                .description("description")
                .baseUnit("base")
                .tags("tag1", "tag1")
                .tags(Tags.of("tag3", "tag3"))
                .tag("tag2", "tag2")
                .source(mock(PrpcSource.class))
                .registry(mock(MeterRegistry.class))
                .valuePropName("Value(v)")
                .build();

        PrpcMultiGauge mg2 = PrpcMultiGauge.builder("name")
                .description("description")
                .baseUnit("base")
                .tags("tag1", "tag1")
                .tags(Tags.of("tag3", "tag3"))
                .tag("tag2", "tag2")
                .source(mock(PrpcSource.class))
                .registry(mock(MeterRegistry.class))
                .valuePropName("Value(v)")
                .build();

        assertThat(mg1).isEqualTo(mg1);
        assertThat(mg1).isEqualTo(mg2);
        assertThat(mg1).isNotEqualTo("");
        assertThat(mg1).isNotEqualTo(null);

        assertThat(mg1).hasSameHashCodeAs(mg1);
        assertThat(mg1).hasSameHashCodeAs(mg2);
    }
}