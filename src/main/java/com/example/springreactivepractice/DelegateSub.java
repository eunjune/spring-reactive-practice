package com.example.springreactivepractice;


import java.util.concurrent.Flow;

public class DelegateSub<T,R> implements Flow.Subscriber<T> {

    Flow.Subscriber sub;

    public DelegateSub(Flow.Subscriber<? super R> subscriber) {
        this.sub = subscriber;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        sub.onSubscribe(subscription);
    }

    @Override
    public void onNext(T item) {
        sub.onNext(item);
    }

    @Override
    public void onError(Throwable throwable) {
        sub.onError(throwable);
    }

    @Override
    public void onComplete() {
        sub.onComplete();
    }
}
