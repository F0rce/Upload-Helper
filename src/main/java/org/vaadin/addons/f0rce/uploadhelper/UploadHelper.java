package org.vaadin.addons.f0rce.uploadhelper;

import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.stream.IntStream;

import org.vaadin.addons.f0rce.uploadhelper.events.UHAllFinishedEvent;
import org.vaadin.addons.f0rce.uploadhelper.events.UHFailedEvent;
import org.vaadin.addons.f0rce.uploadhelper.events.UHFileRejectedEvent;
import org.vaadin.addons.f0rce.uploadhelper.events.UHFinishedEvent;
import org.vaadin.addons.f0rce.uploadhelper.events.UHNoInputStreamEvent;
import org.vaadin.addons.f0rce.uploadhelper.events.UHNoOutputStreamEvent;
import org.vaadin.addons.f0rce.uploadhelper.events.UHProgressUpdateEvent;
import org.vaadin.addons.f0rce.uploadhelper.events.UHStartedEvent;
import org.vaadin.addons.f0rce.uploadhelper.events.UHSucceededEvent;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.server.NoInputStreamException;
import com.vaadin.flow.server.NoOutputStreamException;
import com.vaadin.flow.server.StreamReceiver;
import com.vaadin.flow.server.StreamVariable;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonArray;
import elemental.json.JsonObject;

@Tag("lit-uploadhelper")
@JsModule("./@f0rce/uploadhelper/lit-uploadhelper.js")
@CssImport("./@f0rce/uploadhelper/styles.css")
public class UploadHelper extends Component implements HasSize {

	private StreamVariable streamVariable;
	private boolean interrupted = false;

	private int activeUploads = 0;
	private boolean uploading;

	private UHReceiver receiver;

	private int maxFileSize = Integer.MAX_VALUE;

	private UploadHelper() {
		addUploadErrorListener(event -> {
		});

		addUploadSuccessListener(event -> {
		});

		addFileRejectListener(event -> fireEvent(new UHFileRejectedEvent(this, event.getDetailError())));

		getElement().setAttribute("target",
				new StreamReceiver(getElement().getNode(), "upload-helper", getStreamVariable()));

		final String elementFiles = "element.files";
		DomEventListener allFinishedListener = e -> {
			JsonArray files = e.getEventData().getArray(elementFiles);

			boolean isUploading = IntStream.range(0, files.length()).anyMatch(index -> {
				final String KEY = "uploading";
				JsonObject object = files.getObject(index);
				return object.hasKey(KEY) && object.getBoolean(KEY);
			});

			if (this.uploading && !isUploading) {
				this.fireAllFinish();
			}
			this.uploading = isUploading;
		};

		addUploadStartListener(e -> this.uploading = true);

		getElement().addEventListener("uh-upload-success", allFinishedListener).addEventData(elementFiles);
		getElement().addEventListener("uh-upload-error", allFinishedListener).addEventData(elementFiles);
	}

	public UploadHelper(Component dropZone) {
		this();
		String uuid = UUID.randomUUID().toString();
		dropZone.setId(uuid);
		getElement().setProperty("dropZone", uuid);
	}

	public UploadHelper(Component dropZone, UHReceiver receiver) {
		this();
		String uuid = UUID.randomUUID().toString();
		dropZone.setId(uuid);
		getElement().setProperty("dropZone", uuid);
		setReceiver(receiver);
	}

	public UploadHelper(Component dropZone, String shadowDomDropZone, UHReceiver receiver) {
		this();
		String uuid = UUID.randomUUID().toString();
		dropZone.setId(uuid);
		getElement().setProperty("dropZone", uuid + "|" + shadowDomDropZone);
		setReceiver(receiver);
	}

	public UploadHelper(String dropZone) {
		this();
		getElement().setProperty("dropZone", dropZone);
	}

	public UploadHelper(String dropZone, UHReceiver receiver) {
		this();
		getElement().setProperty("dropZone", dropZone);
		setReceiver(receiver);
	}

	/**
	 * Sets a receiver.
	 * 
	 * @param receiver {@link UHReceiver}
	 */
	public void setReceiver(UHReceiver receiver) {
		this.receiver = receiver;
	}

	/**
	 * Return the current receiver.
	 *
	 * @return the StreamVariable.
	 */
	public UHReceiver getReceiver() {
		return receiver;
	}

	/**
	 * Specify the maximum file size in bytes allowed to upload. Notice that it is a
	 * client-side constraint, which will be checked before sending the request.
	 *
	 * @param maxFileSize the maximum file size in bytes
	 */
	public void setMaxFileSize(int maxFileSize) {
		getElement().setProperty("maxFileSize", maxFileSize);
		this.maxFileSize = maxFileSize;
	}

	/**
	 * Get the maximum allowed file size in the client-side, in bytes.
	 *
	 * @return the maximum file size in bytes
	 */
	public int getMaxFileSize() {
		return this.maxFileSize;
	}

	/**
	 * Add listener that is informed on the finished upload.
	 *
	 * @param listener all finished listener to add
	 * @return a {@link Registration} for removing the event listener
	 */
	public Registration addAllFinishedListener(ComponentEventListener<UHAllFinishedEvent> listener) {
		return addListener(UHAllFinishedEvent.class, listener);
	}

	/**
	 * Add a progress listener that is informed on upload progress.
	 *
	 * @param listener progress listener to add
	 * @return registration for removal of listener
	 */
	public Registration addProgressListener(ComponentEventListener<UHProgressUpdateEvent> listener) {
		return addListener(UHProgressUpdateEvent.class, listener);
	}

	/**
	 * Add a succeeded listener that is informed on upload failure.
	 *
	 * @param listener failed listener to add
	 * @return registration for removal of listener
	 */
	public Registration addFailedListener(ComponentEventListener<UHFailedEvent> listener) {
		return addListener(UHFailedEvent.class, listener);
	}

	/**
	 * Add a succeeded listener that is informed on upload finished.
	 *
	 * @param listener finished listener to add
	 * @return registration for removal of listener
	 */
	public Registration addFinishedListener(ComponentEventListener<UHFinishedEvent> listener) {
		return addListener(UHFinishedEvent.class, listener);
	}

	/**
	 * Add a succeeded listener that is informed on upload start.
	 *
	 * @param listener start listener to add
	 * @return registration for removal of listener
	 */
	public Registration addStartedListener(ComponentEventListener<UHStartedEvent> listener) {
		return addListener(UHStartedEvent.class, listener);
	}

	/**
	 * Add a succeeded listener that is informed on upload succeeded.
	 *
	 * @param listener succeeded listener to add
	 * @return registration for removal of listener
	 */
	public Registration addSucceededListener(ComponentEventListener<UHSucceededEvent> listener) {
		return addListener(UHSucceededEvent.class, listener);
	}

	/**
	 * Adds a listener for {@code uh-file-reject} events fired when a file cannot be
	 * added due to some constrains: {@code setMaxFileSize, setAcceptedFileTypes}
	 *
	 * @param listener the listener
	 * @return a {@link Registration} for removing the event listener
	 */
	public Registration addFileRejectedListener(ComponentEventListener<UHFileRejectedEvent> listener) {
		return addListener(UHFileRejectedEvent.class, listener);
	}

	@DomEvent("uh-file-reject")
	public static class UHFileRejectEvent extends ComponentEvent<UploadHelper> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final JsonObject detail;
		private final JsonObject detailFile;
		private final String detailError;

		public UHFileRejectEvent(UploadHelper source, boolean fromClient, @EventData("event.detail") JsonObject detail,
				@EventData("event.detail.file") JsonObject detailFile,
				@EventData("event.detail.error") String detailError) {
			super(source, fromClient);
			this.detail = detail;
			this.detailFile = detailFile;
			this.detailError = detailError;
		}

		public JsonObject getDetail() {
			return detail;
		}

		public JsonObject getDetailFile() {
			return detailFile;
		}

		public String getDetailError() {
			return detailError;
		}
	}

	private Registration addFileRejectListener(ComponentEventListener<UHFileRejectEvent> listener) {
		return addListener(UHFileRejectEvent.class, listener);
	}

	@DomEvent("uh-upload-error")
	public static class UHUploadErrorEvent extends ComponentEvent<UploadHelper> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final JsonObject detail;
		private final JsonObject detailXhr;
		private final JsonObject detailFile;

		public UHUploadErrorEvent(UploadHelper source, boolean fromClient, @EventData("event.detail") JsonObject detail,
				@EventData("event.detail.xhr") JsonObject detailXhr,
				@EventData("event.detail.file") JsonObject detailFile) {
			super(source, fromClient);
			this.detail = detail;
			this.detailXhr = detailXhr;
			this.detailFile = detailFile;
		}

		public JsonObject getDetail() {
			return detail;
		}

		public JsonObject getDetailXhr() {
			return detailXhr;
		}

		public JsonObject getDetailFile() {
			return detailFile;
		}
	}

	/**
	 * Adds a listener for {@code uh-upload-error} events fired by the webcomponent.
	 *
	 * @param listener the listener
	 * @return a {@link Registration} for removing the event listener
	 */
	private Registration addUploadErrorListener(ComponentEventListener<UHUploadErrorEvent> listener) {
		return addListener(UHUploadErrorEvent.class, listener);
	}

	@DomEvent("uh-upload-start")
	public static class UHUploadStartEvent extends ComponentEvent<UploadHelper> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final JsonObject detail;
		private final JsonObject detailXhr;
		private final JsonObject detailFile;

		public UHUploadStartEvent(UploadHelper source, boolean fromClient, @EventData("event.detail") JsonObject detail,
				@EventData("event.detail.xhr") JsonObject detailXhr,
				@EventData("event.detail.file") JsonObject detailFile) {
			super(source, fromClient);
			this.detail = detail;
			this.detailXhr = detailXhr;
			this.detailFile = detailFile;
		}

		public JsonObject getDetail() {
			return detail;
		}

		public JsonObject getDetailXhr() {
			return detailXhr;
		}

		public JsonObject getDetailFile() {
			return detailFile;
		}
	}

	/**
	 * Adds a listener for {@code uh-upload-start} events fired by the webcomponent.
	 *
	 * @param listener the listener
	 * @return a {@link Registration} for removing the event listener
	 */
	private Registration addUploadStartListener(ComponentEventListener<UHUploadStartEvent> listener) {
		return addListener(UHUploadStartEvent.class, listener);
	}

	@DomEvent("uh-upload-success")
	public static class UHUploadSuccessEvent extends ComponentEvent<UploadHelper> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final JsonObject detail;
		private final JsonObject detailXhr;
		private final JsonObject detailFile;

		public UHUploadSuccessEvent(UploadHelper source, boolean fromClient,
				@EventData("event.detail") JsonObject detail, @EventData("event.detail.xhr") JsonObject detailXhr,
				@EventData("event.detail.file") JsonObject detailFile) {
			super(source, fromClient);
			this.detail = detail;
			this.detailXhr = detailXhr;
			this.detailFile = detailFile;
		}

		public JsonObject getDetail() {
			return detail;
		}

		public JsonObject getDetailXhr() {
			return detailXhr;
		}

		public JsonObject getDetailFile() {
			return detailFile;
		}
	}

	/**
	 * Adds a listener for {@code uh-upload-success} events fired by the
	 * webcomponent.
	 *
	 * @param listener the listener
	 * @return a {@link Registration} for removing the event listener
	 */
	private Registration addUploadSuccessListener(ComponentEventListener<UHUploadSuccessEvent> listener) {
		return addListener(UHUploadSuccessEvent.class, listener);
	}

	private void fireStarted(String filename, String mimeType, long contentLength) {
		fireEvent(new UHStartedEvent(this, filename, mimeType, contentLength));
	}

	private void fireNoInputStream(String filename, String mimeType, long length) {
		fireEvent(new UHNoInputStreamEvent(this, filename, mimeType, length));
	}

	private void fireNoOutputStream(String filename, String mimeType, long length) {
		fireEvent(new UHNoOutputStreamEvent(this, filename, mimeType, length));
	}

	private void fireUploadInterrupted(String filename, String mimeType, long length, Exception e) {
		fireEvent(new UHFailedEvent(this, filename, mimeType, length, e));
	}

	private void fireUploadSuccess(String filename, String mimeType, long length) {
		fireEvent(new UHSucceededEvent(this, filename, mimeType, length));
	}

	private void fireUploadFinish(String filename, String mimeType, long length) {
		fireEvent(new UHFinishedEvent(this, filename, mimeType, length));
	}

	private void fireAllFinish() {
		fireEvent(new UHAllFinishedEvent(this));
	}

	private void fireUpdateProgress(long totalBytes, long contentLength) {
		fireEvent(new UHProgressUpdateEvent(this, totalBytes, contentLength));
	}

	private void startUpload() {
		if (1 <= activeUploads) {
			throw new IllegalStateException("Maximum supported amount of uploads already started");
		}
		activeUploads++;
	}

	private void endUpload() {
		activeUploads--;
		interrupted = false;
	}

	private StreamVariable getStreamVariable() {
		if (streamVariable == null) {
			streamVariable = new DefaultStreamVariable(this);
		}
		return streamVariable;
	}

	private static class DefaultStreamVariable implements StreamVariable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private Deque<StreamVariable.StreamingStartEvent> lastStartedEvent = new ArrayDeque<>();

		private final UploadHelper uploadHelper;

		public DefaultStreamVariable(UploadHelper uploadHelper) {
			this.uploadHelper = uploadHelper;
		}

		@Override
		public boolean listenProgress() {
			return uploadHelper.getEventBus().hasListener(UHProgressUpdateEvent.class);
		}

		@Override
		public void onProgress(StreamVariable.StreamingProgressEvent event) {
			uploadHelper.fireUpdateProgress(event.getBytesReceived(), event.getContentLength());
		}

		@Override
		public boolean isInterrupted() {
			return uploadHelper.interrupted;
		}

		@Override
		public OutputStream getOutputStream() {
			if (uploadHelper.getReceiver() == null) {
				throw new IllegalStateException("Upload cannot be performed without a receiver set. "
						+ "Please firstly set the receiver implementation with uploadHelper.setReceiver");
			}
			StreamVariable.StreamingStartEvent event = lastStartedEvent.pop();
			OutputStream receiveUpload = uploadHelper.getReceiver().receiveUpload(event.getFileName(),
					event.getMimeType());
			return receiveUpload;
		}

		@Override
		public void streamingStarted(StreamVariable.StreamingStartEvent event) {
			uploadHelper.startUpload();
			try {
				uploadHelper.fireStarted(event.getFileName(), event.getMimeType(), event.getContentLength());
			} finally {
				lastStartedEvent.addLast(event);
			}
		}

		@Override
		public void streamingFinished(StreamVariable.StreamingEndEvent event) {
			try {
				uploadHelper.fireUploadSuccess(event.getFileName(), event.getMimeType(), event.getContentLength());
			} finally {
				uploadHelper.endUpload();
				uploadHelper.fireUploadFinish(event.getFileName(), event.getMimeType(), event.getContentLength());
			}
		}

		@Override
		public void streamingFailed(StreamVariable.StreamingErrorEvent event) {
			try {
				Exception exception = event.getException();
				if (exception instanceof NoInputStreamException) {
					uploadHelper.fireNoInputStream(event.getFileName(), event.getMimeType(), 0);
				} else if (exception instanceof NoOutputStreamException) {
					uploadHelper.fireNoOutputStream(event.getFileName(), event.getMimeType(), 0);
				} else {
					uploadHelper.fireUploadInterrupted(event.getFileName(), event.getMimeType(),
							event.getBytesReceived(), exception);
				}
			} finally {
				uploadHelper.endUpload();
				uploadHelper.fireUploadFinish(event.getFileName(), event.getMimeType(), event.getContentLength());
			}
		}

	}

}
