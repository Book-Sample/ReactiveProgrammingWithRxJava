package com.mcivicm.metrics;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import org.junit.Test;

import java.util.Random;

/**
 * Created by zhang on 2017/10/10.
 */

public class TimerTest {

    private static Random random = new Random();

    @Test
    public void name() throws Exception {

        MetricRegistry registry = new MetricRegistry();

        //统计时间段
        Timer timer = registry.timer(MetricRegistry.name(TimerTest.class, "get-latency"));

        JmxReporter reporter = JmxReporter.forRegistry(registry).build();
        reporter.start();

        int num = 0;
        while (num++ < 1000) {
            System.out.println("num: " + num);
            Timer.Context context = timer.time();//计时开始
            Thread.sleep(500);
            context.stop();//计时结束
        }

    }
}
