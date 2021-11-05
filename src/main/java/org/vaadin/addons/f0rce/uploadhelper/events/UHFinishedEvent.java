package org.vaadin.addons.f0rce.uploadhelper.events;

import com.vaadin.flow.component.ComponentEvent;

import org.vaadin.addons.f0rce.uploadhelper.UploadHelper;

public class UHFinishedEvent extends ComponentEvent<UploadHelper> {
    /**
     * Length of the received file.
     */
    private final long length;

    /**
     * MIME type of the received file.
     */
    private final String type;

    /**
     * Received file name.
     */
    private final String fileName;

    /**
     * Create an instance of the event.
     *
     * @param source   the source of the file
     * @param fileName the received file name
     * @param mimeType the MIME type of the received file
     * @param length   the length of the received file
     */
    public UHFinishedEvent(UploadHelper source, String fileName, String mimeType, long length) {
        super(source, false);
        type = mimeType;
        this.fileName = fileName;
        this.length = length;
    }

    /**
     * Upload where the event occurred.
     *
     * @return the Source of the event
     */
    public UploadHelper getUpload() {
        return getSource();
    }

    /**
     * Get the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Get the MIME Type of the file.
     *
     * @return the MIME type
     */
    public String getMIMEType() {
        return type;
    }

    /**
     * Get the length of the file.
     *
     * @return the length
     */
    public long getContentLength() {
        return length;
    }
}
