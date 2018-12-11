import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by sp00x on 20-Oct-18.
 * Project: pg-micrometer
 */
public class CommonTest {

    @Test
    void test() {
        MeterRegistry registry = new SimpleMeterRegistry();

        Counter counter = registry.counter("some.counter", "tag1", "val1");
        counter.increment();

        System.out.println(registry.getMeters());
    }

    @Test
    void test2() throws Exception {
//        MeterRegistry registry = new SimpleMeterRegistry();
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

//        Timer timer = registry.timer("some.timer", "tag1", "val1");
        Timer timer = Timer.builder("some.timer")
                .publishPercentiles(0.1, 0.5, 0.9)
//                .publishPercentileHistogram()
                .register(registry);
//        Timer.Sample sample = Timer.start(registry);
//        Thread.sleep(Math.abs(new Random().nextInt()) * 1000);
//        Thread.sleep(1000);
//        sample.stop(registry.timer("some.timer", "tag1", "val1"));

        timer.record(4, TimeUnit.SECONDS);
        timer.record(4, TimeUnit.SECONDS);
        timer.record(4, TimeUnit.SECONDS);
        timer.record(5, TimeUnit.SECONDS);
        timer.record(5, TimeUnit.SECONDS);
        timer.record(5, TimeUnit.SECONDS);
        timer.record(5, TimeUnit.SECONDS);
        timer.record(5, TimeUnit.SECONDS);
        timer.record(5, TimeUnit.SECONDS);
        timer.record(3, TimeUnit.SECONDS);
        timer.record(2, TimeUnit.SECONDS);
        timer.record(1, TimeUnit.SECONDS);

        System.out.println(registry.scrape());
//        String.valueOf
    }

    @Test
    void test3() {
        Tags t = Tags.of(Tag.of("v1", "v2"));
        System.out.println(t);
        System.out.println(t.hashCode());
    }
}
