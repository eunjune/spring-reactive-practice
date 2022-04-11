package com.example.springreactivepractice;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Flow;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
*
* pub -> data1 -> mapPub -> Data2 -> logSub
* */
@Slf4j
public class PubSub {
    public static void run() throws InterruptedException {
        Flow.Publisher<Integer> pub = iterPub(Stream.iterate(1, a->a+1).limit(10).collect(Collectors.toList()));
        Flow.Publisher<Integer> mapPub = mapPub(pub,(Function<Integer,Integer>) s->s*10);
        Flow.Publisher<Integer> map2Pub = mapPub(mapPub,(Function<Integer,Integer>) s->-s);
        map2Pub.subscribe(logSub());
    }

    private static Flow.Publisher mapPub(Flow.Publisher<Integer> pub, Function<Integer, Integer> integerIntegerFunction) {
        return new Flow.Publisher<Integer>() {
            @Override
            public void subscribe(Flow.Subscriber<? super Integer> subscriber) {
                pub.subscribe(new DelegateSub(subscriber) {
                    @Override
                    public void onNext(Integer item) {
                        subscriber.onNext(integerIntegerFunction.apply(item));
                    }
                });
            }
        };
    }

    private static Flow.Subscriber<Integer> logSub() {
        return new Flow.Subscriber<Integer>() {

            Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer item) {
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        };
    }

    private static Flow.Publisher iterPub(Iterable<Integer> iter) {
        Iterable<Integer> itr = iter;

        return new Flow.Publisher() {
            @Override
            public void subscribe(Flow.Subscriber subscriber) {
                subscriber.onSubscribe(new Flow.Subscription() {
                    @Override
                    public void request(long n) {
                        try {
                            itr.forEach(subscriber::onNext);
                            subscriber.onComplete();
                        } catch (Throwable t) {
                            subscriber.onError(t);
                        }

                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };
    }
}
