package reactive.demo.reactive.service;

import org.springframework.stereotype.Service;
import reactive.demo.reactive.model.Person;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ReactiveService {

    public Mono<String> computeMessage(){
        return Mono.just("Hello! ").delayElement(Duration.ofSeconds(5));
    }

    public Mono<String> getName(){
        return Mono.just("Prasanna").delayElement(Duration.ofSeconds(10));
    }

    public List<Integer> test(){
        List<Integer> number = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
        return number;
    }

    public List<Person> startUserInstance(){
        Person person = new Person();
        person.setFirstname("Prasun");
        person.setLastname("Bhat");
        person.setNumber("9866557777");

        Person person2 = new Person();
        person2.setFirstname("Prasunn");
        person2.setLastname("Bhatt");
        person2.setNumber("9866557777");

        Person person3 = new Person();
        person3.setFirstname("Prasan");
        person3.setLastname("Bhatta");
        person3.setNumber("9866557777");

        Person person4 = new Person();
        person4.setId(1L);
        person4.setFirstname("Prasanna");
        person4.setLastname("Bhattarai");
        person4.setNumber("9866557777");

        List<Person> personList = new ArrayList<>();
        personList.add(person);
        personList.add(person2);
        personList.add(person3);
        personList.add(person4);

        return personList;
    }

    public void StreamTask(){

    }

}
