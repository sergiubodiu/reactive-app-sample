package io.pivotal.apac;

import org.reactivestreams.Publisher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.server.adapter.HttpWebHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.server.HttpServer;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * @author Sergiu Bodiu
 */
@SpringBootApplication
public class ServerApplication {

    public static final String HOST = "localhost";

    public static final int PORT = 8080;

    @Bean
    public RouterFunction<?> routingFunction(PersonHandler handler) {
        return RouterFunctions.route(RequestPredicates.GET("/persons"), handler::all)
                .andRoute(RequestPredicates.GET("/persons/{id}"), handler::byId);
    }

    @Bean
    public HttpServer httpServer(RouterFunction<?> routingFunction){

        HttpWebHandlerAdapter httpHandler = RouterFunctions.toHttpHandler(routingFunction);

        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
        HttpServer server = HttpServer.create(HOST, PORT);
        server.newHandler(adapter).block();

        return server;
    }

    @Bean
    public CommandLineRunner commandLineRunner(PersonRepository personRepository) {
        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {
                Random age = new Random();
                Stream.of("Sergiu", "Jay", "Jade", "Henry", "Fang", "Joanna").forEach(name ->
                        personRepository.save(new Person(name, age.nextInt(50) )));

                personRepository.findAll().forEach(System.out::println);
            }
        };
    }

	public static void main(String[] args) throws IOException {
        SpringApplication.run(ServerApplication.class, args);
        System.out.println("Press ENTER to exit.");
        System.in.read();
	}
}

interface PersonRepository extends MongoRepository<Person, String> {

    CompletableFuture<Person> findById(String id);

    @Query("{}")
    Stream<Person> all();
}

@Document
class Person {

    @Id
    private String id;

    private String name;

    private int age;

    @Override
    public String toString() {
        return "Person{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public Person() {
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

@Component
class PersonHandler {

    final PersonRepository personRepository;

    public PersonHandler(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Mono<ServerResponse> all(ServerRequest request) {
        Flux<Person> people = Flux.fromStream(this.personRepository.all());
        return ServerResponse.ok().body(people, Person.class);
    }

    public Mono<ServerResponse> byId(ServerRequest request) {
        String personId = request.pathVariable("id");
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        return Mono.fromFuture(this.personRepository.findById(personId))
                .then(person -> {
                    Publisher<Person> personPublisher = Mono.just(person);
                    return ServerResponse.ok().body(personPublisher, Person.class);
                })
                .otherwiseIfEmpty(notFound);
    }

}

