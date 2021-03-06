package com.example.springreactivepractice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.sql.In;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

@Slf4j
public class SchedulerEx {
    // onSubscribe : 데이터 처리과정들을 on에 지정한 스레드 안에서 수행을 해달라는 메소드
    public void runSubscribeOn() {
        Flow.Publisher<Integer> pub = subscriber -> {
            subscriber.onSubscribe(new Flow.Subscription() {
                @Override
                public void request(long n) {
                    subscriber.onNext(1);
                    subscriber.onNext(2);
                    subscriber.onNext(3);
                    subscriber.onNext(4);
                    subscriber.onNext(5);
                    subscriber.onComplete();
                }

                @Override
                public void cancel() {

                }
            });
        };

        Flow.Publisher<Integer> subOnPub = sub -> {
            /*
            *[pool-1-thread-1] DEBUG com.example.springreactivepractice.SchedulerEx - onSubscribe
            *[pool-1-thread-1] DEBUG com.example.springreactivepractice.SchedulerEx - onNext:1
            *[pool-1-thread-1] DEBUG com.example.springreactivepractice.SchedulerEx - onNext:2
            *[pool-1-thread-1] DEBUG com.example.springreactivepractice.SchedulerEx - onNext:3
            *[pool-1-thread-1] DEBUG com.example.springreactivepractice.SchedulerEx - onNext:4
            *[pool-1-thread-1] DEBUG com.example.springreactivepractice.SchedulerEx - onNext:5
            *[pool-1-thread-1] DEBUG com.example.springreactivepractice.SchedulerEx - onComplete
            * */
            ExecutorService es = Executors.newSingleThreadExecutor();
            es.execute(() -> pub.subscribe(sub));
        };

        subOnPub.subscribe(new Flow.Subscriber<Integer>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                log.debug("onSubscribe");
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer item) {
                log.debug("onNext:{}",item);
            }

            @Override
            public void onError(Throwable throwable) {
                log.debug("onError:{}",throwable);
            }

            @Override
            public void onComplete() {
                log.debug("onComplete");
            }
        });
    }

    // onPublish : 데이터 소비를 on에 지정한 스레드 안에서 수행을 해달라는 메소드
    public void runOnPublish(String[] args) {
        Flow.Publisher<Integer> pub = subscriber -> {
            subscriber.onSubscribe(new Flow.Subscription() {
                @Override
                public void request(long n) {
                    subscriber.onNext(1);
                    subscriber.onNext(2);
                    subscriber.onNext(3);
                    subscriber.onNext(4);
                    subscriber.onNext(5);
                    subscriber.onComplete();
                }

                @Override
                public void cancel() {

                }
            });
        };

        Flow.Publisher<Integer> pubOnPub = sub -> {
            pub.subscribe(new Flow.Subscriber<Integer>() {
                /*
                * [main] DEBUG com.example.springreactivepractice.SchedulerEx - onSubscribe
                * [pool-1-thread-1] DEBUG com.example.springreactivepractice.SchedulerEx - onNext:1
                * [pool-1-thread-1] DEBUG com.example.springreactivepractice.SchedulerEx - onNext:2
                * [pool-1-thread-1] DEBUG com.example.springreactivepractice.SchedulerEx - onNext:3
                * [pool-1-thread-1] DEBUG com.example.springreactivepractice.SchedulerEx - onNext:4
                * [pool-1-thread-1] DEBUG com.example.springreactivepractice.SchedulerEx - onNext:5
                * [pool-1-thread-1] DEBUG com.example.springreactivepractice.SchedulerEx - onComplete
                * */
                ExecutorService es = Executors.newSingleThreadExecutor();

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    sub.onSubscribe(subscription);
                }

                @Override
                public void onNext(Integer item) {
                    es.execute(()->sub.onNext(item));
                }

                @Override
                public void onError(Throwable throwable) {
                    es.execute(()->sub.onError(throwable));
                }

                @Override
                public void onComplete() {
                    es.execute(()->sub.onComplete());
                }
            });
        };

        pubOnPub.subscribe(new Flow.Subscriber<Integer>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                log.debug("onSubscribe");
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer item) {
                log.debug("onNext:{}",item);
            }

            @Override
            public void onError(Throwable throwable) {
                log.debug("onError:{}",throwable);
            }

            @Override
            public void onComplete() {
                log.debug("onComplete");
            }
        });
    }

    // onSubscribe + onPublish
    /*
    * [subOn-1] DEBUG com.example.springreactivepractice.SchedulerEx - onSubscribe
    * [subOn-1] DEBUG com.example.springreactivepractice.SchedulerEx - request
    * [pubOn-1] DEBUG com.example.springreactivepractice.SchedulerEx - onNext:1
    * [pubOn-1] DEBUG com.example.springreactivepractice.SchedulerEx - onNext:2
    * [pubOn-1] DEBUG com.example.springreactivepractice.SchedulerEx - onNext:3
    * [pubOn-1] DEBUG com.example.springreactivepractice.SchedulerEx - onNext:4
    * [pubOn-1] DEBUG com.example.springreactivepractice.SchedulerEx - onNext:5
    * [pubOn-1] DEBUG com.example.springreactivepractice.SchedulerEx - onComplete
    * */
    public static void main(String[] args) {
        Flow.Publisher<Integer> pub = subscriber -> {
            subscriber.onSubscribe(new Flow.Subscription() {
                @Override
                public void request(long n) {
                    log.debug("request");
                    subscriber.onNext(1);
                    subscriber.onNext(2);
                    subscriber.onNext(3);
                    subscriber.onNext(4);
                    subscriber.onNext(5);
                    subscriber.onComplete();
                }

                @Override
                public void cancel() {

                }
            });
        };

        Flow.Publisher<Integer> subOnPub = sub -> {
            ExecutorService es = Executors.newSingleThreadExecutor(new CustomizableThreadFactory(){
                @Override
                public String getThreadNamePrefix() {
                    return "subOn-";
                }
            });
            es.execute(() -> pub.subscribe(sub));
        };

        Flow.Publisher<Integer> pubOnPub = sub -> {
            subOnPub.subscribe(new Flow.Subscriber<Integer>() {
                ExecutorService es = Executors.newSingleThreadExecutor(new CustomizableThreadFactory(){
                    @Override
                    public String getThreadNamePrefix() {
                        return "pubOn-";
                    }
                });

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    sub.onSubscribe(subscription);
                }

                @Override
                public void onNext(Integer item) {
                    es.execute(()->sub.onNext(item));
                }

                @Override
                public void onError(Throwable throwable) {
                    es.execute(()->sub.onError(throwable));
                    es.shutdown();
                }

                @Override
                public void onComplete() {
                    es.execute(()->sub.onComplete());
                    es.shutdown();
                }
            });
        };

        pubOnPub.subscribe(new Flow.Subscriber<Integer>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                log.debug("onSubscribe");
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer item) {
                log.debug("onNext:{}",item);
            }

            @Override
            public void onError(Throwable throwable) {
                log.debug("onError:{}",throwable);
            }

            @Override
            public void onComplete() {
                log.debug("onComplete");
            }
        });
    }
}
