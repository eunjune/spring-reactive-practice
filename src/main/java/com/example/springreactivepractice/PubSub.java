package com.example.springreactivepractice;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Flow;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
public class PubSub {
    /*
    *
    * pub -> data1 -> mapPub -> Data2 -> logSub
    map2Pub.subscribe(logSub)
        mapPub.subscribe(DelegateSub(logSub))
            pub.subscribe(DelegateSub(DelegateSub(logSub)))
                DelegateSub(DelegateSub(logSub)).onSubscribe()
                    DelegateSub(logSub).onSubscribe()
                        logSub.onSubscribe()
                            subscription.request()
                                DelegateSub(DelegateSub(logSub)).onNext(item)
                                    DelegateSub(logSub).onNext((s->s*10).apply(item))
                                        logSub.onNext((s->-s).apply(item))
                                DelegateSub(DelegateSub(logSub)).onComplete()
                                    DelegateSub(logSub).onComplete()
                                        logSub.onComplete()
                                DelegateSub(DelegateSub(logSub)).onError()
                                    DelegateSub(logSub).onError()
                                        logSub.onError()
    * */
    public static void runMapPub() throws InterruptedException {
        Flow.Publisher<Integer> pub = iterPub(Stream.iterate(1, a->a+1).limit(10).collect(Collectors.toList()));
        Flow.Publisher<String> mapPub = mapPub(pub, s->"[" + s + "]");
//        Flow.Publisher<Integer> mapPub = mapPub(pub,(Function<Integer,Integer>) s->s*10);
//        Flow.Publisher<Integer> map2Pub = mapPub(mapPub,(Function<Integer,Integer>) s->-s);
//        map2Pub.subscribe(logSub());
    }

    public static void runSumPub() {
        Flow.Publisher<Integer> pub = iterPub(Stream.iterate(1, a->a+1).limit(10).collect(Collectors.toList()));
//        Flow.Publisher<Integer> reducePub = reducePub(pub,0,(BiFunction<Integer,Integer,Integer>)(a, b) -> a+b);
//        reducePub.subscribe(logSub());
    }

/*    private static Flow.Publisher<Integer> reducePub(Flow.Publisher<Integer> pub, int init, BiFunction<Integer,Integer,Integer> bf) {
        return new Flow.Publisher<Integer>() {
            @Override
            public void subscribe(Flow.Subscriber<? super Integer> subscriber) {
                pub.subscribe(new DelegateSub(subscriber) {
                    int result = init;

                    @Override
                    public void onNext(T item) {
                        result = bf.apply(result,item);
                    }

                    @Override
                    public void onComplete() {
                        subscriber.onNext(result);
                        subscriber.onComplete();
                    }
                });
            }
        };
    }*/

    private static <T,R>  Flow.Publisher<R> mapPub(Flow.Publisher<T> pub, Function<T, R> integerFunction) {
        return new Flow.Publisher<R>() {
            @Override
            public void subscribe(Flow.Subscriber<? super R> subscriber) {
                pub.subscribe(new DelegateSub<T,R>(subscriber) {
                    @Override
                    public void onNext(T item) {
                        subscriber.onNext(integerFunction.apply(item));
                    }
                });
            }
        };
    }

    private static <T> Flow.Subscriber<T> logSub() {
        return new Flow.Subscriber<T>() {

            Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T item) {
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
