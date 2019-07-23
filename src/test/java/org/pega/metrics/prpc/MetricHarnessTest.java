package org.pega.metrics.prpc;

import org.junit.jupiter.api.Test;
import org.pega.metrics.prpc.cache.Registries;

class MetricHarnessTest {


    @Test
    void test() {
        Registries rh = MetricHarness.getInstance().registryHolder();
        System.out.println(rh);
        Registries rh1 = MetricHarness.getInstance().registryHolder();
        System.out.println(rh1);
    }
}