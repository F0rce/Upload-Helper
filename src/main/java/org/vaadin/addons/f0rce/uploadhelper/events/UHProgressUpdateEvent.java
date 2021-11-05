package org.vaadin.addons.f0rce.uploadhelper.events;

import com.vaadin.flow.component.ComponentEvent;

import org.vaadin.addons.f0rce.uploadhelper.UploadHelper;

public class UHProgressUpdateEvent extends ComponentEvent<UploadHelper> {
    /**
     * Bytes transferred.
     */
    private final long readBytes;

    /**
     * Total size of file currently being uploaded, -1 if unknown
     */
    private final long contentLength;

    /**
     * Event constructor method to construct a new progress event.
     *
     * @param source        the source of the file
     * @param readBytes     bytes transferred
     * @param contentLength total size of file currently being uploaded, -1 if
     *                      unknown
     */
    public UHProgressUpdateEvent(UploadHelper source, long readBytes, long contentLength) {
        super(source, false);
        this.readBytes = readBytes;
        this.contentLength = contentLength;
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
     * Get bytes transferred for this update.
     *
     * @return bytes transferred
     */
    public long getReadBytes() {
        return readBytes;
    }

    /**
     * Get total file size.
     *
     * @return total file size or -1 if unknown
     */
    public long getContentLength() {
        return contentLength;
    }
}
