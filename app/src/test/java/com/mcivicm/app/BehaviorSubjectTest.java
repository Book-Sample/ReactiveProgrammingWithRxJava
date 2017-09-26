package com.mcivicm.app;

import org.junit.Test;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;

/**
 * 一来就有股票
 */

public class BehaviorSubjectTest {
    @Test
    public void name() throws Exception {
        BehaviorSubject<Long> behaviorSubject = BehaviorSubject.createDefault(-1L);
        behaviorSubject.subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                System.out.println("subscriber 0:" + aLong);
            }
        });
        behaviorSubject.onNext(1L);
        behaviorSubject.onNext(2L);
        behaviorSubject.onNext(3L);
        behaviorSubject.onNext(4L);
        behaviorSubject.onNext(5L);
        behaviorSubject.subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                System.out.println("subscriber 1:" + aLong);
            }
        });
        behaviorSubject.onNext(6L);
        behaviorSubject.onNext(7L);
        behaviorSubject.onNext(8L);
        behaviorSubject.subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                System.out.println("subscriber 2:" + aLong);
            }
        });
        behaviorSubject.onNext(9L);
        behaviorSubject.onNext(10L);
        behaviorSubject.onNext(11L);
        behaviorSubject.onComplete();
        behaviorSubject.subscribe(new Observer<Long>() {
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
