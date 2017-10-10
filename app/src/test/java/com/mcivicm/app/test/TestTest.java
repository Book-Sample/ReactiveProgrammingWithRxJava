package com.mcivicm.app.test;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.LongConsumer;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subscribers.TestSubscriber;

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
    public void testObserver() throws Exception {
        //无背压
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

    @Test
    public void timeoutWhenServiceNeverCompletes() throws Exception {
        TestScheduler testScheduler = new TestScheduler();
        MyService mock = mockReturning(Observable.never(), testScheduler);
        TestObserver<String> testObserver = TestObserver.create();

        mock.externalCall().subscribe(testObserver);

        testScheduler.advanceTimeBy(950, TimeUnit.MILLISECONDS);
        testObserver.assertNotTerminated();
        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        testObserver.assertComplete();
        testObserver.assertNoValues();
    }

    @Test
    public void valueIsReturnedJustBeforeTimeout() throws Exception {
        //given
        TestScheduler testScheduler = new TestScheduler();
        Observable<String> slow = Observable
                .timer(950, TimeUnit.MILLISECONDS, testScheduler)
                .map(new Function<Long, String>() {
                    @Override
                    public String apply(@NonNull Long aLong) throws Exception {
                        return String.valueOf(aLong);
                    }
                });
        MyService myService = mockReturning(slow, testScheduler);
        TestObserver<String> testObserver = TestObserver.create();
        //when
        myService.externalCall().subscribe(testObserver);
        //then, 手动控制时间，并作出断言
        testScheduler.advanceTimeBy(930, TimeUnit.MILLISECONDS);
        testObserver.assertNotComplete();
        testObserver.assertNoValues();
        testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS);
        testObserver.assertComplete();
        testObserver.assertValueCount(1);
    }

//    @Before
//    public void setUp() throws Exception {
//        //设定全局的Scheduler
//        RxJavaPlugins.initComputationScheduler(new Callable<Scheduler>() {
//            @Override
//            public Scheduler call() throws Exception {
//                return testScheduler;
//            }
//        });
//        RxJavaPlugins.initIoScheduler(new Callable<Scheduler>() {
//            @Override
//            public Scheduler call() throws Exception {
//                return testScheduler;
//            }
//        });
//        RxJavaPlugins.initNewThreadScheduler(new Callable<Scheduler>() {
//            @Override
//            public Scheduler call() throws Exception {
//                return testScheduler;
//            }
//        });
//        RxJavaPlugins.initSingleScheduler(new Callable<Scheduler>() {
//            @Override
//            public Scheduler call() throws Exception {
//                return testScheduler;
//            }
//        });
//    }

    @Test
    public void testSubscriber() throws Exception {
        //有背压
        Flowable<Long> naturals1 = Flowable.create(new FlowableOnSubscribe<Long>() {


            @Override
            public void subscribe(@NonNull FlowableEmitter<Long> e) throws Exception {
                long i = 0;
                while (!e.isCancelled()) {
                    if (e.requested() > 0) {
                        e.onNext(i++);
                    }
                }
            }
        }, BackpressureStrategy.MISSING).subscribeOn(Schedulers.newThread());

        TestSubscriber<Long> testSubscriber = new TestSubscriber<>(0);

        naturals1
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        System.out.println("natural: " + aLong);
                    }
                })
                .doOnRequest(new LongConsumer() {
                    @Override
                    public void accept(long t) throws Exception {
                        System.out.println("request: " + t);
                    }
                })
                .take(10)
                .subscribe(testSubscriber);
        System.out.println("before asserting");
        testSubscriber.assertNoValues();//没有值
        testSubscriber.requestMore(100);
        testSubscriber.await();//因为是异步的，所以要等待一下，不然下面一句话会报错
        testSubscriber.assertValueCount(10);
        testSubscriber.assertComplete();
    }
}
