package org.pega.metrics.prpc.cache;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RegistriesTest {

    private static final String REGISTRY_NAME_TEST_1 = "test1";

    private Registries registries;

    @BeforeEach
    void before() {
        registries = new Registries();
    }

//    @Test
//    void should_getInstanceReturnsSameObject() {
//        assertThat(Registries.getInstance()).isSameAs(Registries.getInstance());
//    }

    @Test
    void should_putAndGetOperatesWithSameObject() {
        MeterRegistry registry = mock(MeterRegistry.class);

        registries.put(REGISTRY_NAME_TEST_1, registry);

        assertThat(registries.get(REGISTRY_NAME_TEST_1)).isSameAs(registry);
    }

    @Test
    void should_clearRemoveAllObjects() {
        MeterRegistry registry = mock(MeterRegistry.class);

        registries.put(REGISTRY_NAME_TEST_1, registry);
        registries.clear();

        assertThat(registries.get(REGISTRY_NAME_TEST_1)).isNull();
    }

    @Test
    void should_sizeReturnsActualHolderSize_when_putInvokesConcurrently() {
        int size = 100;

        Stream.iterate(0, i -> i + 1).limit(size).parallel().forEach(i -> {
            registries.put(String.valueOf(i), mock(MeterRegistry.class));
        });

        assertThat(registries.size()).isEqualTo(size);
    }
}