package com.netshoes.sample.reactor.prime;

import java.time.Duration;
import reactor.core.Disposable;
import reactor.util.Logger;
import reactor.util.Loggers;

public class Main {
  private static final Logger LOG = Loggers.getLogger(Main.class);

  public static void main(String... args) {
    discoverPrimeNumbers(100000);
  }

  private static void discoverPrimeNumbers(int maxNumber) {
    final DiscoverPrimeNumbers discover = new DiscoverPrimeNumbers();

    final Disposable disposable =
        discover
            .execute(maxNumber)
            .buffer(Duration.ofMillis(100))
            .subscribe(primeNumber -> LOG.info("New prime numbers found: {}", primeNumber));

    waitToTerminate(disposable);
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
}
