package com.example.springreactivepractice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.relational.core.sql.In;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class IntervalEx {
    public static void main(String[] args) {
        Flow.Publisher<Integer> pub = sub -> {
            sub.onSubscribe(new Flow.Subscription() {
                int no = 0;

                @Override
                public void request(long n) {
                    ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
                    exec.scheduleAtFixedRate(() -> {
                        sub.onNext(no++);
                    },0,300, TimeUnit.MILLISECONDS);
                }

                @Override
                public void cancel() {

                }
            });
        };

        pub.subscribe(new Flow.Subscriber<Integer>() {
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
