package org.pega.metrics.prpc;

import org.junit.jupiter.api.Test;
import org.pega.metrics.prpc.cache.Meters;
import org.pega.metrics.prpc.cache.Registries;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsTest {

    @Test
    void should_returnRegistriesSingleton() {
        Registries rh1 = Metrics.getInstance().registries();
        Registries rh2 = Metrics.getInstance().registries();

        assertThat(rh1).isNotNull().isSameAs(rh2);
    }

    @Test
    void should_returnMetersSingleton() {
        Meters m1 = Metrics.getInstance().meters();
        Meters m2 = Metrics.getInstance().meters();

        assertThat(m1).isNotNull().isSameAs(m2);
    }
}