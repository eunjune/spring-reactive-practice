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
                boolean cancelled = false;

                @Override
                public void request(long n) {
                    ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
                    exec.scheduleAtFixedRate(() -> {
                        if(cancelled) {
                            exec.shutdown();
                            return;
                        }

                        sub.onNext(no++);
                    },0,300, TimeUnit.MILLISECONDS);
                }

                @Override
                public void cancel() {
                    cancelled = true;
                }
            });
        };

        Flow.Publisher<Integer> takePub = sub -> {
            pub.subscribe(new Flow.Subscriber<Integer>() {
                int count=0;
                Flow.Subscription subsc;

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subsc = subscription;
                    sub.onSubscribe(subscription);
                }

                @Override
                public void onNext(Integer item) {
                    sub.onNext(item);
                    if(++count > 4) {
                        subsc.cancel();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    sub.onError(throwable);
                }

                @Override
                public void onComplete() {
                    sub.onComplete();
                }
            });
        };

        takePub.subscribe(new Flow.Subscriber<Integer>() {
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
