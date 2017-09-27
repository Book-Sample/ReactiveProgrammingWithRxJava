package com.mcivicm.app;

import java.time.LocalDate;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * Created by zhang on 2017/9/27.
 */

public class VocationTest {
    Observable<LocalDate> nextTenDays =
            Observable
                    .range(1, 10)
                    .map(new Function<Integer, LocalDate>() {
                        @Override
                        public LocalDate apply(@NonNull Integer integer) throws Exception {
                            return LocalDate.now().plusDays(integer);
                        }
                    });

}
