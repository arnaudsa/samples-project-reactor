package reactor;

import java.time.Duration;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class DiscoverPrimeNumbers {
  private static final Logger LOG = Loggers.getLogger(DiscoverPrimeNumbers.class);
  private static final Scheduler schedulerForCalc = Schedulers.newParallel("calc", 4, true);
  private static final Scheduler schedulerForVerify = Schedulers.newParallel("verify", 2, true);

  public static void main(String... args) {
    discoverPrimeNumbers(100000);
  }

  private static void discoverPrimeNumbers(int maxNumber) {
    final Disposable disposable =
        Flux.range(1, maxNumber)
            .flatMap(DiscoverPrimeNumbers::calculateQtyDividers)
            .publishOn(schedulerForVerify)
            .filter(DiscoverPrimeNumbers::hasUniqueDivisor)
            .map(Tuple2::getT1)
            .map(DiscoverPrimeNumbers::logPrimeNumber)
            .buffer(Duration.ofMillis(100))
            .subscribe(primeNumber -> LOG.info("New prime numbers found: {}", primeNumber));

    waitToTerminate(disposable);
  }

  private static boolean hasUniqueDivisor(Tuple2<Integer, Long> tuple) {
    return tuple.getT2() == 1;
  }

  private static boolean isZero(Integer remainder) {
    return remainder == 0;
  }

  private static Publisher<? extends Tuple2<Integer, Long>> calculateQtyDividers(Integer number) {
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
        .filter(DiscoverPrimeNumbers::isZero)
        .count()
        .map(qtyDividers -> Tuples.of(number, qtyDividers));
  }

  private static Mono<Integer> calculateRemainder(Integer number, Integer divider) {
    Integer remainder = number % divider;
    LOG.trace("The remainder of {}/{} is {}", number, divider, remainder);
    return Mono.just(remainder);
  }

  private static void waitToTerminate(Disposable disposable) {
    while (!disposable.isDisposed()) {
      try {
        Thread.sleep(500L);
      } catch (InterruptedException e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  private static Integer logPrimeNumber(Integer primeNumber) {
    LOG.debug("Number: {} is prime.", primeNumber);
    return primeNumber;
  }
}
