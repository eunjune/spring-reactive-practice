package com.example.springreactivepractice;

import reactor.core.publisher.Flux;

import java.time.Duration;

public class ReactorEx {

    public static void main(String[] args) {
        Flux.<Integer>create(e->{
            e.next(1);
            e.next(2);
            e.next(3);
            e.complete();
        })
        .log()
        .map(s->s*10)
        .reduce(0, (a,b)->a+b)
        .log()
        .subscribe(System.out::println);

        /*
         * interval은 유저스레드가 아닌 데몬스레드로 생성.
         * 데몬스레드의 경우 메인스레드 종료시 같이 종료됨.
         *
         * take : 중간 operator에 의해 스레드가 제한됨
         * */
        Flux.interval(Duration.ofMillis(200))
                .take(10)
                .subscribe(System.out::println);
    }

}
