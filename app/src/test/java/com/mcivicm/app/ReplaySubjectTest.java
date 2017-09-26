package com.mcivicm.app;

import org.junit.Test;

import io.reactivex.functions.Consumer;
import io.reactivex.subjects.ReplaySubject;

/**
 * Created by zhang on 2017/9/25.
 */

public class ReplaySubjectTest {
    @Test
    public void name() throws Exception {
        ReplaySubject<Long> replaySubject = ReplaySubject.createWithSize(1);//仅保留一个值
        replaySubject.onNext(1L);
        replaySubject.onNext(2L);
        replaySubject.onNext(3L);
        replaySubject.onNext(4L);
        replaySubject.onNext(5L);
        replaySubject.subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                System.out.println(aLong);
            }
        });
        replaySubject.subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                System.out.println(aLong);
            }
        });
        replaySubject.subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                System.out.println(aLong);
            }
        });

    }
}
