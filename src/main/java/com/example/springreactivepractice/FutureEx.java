package com.example.springreactivepractice;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.*;

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
        * 예외발생시 try catch로 감싸야 함
        * */
        System.out.println(f.get());
    }

    public void run_다른방식() {
        ExecutorService es = Executors.newCachedThreadPool();

        FutureTask<String> f = new FutureTask<>(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            log.info("Hello");
            return "Hello";
        }) {
            /*
            * 스레드가 끝났을 때 실행할 코드
            * */
            @Override
            protected void done() {
                try {
                    System.out.println(get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };

        es.execute(f);
        es.shutdown();
    }

    interface SuccessCallback {
        void onSuccess(String result);
    }

    interface ExceptionCallback {
        void onError(Throwable t);
    }

    public static class CallbackFutureTask extends FutureTask<String> {
        SuccessCallback sc;
        ExceptionCallback ec;

        public CallbackFutureTask(Callable<String> callable, SuccessCallback sc, ExceptionCallback ec) {
            super(callable);
            this.sc = Objects.requireNonNull(sc);
            this.ec = Objects.requireNonNull(ec);
        }

        /*
         * 스레드가 끝났을 때 실행할 코드
         * */
        @Override
        protected void done() {
            try {
                sc.onSuccess(get());
            } catch (InterruptedException e) {
                // 예외를 받는것보다는 interrupt 시그널을 주는게 중요
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                // 비동기 작업 수행중 발생한 예외
                ec.onError(e.getCause());
            }
        }
    }

    public void run_다른방식2() {
        ExecutorService es = Executors.newCachedThreadPool();

        CallbackFutureTask f = new CallbackFutureTask(() -> {
            Thread.sleep(2000);
            if(1 == 1) throw new RuntimeException("Async ERROR!!!");
            log.info("Hello");
            return "Hello";
        },
                s -> System.out.println("Result : " + s),
                e -> System.out.println("Error: " + e.getMessage())
        );

        es.execute(f);
        es.shutdown();
    }
}
