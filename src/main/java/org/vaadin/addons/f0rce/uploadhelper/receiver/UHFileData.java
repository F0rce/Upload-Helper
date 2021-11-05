package org.vaadin.addons.f0rce.uploadhelper.receiver;

import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;

import com.vaadin.flow.component.upload.receivers.UploadOutputStream;

public class UHFileData implements Serializable {
	
    private final String fileName, mimeType;
	private final OutputStream outputBuffer;

	/**
	 * Create a UHFileData instance for a file.
	 *
	 * @param fileName     the file name
	 * @param mimeType     the file MIME type
	 * @param outputBuffer the output buffer where to write the file
	 */
	public UHFileData(String fileName, String mimeType, OutputStream outputBuffer) {
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.outputBuffer = outputBuffer;
	}

	/**
	 * Return the mimeType of this file.
	 *
	 * @return mime types of the files
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Return the name of this file.
	 *
	 * @return file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Return the output buffer for this file data.
	 *
	 * @return output buffer
	 */
	public OutputStream getOutputBuffer() {
		return outputBuffer;
	}

	/**
	 *
	 * @return Temporary file containing the uploaded data.
	 * @throws NullPointerException          if outputBuffer is null
	 * @throws UnsupportedOperationException if outputBuffer is not an
	 *                                       {@link UploadOutputStream}
	 */
	public File getFile() {
		if (outputBuffer == null) {
			throw new NullPointerException("OutputBuffer is null");
		}
		if (outputBuffer instanceof UHUploadOutputStream) {
			return ((UHUploadOutputStream) outputBuffer).getFile();
		}
		final String MESSAGE = String.format("%s not supported. Use a UHUploadOutputStream", outputBuffer.getClass());
		throw new UnsupportedOperationException(MESSAGE);
	}
}
