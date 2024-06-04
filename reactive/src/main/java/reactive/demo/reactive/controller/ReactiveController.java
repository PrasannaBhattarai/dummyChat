package reactive.demo.reactive.controller;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Subscription;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactive.demo.reactive.model.Person;
import reactive.demo.reactive.service.ReactiveService;
import reactive.demo.reactive.service.ReactiveSources;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReactiveController {

    private final ReactiveService reactiveService;
    private final ReactiveSources reactiveSources;

    @GetMapping("/demo")
    public Mono<String> greeting(){
        //method ends despite holding for 5 seconds
        //mono will be resolved without thread exhaustion
        return reactiveService.computeMessage().zipWith(reactiveService.getName())
                .map(value -> {
                return value.getT1()+ value.getT2();
                });
    }

    @GetMapping("/test")
    public void test(){
        List<Integer> intValues = reactiveService.test();

        intValues.forEach(System.out::println);

        intValues.stream().filter(number -> number<5).forEach(System.out::println);

        intValues.stream().filter(n -> n < 5).limit(2).forEach(System.out::println);

        int val = intValues.stream().filter(n -> n > 5).limit(1).findFirst().orElse(-1);
        System.out.println(val);

        List<Person> personList = reactiveService.startUserInstance();

        personList.stream().map(Person::getFirstname).forEach(System.out::println);

        List<Long> longValues= Arrays.asList(1L,2L,3L,4L,5L,6L,7L,8L,9L,10L);

        System.out.println("\n");
        System.out.println("\n");

        longValues.stream().filter(id -> personList.stream().anyMatch(user -> user.getId() == id)).forEach(System.out::println);

        //stream of stream of data loop
        longValues.stream().flatMap(id -> personList.stream().filter(user -> user.getId() == id)).forEach(user -> System.out.println(user.getFirstname()));
    }

    @GetMapping("/user")
    public void user(){
        //stream when emitted, run subscribe function per event
        //stream itself controls how it works, it doesn't run all at once -> the inNumbersFlux method itself
        reactiveSources.intNumbersFlux().subscribe(e -> System.out.println(e));

        //subscribe is passing function to stream
        //where it runs how many threads, depends on flux implementation
        //subscribe does not give stream
        reactiveSources.personFlux().subscribe(person -> System.out.println(person));
    }

    @GetMapping("/try")
    public void tryMethod(){
        //blocking a flux (avoid blocking because it means reactive has no use)
        //takes flux elements and converts to stream
        //waits for item to come in event level
        List<Integer> numbers = reactiveSources.intNumbersFlux()
                .log()
                .toStream()
                .toList();

        //will not execute until stream is complete
        System.out.println(numbers);
    }

    @GetMapping("/mono")
    public void mono(){
        reactiveSources.intNumberMono().subscribe(n -> System.out.println(n));

        //blocking a mono
        //null or returned value
        //mono returns only one result or zero, maybe one list, one value but max only one
        Integer number = reactiveSources.intNumberMono().block();

        //only when event occurs
        Optional<Integer> optNumber = reactiveSources.intNumberMono().blockOptional();

        //Person person = reactiveSources.personMono().block();

        //error or complete is terminal event (both terminates the process), or it receives an item (works per item)
        reactiveSources.intNumberMono().subscribe(
                n -> System.out.println(n),
                er -> System.out.println(er.getMessage()),
                () -> System.out.println("Completion")
        );
    }

    @GetMapping("/flux")
    public void fluxWithHooks(){
        reactiveSources.intNumbersFlux().subscribe(
                n -> System.out.println(n),
                er -> System.out.println(er.getMessage()),
                () -> System.out.println("Completion")
        );

//        Disposable sub = reactiveSources.intNumbersFlux().subscribe(
//                n -> System.out.println(n),
//                er -> System.out.println(er.getMessage()),
//                () -> System.out.println("Completion")
//        );

        reactiveSources.intNumbersFlux().subscribe(new MySubscriber<>());

        //handling backpressure -> controlling rate of flow
        //no backpressure in mono
        //flux has backpressure since multiple times run


        //read for 5 seconds, give up after
        Integer number = reactiveSources.intNumberMono().block(Duration.ofSeconds(5));

    }

    @RequestMapping(value = "/log")
    public void log(){
        reactiveSources.intNumbersFlux()
                .filter(e -> e>5)
                .log()
                .subscribe(System.out::println);

        reactiveSources.intNumbersFlux()
                .log()
                .filter(e -> e> 5)
                .map(e -> e * 10)
                .subscribe(System.out::println);

        reactiveSources.intNumbersFlux()
                .filter(e -> e > 5)
                .map(e -> e * 10)
                .log()

                //order of take matters
                .take(3)
                .subscribe(System.out::println);

        reactiveSources.intNumbersFlux()
                .filter(e -> e > 20)
                .defaultIfEmpty(-1)
                .subscribe(System.out::println);
    }

    @RequestMapping(value = "/map")
    public void map(){
        reactiveSources.intNumbersFlux()
                .map(n -> reactiveSources.personFlux().filter(user -> n.equals(user.getId())))
                .take(1)
                .subscribe(System.out::println);

        reactiveSources.intNumbersFlux()
                .distinct()
                .log()
                .subscribe();

        //not allowing two repeating after succession, otherwise allow
        //counting values at end
        reactiveSources.intNumbersFlux()
                .distinctUntilChanged()
                .log()
                .count()
                .subscribe();
    }

    @RequestMapping(value = "/error")
    public void error(){
        Disposable flux = reactiveSources.intError()
                //do something when error happens
                .doOnError(e -> System.out.println(e.getMessage()))
                //continue despite error, stopping request for more by default is avoided
                .onErrorContinue((e,item) -> System.out.println("Error in item = "+item + " Message= " + e.getMessage()))

                //whenever error, use another flux instead of current
                .onErrorResume(err -> Flux.just(-1,-2,1))
                .doFinally(signalType -> {
                    if (signalType == SignalType.ON_COMPLETE){
                        System.out.println("Complete");
                    }
                    else if (signalType == SignalType.ON_ERROR) {
                        System.out.println("ERROR!");
                    }
                })
                .subscribe(System.out::println,

                //swallow the error completely
                err -> System.out.println(err));


        reactiveSources.intNumbersFlux().collectList().subscribe(System.out::println);

        //mono to flux
        //how many elements to take -> buffer
        reactiveSources.intNumbersFlux().buffer(2).map(list -> list.get(0) + list.get(1))
                .subscribe(System.out::println);
    }
}

class MySubscriber<T> extends BaseSubscriber<T> {
    public void hookOnSubscribe(Subscription subscription){
        System.out.println("Subscribed!");
        request(1);
    }

    public void hookOnNext(T value){
        System.out.println(value.toString() + " received!");

        //toggle this comment for complete value
        //will keep requesting for more upon each execution
        request(1);
    }
}

