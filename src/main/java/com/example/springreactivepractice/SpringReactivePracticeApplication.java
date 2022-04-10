package com.example.springreactivepractice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
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

}
