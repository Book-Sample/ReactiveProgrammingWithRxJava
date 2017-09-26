package com.mcivicm.app;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * Created by zhang on 2017/9/23.
 */

public class AsyncSync {
    @Test
    public void name() throws Exception {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<Integer> e) throws Exception {
                //for educational purpose
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        while (i < 5) {
                            e.onNext(i);
                            ++i;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e1) {
                                e.onError(e1);
                            }
                        }
                        e.onComplete();
                    }
                });
                thread.setName("background thread");
                thread.start();
            }
        }).doOnNext(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                System.out.println(Thread.currentThread());
            }
        }).filter(new Predicate<Integer>() {
            @Override
            public boolean test(@NonNull Integer integer) throws Exception {
                return integer % 2 == 0;
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(@NonNull Integer integer) throws Exception {
                return "Value " + integer + " processed on " + Thread.currentThread();
            }
        }).blockingSubscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull String s) {
                System.out.println("Some Value => " + s + " subscribe on " + Thread.currentThread());
            }

            @Override
            public void onError(@NonNull Throwable e) {
                System.out.println("error: " + e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
        System.out.println("Will print BEFORE values are emitted");

    }
}
