package org.vaadin.addons.f0rce.uploadhelper.receiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class UHAbstractFileBuffer implements Serializable {

    private UHFileFactory factory;

    /**
     * Constructor for creating a file buffer with the default file factory.
     * <p>
     * Files will be created using {@link File#createTempFile(String, String)} and
     * have that build 'upload_tmpfile_{FILENAME}_{currentTimeMillis}'
     */
    public UHAbstractFileBuffer() {
        factory = fileName -> {
            final String tempFileName = "upload_tmpfile_" + fileName + "_" + System.currentTimeMillis();
            return File.createTempFile(tempFileName, null);
        };
    }

    /**
     * Constructor taking in the file factory used to create upload {@link File}.
     *
     * @param factory file factory for file buffer
     */
    public UHAbstractFileBuffer(UHFileFactory factory) {
        this.factory = factory;
    }

    /**
     * Create a file output stream for the file.
     *
     * @param fileName the name of the file
     * @return the file output stream
     */
    protected FileOutputStream createFileOutputStream(String fileName) {
        try {
            return new UHUploadOutputStream(factory.createFile(fileName));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to create file output stream for: '" + fileName + "'", e);
        }
        return null;
    }

    protected Logger getLogger() {
        return Logger.getLogger(this.getClass().getName());
    }
}
