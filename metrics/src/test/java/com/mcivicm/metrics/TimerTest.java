package com.mcivicm.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhang on 2017/10/10.
 */

public class TimerTest {

    private static Random random = new Random();

    @Test
    public void name() throws Exception {
        MetricRegistry registry = new MetricRegistry();

        Timer timer = registry.timer(MetricRegistry.name(TimerTest.class, "get-latency"));

        ConsoleReporter reporter = ConsoleReporter.forRegistry(registry).build();
        reporter.start(1, TimeUnit.SECONDS);

        int num = 0;
        while (num++ < 10) {
            Timer.Context context = timer.time();
            Thread.sleep(random.nextInt(1000));
            context.stop();
        }
    }
}
