package com.mcivicm.app.measure;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by zhang on 2017/10/10.
 */

public class MeasureTest {

    MetricRegistry metricRegistry = new MetricRegistry();

    @Before
    public void setUp() throws Exception {

        Slf4jReporter slf4jReporter = Slf4jReporter
                .forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger(MeasureTest.class))
                .build();
        slf4jReporter.start(1, TimeUnit.SECONDS);

    }

    @Test
    public void items() throws Exception {

        Counter items = metricRegistry.counter("items");
        Observable.interval(0, 500, TimeUnit.MILLISECONDS)
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        items.inc();
                    }
                })
                .blockingSubscribe();

    }

    private Random random = new Random();

    private Observable<Long> makeNetworkCall(long x) {
        int r = random.nextInt(10) + 1;//不为0
        System.out.println("random: " + r);
        return Observable.just(x)
                .delay(r, TimeUnit.SECONDS);
    }

    @Test
    public void concurrentNumber() throws Exception {
        Counter counter = metricRegistry.counter("counter");
        Observable.range(0, 25)
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        System.out.println("start " + integer);
                        counter.inc();
                    }
                })
                .flatMap(new Function<Integer, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(@NonNull Integer integer) throws Exception {
                        return makeNetworkCall(integer);
                    }
                })
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        System.out.println("dec: " + aLong);
                        counter.dec();
                    }
                })
                .blockingSubscribe();

    }

    @Test
    public void concurrentNumber1() throws Exception {

        Counter counter = metricRegistry.counter("counter");

        Observable.range(0, 25)
                .flatMap(new Function<Integer, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(@NonNull Integer integer) throws Exception {
                        return makeNetworkCall(integer)
                                .doOnSubscribe(new Consumer<Disposable>() {
                                    @Override
                                    public void accept(Disposable disposable) throws Exception {
                                        System.out.println("subscribe: " + integer);
                                        counter.inc();
                                    }
                                })
                                .doOnTerminate(new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        System.out.println("terminal: " + integer);
                                        counter.dec();
                                    }
                                });
                    }
                })
                .blockingSubscribe();

    }

    @Test
    public void timer() throws Exception {

        Observable<Long> external =
                Observable.timer(1500, TimeUnit.MILLISECONDS);

        Timer timer = metricRegistry.timer("timer");

        Observable<Long> externalWithTimer =
                Observable.defer(new Callable<ObservableSource<? extends Long>>() {
                    @Override
                    public ObservableSource<? extends Long> call() throws Exception {
                        return Observable.just(timer.time())//开始计时，得到上下文Context
                                .flatMap(new Function<Timer.Context, ObservableSource<Long>>() {
                                    @Override
                                    public ObservableSource<Long> apply(@NonNull Timer.Context context) throws Exception {
                                        return external
                                                .doOnComplete(new Action() {
                                                    @Override
                                                    public void run() throws Exception {
                                                        context.stop();//结束时停止计时
                                                    }
                                                });
                                    }
                                });
                    }
                });

        externalWithTimer.blockingSubscribe();
        Thread.sleep(3000);//wait reporter.
    }
}
