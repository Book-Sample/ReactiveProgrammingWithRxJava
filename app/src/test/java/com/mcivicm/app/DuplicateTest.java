package com.mcivicm.app;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by zhang on 2017/9/28.
 */

public class DuplicateTest {

    private Observable<Integer> randomInts =
            Observable.create(new ObservableOnSubscribe<Integer>() {
                @Override
                public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                    Random random = new Random();
                    while (!e.isDisposed()) {
                        e.onNext(random.nextInt(10));
                        Thread.sleep(1000);
                    }
                }
            });

    @Test
    public void name() throws Exception {

        randomInts.take(10)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        System.out.print(integer + " ");
                    }
                });

        System.out.println();

    }

    @Test
    public void name1() throws Exception {

        Observable<Integer> uniqueRandomInts = randomInts
                .distinct()
                .take(10);

        uniqueRandomInts.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                System.out.print(integer + " ");
            }
        });
    }

    @Test
    public void name2() throws Exception {

        randomInts
                .distinct(new Function<Integer, String>() {
                    @Override
                    public String apply(@NonNull Integer integer) throws Exception {
                        return String.valueOf(integer);
                    }
                }, new Callable<Collection<? super String>>() {//distinc的这个参数到底是干嘛的                    @Override
                    public Collection<? super String> call() throws Exception {
                        List<String> list = new ArrayList<String>() {
                            @Override
                            public boolean add(String o) {
                                System.out.println("add:" + o);
                                return super.add(o);
                            }
                        };
                        list.add("1");
                        list.add("2");
                        list.add("3");
                        list.add("4");
                        list.add("5");
                        list.add("6");
                        list.add("7");
                        list.add("8");
                        list.add("9");
                        System.out.println(Arrays.toString(list.toArray()));
                        return list;
                    }
                })
                .take(10)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        System.out.println(integer);
                    }
                });

    }

    @Test
    public void distinctUntilChanged() throws Exception {
        randomInts
                .distinctUntilChanged(new Function<Integer, String>() {
                    @Override
                    public String apply(@NonNull Integer integer) throws Exception {
                        System.out.println("integer:" + integer);
                        return String.valueOf(integer);
                    }
                })
                .blockingSubscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        System.out.println(integer);
                    }
                });

    }
}
