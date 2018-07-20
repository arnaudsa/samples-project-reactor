package com.netshoes.sample.reactor.prime;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class DiscoverPrimeNumbers {
  private static final Scheduler schedulerForCalc = Schedulers.newParallel("calc", 4, true);
  private static final Scheduler schedulerForVerify = Schedulers.newParallel("verify", 2, true);
  private final Logger log = Loggers.getLogger(DiscoverPrimeNumbers.class);

  public Flux<Integer> execute(int maxNumber) {
    return Flux.range(1, maxNumber)
        .flatMap(this::calculateQtyDividers)
        .publishOn(schedulerForVerify)
        .filter(this::hasUniqueDivisor)
        .map(Tuple2::getT1)
        .map(this::logPrimeNumber);
  }

  private boolean hasUniqueDivisor(Tuple2<Integer, Long> tuple) {
    return tuple.getT2() == 1;
  }

  private boolean isZero(Integer remainder) {
    return remainder == 0;
  }

  private Publisher<? extends Tuple2<Integer, Long>> calculateQtyDividers(Integer number) {
    return Flux.generate(
            () -> number,
            (state, sink) -> {
              if (state < 2) {
                sink.complete();
              }
              sink.next(state);
              return state - 1;
            })
        .publishOn(schedulerForCalc)
        .flatMap(divider -> calculateRemainder(number, (Integer) divider))
        .filter(this::isZero)
        .count()
        .map(qtyDividers -> Tuples.of(number, qtyDividers));
  }

  private Mono<Integer> calculateRemainder(Integer number, Integer divider) {
    Integer remainder = number % divider;
    log.trace("The remainder of {}/{} is {}", number, divider, remainder);
    return Mono.just(remainder);
  }

  private Integer logPrimeNumber(Integer primeNumber) {
    log.debug("Number: {} is prime.", primeNumber);
    return primeNumber;
  }
}
