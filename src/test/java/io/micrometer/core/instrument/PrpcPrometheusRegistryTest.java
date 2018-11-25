package io.micrometer.core.instrument;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.prpc.PrpcSource;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by sp00x on 15-Nov-18.
 * Project: pg-micrometer
 */
public class PrpcPrometheusRegistryTest {

    @Test
    void test() {

        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        registry.gauge("name", Tags.of(Tag.of("t1", "v1")), 1);
        registry.gauge("name", Tags.of(Tag.of("t1", "v2")), 2);

        System.out.println(registry.scrape());

    }

    @Test
    void test3() {

        PrpcSource prpcSource = getPrpcSourceSpy(Arrays.asList("1.0", "2.00"));

        PrpcPrometheusRegistry registry = new PrpcPrometheusRegistry(PrometheusConfig.DEFAULT);
        registry.gauge("brand.new.prpc.gauge", Tags.of("key", "val"), prpcSource);

        System.out.println(registry.scrape());
    }

    @Test
    void test4() {

        PrpcSource prpcSource = getPrpcSourceSpy(Arrays.asList("1.0", "2.00"));

        PrpcPrometheusRegistry registry = new PrpcPrometheusRegistry(PrometheusConfig.DEFAULT);
        registry.gauge("brand.new.prpc.gauge", Tags.of("key", "val"), "Value", prpcSource);

        System.out.println(registry.scrape());
    }


    private PrpcSource getPrpcSourceSpy(List<String> valueList) {
        String valueProp = ".Value";

        ClipboardProperty property = getSourceResultsMock(valueProp, valueList);

        PrpcSource prpcSource = spy(AbstractPrpcSource.class);
        when(prpcSource.collect()).thenReturn(property);
        when(prpcSource.valueProp()).thenReturn(valueProp);
        return prpcSource;
    }

    private ClipboardProperty getSourceResultsMock(String valueProp, List<String> valueList) {
        List<ClipboardProperty> pxResultList = new LinkedList<>();

        valueList.forEach(v -> {
            ClipboardProperty valueProperty = mock(ClipboardProperty.class);
            when(valueProperty.getStringValue()).thenReturn(v);

            ClipboardPage meterPage = mock(ClipboardPage.class);
            when(meterPage.getProperty(valueProp)).thenReturn(valueProperty);

            ClipboardProperty meterProperty = mock(ClipboardProperty.class);
            when(meterProperty.getPageValue()).thenReturn(meterPage);

            pxResultList.add(meterProperty);
        });

        ClipboardProperty property = mock(ClipboardProperty.class);
        when(property.iterator()).thenReturn(pxResultList.iterator());
        return property;
    }
}
