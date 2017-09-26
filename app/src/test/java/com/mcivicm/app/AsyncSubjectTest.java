package com.mcivicm.app;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.AsyncSubject;

/**
 * Subject is a useful tool for creating Observable instances when Observable.cre
 * ate(...) seems too complex to manage.
 * 复杂的逻辑用Subject
 */

public class AsyncSubjectTest {

    private AsyncSubject<Long> asyncSubject = AsyncSubject.create();

    @Test
    public void client() throws Exception {
        //取多个异步源的最后一个
        Observable.merge(Observable.just(1L).delay(5, TimeUnit.SECONDS), Observable.just(0L).delay(10, TimeUnit.SECONDS))

                .blockingSubscribe(new DisposableObserver<Long>() {

                    @Override
                    public void onNext(@NonNull Long aLong) {
                        System.out.println("server:" + aLong);
                        asyncSubject.onNext(aLong);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        System.out.println("server:" + "onComplete");
                        asyncSubject.onComplete();
                    }
                });

        asyncSubject.subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                System.out.println(aLong);
            }
        });

        asyncSubject.subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                System.out.println(aLong);
            }
        });
    }
}
