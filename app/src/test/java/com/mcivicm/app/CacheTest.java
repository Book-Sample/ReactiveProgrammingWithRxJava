package com.mcivicm.app;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Created by zhang on 2017/9/23.
 */

public class CacheTest {
    @Test
    public void name() throws Exception {
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                System.out.println("create");
                e.onNext(1);
                e.onComplete();
            }
        }).cache();
        System.out.println("before any subscribe");
        observable.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                System.out.println("first:" + integer);
            }
        });
        System.out.println("after first");
        observable.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                System.out.println("second:" + integer);
            }
        });
        System.out.println("after second");
    }
}
