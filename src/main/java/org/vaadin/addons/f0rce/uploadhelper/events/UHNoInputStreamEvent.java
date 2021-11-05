package org.vaadin.addons.f0rce.uploadhelper.events;

import org.vaadin.addons.f0rce.uploadhelper.UploadHelper;

public class UHNoInputStreamEvent extends UHFailedEvent {

    /**
     * Create an instance of the event.
     *
     * @param source   the source of the file
     * @param fileName the received file name
     * @param mimeType the MIME type of the received file
     * @param length   the length of the received file
     */
    public UHNoInputStreamEvent(UploadHelper source, String fileName, String mimeType, long length) {
        super(source, fileName, mimeType, length);
    }

}
