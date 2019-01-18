package ru.sbrf.pegi18.mon.prpc;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import ru.sbrf.pegi18.mon.prpc.meter.MeterTestSupport;

import static io.prometheus.client.Collector.Type.*;
import static org.assertj.core.api.Assertions.assertThat;

class PrpcPrometheusMeterRegistryTest extends MeterTestSupport {

    @Test
    void should_scrapeStringContains2LinesInHeaderPlusSourceSizeLines_when_gaugeWithListSource() {
        PrpcPrometheusMeterRegistry registry = new PrpcPrometheusMeterRegistry();

        registry.gauge(METER_NAME_NAME, getSourceMock(VALUE_PROP_NAME_VALUE, 2.0, 5.0), VALUE_PROP_NAME_VALUE);

        String result = registry.scrape();
        assertThat(result).hasLineCount(2 + 2);
        assertThat(StringUtils.countMatches(result, METER_NAME_NAME)).isEqualTo(2 + 2);
        assertThat(result).contains(GAUGE.toString().toLowerCase());
    }

    @Test
    void should_scrapeStringContains2LinesInHeaderPlusSourceSizeLines_when_counterWithListSource() {
        PrpcPrometheusMeterRegistry registry = new PrpcPrometheusMeterRegistry();

        registry.counter(METER_NAME_NAME, getSourceMock(VALUE_PROP_NAME_VALUE, 2.0, 5.0), VALUE_PROP_NAME_VALUE);

        String result = registry.scrape();
        assertThat(result).hasLineCount(2 + 2);
        assertThat(StringUtils.countMatches(result, METER_NAME_NAME)).isEqualTo(2 + 2);
        assertThat(result).contains(COUNTER.toString().toLowerCase());
    }

    @Test
    void should_scrapeStringContains2LinesInHeaderPlusSourceSizeLines_when_timerWithListSource() {
        PrpcPrometheusMeterRegistry registry = new PrpcPrometheusMeterRegistry();

        registry.timer(METER_NAME_NAME, getSourceMock(VALUE_PROP_NAME_VALUE, 2.0, 5.0), VALUE_PROP_NAME_VALUE, VALUE_PROP_NAME_VALUE);

        String result = registry.scrape();
        assertThat(result).hasLineCount(2 + 2 * 2);
        assertThat(StringUtils.countMatches(result, METER_NAME_NAME)).isEqualTo(2 + 2 * 2);
        assertThat(result).contains(SUMMARY.toString().toLowerCase());
    }
}