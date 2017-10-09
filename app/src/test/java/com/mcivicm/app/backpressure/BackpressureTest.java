package com.mcivicm.app.backpressure;

import org.junit.Test;
import org.reactivestreams.Subscription;

import java.util.concurrent.CountDownLatch;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 背压，或者叫反压力（这个好像更好懂），就是对过量数据的一种处理策略。可以类比家长的压力，上司的压力，社会的压力等等。。。
 */

public class BackpressureTest {

    private Observable<Dish> dishes = Observable
            .range(1, 1000000000)
            .map(new Function<Integer, Dish>() {
                @Override
                public Dish apply(@NonNull Integer integer) throws Exception {
                    return new Dish(integer);
                }
            });

    private void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void normal() throws Exception {
        dishes
                .subscribe(new Consumer<Dish>() {
                    @Override
                    public void accept(Dish dish) throws Exception {
                        System.out.println("Washing: " + dish);
                        sleepMillis(50);
                    }
                });
    }

    @Test
    public void io() throws Exception {
        dishes
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Dish>() {
                    @Override
                    public void accept(Dish dish) throws Exception {
                        System.out.println("Washing: " + dish.toString());
                        sleepMillis(50);
                    }
                });

    }

    private Flowable<Integer> myRange(int from, int count) {
        return Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<Integer> e) throws Exception {
                int i = from;
                while (i < from + count) {
                    if (!e.isCancelled()) {
                        if (i < e.requested()) {
                            e.onNext(i++);
                        }
                    } else {
                        return;
                    }
                }
                e.onComplete();
            }
        }, BackpressureStrategy.MISSING);
    }

    @Test
    public void myRange() throws Exception {
        myRange(1, 1000000000)
                .map(new Function<Integer, Dish>() {
                    @Override
                    public Dish apply(@NonNull Integer integer) throws Exception {
                        return new Dish(integer);
                    }
                })
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribe(new FlowableSubscriber<Dish>() {
                    @Override
                    public void onSubscribe(@NonNull Subscription s) {
                        s.request(1);
                    }

                    @Override
                    public void onNext(Dish dish) {
                        System.out.println("Washing: " + dish.toString());
                        sleepMillis(50);
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.out.println(t.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Test
    public void flowable() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Flowable
                .range(1, 10)
                .subscribeOn(Schedulers.io())
                .subscribe(new FlowableSubscriber<Integer>() {

                    Subscription subscription;

                    @Override
                    public void onSubscribe(@NonNull Subscription s) {
                        subscription = s;
                        s.request(1);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        System.out.println(integer);
                        sleepMillis(1000);
                        subscription.request(1);
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.out.println(t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });
        latch.await();
    }

    @Test
    public void onBackpressureBuffer() throws Exception {
        Flowable.range(0, 1000000000)
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        System.out.println("range: " + integer);
                    }
                })
                .onBackpressureBuffer(400, new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("source has generate more than 200 items.");
                    }
                })
                .observeOn(Schedulers.io())
                .blockingSubscribe(new FlowableSubscriber<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Subscription s) {
//                        s.request(1);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        System.out.println("get: " + integer);
                        sleepMillis(50);

                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    @Test
    public void onBackpressureDrop() throws Exception {
        //onBackpressureDrop丢弃所有没有请求的数据，如果已经产生但是下游没有请求，那么数据就丢了。下游如果在上游全部数据发送完之后再请求上游的数据，就请求不到任何数据。
        Flowable.range(1, 1000000000)
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        System.out.println("produce " + integer);
                    }
                })
                .onBackpressureDrop(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        System.out.println("drop " + integer);
                    }
                })
                .subscribe(new FlowableSubscriber<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Subscription s) {

                    }

                    @Override
                    public void onNext(Integer integer) {

                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    @Test
    public void onBackpressureLatest() throws Exception {
        //onBackpressureLatest跟onBackpressureDrop类似，都会丢弃多余的数据，但是会保留最后一个数据。下游如果在上游全部数据发送完之后再请求上游的数据，可以请求到最后一条数据。
        Flowable.range(1, 10)
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        System.out.println("produce " + integer);
                    }
                })
                .onBackpressureLatest()
                .blockingSubscribe(new FlowableSubscriber<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Subscription s) {
                        sleepMillis(3000);//睡3s钟再请求数据，此时上游早已发送完全部数据
                        s.request(1);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        System.out.println("consume " + integer);//但和onBackpressureDrop不同的是：Drop请求不到任何数据，而Latest可以请求到最后的10
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

}
