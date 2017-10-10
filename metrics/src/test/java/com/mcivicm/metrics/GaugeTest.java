package com.mcivicm.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

import org.junit.Test;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhang on 2017/10/10.
 */

public class GaugeTest {

    public static Queue<String> q = new LinkedList<>();

    @Test
    public void name() throws Exception {
        //声明一个单位注册器
        MetricRegistry metricRegistry = new MetricRegistry();
        //注册一个感兴趣的单位，name方法接受可变长度的参数，可产生分级的名称。
        metricRegistry.register(MetricRegistry.name(GaugeTest.class, "queue", "size"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return q.size();
            }
        });
        //根据单位注册器产生一个报告器
        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricRegistry).build();
        //运行报告器
        consoleReporter.start(1, TimeUnit.SECONDS);
        //运行实际系统
        int i = 0;
        while (i++ < 100) {
            Thread.sleep(1000);
            q.add("Job-xxx");
        }

    }
}
