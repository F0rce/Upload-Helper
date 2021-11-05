package org.vaadin.addons.f0rce.uploadhelper.events;

import com.vaadin.flow.component.ComponentEvent;

import org.vaadin.addons.f0rce.uploadhelper.UploadHelper;

public class UHFileRejectedEvent extends ComponentEvent<UploadHelper> {

    private String errorMessage;

    /**
     * Creates a new event using the given source and indicator whether the event
     * originated from the client side or the server side.
     *
     * @param source       the source component
     * @param errorMessage the error message
     */
    public UHFileRejectedEvent(UploadHelper source, String errorMessage) {
        super(source, true);
        this.errorMessage = errorMessage;
    }

    /**
     * Get the error message
     *
     * @return errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
