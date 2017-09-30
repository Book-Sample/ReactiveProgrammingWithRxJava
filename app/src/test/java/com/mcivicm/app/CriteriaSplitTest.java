package com.mcivicm.app;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;

/**
 * Created by zhang on 2017/9/29.
 */

public class CriteriaSplitTest {

    private class Event {
        String id;
        int value;
    }

    @Test
    public void name() throws Exception {

        Random random = new Random();

        Observable.interval(0, 1, TimeUnit.SECONDS)
                .map(new Function<Long, Event>() {
                    @Override
                    public Event apply(@NonNull Long aLong) throws Exception {
                        int r = random.nextInt(10);
                        Event event = new Event();
                        event.id = String.valueOf(r);
                        event.value = r;
                        return event;
                    }
                })
                .groupBy(new Function<Event, String>() {
                    @Override
                    public String apply(@NonNull Event event) throws Exception {
                        return event.id;
                    }
                })
                .blockingSubscribe(new Consumer<GroupedObservable<String, Event>>() {
                    @Override
                    public void accept(GroupedObservable<String, Event> stringEventGroupedObservable) throws Exception {
                        if (stringEventGroupedObservable.getKey().equals("5")) {
                            stringEventGroupedObservable.subscribe(new Consumer<Event>() {
                                @Override
                                public void accept(Event event) throws Exception {
                                    System.out.println("Five:" + event.value);
                                }
                            });
                        } else {
                            stringEventGroupedObservable.subscribe(new Consumer<Event>() {
                                @Override
                                public void accept(Event event) throws Exception {
                                    System.out.println(event.id + ":" + event.value);
                                }
                            });
                        }
                    }
                });

    }


}
