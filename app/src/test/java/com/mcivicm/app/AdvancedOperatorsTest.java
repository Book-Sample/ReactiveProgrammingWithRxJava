package com.mcivicm.app;

import org.junit.Test;

import java.math.BigInteger;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by zhang on 2017/9/28.
 */

public class AdvancedOperatorsTest {
    /**
     * 注重过程Observable
     *
     * @throws Exception
     */
    @Test
    public void scan() throws Exception {
        Observable<Integer> progress = Observable.fromArray(10, 14, 12, 13, 14, 16);

        Observable<Integer> totalProgress = progress
                .scan(new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(@NonNull Integer integer, @NonNull Integer integer2) throws Exception {
                        return integer + integer2;
                    }
                });

        totalProgress.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                System.out.println(integer);
            }
        });


    }

    /**
     * 注重过程
     *
     * @throws Exception
     */
    @Test
    public void name1() throws Exception {
        Observable<BigInteger> factorials = Observable
                .range(2, 100)
                .scan(BigInteger.ONE, new BiFunction<BigInteger, Integer, BigInteger>() {
                    @Override
                    public BigInteger apply(@NonNull BigInteger bigInteger, @NonNull Integer integer) throws Exception {
                        return bigInteger.multiply(BigInteger.valueOf(integer));
                    }
                });
        factorials.subscribe(new Consumer<BigInteger>() {
            @Override
            public void accept(BigInteger bigInteger) throws Exception {
                System.out.println(bigInteger);
            }
        });

    }

    /**
     * 单一的数据对象
     *
     * @throws Exception
     */
    @Test
    public void name2() throws Exception {
        Single<String> collectString = Observable
                .range(1, 10)
                .collect(new Callable<StringBuilder>() {
                    @Override
                    public StringBuilder call() throws Exception {
                        return new StringBuilder();
                    }
                }, new BiConsumer<StringBuilder, Integer>() {
                    @Override
                    public void accept(StringBuilder stringBuilder, Integer integer) throws Exception {
                        stringBuilder.append(integer).append(", ");
                    }
                })
                .map(new Function<StringBuilder, String>() {
                    @Override
                    public String apply(@NonNull StringBuilder stringBuilder) throws Exception {
                        return stringBuilder.toString();
                    }
                });
        collectString.subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                System.out.println(s);
            }
        });

    }

    /**
     * 注重结果Maybe
     *
     * @throws Exception
     */
    @Test
    public void name3() throws Exception {
        Observable.range(1, 10)
                .reduce(new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(@NonNull Integer integer, @NonNull Integer integer2) throws Exception {
                        return integer2;
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        System.out.println(integer);
                    }
                });

    }
}
