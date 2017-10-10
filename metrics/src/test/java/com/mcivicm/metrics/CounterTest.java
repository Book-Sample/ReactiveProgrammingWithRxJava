package com.mcivicm.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

import org.junit.Test;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhang on 2017/10/10.
 */

public class CounterTest {

    private static Queue<String> q = new LinkedBlockingQueue<>();
    private static Counter jobs;
    private static Random random = new Random();

    private void addJob(String job) {
        jobs.inc();
        q.offer(job);
    }

    private String removeJob() {
        jobs.dec();
        return q.poll();
    }

    @Test
    public void name() throws Exception {

        MetricRegistry metricRegistry = new MetricRegistry();
        jobs = metricRegistry.counter(MetricRegistry.name(Queue.class, "pending-jobs", "size"));
        //实时报告物理量中的值
        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricRegistry).build();
        consoleReporter.start(1, TimeUnit.SECONDS);

        int num = 0;
        while (num++ < 100) {
            Thread.sleep(200);
            if (random.nextDouble() > 0.7) {
                String job = removeJob();
//                System.out.println("take job : " + job);
             } else {
                String job = "Job-" + num;
                addJob(job);
//                System.out.println("add job : " + job);
            }
        }

    }
}
