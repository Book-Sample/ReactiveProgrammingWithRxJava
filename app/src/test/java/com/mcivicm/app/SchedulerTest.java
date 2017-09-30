package com.mcivicm.app;

import java.util.concurrent.TimeUnit;

import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by zhang on 2017/9/30.
 */

public class SchedulerTest {
    public class SimpleHandlerScheduler extends Scheduler {

        @Override
        public Worker createWorker() {
            return new HandlerWorker();
        }

        private class HandlerWorker extends Worker {

            @Override
            public Disposable schedule(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
                return null;
            }

            @Override
            public void dispose() {

            }

            @Override
            public boolean isDisposed() {
                return false;
            }
        }

    }
}
