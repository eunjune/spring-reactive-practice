package com.example.springreactivepractice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.sql.In;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

// subscribeOn : 데이터 처리과정들을 on에 지정한 스레드 안에서 수행을 해달라는 메소드
// publishOn :

@Slf4j
public class SchedulerEx {
    public static void main(String[] args) {
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
}
