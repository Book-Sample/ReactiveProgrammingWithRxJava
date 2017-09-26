package com.mcivicm.app;

import org.junit.Test;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

/**
 * 有业绩才有股票
 */

public class PublishSubjectTest {
    @Test
    public void name() throws Exception {
        PublishSubject<Long> publishSubject = PublishSubject.create();
        publishSubject.subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                System.out.println("subscriber 0:" + aLong);
            }
        });
        publishSubject.onNext(1L);
        publishSubject.onNext(2L);
        publishSubject.onNext(3L);
        publishSubject.onNext(4L);
        publishSubject.onNext(5L);
        publishSubject.subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                System.out.println("subscriber 1:" + aLong);
            }
        });
        publishSubject.onNext(6L);
        publishSubject.onNext(7L);
        publishSubject.onNext(8L);
        publishSubject.subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                System.out.println("subscriber 2:" + aLong);
            }
        });
        publishSubject.onNext(9L);
        publishSubject.onNext(10L);
        publishSubject.onNext(11L);
        publishSubject.onComplete();
        publishSubject.subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Long aLong) {

            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {
                System.out.println("subscriber 3:" + "have nothing");
            }
        });

    }
}
