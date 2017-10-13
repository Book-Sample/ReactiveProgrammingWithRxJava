package com.mcivicm.hystrix;


import com.netflix.hystrix.HystrixCollapser;
import com.netflix.hystrix.HystrixObservableCollapser;
import com.netflix.hystrix.HystrixObservableCommand;

import java.util.Collection;

import rx.functions.Func1;

/**
 * 合并多个
 */

public class FetchRatingCollapser extends HystrixObservableCollapser<Book, Rating, Rating, Book> {
    private final Book book;

    public FetchRatingCollapser(Book book) {
        this.book = book;
    }

    @Override
    public Book getRequestArgument() {
        return book;
    }

    @Override
    protected HystrixObservableCommand<Rating> createCommand(Collection<HystrixCollapser.CollapsedRequest<Rating, Book>> collection) {
        return null;
    }

    @Override
    protected Func1<Rating, Book> getBatchReturnTypeKeySelector() {
        return new Func1<Rating, Book>() {
            @Override
            public Book call(Rating rating) {
                return rating.getBook();
            }
        };
    }

    @Override
    protected Func1<Book, Book> getRequestArgumentKeySelector() {
        return new Func1<Book, Book>() {
            @Override
            public Book call(Book book) {
                return book;
            }
        };
    }

    @Override
    protected void onMissingResponse(HystrixCollapser.CollapsedRequest<Rating, Book> collapsedRequest) {
        collapsedRequest.setException(new RuntimeException("Not found for: " + collapsedRequest.getArgument()));
    }

    @Override
    protected Func1<Rating, Rating> getBatchReturnTypeToResponseTypeMapper() {
        return new Func1<Rating, Rating>() {
            @Override
            public Rating call(Rating rating) {
                return rating;
            }
        };
    }
}
