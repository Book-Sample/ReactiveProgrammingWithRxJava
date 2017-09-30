package com.mcivicm.app;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by zhang on 2017/9/28.
 */

public class SpeakTest {
    private static class Pair<L, R> {

        L left;
        R right;

        static <L, R> Pair<L, R> of(L l, R r) {
            Pair<L, R> pair = new Pair<L, R>();
            pair.left = l;
            pair.right = r;
            return pair;
        }
    }

    private Observable<String> speak(String quote, long millisPerChar) {
        String[] tokens = quote.replaceAll("[:,]", "").split(" ");
        Observable<String> words = Observable.fromArray(tokens);
        Observable<Long> absoluteDelay = words
                .map(new Function<String, Integer>() {
                    @Override
                    public Integer apply(@NonNull String s) throws Exception {
                        return s.length();
                    }
                })
                .map(new Function<Integer, Long>() {
                    @Override
                    public Long apply(@NonNull Integer integer) throws Exception {
                        return integer * millisPerChar;
                    }
                })
                .scan(new BiFunction<Long, Long, Long>() {
                    @Override
                    public Long apply(@NonNull Long aLong, @NonNull Long aLong2) throws Exception {
                        return aLong + aLong2;
                    }
                });
        return words
                .zipWith(absoluteDelay.startWith(0L), new BiFunction<String, Long, Pair<String, Long>>() {
                    @Override
                    public Pair<String, Long> apply(@NonNull String s, @NonNull Long aLong) throws Exception {
                        return Pair.of(s, aLong);
                    }
                })
                .flatMap(new Function<Pair<String, Long>, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull Pair<String, Long> stringLongPair) throws Exception {
                        return Observable
                                .just(stringLongPair.left)
                                .delay(stringLongPair.right, TimeUnit.MILLISECONDS);
                    }
                });


    }

    @Test
    public void merge() throws Exception {
        Observable<String> alice = speak("To be, or not to be: that is the question", 110);
        Observable<String> bob = speak("Though this be madness, yet there is method in't", 90);
        Observable<String> jane = speak("There are more things in Heaven and Earth, " +
                "Horatio, than are dreamt of in your philosophy", 100);
        Observable
                .merge(alice.map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String s) throws Exception {
                        return "Alice:" + s;
                    }
                }), bob.map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String s) throws Exception {
                        return "Bob:" + s;
                    }
                }), jane.map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String s) throws Exception {
                        return "Jane:" + s;
                    }
                }))
                .blockingSubscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println(s);
                    }
                });
    }

    @Test
    public void concat() throws Exception {
        Observable<String> alice = speak("To be, or not to be: that is the question", 110);
        Observable<String> bob = speak("Though this be madness, yet there is method in't", 90);
        Observable<String> jane = speak("There are more things in Heaven and Earth, " +
                "Horatio, than are dreamt of in your philosophy", 100);
        Observable
                .concat(alice.map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String s) throws Exception {
                        return "Alice:" + s;
                    }
                }), bob.map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String s) throws Exception {
                        return "Bob:" + s;
                    }
                }), jane.map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String s) throws Exception {
                        return "Jane:" + s;
                    }
                }))
                .blockingSubscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println(s);
                    }
                });

    }

    /**
     * 转到下一个
     *
     * @throws Exception
     */
    @Test
    public void switchOnNext() throws Exception {
        //猴子摘西瓜
        Observable<String> alice = speak("To be, or not to be: that is the question", 500);
        Observable<String> bob = speak("Though this be madness, yet there is method in't", 300);
        Observable<String> jane = speak("There are more things in Heaven and Earth, " +
                "Horatio, than are dreamt of in your philosophy", 400);
        Random random = new Random();
        Observable<Observable<String>> quotes = Observable.just(
                alice.map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String s) throws Exception {
                        return "Alice:" + s;
                    }
                }).doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        System.out.println("Alice Subscribe");
                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("Alice Complete");
                    }
                }).doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("Alice Dispose");
                    }
                }), bob.map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String s) throws Exception {
                        return "Bob:" + s;
                    }
                }).doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        System.out.println("Bob Subscribe");
                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("Bob Complete");
                    }
                }).doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("Bob Dispose");
                    }
                }), jane.map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String s) throws Exception {
                        return "Jane:" + s;
                    }
                }).doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        System.out.println("Jane Subscribe");
                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("Jane Complete");
                    }
                }).doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("Jane Dispose");
                    }
                }))
                .flatMap(new Function<Observable<String>, ObservableSource<Observable<String>>>() {
                    @Override
                    public ObservableSource<Observable<String>> apply(@NonNull Observable<String> stringObservable) throws Exception {
                        return Observable
                                .just(stringObservable)
                                .doOnSubscribe(new Consumer<Disposable>() {
                                    @Override
                                    public void accept(Disposable disposable) throws Exception {
                                        System.out.println("just Subscribe");
                                    }
                                })
                                .doOnComplete(new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        System.out.println("just Complete");
                                    }
                                })
                                .doOnDispose(new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        System.out.println("just Complete");
                                    }
                                })
                                .delay(random.nextInt(5), TimeUnit.SECONDS)
                                .doOnSubscribe(new Consumer<Disposable>() {
                                    @Override
                                    public void accept(Disposable disposable) throws Exception {
                                        System.out.println("delay Subscribe");
                                    }
                                })
                                .doOnComplete(new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        System.out.println("delay Complete");
                                    }
                                })
                                .doOnDispose(new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        System.out.println("delay Dispose");
                                    }
                                });
                    }
                });
        Observable.switchOnNext(quotes)
                .blockingSubscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println(s);
                    }
                });
    }

    @Test
    public void switchOnNext1() throws Exception {
        //猴子摘西瓜
        Observable<String> alice = speak("To be, or not to be: that is the question", 110);
        Observable<String> bob = speak("Though this be madness, yet there is method in't", 90);
        Observable<String> jane = speak("There are more things in Heaven and Earth, " +
                "Horatio, than are dreamt of in your philosophy", 100);
        Random random = new Random();
        //choose the final subscribe.
        Observable<Observable<String>> quotes = Observable.just(
                alice.map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String s) throws Exception {
                        return "Alice:" + s;
                    }
                }).doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        System.out.println("Alice Subscribe");
                    }
                }), bob.map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String s) throws Exception {
                        return "Bob:" + s;
                    }
                }).doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        System.out.println("Bob Subscribe");
                    }
                }), jane.map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String s) throws Exception {
                        return "Jane:" + s;
                    }
                }).doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        System.out.println("Jane Subscribe");
                    }
                }))
                .map(new Function<Observable<String>, Observable<String>>() {
                    @Override
                    public Observable<String> apply(@NonNull Observable<String> stringObservable) throws Exception {
                        int delay = random.nextInt(10);
                        System.out.println(delay);
                        return stringObservable.delay(delay, TimeUnit.SECONDS);
                    }
                });
        Observable.switchOnNext(quotes)
                .blockingSubscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println(s);
                    }
                });
    }
}
