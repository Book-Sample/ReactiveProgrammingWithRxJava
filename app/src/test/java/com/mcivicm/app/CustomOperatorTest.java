package com.mcivicm.app;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.ObservableOperator;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * Created by zhang on 2017/9/29.
 */

public class CustomOperatorTest {
    Observable<Boolean> trueFalse = Observable.just(true, false).repeat();

    /**
     * 无法使用链式编程
     *
     * @param upstream
     * @param <T>
     * @return
     */
    static <T> Observable<T> odd(Observable<T> upstream) {
        Observable<Boolean> trueFalse = Observable.just(true, false).repeat();
        return upstream
                .zipWith(trueFalse, new BiFunction<T, Boolean, Pair<T, Boolean>>() {
                    @Override
                    public Pair<T, Boolean> apply(@NonNull T t, @NonNull Boolean aBoolean) throws Exception {
                        return Pair.of(t, aBoolean);
                    }
                })
                .filter(new Predicate<Pair<T, Boolean>>() {
                    @Override
                    public boolean test(@NonNull Pair<T, Boolean> tBooleanPair) throws Exception {
                        return tBooleanPair.getRight();
                    }
                })
                .map(new Function<Pair<T, Boolean>, T>() {
                    @Override
                    public T apply(@NonNull Pair<T, Boolean> tBooleanPair) throws Exception {
                        return tBooleanPair.getLeft();
                    }
                });
    }

    private <T> ObservableTransformer<T, T> odd() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
                return upstream
                        .zipWith(
                                Observable.just(true, false).repeat().skip(1),
                                new BiFunction<T, Boolean, Pair<T, Boolean>>() {
                                    @Override
                                    public Pair<T, Boolean> apply(@NonNull T t, @NonNull Boolean aBoolean) throws Exception {
                                        return Pair.of(t, aBoolean);
                                    }
                                })
                        .filter(new Predicate<Pair<T, Boolean>>() {
                            @Override
                            public boolean test(@NonNull Pair<T, Boolean> tBooleanPair) throws Exception {
                                return tBooleanPair.getRight();
                            }
                        })
                        .map(new Function<Pair<T, Boolean>, T>() {
                            @Override
                            public T apply(@NonNull Pair<T, Boolean> tBooleanPair) throws Exception {
                                return tBooleanPair.getLeft();
                            }
                        });
            }
        };
    }

    Observable<Character> alphabet = Observable
            .range(0, 'Z' - 'A' + 1)
            .map(new Function<Integer, Character>() {
                @Override
                public Character apply(@NonNull Integer integer) throws Exception {
                    return (char) ('A' + integer);
                }
            });

    @Test
    public void name() throws Exception {
        alphabet.compose(odd())
                .blockingSubscribe(new Consumer<Character>() {
                    @Override
                    public void accept(Character character) throws Exception {
                        System.out.println(character);
                    }
                });

    }

    private <T> ObservableOperator<String, T> toStringOfOdd() {

        return new ObservableOperator<String, T>() {

            private boolean odd = true;

            @Override
            public Observer<? super T> apply(@NonNull Observer<? super String> child) throws Exception {

                return new Observer<T>() {

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        child.onSubscribe(d);
                    }

                    @Override
                    public void onNext(@NonNull T t) {
                        if (odd) {
                            child.onNext(t.toString());
                        }
                        odd = !odd;
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        child.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        child.onComplete();
                    }
                };
            }
        };
    }

    @Test
    public void lift() throws Exception {
        Observable.range(1, 9)
                .lift(toStringOfOdd())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println(s);
                    }
                });

    }
}
