package com.example.springreactivepractice;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

public class PubSub {
    public static void run() throws InterruptedException {

        Iterable<Integer> itr = Arrays.asList(1,2,3,4,5);
        ExecutorService es = Executors.newSingleThreadExecutor();

        Flow.Publisher p = new Flow.Publisher() {
            @Override
            public void subscribe(Flow.Subscriber subscriber) {
                Iterator<Integer> it = itr.iterator();
                // 1.
                subscriber.onSubscribe(new Flow.Subscription() {
                    @Override
                    public void request(long n) {

                        es.execute(() -> {
                            int i = 0;
                            try {
                                while(i++ < n) {
                                    if(it.hasNext()) {
                                        // 2.
                                        subscriber.onNext(it.next());
                                    } else {
                                        // 3.
                                        subscriber.onComplete();
                                        break;
                                    }
                                }
                            } catch (RuntimeException e) {
                                // 3.
                                subscriber.onError(e);
                            }
                        });
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };

        Flow.Subscriber<Integer> s = new Flow.Subscriber<Integer>() {

            Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                this.subscription.request(1L);
            }

            @Override
            public void onNext(Integer item) {
                this.subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        };

        // 시작
        p.subscribe(s);

        es.awaitTermination(10, TimeUnit.HOURS);
    }
}
