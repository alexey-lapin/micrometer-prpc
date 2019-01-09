package ru.sbrf.pegi18.mon;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author lapin2-aa
 */
class RegistryHolderTest {

    private static final String REGISTRY_NAME_TEST_1 = "test1";

    @Test
    void should_getInstanceReturnsSameObject() {
        assertThat(RegistryHolder.getInstance()).isSameAs(RegistryHolder.getInstance());
    }

    @Test
    void should_putAndGetOperatesWithSameObject() {
        MeterRegistry registry = mock(MeterRegistry.class);

        RegistryHolder.getInstance().put(REGISTRY_NAME_TEST_1, registry);

        assertThat(RegistryHolder.getInstance().get(REGISTRY_NAME_TEST_1)).isSameAs(registry);
    }

    @Test
    void should_clearRemoveAllObjects() {
        MeterRegistry registry = mock(MeterRegistry.class);

        RegistryHolder.getInstance().put(REGISTRY_NAME_TEST_1, registry);
        RegistryHolder.getInstance().clear();

        assertThat(RegistryHolder.getInstance().get(REGISTRY_NAME_TEST_1)).isNull();
    }

    @Test
    void should_sizeReturnsActualHolderSize_when_putInvokesConcurrently() {
        int size = 100;

        Stream.iterate(0, i -> i + 1).limit(size).parallel().forEach(i -> {
            RegistryHolder.getInstance().put(String.valueOf(i), mock(MeterRegistry.class));
        });

        assertThat(RegistryHolder.getInstance().size()).isEqualTo(size);
    }
}