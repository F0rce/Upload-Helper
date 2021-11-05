package org.vaadin.addons.f0rce.uploadhelper.events;

import com.vaadin.flow.component.ComponentEvent;

import org.vaadin.addons.f0rce.uploadhelper.UploadHelper;

public class UHAllFinishedEvent extends ComponentEvent<UploadHelper> {

    /**
     * Create an instance of the event.
     *
     * @param source the source of the file
     */
    public UHAllFinishedEvent(UploadHelper source) {
        super(source, false);
    }

}
