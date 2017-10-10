package com.mcivicm.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhang on 2017/10/10.
 */

public class MeterTest {

    private static Random random = new Random();

    @Test
    public void name() throws Exception {
        //声明注册器，并注册单位
        MetricRegistry metricRegistry = new MetricRegistry();
        Meter tps = metricRegistry.meter(MetricRegistry.name(MetricTest.class, "request", "tps"));
        //构造打印器
        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricRegistry).build();
        consoleReporter.start(1, TimeUnit.SECONDS);
        //实际的逻辑
        int n = 0;
        while (n++ < 20) {
            System.out.println("the n: " + n);
            tps.mark(random.nextInt(5));//频率，出现一个记录一次
            Thread.sleep(1000);
        }
    }
}
