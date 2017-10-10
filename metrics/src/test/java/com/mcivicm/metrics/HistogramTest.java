package com.mcivicm.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhang on 2017/10/10.
 */

public class HistogramTest {

    private static Random random = new Random();

    @Test
    public void name() throws Exception {
        MetricRegistry registry = new MetricRegistry();

        Histogram histogram = new Histogram(new ExponentiallyDecayingReservoir());
        registry.register(MetricRegistry.name(HistogramTest.class, "request", "histogram"), histogram);

        ConsoleReporter reporter = ConsoleReporter.forRegistry(registry).build();
        reporter.start(1, TimeUnit.SECONDS);

        int num = 0;
        while (num++ < 100) {
            Thread.sleep(1000);
            histogram.update(random.nextInt(100000));
        }
    }
}
