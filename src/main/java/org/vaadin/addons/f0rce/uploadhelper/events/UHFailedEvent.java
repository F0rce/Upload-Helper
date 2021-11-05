package org.vaadin.addons.f0rce.uploadhelper.events;

import org.vaadin.addons.f0rce.uploadhelper.UploadHelper;

public class UHFailedEvent extends UHFinishedEvent {

    private Exception reason = null;

    /**
     * Create an instance of the event.
     *
     * @param source   the source of the file
     * @param filename the received file name
     * @param mimeType the MIME type of the received file
     * @param length   the number of uploaded bytes
     * @param reason   exception that failed the upload
     */
    public UHFailedEvent(UploadHelper source, String filename, String mimeType, long length, Exception reason) {
        this(source, filename, mimeType, length);
        this.reason = reason;
    }

    /**
     * Create an instance of the event.
     *
     * @param source   the source of the file
     * @param filename the received file name
     * @param MIMEType the MIME type of the received file
     * @param length   the number of uploaded bytes
     */
    public UHFailedEvent(UploadHelper source, String filename, String MIMEType, long length) {
        super(source, filename, MIMEType, length);
    }

    /**
     * Get the exception that caused the failure.
     *
     * @return the exception that caused the failure, null if n/a
     */
    public Exception getReason() {
        return reason;
    }

    /**
     * Get the number of uploaded bytes.
     *
     * @return the number of uploaded bytes
     */
    @Override
    public long getContentLength() {
        return super.getContentLength();
    }

}
