package com.mcivicm.app.test;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Created by zhang on 2017/10/9.
 */

public class TestTest {

    private TestScheduler testScheduler = new TestScheduler();

    @Test
    public void testScheduler() throws Exception {

        Observable<String> fast =
                Observable
                        .interval(10, TimeUnit.MILLISECONDS, testScheduler)
                        .map(new Function<Long, String>() {
                            @Override
                            public String apply(@NonNull Long aLong) throws Exception {
                                return "F" + aLong;
                            }
                        })
                        .take(3);

        Observable<String> slow = Observable
                .interval(50, TimeUnit.MILLISECONDS, testScheduler)
                .map(new Function<Long, String>() {
                    @Override
                    public String apply(@NonNull Long aLong) throws Exception {
                        return "S" + aLong;
                    }
                });

        Observable<String> stream = Observable.concat(fast, slow);
        stream.subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                System.out.println(s);
            }
        });
        System.out.println("Subscribed");

        TimeUnit.SECONDS.sleep(1);
        System.out.println("After one second");
        testScheduler.advanceTimeBy(25, TimeUnit.MILLISECONDS);//手动控制时间

        TimeUnit.SECONDS.sleep(1);
        System.out.println("After one more second");
        testScheduler.advanceTimeBy(75, TimeUnit.MILLISECONDS);//手动控制时间

        TimeUnit.SECONDS.sleep(1);
        System.out.println("...and one more");
        testScheduler.advanceTimeTo(200, TimeUnit.MILLISECONDS);//手动控制时间

    }

    @Test
    public void shouldApplyConcatMapInOrder() throws Exception {
        List<String> list =
                Observable
                        .range(1, 3)
                        .concatMap(new Function<Integer, ObservableSource<Integer>>() {
                            @Override
                            public ObservableSource<Integer> apply(@NonNull Integer integer) throws Exception {
                                return Observable.just(integer, -integer);
                            }
                        })
                        .map(new Function<Integer, String>() {
                            @Override
                            public String apply(@NonNull Integer integer) throws Exception {
                                return String.valueOf(integer);
                            }
                        })
                        .toList()
                        .blockingGet();
        System.out.println(list);
    }

    @Test
    public void testSubscriber() throws Exception {

        Observable<Integer> observable = Observable
                .just(3, 0, 2, 0, 1, 0)
                .concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(@NonNull Integer integer) throws Exception {
                        return Observable.fromCallable(new Callable<Integer>() {
                            @Override
                            public Integer call() throws Exception {
                                return 100 / integer;
                            }
                        });
                    }
                });

        TestObserver<Integer> testObserver = new TestObserver<>();
        observable.subscribe(testObserver);

        testObserver.assertValues(33, 50, 100);
        testObserver.assertError(ArithmeticException.class);//CompositeException instead.
    }

    interface MyService {
        Observable<String> externalCall();
    }

    private class MyServiceWithTimeout implements MyService {

        private final MyService delegate;
        private final Scheduler scheduler;

        MyServiceWithTimeout(MyService delegate, Scheduler scheduler) {
            this.delegate = delegate;
            this.scheduler = scheduler;
        }

        @Override
        public Observable<String> externalCall() {
            return delegate.externalCall()
                    .timeout(1, TimeUnit.SECONDS, scheduler, Observable.empty());
        }
    }

    private MyServiceWithTimeout mockReturning(Observable<String> result,
                                               TestScheduler testScheduler) {
        MyService myService = mock(MyService.class);
        given(myService.externalCall()).willReturn(result);
        return new MyServiceWithTimeout(myService, testScheduler);
    }
}
