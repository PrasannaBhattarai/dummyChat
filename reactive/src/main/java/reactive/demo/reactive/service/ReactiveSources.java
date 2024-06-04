package reactive.demo.reactive.service;

import org.springframework.stereotype.Service;
import reactive.demo.reactive.model.Person;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class ReactiveSources {

    public Flux<String> stringNumbersFlux(){
        return Flux.just("one","two","three","four","five","six","seven","eight","nine","ten")
                .delayElements(Duration.ofSeconds(1));
    }

    public Flux<Integer> intNumbersFlux(){
        return Flux
                .range(1,10)
                .delayElements(Duration.ofSeconds(1));
    }


    public Flux<Integer> intError() throws RuntimeException{
        return Flux
                .range(1,10)
                .map(e -> {
                    if (e==5) throw new RuntimeException();
                    return e;
                });
    }

    public Flux<Integer> intNumbersFluxWithException(){
        return Flux.range(1,10)
                .delayElements(Duration.ofSeconds(1))
                .map(e -> {
                    if (e==5) throw new RuntimeException("An error in flux!");
                    return e;
                });
    }

    public Mono<Integer> intNumberMono(){
        return Mono.just(42)
                .delayElement(Duration.ofSeconds(1));
    }

    public Flux<Person> personFlux(){
        return Flux.just(
                new Person(10L,"Lio","Messi","10000"),
                new Person(7L,"Cris","Ronaldo","10001"),
                new Person(11L,"Jeremy","Doku","10002")
        ).delayElements(Duration.ofSeconds(1));
    }
}
