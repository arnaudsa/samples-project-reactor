package com.netshoes.sample.reactor;

import java.io.File;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

public class Sample {
  private static final Logger LOG = LoggerFactory.getLogger(Sample.class);
  private static final String RESOURCES_PATH = "src/main/resources";

  public static void main(String... args) {
    rangeSimple();
    generateSimple();
    directoryNavigationExampleExpand();
    directoryNavigationExampleExpandDeep();
    directoryNavigationExampleExpandDeepMaxTwoLevels();
  }

  private static void directoryNavigationExampleExpand() {
    LOG.info("DirectoryNavigationExample - Expand");
    Flux.fromIterable(Arrays.asList(new File(RESOURCES_PATH).listFiles()))
        .filter(File::isDirectory)
        .expand(
            file -> Flux.fromArray(file.listFiles()).filter(innerFile -> innerFile.isDirectory()))
        .subscribe(result -> LOG.debug(result.toString()));
  }

  private static void directoryNavigationExampleExpandDeep() {
    LOG.info("DirectoryNavigationExample - ExpandDeep");
    Flux.fromIterable(Arrays.asList(new File(RESOURCES_PATH).listFiles()))
        .filter(File::isDirectory)
        .expandDeep(file -> Flux.fromArray(file.listFiles()).filter(File::isDirectory))
        .subscribe(result -> LOG.debug(result.toString()));
  }

  private static void directoryNavigationExampleExpandDeepMaxTwoLevels() {
    LOG.info("DirectoryNavigationExample - ExpandDeep - Max Two Levels");
    Flux.fromIterable(Arrays.asList(new File(RESOURCES_PATH).listFiles()))
        .filter(File::isDirectory)
        .map(FileNode::new)
        .expandDeep(
            fileNode ->
                Flux.fromArray(fileNode.file.listFiles())
                    .filter(innerFile -> innerFile.isDirectory() && fileNode.deepLevel < 2)
                    .map(file -> new FileNode(file, fileNode.deepLevel + 1)))
        .subscribe(result -> LOG.debug(result.toString()));
  }

  private static void rangeSimple() {
    LOG.info("RangeSimple");
    Flux.range(1, 3).subscribe(i -> LOG.debug("Number: {}", i));
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
        .subscribe(result -> LOG.debug("Multiply by 3: {}", result));
  }

  private static class FileNode {
    private final File file;
    private final int deepLevel;

    public FileNode(File file) {
      this.file = file;
      this.deepLevel = 0;
    }

    public FileNode(File file, int deepLevel) {
      this.file = file;
      this.deepLevel = deepLevel;
    }

    public String toString() {
      return String.format("%s (%d)", file.toString(), deepLevel);
    }
  }
}
