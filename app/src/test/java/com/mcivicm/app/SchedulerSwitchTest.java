package com.mcivicm.app;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;


/**
 * 1. subscribeOn可以放在Observable和Observer之间的【任何】位置，决定【ObservableOnSubscribe】的执行线程，多个调用subscribeOn仅【第一次】有效
 * 2. observeOn决定【之后】操作符的执行线程，多次调用时，每一次调用都生效
 */

public class SchedulerSwitchTest {

    private ThreadFactory threadFactory(String pattern) {
        return new ThreadFactoryBuilder()
                .setNameFormat(pattern)
                .build();
    }

    private ExecutorService poolA = Executors.newFixedThreadPool(10, threadFactory("Scheduler-A-%d"));
    private ExecutorService poolB = Executors.newFixedThreadPool(10, threadFactory("Scheduler-B-%d"));
    private ExecutorService poolC = Executors.newFixedThreadPool(10, threadFactory("Scheduler-C-%d"));

    Scheduler schedulerA = Schedulers.from(poolA);
    Scheduler schedulerB = Schedulers.from(poolB);
    Scheduler schedulerC = Schedulers.from(poolC);

    private void log(String s) {
        System.out.println(
                System.currentTimeMillis() + "\t| " +
                        Thread.currentThread().getName() + "\t| " +
                        s);
    }

    private Observable<String> simple() {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                log("Subscribed");
                e.onNext("A");
                e.onNext("B");
                e.onComplete();
            }
        });
    }

    private Observable<String> intervalSimple() {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                log("Subscribed");
                Thread.sleep(1000);
                e.onNext("A");
                Thread.sleep(1000);
                e.onNext("B");
                Thread.sleep(1000);
                e.onComplete();
            }
        });
    }

    @Test
    public void same() throws Exception {
        log("Starting");
        final Observable<String> obs = simple();
        log("Created");
        //to indicate it is lazy.
        final Observable<String> obs2 = obs
                .map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String s) throws Exception {
                        return s;
                    }
                })
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(@NonNull String s) throws Exception {
                        return true;
                    }
                });
        log("Transformed");
        obs2.subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                log("Got " + s);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                log("Completed");
            }
        });
        log("Exiting");
    }

    @Test
    public void background() throws Exception {
        log("Starting");
        final Observable<String> obs = simple();
        log("Created");
        obs.subscribeOn(schedulerA)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        log("Got " + s);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        log("Completed");
                    }
                });
        log("Exiting");
    }

    @Test
    public void manyBackground() throws Exception {
        log("Starting");
        final Observable<String> obs = simple();
        log("Created");
        obs.subscribeOn(schedulerA)
                .subscribeOn(schedulerB)
                .subscribeOn(schedulerC)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        log("Got " + s);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        log("Completed");
                    }
                });
        log("Exiting");
    }

    @Test
    public void position() throws Exception {
        //the book said that the position of subscribeOn() is not relevant.
        //but i doubt it.
        //finally, it proves to be right.
        log("Starting");
        final Observable<String> obs = simple();
        log("Created");
        obs

                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        log(s);
                    }
                })

                .map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String s) throws Exception {
                        return s + '1';
                    }
                })

                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        log(s);
                    }
                })

                .map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String s) throws Exception {
                        return s + '2';
                    }
                })

                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        log(s);
                    }
                })
                .subscribeOn(schedulerA)//the position is not relevant? i doubt.But it is real.
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        log("Got " + s);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        log("Completed");
                    }
                });
        log("Exiting");
        Thread.sleep(1000);
    }


    private class RxGroceries {

        Observable<BigDecimal> purchase(String name, int quality) {
            return Observable.fromCallable(new Callable<BigDecimal>() {
                @Override
                public BigDecimal call() throws Exception {
                    return doPurchase(name, quality);
                }
            });
        }

        BigDecimal doPurchase(String name, int quality) {
            log("Purchasing " + quality + " " + name);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log("Done " + quality + " " + name);
            return BigDecimal.ZERO;
        }
    }

    private RxGroceries rxGroceries = new RxGroceries();

    @Test
    public void parallel1() throws Exception {
        //they just sequentially execute in a thread.
        CountDownLatch latch = new CountDownLatch(1);
        Observable<BigDecimal> totalPrice =
                Observable
                        .just("bread", "butter", "milk", "tomato", "cheese")
                        .subscribeOn(schedulerA)
                        .map(new Function<String, BigDecimal>() {
                            @Override
                            public BigDecimal apply(@NonNull String s) throws Exception {
                                return rxGroceries.doPurchase(s, 1);
                            }
                        })
                        .reduce(new BiFunction<BigDecimal, BigDecimal, BigDecimal>() {
                            @Override
                            public BigDecimal apply(@NonNull BigDecimal bigDecimal, @NonNull BigDecimal bigDecimal2) throws Exception {
                                return bigDecimal.add(bigDecimal2);
                            }
                        })
                        .toObservable();
        totalPrice.subscribe(new Observer<BigDecimal>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull BigDecimal bigDecimal) {

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

    @Test
    public void parallel2() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable
                .just("bread", "butter", "milk", "tomato", "cheese")
                .subscribeOn(schedulerA)
                //use flatMap does not work at all.
                .flatMap(new Function<String, ObservableSource<BigDecimal>>() {
                    @Override
                    public ObservableSource<BigDecimal> apply(@NonNull String s) throws Exception {
                        return rxGroceries.purchase(s, 1);
                    }
                })
                .reduce(new BiFunction<BigDecimal, BigDecimal, BigDecimal>() {
                    @Override
                    public BigDecimal apply(@NonNull BigDecimal bigDecimal, @NonNull BigDecimal bigDecimal2) throws Exception {
                        return bigDecimal.add(bigDecimal2);
                    }
                })
                .toObservable()
                .subscribe(new Observer<BigDecimal>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull BigDecimal bigDecimal) {

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

    @Test
    public void parallel3() throws Exception {
        Observable<BigDecimal> totalPrice =
                Observable.just("bread", "butter", "milk", "tomato", "cheese")
                        .flatMap(new Function<String, ObservableSource<BigDecimal>>() {
                            @Override
                            public ObservableSource<BigDecimal> apply(@NonNull String s) throws Exception {
                                return rxGroceries
                                        .purchase(s, 1)
                                        //subscribeOn means a new worker or thread.
                                        .subscribeOn(schedulerA);//here, it really parallelled.
                            }
                        })
                        .reduce(new BiFunction<BigDecimal, BigDecimal, BigDecimal>() {
                            @Override
                            public BigDecimal apply(@NonNull BigDecimal bigDecimal, @NonNull BigDecimal bigDecimal2) throws Exception {
                                return bigDecimal.add(bigDecimal2);
                            }
                        })
                        .toObservable();
        CountDownLatch latch = new CountDownLatch(1);
        totalPrice.subscribe(new Observer<BigDecimal>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull BigDecimal bigDecimal) {

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

    @Test
    public void groupBy() throws Exception {
        Observable<BigDecimal> totalPrice =
                Observable
                        .just("bread", "butter", "egg", "milk",
                                "tomato", "cheese", "tomato",
                                "egg", "egg")
                        .groupBy(new Function<String, String>() {
                            @Override
                            public String apply(@NonNull String s) throws Exception {
                                return s;//按名称分组
                            }
                        })
                        .flatMap(new Function<GroupedObservable<String, String>, ObservableSource<Pair<String, Long>>>() {
                            @Override
                            public ObservableSource<Pair<String, Long>> apply(@NonNull GroupedObservable<String, String> stringStringGroupedObservable) throws Exception {
                                return stringStringGroupedObservable
                                        .count()
                                        .map(new Function<Long, Pair<String, Long>>() {
                                            @Override
                                            public Pair<String, Long> apply(@NonNull Long aLong) throws Exception {
                                                return Pair.of(stringStringGroupedObservable.getKey(), aLong);
                                            }
                                        })
                                        .toObservable();
                            }
                        })
                        .flatMap(new Function<Pair<String, Long>, Observable<BigDecimal>>() {
                            @Override
                            public Observable<BigDecimal> apply(@NonNull Pair<String, Long> stringLongPair) throws Exception {
                                return rxGroceries
                                        .purchase(stringLongPair.getKey(), stringLongPair.getValue().intValue())
                                        .subscribeOn(schedulerA);
                            }
                        })
                        .reduce(new BiFunction<BigDecimal, BigDecimal, BigDecimal>() {
                            @Override
                            public BigDecimal apply(@NonNull BigDecimal bigDecimal, @NonNull BigDecimal bigDecimal2) throws Exception {
                                return bigDecimal.add(bigDecimal2);
                            }
                        })
                        .toObservable();
        totalPrice.blockingSubscribe(new Observer<BigDecimal>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull BigDecimal bigDecimal) {

            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Test
    public void observeOn() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        log("Starting");
        final Observable<String> obs = intervalSimple();
        log("Created");
        obs
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        log("Found 1: " + s);
                    }
                })
                .observeOn(schedulerA)
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        log("Found 2: " + s);
                    }
                })
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        log("Got 1: " + s);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        log("Completed");
                        latch.countDown();
                    }
                });
        log("Existing");
        latch.await();
    }

    @Test
    public void observeOn1() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        log("Starting");
        final Observable<String> obs = intervalSimple();
        log("Created");
        obs
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        log("Found 1: " + s);
                    }
                })
                .observeOn(schedulerB)
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        log("Found 2: " + s);
                    }
                })
                .observeOn(schedulerC)
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        log("Found 3: " + s);
                    }
                })
                .subscribeOn(schedulerA)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        log("Got 1: " + s);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        log("Completed");
                        latch.countDown();
                    }
                });
        log("Existing");
        latch.await();
    }

    @Test
    public void join() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        log("Starting");
        Observable<String> obs = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                log("Subscribed");
                e.onNext("A");
                e.onNext("B");
                e.onNext("C");
                e.onNext("D");
                e.onComplete();
            }
        });
        log("Created");
        obs
                .subscribeOn(schedulerA)
                .flatMap(new Function<String, ObservableSource<UUID>>() {
                    @Override
                    public ObservableSource<UUID> apply(@NonNull String s) throws Exception {
                        return store(s).subscribeOn(schedulerB);
                    }
                })
                .observeOn(schedulerC)
                .subscribe(new Observer<UUID>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull UUID uuid) {
                        log("Got: " + uuid);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        log("Completed");
                        latch.countDown();
                    }
                });
        log("Existing");
        latch.await();
    }

    private Observable<UUID> store(String s) {
        return Observable.create(new ObservableOnSubscribe<UUID>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<UUID> e) throws Exception {
                log("Storing " + s);
                //hard work, need time
                e.onNext(UUID.randomUUID());
                e.onComplete();
            }
        });
    }
}
