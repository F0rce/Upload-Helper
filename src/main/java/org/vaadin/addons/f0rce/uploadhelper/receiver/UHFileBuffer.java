package org.vaadin.addons.f0rce.uploadhelper.receiver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import org.vaadin.addons.f0rce.uploadhelper.UHReceiver;

import com.vaadin.flow.component.upload.receivers.FileData;

/**
 * Basic in file receiver implementation. File is stored by default to File
 * created using {@link java.io.File#createTempFile(String, String)} with a null
 * suffix.
 * <p>
 * For a custom file the constructor {@link UHAbstractFileBuffer(UHFileFactory)}
 * should be used.
 */
public class UHFileBuffer extends UHAbstractFileBuffer implements UHReceiver {

	private FileData file;

	@Override
	public OutputStream receiveUpload(String fileName, String MIMEType) {
		FileOutputStream outputBuffer = createFileOutputStream(fileName);
		file = new FileData(fileName, MIMEType, outputBuffer);

		return outputBuffer;
	}

	/**
	 * Get the file data object.
	 *
	 * @return file data for the latest upload or null
	 */
	public FileData getFileData() {
		return file;
	}

	/**
	 * Get the file name for this buffer.
	 *
	 * @return file name or empty if no file
	 */
	public String getFileName() {
		return file != null ? file.getFileName() : "";
	}

	/**
	 * Get the output stream for file.
	 *
	 * @return file output stream or null if not available
	 */
	public FileDescriptor getFileDescriptor() {
		if (file != null) {
			try {
				return ((FileOutputStream) file.getOutputBuffer()).getFD();
			} catch (IOException e) {
				getLogger().log(Level.WARNING, "Failed to get file descriptor for: '" + getFileName() + "'", e);
			}
		}
		return null;
	}

	/**
	 * Get the input stream for file.
	 *
	 * @return input stream for file or empty stream if file not found
	 */
	public InputStream getInputStream() {
		if (file != null) {
			final File path = file.getFile();
			try {
				return new FileInputStream(path);
			} catch (IOException e) {
				getLogger().log(Level.WARNING, "Failed to create InputStream for: '" + getFileName() + "'", e);
			}
		}
		return new ByteArrayInputStream(new byte[0]);
	}
}
