package ru.sbrf.pegi18.mon.prpc.source;

import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Created by sp00x on 28.06.2019.
 * Project: micrometer-prpc
 */
public class NewTest {

//    @BeforeEach
//    void beforeEach() {
//        ClipboardProperty noop = mock(ClipboardProperty.class);
//        SourceManager manager = new SourceManager(() -> noop);
//        SourceManager.init(manager);
//    }


    @Test
    void test3() {
        DataPageSource source = DataPageSource.builder().ruleName("D_P").build();
        System.out.println(source.hashCode());
        System.out.println(source);
        Object o = SourceManager.getInstance().getCache().getIfPresent(source.hashCode());
        System.out.println(o);
        System.out.println(SourceManager.getInstance().getCache().asMap());
    }

    @Test
    void test4() {
        ClipboardProperty prop = mock(ClipboardProperty.class, Answers.RETURNS_DEEP_STUBS);
        when(prop.isGroup()).thenReturn(true);
        when(prop.getPageValue(any()).getProperty(any()).toDouble()).thenReturn(6.8);

        DataPageSource source = sourceSpy(DataPageSource.builder().ruleName("D_P").build());
        doReturn(Optional.of(prop)).when(source).collect();
        doReturn(Optional.of(prop)).when(source).get();

        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
//        registry.gauge("some", SourceManager.getInstance(), SourceManager.getInstance().callback(source.hashCode(), "11", "dd"));

        System.out.println(registry.scrape());
    }

    static <T extends PrpcSource> T sourceSpy(T source) {
        T spy = spy(source);
        SourceManager.getInstance().getCache().asMap().remove(source.hashCode());
        SourceManager.getInstance().getCache().put(spy.hashCode(), spy);
        return spy;
    }

}
