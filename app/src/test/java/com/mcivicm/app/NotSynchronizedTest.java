package com.mcivicm.app;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Timed;

/**
 * Created by zhang on 2017/9/28.
 */

public class NotSynchronizedTest {
    private Observable<Long> red = Observable.interval(10, TimeUnit.MILLISECONDS);
    private Observable<Long> green = Observable.interval(10, TimeUnit.MILLISECONDS);

    @Test
    public void name() throws Exception {
        Observable.zip(
                red.timestamp(),
                green.timestamp(),
                new BiFunction<Timed<Long>, Timed<Long>, Long>() {
                    @Override
                    public Long apply(@NonNull Timed<Long> longTimed, @NonNull Timed<Long> longTimed2) throws Exception {
                        return longTimed.time(TimeUnit.MILLISECONDS) - longTimed2.time(TimeUnit.MILLISECONDS);
                    }
                })
                .blockingSubscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        System.out.println(aLong);
                    }
                });

    }
}
