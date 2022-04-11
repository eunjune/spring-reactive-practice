package com.example.springreactivepractice;


import java.util.concurrent.Flow;

public class DelegateSub implements Flow.Subscriber<Integer> {

    Flow.Subscriber sub;

    public DelegateSub(Flow.Subscriber subscriber) {
        this.sub = subscriber;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        sub.onSubscribe(subscription);
    }

    @Override
    public void onNext(Integer item) {
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
