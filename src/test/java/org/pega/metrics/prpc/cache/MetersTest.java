package org.pega.metrics.prpc.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pega.metrics.prpc.meter.AbstractPrpcMultiMeter;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MetersTest {

    private Meters meters;

    @BeforeEach
    void before() {
        meters = new Meters();
    }

    @Test
    void should_sizeBeCorrect_when_addIsInvokedConcurrently() {
        int size = 100;

        Stream.iterate(0, i -> i + 1).limit(size).parallel().forEach( i -> {
            meters.add(mock(AbstractPrpcMultiMeter.class));
        });

        assertThat(meters.size()).isEqualTo(size);
    }

    @Test
    void should_clearRemovesAllElements() {
        AbstractPrpcMultiMeter meter = mock(AbstractPrpcMultiMeter.class);

        meters.add(meter);
        meters.clear();

        assertThat(meters.size()).isEqualTo(0);
    }

    @Test
    void should_addBeCorrect_when_addIsInvokedOnSameObject() {
        AbstractPrpcMultiMeter meter1 = mock(AbstractPrpcMultiMeter.class);

        meters.add(meter1);
        meters.add(meter1);

        assertThat(meters.size()).isEqualTo(1);
    }

    @Test
    void should_registerInvokesMetersRegisterMethod() {
        AbstractPrpcMultiMeter meter1 = mock(AbstractPrpcMultiMeter.class);
        AbstractPrpcMultiMeter meter2 = mock(AbstractPrpcMultiMeter.class);

        meters.add(meter1);
        meters.add(meter2);

        meters.register();

        verify(meter1).register(true);
        verify(meter2).register(true);
    }
}