package org.vaadin.addons.f0rce.uploadhelper.receiver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.vaadin.addons.f0rce.uploadhelper.UHMultiFileReceiver;

public class UHMultiFileMemoryBuffer implements UHMultiFileReceiver {
  private Map<String, UHFileData> files = new HashMap<>();

  @Override
  public OutputStream receiveUpload(String fileName, String MIMEType) {
    ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
    this.files.put(fileName, new UHFileData(fileName, MIMEType, outputBuffer));

    return outputBuffer;
  }

  /**
   * Get the files in memory for this buffer.
   *
   * @return files in memory
   */
  public Set<String> getFiles() {
    return this.files.keySet();
  }

  /**
   * Get file data for upload with file name.
   *
   * @param fileName file name to get upload data for
   * @return file data for filename or null if not found
   */
  public UHFileData getFileData(String fileName) {
    return this.files.get(fileName);
  }

  /**
   * Get the output stream for file.
   *
   * @param fileName name of file to get stream for
   * @return file output stream or empty stream if no file found
   */
  public ByteArrayOutputStream getOutputBuffer(String fileName) {
    if (this.files.containsKey(fileName)) {
      return (ByteArrayOutputStream) this.files.get(fileName).getOutputBuffer();
    }
    return new ByteArrayOutputStream();
  }

  /**
   * Get the input stream for file with filename.
   *
   * @param filename name of file to get input stream for
   * @return input stream for file or empty stream if file not found
   */
  public InputStream getInputStream(String filename) {
    if (this.files.containsKey(filename)) {
      return new ByteArrayInputStream(
          ((ByteArrayOutputStream) this.files.get(filename).getOutputBuffer()).toByteArray());
    }
    return new ByteArrayInputStream(new byte[0]);
  }
}
