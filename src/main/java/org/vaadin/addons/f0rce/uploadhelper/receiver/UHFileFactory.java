package org.vaadin.addons.f0rce.uploadhelper.receiver;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * File factory interface for generating file to store the uploaded data into.
 */
public interface UHFileFactory extends Serializable {

    /**
     * Create a new file for given file name.
     *
     * @param fileName file name to create file for
     * @return {@link File} that should be used
     */
    File createFile(String fileName) throws IOException;
}
