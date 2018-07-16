package com.netshoes.sample.reactor;

import java.io.File;

class FileNode {
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

  public File getFile() {
    return file;
  }

  public int getDeepLevel() {
    return deepLevel;
  }

  public String toString() {
    return String.format("%s (%d)", file.toString(), deepLevel);
  }
}
