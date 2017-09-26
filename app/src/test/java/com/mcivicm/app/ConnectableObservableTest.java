package com.mcivicm.app;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by zhang on 2017/9/26.
 */

public class ConnectableObservableTest {
    @Test
    public void name() throws Exception {
        Observable<Long> observable = Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Long> e) throws Exception {

                System.out.println("Establishing connection");

                e.setDisposable(new Disposable() {

                    boolean isDisposable = false;

                    @Override
                    public void dispose() {
                        System.out.println("Disconnecting");
                        isDisposable = true;
                    }

                    @Override
                    public boolean isDisposed() {
                        return isDisposable;
                    }
                });
            }
        });
        Disposable disposable1 = observable.subscribe();
        System.out.println("Subscribed 1");

        Disposable disposable2 = observable.subscribe();
        System.out.println("Subscribed 2");

        disposable1.dispose();
        System.out.println("Disposed 1");

        disposable2.dispose();
        System.out.println("Disposed 2");

    }

    @Test
    public void name1() throws Exception {
        Observable<Long> observable = Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Long> e) throws Exception {

                System.out.println("Establishing connection");

                e.setDisposable(new Disposable() {

                    boolean isDisposable = false;

                    @Override
                    public void dispose() {
                        System.out.println("Disconnecting");
                        isDisposable = true;
                    }

                    @Override
                    public boolean isDisposed() {
                        return isDisposable;
                    }
                });
            }
        });

        Observable<Long> lazy = observable.publish().refCount();
        System.out.println("Before subscribers");
        Disposable disposable1=lazy.subscribe();
        System.out.println("Subscribed 1");
        Disposable disposable2=lazy.subscribe();
        System.out.println("Subscribed 2");
        disposable1.dispose();
        System.out.println("disposed 1");
        disposable2.dispose();
        System.out.println("disposed 2");
    }
}
