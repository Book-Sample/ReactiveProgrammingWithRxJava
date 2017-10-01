package com.mcivicm.app;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Timed;

/**
 * Created by zhang on 2017/10/1.
 */

public class FlowControlTest {
    @Test
    public void sample() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        long startTime = System.currentTimeMillis();
        Observable
                .interval(7, TimeUnit.MILLISECONDS)
                .timestamp()
                .sample(1, TimeUnit.SECONDS)
                .map(new Function<Timed<Long>, String>() {
                    @Override
                    public String apply(@NonNull Timed<Long> longTimed) throws Exception {
                        return longTimed.time(TimeUnit.MILLISECONDS) - startTime + "ms: " + longTimed.value();
                    }
                })
                .take(5)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        System.out.println(s);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });
        latch.await();

    }

    private <T> Observable<T> delayedCompletion() {
        return Observable.<T>empty().delay(1, TimeUnit.SECONDS);
    }

    private Observable<String> names = Observable
            .just("Mary", "Patricia", "Linda",
                    "Barbara",
                    "Elizabeth", "Jennifer", "Maria", "Susan",
                    "Margaret", "Dorothy");

    private Observable<Long> absoluteDelayMillis = Observable
            .just(0.1, 0.6, 0.9,
                    1.1,
                    3.3, 3.4, 3.5, 3.6,
                    4.4, 4.8)
            .map(new Function<Double, Long>() {
                @Override
                public Long apply(@NonNull Double aDouble) throws Exception {
                    return Double.valueOf(aDouble * 1000).longValue();
                }
            });

    private Observable<String> delayedNames = names
            .zipWith(absoluteDelayMillis, new BiFunction<String, Long, Observable<String>>() {
                @Override
                public Observable<String> apply(@NonNull String s, @NonNull Long aLong) throws Exception {
                    return Observable.just(s).delay(aLong, TimeUnit.MILLISECONDS);
                }
            })
            .flatMap(new Function<Observable<String>, ObservableSource<String>>() {
                @Override
                public ObservableSource<String> apply(@NonNull Observable<String> s) throws Exception {
                    return s;
                }
            });

    @Test
    public void sample2() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        delayedNames
                .concatWith(delayedCompletion())//延迟一秒
                .sample(1, TimeUnit.SECONDS)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        System.out.println(s);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        System.out.println(e.getMessage());
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });
        latch.await();
    }

    @Test
    public void throttleFirst() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        delayedNames
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        System.out.println(s);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        System.out.println(e.getMessage());
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });
        latch.await();

    }

    @Test
    public void throttleLast() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        delayedNames
                .throttleLast(1, TimeUnit.SECONDS)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        System.out.println(s);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        System.out.println(e.getMessage());
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });
        latch.await();

    }

    @Test
    public void buffer() throws Exception {
        Observable
                .range(1, 7)
                .buffer(3)
                .subscribe(new Consumer<List<Integer>>() {
                    @Override
                    public void accept(List<Integer> list) throws Exception {
                        System.out.println(Arrays.toString(list.toArray()));
                    }
                });
    }

    private double averageOfList(List<Double> list) {
        return list.stream().collect(Collectors.averagingDouble(new ToDoubleFunction<Double>() {
            @Override
            public double applyAsDouble(Double value) {
                return value;
            }
        }));
    }

    @Test
    public void buffer1() throws Exception {
        Random random = new Random();
        Observable
                .defer(new Callable<ObservableSource<Double>>() {
                    @Override
                    public ObservableSource<Double> call() throws Exception {
                        return Observable.just(random.nextGaussian());
                    }
                })
                .repeat(1000)
                .buffer(100, 1)
                .map(new Function<List<Double>, Double>() {
                    @Override
                    public Double apply(@NonNull List<Double> doubles) throws Exception {
                        return averageOfList(doubles);
                    }
                })
                .subscribe(new Consumer<Double>() {
                    @Override
                    public void accept(Double aDouble) throws Exception {
                        System.out.println(aDouble);
                    }
                });

    }

    @Test
    public void buffer2() throws Exception {
        Observable
                .range(1, 7)
                .buffer(1, 2)
                .subscribe(new Consumer<List<Integer>>() {
                    @Override
                    public void accept(List<Integer> list) throws Exception {
                        System.out.println(list);
                    }
                });

    }

    @Test
    public void buffer3() throws Exception {
        Observable
                .range(1, 7)
                .buffer(1, 2)
                .flatMapIterable(new Function<List<Integer>, Iterable<Integer>>() {
                    @Override
                    public Iterable<Integer> apply(@NonNull List<Integer> list) throws Exception {
                        return list;
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        System.out.println(integer);
                    }
                });

    }

    @Test
    public void buffer4() throws Exception {
        delayedNames
                .buffer(1L, TimeUnit.SECONDS)
                .blockingSubscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> strings) throws Exception {
                        System.out.println(strings);
                    }
                });

    }
}
