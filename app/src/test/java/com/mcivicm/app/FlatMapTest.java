package com.mcivicm.app;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by zhang on 2017/9/27.
 */

public class FlatMapTest {
    @Test
    public void name() throws Exception {
        Observable.just(10L, 1L)
                .flatMap(new Function<Long, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(@NonNull Long aLong) throws Exception {
                        return Observable.just(aLong).delay(aLong, TimeUnit.SECONDS);
                    }
                })
                .blockingSubscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        System.out.println(aLong);
                    }
                });

    }

    @Test
    public void name1() throws Exception {
        Observable.just(DayOfWeek.SUNDAY, DayOfWeek.MONDAY)
                .flatMap(new Function<DayOfWeek, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull DayOfWeek dayOfWeek) throws Exception {
                        return loadRecordFor(dayOfWeek);
                    }
                })
                .blockingSubscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println(s);
                    }
                });

    }

    private enum DayOfWeek {
        SUNDAY, MONDAY
    }

    private Observable<String> loadRecordFor(DayOfWeek dow) {
        switch (dow) {
            case SUNDAY:
                return Observable
                        .interval(90, TimeUnit.MILLISECONDS)
                        .take(5)
                        .map(new Function<Long, String>() {
                            @Override
                            public String apply(@NonNull Long aLong) throws Exception {
                                return "Sun-" + aLong;
                            }
                        });
            case MONDAY:
                return Observable
                        .interval(65, TimeUnit.MILLISECONDS)
                        .take(5)
                        .map(new Function<Long, String>() {
                            @Override
                            public String apply(@NonNull Long aLong) throws Exception {
                                return "Mon-" + aLong;
                            }
                        });
            default:
                return Observable.empty();
        }
    }

    @Test
    public void name3() throws Exception {
        Observable.just(DayOfWeek.SUNDAY, DayOfWeek.MONDAY)
                .concatMap(new Function<DayOfWeek, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull DayOfWeek dayOfWeek) throws Exception {
                        return loadRecordFor(dayOfWeek);
                    }
                })
                .blockingSubscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println(s);
                    }
                });

    }
}
