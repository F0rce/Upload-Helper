package org.vaadin.addons.f0rce.uploadhelper;

import java.io.OutputStream;
import java.io.Serializable;

@FunctionalInterface
public interface UHReceiver extends Serializable {

	/**
	 * Invoked when a new upload arrives.
	 *
	 * @param fileName the desired filename of the upload, usually as specified by
	 *                 the client
	 * @param mimeType the MIME type of the uploaded file
	 * @return stream to which the uploaded file should be written
	 */
	OutputStream receiveUpload(String fileName, String mimeType);
}
