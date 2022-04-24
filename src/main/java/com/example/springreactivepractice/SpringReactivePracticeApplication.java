package com.example.springreactivepractice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.*;

@SpringBootApplication
@Slf4j
@EnableAsync
public class SpringReactivePracticeApplication {

    public static void main(String[] args) {

        // Iterable 방식
        Iterable<Integer> iter = () -> new Iterator<>() {
            int i = 0;
            final static int MAX = 10;

            @Override
            public boolean hasNext() {
                return i < MAX;
            }

            @Override
            public Integer next() {
                return ++i;
            }
        };

        for(Integer i : iter) {
            System.out.println(i);
        }

        for (Iterator<Integer> it = iter.iterator(); it.hasNext();) {
            System.out.println(it.next()); // 데이터 받는 쪽
        }


        // Observable 방식
        Observer ob = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                // 데이터 받는 쪽
                System.out.println(arg);
            }
        };

        IntObservable io = new IntObservable();
        io.addObserver(ob);

        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(io);

        es.shutdown();

        SpringApplication.run(SpringReactivePracticeApplication.class, args);
    }

    // 데이터 쏘는 쪽
    static class IntObservable extends Observable implements Runnable {

        @Override
        public void run() {
            for (int i = 1; i <= 10; i++) {
                setChanged();
                notifyObservers(i); // push. 넘어갈 데이터를 준다.
                // int i = it.next()   pull.
            }
        }
    }

    @RestController
    public static class Controller {
        @RequestMapping("/hello")
        public Flow.Publisher<String> hello(String name) {
            return new Flow.Publisher<String>() {
                @Override
                public void subscribe(Flow.Subscriber<? super String> subscriber) {
                    subscriber.onSubscribe(new Flow.Subscription() {
                        @Override
                        public void request(long n) {
                            subscriber.onNext("Hello" + name);
                            subscriber.onComplete();
                        }

                        @Override
                        public void cancel() {

                        }
                    });
                }
            };
        }
    }


    @Component
    public static class MyService {

        /*
        * 보통 장시간의 작업을 진행하는 경우 쓴다.
        * 결과를 가져오는 방법
        * - 결과를 db 같은데 넣고 db를 계속 access 해서 결과가 났는지 확인
        * - Future 결과를 세션에 저장
        * */
        @Async(value = "tp") // 스레드 풀을 분리해서 적용하고 싶다면 value ㄱㄱ
        public Future<String> hello() throws InterruptedException {
            log.info("hello()");
            Thread.sleep(1000);
            return new AsyncResult<>("Hello");
        }

        @Async
        public ListenableFuture<String> helloListenable() throws InterruptedException {
            log.info("hello()");
            Thread.sleep(1000);
            return new AsyncResult<>("Hello");
        }
    }

    @Bean
    ThreadPoolTaskExecutor tp() {
        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
        te.setCorePoolSize(10); // runtime에 값 수정 가능. JMX를 사용해야 함.
        te.setMaxPoolSize(100); // queue가 꽉차면 여기에 찬다
        te.setQueueCapacity(200); // 대기를 얼마나 걸것인지. core pool이 다 차면 queue에 찬다다
        // te.setAllowCoreThreadTimeOut();
        // te.setKeepAliveSeconds(); 불필요하게 메모리를 점유하지 않도록 스레드 살아있는 시간 정함
        // te.setTaskDecorator(); 스레드 만들거나 반환하는 시점에 앞뒤에 콜백을 걸어서 스레드 분석을 할 수 있음.
        te.setThreadNamePrefix("mythread");
        te.initialize();
        return te;
    }

    public void runSpringAsync() {
        try(ConfigurableApplicationContext c = SpringApplication.run(SpringReactivePracticeApplication.class)){

        }
    }

    @Autowired
    MyService myService;

    @Bean
    ApplicationRunner run() {
        return args -> {
            log.info("run()");
            Future<String> f = myService.hello();
            log.info("exit : " + f.isDone());
            log.info("result : " + f.get());

            log.info("run()");
            ListenableFuture<String> fl = myService.helloListenable();
            fl.addCallback(s -> System.out.println(s), e-> System.out.println(e.getMessage()));
            log.info("exit");
        };
    }


    @RestController
    public static class MyController {
        @Autowired
        MyService myService;

        Queue<DeferredResult<String>> results = new ConcurrentLinkedQueue<>();


        @GetMapping("/callable")
        public Callable<String> callable() throws InterruptedException {
            log.info("callable");
            
            // 별도의 스레드로 실행된다
           return () -> {
                log.info("async");
                Thread.sleep(2000);
                return "hello";
            };
        }

        @GetMapping("/dr")
        public DeferredResult<String> dr() throws InterruptedException {

            log.info("dr");

            // setResult가 올때까지 응답은 대기된다
           DeferredResult<String> dr = new DeferredResult<>();
            results.add(dr);
            return dr;
        }

        @GetMapping("/dr/count")
        public String drcount() {
            return String.valueOf(results.size());
        }

        @GetMapping("/dr/event")
        public String drevent(String msg) {
            for(DeferredResult<String> dr : results) {
                dr.setResult("Hello " + msg);
                results.remove(dr);
            }

            return "OK";
        }
    }



}
