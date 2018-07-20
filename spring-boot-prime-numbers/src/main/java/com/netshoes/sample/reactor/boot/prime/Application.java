package com.netshoes.sample.reactor.boot.prime;

import com.netshoes.sample.reactor.prime.DiscoverPrimeNumbers;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.TopicProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;

@SpringBootApplication
@Slf4j
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  DiscoverPrimeNumbers discoverPrimeNumbers() {
    return new DiscoverPrimeNumbers();
  }

  @Bean
  TopicProcessor<List<Integer>> topicProcessor() {
    return TopicProcessor.share("primeNumbers", Queues.XS_BUFFER_SIZE);
  }

  @Bean
  Scheduler discoverPrimeNumbersScheduler() {
    return Schedulers.newElastic("discoverPrimeNumbers", 60);
  }

  @RestController
  @RequiredArgsConstructor
  public static class Controller {
    private final DiscoverPrimeNumbers discoverPrimeNumbers;
    private final TopicProcessor<List<Integer>> topicProcessor;
    private final Scheduler discoverPrimeNumbersScheduler;

    @PostMapping("run")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> run() {
      final Flux<List<Integer>> execute =
          discoverPrimeNumbers.execute(100000).buffer(Duration.ofMillis(100));
      execute.publishOn(discoverPrimeNumbersScheduler);
      execute.subscribe(primeNumber -> log.info("New prime numbers found: {}", primeNumber));
      execute.subscribe(topicProcessor::onNext);
      return Mono.empty();
    }

    @GetMapping(value = "listen", produces = "application/stream+json")
    public Flux<List<Integer>> listen() {
      return topicProcessor.publish().autoConnect();
    }
  }
}
