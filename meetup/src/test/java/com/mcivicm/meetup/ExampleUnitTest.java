package com.mcivicm.meetup;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    MeetupApi meetupApi;

    GeoNames geoNames;

    @Before
    public void setUp() throws Exception {
        //提供解析服务
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        meetupApi = new Retrofit.Builder()
                .baseUrl("https://api.meetup.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build()
                .create(MeetupApi.class);

        geoNames = new Retrofit.Builder()
                .baseUrl("http://api.geonames.org")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build()
                .create(GeoNames.class);
    }

    @Test
    public void name() throws Exception {
        double warsawLat = 52.229841;
        double warsawLon = 21.011736;
        final Observable<Cities> citiesObservable = meetupApi.listCities(warsawLat, warsawLon);
        Observable<City> cityObservable = citiesObservable.concatMapIterable(new Function<Cities, Iterable<? extends City>>() {
            @Override
            public Iterable<? extends City> apply(Cities cities) throws Exception {
                return cities.getResults();
            }
        });
        Observable<String> map = cityObservable
                .filter(new Predicate<City>() {
                    @Override
                    public boolean test(City city) throws Exception {
                        return city.getDistance() < 5;
                    }
                })
                .map(new Function<City, String>() {
                    @Override
                    public String apply(City city) throws Exception {
                        return city.getCity();
                    }
                });

        Observable<Integer> totalPopulation =
                map
                        .flatMap(new Function<String, ObservableSource<Integer>>() {
                            @Override
                            public ObservableSource<Integer> apply(String s) throws Exception {
                                return populationOf(s);//接口接力
                            }
                        })
                        .reduce(0, new BiFunction<Integer, Integer, Integer>() {
                            @Override
                            public Integer apply(Integer integer, Integer integer2) throws Exception {
                                return integer + integer2;
                            }
                        })
                        .toObservable();

        totalPopulation.blockingSubscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                System.out.println("total population: " + integer);
            }
        });
    }

    private Observable<SearchResult> search(String query) {
        return geoNames.search(query, 1, "LONG", "zhangliangbo");
    }

    private Observable<Integer> populationOf(final String query) {
        return search(query)
                .concatMapIterable(new Function<SearchResult, Iterable<GeoName>>() {
                    @Override
                    public Iterable<GeoName> apply(SearchResult searchResult) throws Exception {
                        return searchResult.getGeonames();
                    }
                })
                .map(new Function<GeoName, Integer>() {
                    @Override
                    public Integer apply(GeoName geoName) throws Exception {
                        return geoName.getPopulation();
                    }
                })
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        return integer != null;
                    }
                })
                .defaultIfEmpty(0)//没有传0
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        System.out.println("Falling back to 0 for {}" + throwable.getMessage());
                    }
                })
                .onErrorReturnItem(0)//出错传0
                .subscribeOn(Schedulers.io());
    }
}