package com.netshoes.sample.reactor.boot.prime;

import com.netshoes.sample.reactor.prime.DiscoverPrimeNumbers;
import java.time.Duration;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
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
    private Disposable process;

    @PostMapping(value = "start/{qty}")
    public ResponseEntity<Void> start(@PathVariable @NotNull Integer qty) {
      if (isRunning()) {
        return new ResponseEntity(HttpStatus.CONFLICT);
      }

      process =
          discoverPrimeNumbers
              .execute(qty)
              .buffer(Duration.ofMillis(1000))
              .publishOn(discoverPrimeNumbersScheduler)
              .subscribe(
                  primeNumbers -> {
                    log.info("New prime numbers found: {}", primeNumbers);
                    topicProcessor.onNext(primeNumbers);
                  });

      return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @GetMapping(value = "isRunning")
    public boolean isRunning() {
      boolean running = process != null && !process.isDisposed();
      log.info("Process is running: {}", running);
      return running;
    }

    @PostMapping("stop")
    public ResponseEntity<Void> stop() {
      if (!isRunning()) {
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
      }
      process.dispose();
      return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @GetMapping(value = "listen", produces = "application/stream+json")
    public Flux<List<Integer>> listen() {
      return topicProcessor.publish().autoConnect();
    }
  }
}
