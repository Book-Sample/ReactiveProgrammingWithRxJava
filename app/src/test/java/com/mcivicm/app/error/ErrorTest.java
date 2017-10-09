package com.mcivicm.app.error;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by zhang on 2017/10/9.
 */

public class ErrorTest {
    @Test
    public void onErrorReturn() throws Exception {
        Observable.range(0, 10)
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(@NonNull Integer integer) throws Exception {
                        return String.valueOf(integer);
                    }
                })
                .onErrorReturn(new Function<Throwable, String>() {
                    @Override
                    public String apply(@NonNull Throwable throwable) throws Exception {
                        //bla bla ... omit 3000 words
                        return null;
                    }
                });
    }

    //Replacing errors with 【a fixed result】 using onErrorReturnItem()
    @Test
    public void onErrorReturnItem() throws Exception {
        Observable.range(0, 10)
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(@NonNull Integer integer) throws Exception {
                        return String.valueOf(integer);
                    }
                })
                .onErrorReturnItem("error");
    }

    //Lazily computing fallback value using onErrorResumeNext(）
    @Test
    public void onErrorResumeNext() throws Exception {
        Observable.range(0, 10)
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(@NonNull Integer integer) throws Exception {
                        return String.valueOf(integer);
                    }
                })
                .onErrorResumeNext(Observable.just("error"));

    }

    private Observable<Long> delayBeforeCompletion =
            Observable
                    .<Long>empty()
                    .delay(200, TimeUnit.MILLISECONDS);

    private Observable<Long> confirmation = Observable
            .just(1L, 2L, 3L)
            .delay(100, TimeUnit.MILLISECONDS)
            .concatWith(delayBeforeCompletion);

    @Test
    public void timeout() throws Exception {

        confirmation
                .timeout(200, TimeUnit.MILLISECONDS)
                .blockingSubscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        System.out.println(aLong);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (throwable instanceof TimeoutException) {
                            System.out.println("Too long");
                        } else {
                            System.out.println(throwable.getMessage());
                        }
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("onComplete");
                    }
                });

    }

    @Test
    public void timeout1() throws Exception {
        confirmation
                .timeout(
                        Observable.timer(110, TimeUnit.MILLISECONDS),
                        new Function<Long, ObservableSource<Long>>() {
                            @Override
                            public ObservableSource<Long> apply(@NonNull Long aLong) throws Exception {
                                System.out.println("second: " + aLong);//表示从上游发送的值
                                return Observable.timer(210, TimeUnit.MILLISECONDS);//重要的是定义的时间窗，可根据发送的数据项改变
                            }
                        })
                .blockingSubscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        System.out.println(aLong);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        System.out.println("error: " + throwable.getMessage());
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("onComplete");
                    }
                });
    }

    private Observable<String> risky() {
        return Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                if (Math.random() < 0.1) {
                    Thread.sleep((long) (Math.random() * 2000));
                    return "OK";
                } else {
                    throw new RuntimeException("Transient");
                }
            }
        });
    }

    @Test
    public void retry() throws Exception {
        risky()
                .timeout(1, TimeUnit.SECONDS)
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        System.out.println("Will retry " + throwable.getMessage());
                    }
                })
                .retry()//有多种重载方法
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println(s);
                    }
                });

    }

    @Test
    public void retryWhen() throws Exception {
        risky()
                .timeout(1, TimeUnit.SECONDS)
                .retryWhen(new Function<Observable<Throwable>, ObservableSource<Throwable>>() {
                    @Override
                    public ObservableSource<Throwable> apply(@NonNull Observable<Throwable> throwableObservable) throws Exception {
                        return throwableObservable.delay(1, TimeUnit.SECONDS);
                    }
                });

    }
}
