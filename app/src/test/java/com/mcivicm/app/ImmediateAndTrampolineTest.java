package com.mcivicm.app;

import org.junit.Test;

import io.reactivex.Scheduler;
import io.reactivex.internal.schedulers.ImmediateThinScheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * 两者的区别在于对待嵌套任务的处理方式，immediate在碰到嵌套任务时立即开始执行嵌套任务(先序)，而trampoline在碰到嵌套任务时是先把嵌套任务加入队列，等当前层的任务执行完之后再执行嵌套任务（层次）
 */

public class ImmediateAndTrampolineTest {
    @Test
    public void imme() throws Exception {

        Scheduler.Worker worker = ImmediateThinScheduler.INSTANCE.createWorker();//虽然immediate从Scheduler中拿掉，但是实现类还是有的
        log("Main start");
        worker.schedule(new Runnable() {
            @Override
            public void run() {
                log(" Outer start");
                sleepOneSecond();
                worker.schedule(new Runnable() {
                    @Override
                    public void run() {
                        log("  Inner start");
                        sleepOneSecond();
                        worker.schedule(new Runnable() {
                            @Override
                            public void run() {
                                log("   Inner Inner start");
                                sleepOneSecond();
                                log("   Inner Inner end");
                            }
                        });
                        log("  Inner end");
                    }
                });
                log(" Outer end");
            }
        });
        log("Main end");
        worker.dispose();
    }

    private void log(String s) {
        System.out.println(s);
    }

    private void sleepOneSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log("sleep failure");
        }
    }

    @Test
    public void tram() throws Exception {
        Scheduler.Worker worker = Schedulers.trampoline().createWorker();
        log("Main start");
        worker.schedule(new Runnable() {
            @Override
            public void run() {
                log(" Outer start");
                sleepOneSecond();
                worker.schedule(new Runnable() {
                    @Override
                    public void run() {
                        log("  Inner start");
                        sleepOneSecond();
                        worker.schedule(new Runnable() {
                            @Override
                            public void run() {
                                log("   Inner Inner start");
                                sleepOneSecond();
                                log("   Inner Inner end");
                            }
                        });
                        log("  Inner end");
                    }
                });
                log(" Outer end");
            }
        });
        log("Main end");
        worker.dispose();
    }
}
