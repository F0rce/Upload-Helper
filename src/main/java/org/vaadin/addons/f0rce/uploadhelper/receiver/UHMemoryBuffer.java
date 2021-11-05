package org.vaadin.addons.f0rce.uploadhelper.receiver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.vaadin.addons.f0rce.uploadhelper.UHReceiver;

/**
 * Basic in memory file receiver implementation.
 */
public class UHMemoryBuffer implements UHReceiver {

    private UHFileData file;

    @Override
    public OutputStream receiveUpload(String fileName, String MIMEType) {
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        file = new UHFileData(fileName, MIMEType, outputBuffer);

        return outputBuffer;
    }

    /**
     * Get the file data object.
     * 
     * @return file data for the latest upload or null
     */
    public UHFileData getFileData() {
        return file;
    }

    /**
     * Get the file name for this buffer.
     * 
     * @return file name or empty if no file
     */
    public String getFileName() {
        return file != null ? file.getFileName() : "";
    }

    /**
     * Get the input stream for file with filename.
     * 
     * @return input stream for file or empty stream if file not found
     */
    public InputStream getInputStream() {
        if (file != null) {
            return new ByteArrayInputStream(((ByteArrayOutputStream) file.getOutputBuffer()).toByteArray());
        }
        return new ByteArrayInputStream(new byte[0]);
    }
}
