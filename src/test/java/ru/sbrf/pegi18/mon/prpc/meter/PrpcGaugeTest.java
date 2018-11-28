package ru.sbrf.pegi18.mon.prpc.meter;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tags;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static io.prometheus.client.Collector.Type.GAUGE;
import static org.assertj.core.api.Assertions.assertThat;

class PrpcGaugeTest extends MeterTestSupport {

    @Test
    void should_promifyStringContains2LinesInHeaderPlusSourceSizeLines_when_sourceIsList() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        PrpcGauge gauge = PrpcGauge.builder()
            .id(new Meter.Id(METER_NAME_NAME, Tags.empty(), null, null, Meter.Type.GAUGE))
            .source(getSourceMock(VALUE_PROP_NAME_VALUE, 1.0, 3.0, 5.0))
            .valuePropName(VALUE_PROP_NAME_VALUE)
            .config(registry.config())
            .build();
        String result = gauge.promify();

        assertThat(result).hasLineCount(2 + 3);
        assertThat(StringUtils.countMatches(result, METER_NAME_NAME)).isEqualTo(2 + 3);
        assertThat(result).contains(GAUGE.toString().toLowerCase());
    }

    @Test
    void should_promifyStringContains2LinesInHeaderPlusSourceSizeLines_when_sourceIsPage() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        PrpcGauge gauge = PrpcGauge.builder()
            .id(new Meter.Id(METER_NAME_NAME, Tags.empty(), null, null, Meter.Type.GAUGE))
            .source(getPageSourceMock(VALUE_PROP_NAME_VALUE, 2.0))
            .valuePropName(VALUE_PROP_NAME_VALUE)
            .config(registry.config())
            .build();
        String result = gauge.promify();

        assertThat(result).hasLineCount(2 + 1);
        assertThat(StringUtils.countMatches(result, METER_NAME_NAME)).isEqualTo(2 + 1);
        assertThat(result).contains(GAUGE.toString().toLowerCase());
    }

    @Test
    void should_promifyStringIsEmpty_when_sourceCollectThrowsException() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        PrpcGauge gauge = PrpcGauge.builder()
            .id(new Meter.Id(METER_NAME_NAME, Tags.empty(), null, null, Meter.Type.GAUGE))
            .source(getThrowingSource())
            .valuePropName(VALUE_PROP_NAME_VALUE)
            .config(registry.config())
            .build();
        String result = gauge.promify();

        assertThat(result).isBlank();
    }
}