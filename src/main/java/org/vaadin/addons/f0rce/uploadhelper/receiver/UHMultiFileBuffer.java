package org.vaadin.addons.f0rce.uploadhelper.receiver;

import java.io.ByteArrayInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.vaadin.addons.f0rce.uploadhelper.UHMultiFileReceiver;

public class UHMultiFileBuffer extends UHAbstractFileBuffer implements UHMultiFileReceiver {
  private Map<String, UHFileData> files = new HashMap<>();

  @Override
  public OutputStream receiveUpload(String fileName, String MIMEType) {
    FileOutputStream outputBuffer = this.createFileOutputStream(fileName);
    this.files.put(fileName, new UHFileData(fileName, MIMEType, outputBuffer));

    return outputBuffer;
  }

  /**
   * Get the files stored for this buffer.
   *
   * @return files stored
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
   * @return file output stream or null if not available
   */
  public FileDescriptor getFileDescriptor(String fileName) {
    if (this.files.containsKey(fileName)) {
      try {
        return ((FileOutputStream) this.files.get(fileName).getOutputBuffer()).getFD();
      } catch (IOException e) {
        this.getLogger()
            .log(Level.WARNING, "Failed to get file descriptor for: '" + fileName + "'", e);
      }
    }
    return null;
  }

  /**
   * Get the input stream for file with fileName.
   *
   * @param fileName name of file to get input stream for
   * @return input stream for file or empty stream if file not found
   */
  public InputStream getInputStream(String fileName) {
    if (this.files.containsKey(fileName)) {
      try {
        return new FileInputStream(this.files.get(fileName).getFile());
      } catch (IOException e) {
        this.getLogger()
            .log(Level.WARNING, "Failed to create InputStream for: '" + fileName + "'", e);
      }
    }
    return new ByteArrayInputStream(new byte[0]);
  }
}
