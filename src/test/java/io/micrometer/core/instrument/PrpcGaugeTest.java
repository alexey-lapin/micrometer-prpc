package io.micrometer.core.instrument;

import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.junit.jupiter.api.Test;
import ru.sbrf.pegi18.mon.prpc.PrpcGauge;

/**
 * @author lapin2-aa
 */
class PrpcGaugeTest {

    @Test
    void test1() {

        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        PrpcGauge gauge = PrpcGauge.builder()
            .id(new Meter.Id("name", null, null, null, Meter.Type.GAUGE))
            .valuePropName("Value")
            .config(registry.config())
            .build();


        System.out.println(gauge);
    }

}