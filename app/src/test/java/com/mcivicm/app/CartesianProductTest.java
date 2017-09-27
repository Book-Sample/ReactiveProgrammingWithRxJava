package com.mcivicm.app;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by zhang on 2017/9/27.
 */

public class CartesianProductTest {
    private Observable<Integer> oneToEight = Observable.range(1, 8);
    private Observable<String> ranks = oneToEight.map(new Function<Integer, String>() {
        @Override
        public String apply(@NonNull Integer integer) throws Exception {
            return integer.toString();
        }
    });
    private Observable<String> files = oneToEight
            .map(new Function<Integer, String>() {
                @Override
                public String apply(@NonNull Integer integer) throws Exception {
                    return Character.toString((char) ('a' + integer - 1));
                }
            });
    private Observable<String> squares = files
            .flatMap(new Function<String, ObservableSource<String>>() {
                @Override
                public ObservableSource<String> apply(@NonNull final String file) throws Exception {
                    return ranks.map(new Function<String, String>() {
                        @Override
                        public String apply(@NonNull String rank) throws Exception {
                            return file + rank;
                        }
                    });
                }
            });

    @Test
    public void name1() throws Exception {
        System.out.println('a' + 1);
    }

    @Test
    public void name() throws Exception {
        squares.blockingSubscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                System.out.println(s);
            }
        });

    }
}
