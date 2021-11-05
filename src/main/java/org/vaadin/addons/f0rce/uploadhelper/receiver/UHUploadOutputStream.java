package org.vaadin.addons.f0rce.uploadhelper.receiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;

public class UHUploadOutputStream extends FileOutputStream implements Serializable {
    private final File file;

    /**
     * @see FileOutputStream#FileOutputStream(File)
     * @param file File to write.
     * @throws FileNotFoundException see
     *                               {@link FileOutputStream#FileOutputStream(File)}
     */
    public UHUploadOutputStream(File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    /**
     * @return File written by this output stream.
     */
    public File getFile() {
        return file;
    }
}
