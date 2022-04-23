package com.example.springreactivepractice;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class FutureEx {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService es = Executors.newCachedThreadPool();

        // 스레드 실행
        es.execute(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {}
            log.info("Hello");
        });

        // 스레드 실행하고 결과값 리턴
        Future<String> f = es.submit(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {}
            log.info("Hello");
            return "Hello";
        });

        log.info("Exit");

        /*
        * future 결과가 올 때까지 blocking 한다
        * */
        System.out.println(f.get());
    }
}
