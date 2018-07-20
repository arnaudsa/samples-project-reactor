package com.netshoes.sample.reactor.simple;

import java.io.File;
import java.util.Arrays;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class Main {
  private static final Logger LOG = Loggers.getLogger(Main.class);
  private static final String RESOURCES_PATH = "src/main/resources";

  public static void main(String... args) {
    generateSimple();
    rangeSimple();
    generateSimple();
    directoryNavigationExampleExpand();
    directoryNavigationExampleExpandDeep();
    directoryNavigationExampleExpandDeepMaxTwoLevels();
    errorHandlerWithFallbackResume();
    errorHandlerWithFallbackReturn();
    errorHandlerWithMap();
    errorWhenCallbackIsNotImplemented();
  }

  private static void rangeSimple() {
    LOG.info("RangeSimple");
    Flux.range(1, 3).subscribe(i -> LOG.info("Number: {}", i));
  }

  private static void generateSimple() {
    LOG.info("GenerateSimple");
    Flux.generate(
            () -> 0,
            (state, sink) -> {
              sink.next(3 * state);
              return state + 1;
            })
        .take(5)
        .subscribe(result -> LOG.info("Multiply by 3: {}", result));
  }

  private static void directoryNavigationExampleExpand() {
    LOG.info("DirectoryNavigationExample - Expand");
    Flux.fromIterable(Arrays.asList(new File(RESOURCES_PATH).listFiles()))
        .filter(File::isDirectory)
        .expand(file -> Flux.fromArray(file.listFiles()).filter(File::isDirectory))
        .subscribe(result -> LOG.info(result.toString()));
  }

  private static void directoryNavigationExampleExpandDeep() {
    LOG.info("DirectoryNavigationExample - ExpandDeep");
    Flux.fromIterable(Arrays.asList(new File(RESOURCES_PATH).listFiles()))
        .filter(File::isDirectory)
        .expandDeep(file -> Flux.fromArray(file.listFiles()).filter(File::isDirectory))
        .subscribe(result -> LOG.info(result.toString()));
  }

  private static void directoryNavigationExampleExpandDeepMaxTwoLevels() {
    LOG.info("DirectoryNavigationExample - ExpandDeep - Max Two Levels");
    Flux.fromIterable(Arrays.asList(new File(RESOURCES_PATH).listFiles()))
        .filter(File::isDirectory)
        .map(FileNode::new)
        .expandDeep(
            fileNode ->
                Flux.fromArray(fileNode.getFile().listFiles())
                    .filter(innerFile -> innerFile.isDirectory() && fileNode.getDeepLevel() < 2)
                    .map(file -> new FileNode(file, fileNode.getDeepLevel() + 1)))
        .subscribe(result -> LOG.info(result.toString()));
  }

  private static void errorHandlerWithFallbackResume() {
    LOG.info("ErrorHandlerWithFallbackResume");
    Mono.just("error")
        .flatMap(
            str -> {
              throw new IllegalArgumentException();
            })
        .onErrorResume(IllegalArgumentException.class, throwable -> Mono.just("success"))
        .subscribe(result -> LOG.info("ErrorHandlerWithFallbackResume: {}", result));
  }

  private static void errorHandlerWithFallbackReturn() {
    LOG.info("ErrorHandlerWithFallbackReturn");
    Mono.just("error")
        .flatMap(
            str -> {
              throw new IllegalArgumentException();
            })
        .onErrorReturn(IllegalArgumentException.class, "success")
        .subscribe(result -> LOG.info("ErrorHandlerWithFallbackReturn: {}", result));
  }

  private static void errorHandlerWithMap() {
    LOG.info("ErrorHandlerWithMap");
    Mono.just("error")
        .flatMap(
            str -> {
              throw new NullPointerException();
            })
        .onErrorMap(NullPointerException.class, e -> new IllegalArgumentException())
        .subscribe(result -> LOG.info("ErrorHandlerWithMap: {}", result));
  }

  private static void errorWhenCallbackIsNotImplemented() {
    LOG.info("ErrorWhenCallbackIsNotImplemented");
    Mono.just("error")
        .flatMap(
            str -> {
              throw new NullPointerException();
            })
        .flatMap(
            str -> {
              throw new IllegalArgumentException("flatMap");
            })
        .onErrorMap(
            NullPointerException.class, throwable -> new IllegalArgumentException("onErrorMap"))
        .subscribe(result -> LOG.info("ErrorWhenCallbackIsNotImplemented: {}", result));
  }
}
