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

import com.vaadin.flow.component.AttachEvent;
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
  private int maxFiles = 1;

  private String dropZoneString = "";
  private boolean visualFeedback = true;

  private UploadHelper() {
    this.addUploadErrorListener(event -> {});

    this.addUploadSuccessListener(event -> {});

    this.addFileRejectListener(
        event -> this.fireEvent(new UHFileRejectedEvent(this, event.getDetailError())));

    this.getElement()
        .setAttribute(
            "target",
            new StreamReceiver(
                this.getElement().getNode(), "upload-helper", this.getStreamVariable()));

    final String elementFiles = "element.files";
    DomEventListener allFinishedListener =
        e -> {
          JsonArray files = e.getEventData().getArray(elementFiles);

          boolean isUploading =
              IntStream.range(0, files.length())
                  .anyMatch(
                      index -> {
                        final String KEY = "uploading";
                        JsonObject object = files.getObject(index);
                        return object.hasKey(KEY) && object.getBoolean(KEY);
                      });

          if (this.uploading && !isUploading) {
            this.fireAllFinish();
          }
          this.uploading = isUploading;
        };

    this.addUploadStartListener(e -> this.uploading = true);

    this.getElement()
        .addEventListener("uh-upload-success", allFinishedListener)
        .addEventData(elementFiles);
    this.getElement()
        .addEventListener("uh-upload-error", allFinishedListener)
        .addEventData(elementFiles);
  }

  public UploadHelper(Component dropZone) {
    this();
    String uuid = UUID.randomUUID().toString();
    dropZone.setId(uuid);
    this.getElement().setProperty("dropZone", uuid);
  }

  public UploadHelper(Component dropZone, UHReceiver receiver) {
    this();
    String uuid = UUID.randomUUID().toString();
    dropZone.setId(uuid);
    this.getElement().setProperty("dropZone", uuid);
    this.setReceiver(receiver);
  }

  public UploadHelper(Component dropZone, String shadowDomDropZone, UHReceiver receiver) {
    this();
    String uuid = UUID.randomUUID().toString();
    dropZone.setId(uuid);
    this.getElement().setProperty("dropZone", uuid + "|" + shadowDomDropZone);
    this.setReceiver(receiver);
  }

  public UploadHelper(String dropZone) {
    this();
    this.getElement().setProperty("dropZone", dropZone);
  }

  public UploadHelper(String dropZone, UHReceiver receiver) {
    this();
    this.getElement().setProperty("dropZone", dropZone);
    this.setReceiver(receiver);
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    this.getElement().setProperty("dropZone", this.dropZoneString);
  }

  /**
   * Sets a receiver.
   *
   * @param receiver {@link UHReceiver}
   */
  public void setReceiver(UHReceiver receiver) {
    this.receiver = receiver;
    if (!(receiver instanceof UHMultiFileReceiver)) {
      this.setMaxFiles(1);
    }
  }

  /**
   * Sets the dropZone (the {@link Component} which Upload-Helper should be enabled on).
   *
   * @param dropZone {@link Component}
   */
  public void setDropZone(Component dropZone) {
    String uuid = UUID.randomUUID().toString();
    dropZone.setId(uuid);
    this.dropZoneString = uuid;
    this.getElement().setProperty("dropZone", this.dropZoneString);
  }

  /**
   * Sets the dropZone in the {@link Component}'s ShadowDom.
   *
   * @param dropZone {@link Component}
   * @param shadowDomDropZone {@link String}
   */
  public void setDropZone(Component dropZone, String shadowDomDropZone) {
    String uuid = UUID.randomUUID().toString();
    dropZone.setId(uuid);
    this.dropZoneString = uuid + "|" + shadowDomDropZone;
    this.getElement().setProperty("dropZone", this.dropZoneString);
  }

  /**
   * Sets the dropZone (the Element ID which Upload-Helper should be enabled on).
   *
   * @param dropZone {@link String}
   */
  public void setDropZone(String dropZone) {
    this.dropZoneString = dropZone;
    this.getElement().setProperty("dropZone", this.dropZoneString);
  }

  /**
   * Sets the dropZone in the ShadowDom of given Element ID.
   *
   * @param dropZone {@link String}
   * @param shadowDomDropZone {@link String}
   */
  public void setDropZone(String dropZone, String shadowDomDropZone) {
    this.dropZoneString = dropZone + "|" + shadowDomDropZone;
    this.getElement().setProperty("dropZone", this.dropZoneString);
  }

  /** Removes the dropZone / disables uploading to it. */
  public void removeDropZone() {
    this.dropZoneString = "";
    this.getElement().setProperty("dropZone", this.dropZoneString);
  }

  /**
   * Return the current receiver.
   *
   * @return the StreamVariable.
   */
  public UHReceiver getReceiver() {
    return this.receiver;
  }

  /**
   * Specify the maximum file size in bytes allowed to upload. Notice that it is a client-side
   * constraint, which will be checked before sending the request.
   *
   * @param maxFileSize the maximum file size in bytes
   */
  public void setMaxFileSize(int maxFileSize) {
    this.getElement().setProperty("maxFileSize", maxFileSize);
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
   * Limit of files to upload, by default it is 1. If the value is set to one, native file browser
   * will prevent selecting multiple files.
   *
   * @param maxFiles int
   */
  public void setMaxFiles(int maxFiles) {
    this.getElement().setProperty("maxFiles", maxFiles);
    this.maxFiles = maxFiles;
  }

  /** @return maxFiles int */
  public int getMaxFiles() {
    return this.maxFiles;
  }

  /**
   * Add listener that is informed on the finished upload.
   *
   * @param listener all finished listener to add
   * @return a {@link Registration} for removing the event listener
   */
  public Registration addAllFinishedListener(ComponentEventListener<UHAllFinishedEvent> listener) {
    return this.addListener(UHAllFinishedEvent.class, listener);
  }

  /**
   * Add a progress listener that is informed on upload progress.
   *
   * @param listener progress listener to add
   * @return registration for removal of listener
   */
  public Registration addProgressListener(ComponentEventListener<UHProgressUpdateEvent> listener) {
    return this.addListener(UHProgressUpdateEvent.class, listener);
  }

  /**
   * Add a succeeded listener that is informed on upload failure.
   *
   * @param listener failed listener to add
   * @return registration for removal of listener
   */
  public Registration addFailedListener(ComponentEventListener<UHFailedEvent> listener) {
    return this.addListener(UHFailedEvent.class, listener);
  }

  /**
   * Add a succeeded listener that is informed on upload finished.
   *
   * @param listener finished listener to add
   * @return registration for removal of listener
   */
  public Registration addFinishedListener(ComponentEventListener<UHFinishedEvent> listener) {
    return this.addListener(UHFinishedEvent.class, listener);
  }

  /**
   * Add a succeeded listener that is informed on upload start.
   *
   * @param listener start listener to add
   * @return registration for removal of listener
   */
  public Registration addStartedListener(ComponentEventListener<UHStartedEvent> listener) {
    return this.addListener(UHStartedEvent.class, listener);
  }

  /**
   * Add a succeeded listener that is informed on upload succeeded.
   *
   * @param listener succeeded listener to add
   * @return registration for removal of listener
   */
  public Registration addSucceededListener(ComponentEventListener<UHSucceededEvent> listener) {
    return this.addListener(UHSucceededEvent.class, listener);
  }

  /**
   * Adds a listener for {@code uh-file-reject} events fired when a file cannot be added due to some
   * constrains: {@code setMaxFileSize, setAcceptedFileTypes}
   *
   * @param listener the listener
   * @return a {@link Registration} for removing the event listener
   */
  public Registration addFileRejectedListener(
      ComponentEventListener<UHFileRejectedEvent> listener) {
    return this.addListener(UHFileRejectedEvent.class, listener);
  }

  /**
   * If set the false, the background color and the opacity of the dropZone Element is not changed.
   * Else CSS Classes are added to have some kind of visual feedback.
   *
   * @param visualFeedback boolean
   */
  public void setVisualFeedback(boolean visualFeedback) {
    this.getElement().setProperty("visualFeedback", visualFeedback);
    this.visualFeedback = visualFeedback;
  }

  /**
   * Returns if visual feedback is enabled.
   *
   * @return boolean
   */
  public boolean isVisualFeedback() {
    return this.visualFeedback;
  }

  @DomEvent("uh-file-reject")
  public static class UHFileRejectEvent extends ComponentEvent<UploadHelper> {
    /** */
    private static final long serialVersionUID = 1L;

    private final JsonObject detail;
    private final JsonObject detailFile;
    private final String detailError;

    public UHFileRejectEvent(
        UploadHelper source,
        boolean fromClient,
        @EventData("event.detail") JsonObject detail,
        @EventData("event.detail.file") JsonObject detailFile,
        @EventData("event.detail.error") String detailError) {
      super(source, fromClient);
      this.detail = detail;
      this.detailFile = detailFile;
      this.detailError = detailError;
    }

    public JsonObject getDetail() {
      return this.detail;
    }

    public JsonObject getDetailFile() {
      return this.detailFile;
    }

    public String getDetailError() {
      return this.detailError;
    }
  }

  private Registration addFileRejectListener(ComponentEventListener<UHFileRejectEvent> listener) {
    return this.addListener(UHFileRejectEvent.class, listener);
  }

  @DomEvent("uh-upload-error")
  public static class UHUploadErrorEvent extends ComponentEvent<UploadHelper> {
    /** */
    private static final long serialVersionUID = 1L;

    private final JsonObject detail;
    private final JsonObject detailXhr;
    private final JsonObject detailFile;

    public UHUploadErrorEvent(
        UploadHelper source,
        boolean fromClient,
        @EventData("event.detail") JsonObject detail,
        @EventData("event.detail.xhr") JsonObject detailXhr,
        @EventData("event.detail.file") JsonObject detailFile) {
      super(source, fromClient);
      this.detail = detail;
      this.detailXhr = detailXhr;
      this.detailFile = detailFile;
    }

    public JsonObject getDetail() {
      return this.detail;
    }

    public JsonObject getDetailXhr() {
      return this.detailXhr;
    }

    public JsonObject getDetailFile() {
      return this.detailFile;
    }
  }

  /**
   * Adds a listener for {@code uh-upload-error} events fired by the webcomponent.
   *
   * @param listener the listener
   * @return a {@link Registration} for removing the event listener
   */
  private Registration addUploadErrorListener(ComponentEventListener<UHUploadErrorEvent> listener) {
    return this.addListener(UHUploadErrorEvent.class, listener);
  }

  @DomEvent("uh-upload-start")
  public static class UHUploadStartEvent extends ComponentEvent<UploadHelper> {
    /** */
    private static final long serialVersionUID = 1L;

    private final JsonObject detail;
    private final JsonObject detailXhr;
    private final JsonObject detailFile;

    public UHUploadStartEvent(
        UploadHelper source,
        boolean fromClient,
        @EventData("event.detail") JsonObject detail,
        @EventData("event.detail.xhr") JsonObject detailXhr,
        @EventData("event.detail.file") JsonObject detailFile) {
      super(source, fromClient);
      this.detail = detail;
      this.detailXhr = detailXhr;
      this.detailFile = detailFile;
    }

    public JsonObject getDetail() {
      return this.detail;
    }

    public JsonObject getDetailXhr() {
      return this.detailXhr;
    }

    public JsonObject getDetailFile() {
      return this.detailFile;
    }
  }

  /**
   * Adds a listener for {@code uh-upload-start} events fired by the webcomponent.
   *
   * @param listener the listener
   * @return a {@link Registration} for removing the event listener
   */
  private Registration addUploadStartListener(ComponentEventListener<UHUploadStartEvent> listener) {
    return this.addListener(UHUploadStartEvent.class, listener);
  }

  @DomEvent("uh-upload-success")
  public static class UHUploadSuccessEvent extends ComponentEvent<UploadHelper> {
    /** */
    private static final long serialVersionUID = 1L;

    private final JsonObject detail;
    private final JsonObject detailXhr;
    private final JsonObject detailFile;

    public UHUploadSuccessEvent(
        UploadHelper source,
        boolean fromClient,
        @EventData("event.detail") JsonObject detail,
        @EventData("event.detail.xhr") JsonObject detailXhr,
        @EventData("event.detail.file") JsonObject detailFile) {
      super(source, fromClient);
      this.detail = detail;
      this.detailXhr = detailXhr;
      this.detailFile = detailFile;
    }

    public JsonObject getDetail() {
      return this.detail;
    }

    public JsonObject getDetailXhr() {
      return this.detailXhr;
    }

    public JsonObject getDetailFile() {
      return this.detailFile;
    }
  }

  /**
   * Adds a listener for {@code uh-upload-success} events fired by the webcomponent.
   *
   * @param listener the listener
   * @return a {@link Registration} for removing the event listener
   */
  private Registration addUploadSuccessListener(
      ComponentEventListener<UHUploadSuccessEvent> listener) {
    return this.addListener(UHUploadSuccessEvent.class, listener);
  }

  private void fireStarted(String filename, String mimeType, long contentLength) {
    this.fireEvent(new UHStartedEvent(this, filename, mimeType, contentLength));
  }

  private void fireNoInputStream(String filename, String mimeType, long length) {
    this.fireEvent(new UHNoInputStreamEvent(this, filename, mimeType, length));
  }

  private void fireNoOutputStream(String filename, String mimeType, long length) {
    this.fireEvent(new UHNoOutputStreamEvent(this, filename, mimeType, length));
  }

  private void fireUploadInterrupted(String filename, String mimeType, long length, Exception e) {
    this.fireEvent(new UHFailedEvent(this, filename, mimeType, length, e));
  }

  private void fireUploadSuccess(String filename, String mimeType, long length) {
    this.fireEvent(new UHSucceededEvent(this, filename, mimeType, length));
  }

  private void fireUploadFinish(String filename, String mimeType, long length) {
    this.fireEvent(new UHFinishedEvent(this, filename, mimeType, length));
  }

  private void fireAllFinish() {
    this.fireEvent(new UHAllFinishedEvent(this));
  }

  private void fireUpdateProgress(long totalBytes, long contentLength) {
    this.fireEvent(new UHProgressUpdateEvent(this, totalBytes, contentLength));
  }

  private void startUpload() {
    if (this.maxFiles != 0 && this.maxFiles <= this.activeUploads) {
      throw new IllegalStateException("Maximum supported amount of uploads already started");
    }
    this.activeUploads++;
  }

  private void endUpload() {
    this.activeUploads--;
    this.interrupted = false;
  }

  private StreamVariable getStreamVariable() {
    if (this.streamVariable == null) {
      this.streamVariable = new DefaultStreamVariable(this);
    }
    return this.streamVariable;
  }

  private static class DefaultStreamVariable implements StreamVariable {

    /** */
    private static final long serialVersionUID = 1L;

    private Deque<StreamVariable.StreamingStartEvent> lastStartedEvent = new ArrayDeque<>();

    private final UploadHelper uploadHelper;

    public DefaultStreamVariable(UploadHelper uploadHelper) {
      this.uploadHelper = uploadHelper;
    }

    @Override
    public boolean listenProgress() {
      return this.uploadHelper.getEventBus().hasListener(UHProgressUpdateEvent.class);
    }

    @Override
    public void onProgress(StreamVariable.StreamingProgressEvent event) {
      this.uploadHelper.fireUpdateProgress(event.getBytesReceived(), event.getContentLength());
    }

    @Override
    public boolean isInterrupted() {
      return this.uploadHelper.interrupted;
    }

    @Override
    public OutputStream getOutputStream() {
      if (this.uploadHelper.getReceiver() == null) {
        throw new IllegalStateException(
            "Upload cannot be performed without a receiver set. "
                + "Please firstly set the receiver implementation with uploadHelper.setReceiver");
      }
      StreamVariable.StreamingStartEvent event = this.lastStartedEvent.pop();
      OutputStream receiveUpload =
          this.uploadHelper.getReceiver().receiveUpload(event.getFileName(), event.getMimeType());
      return receiveUpload;
    }

    @Override
    public void streamingStarted(StreamVariable.StreamingStartEvent event) {
      this.uploadHelper.startUpload();
      try {
        this.uploadHelper.fireStarted(
            event.getFileName(), event.getMimeType(), event.getContentLength());
      } finally {
        this.lastStartedEvent.addLast(event);
      }
    }

    @Override
    public void streamingFinished(StreamVariable.StreamingEndEvent event) {
      try {
        this.uploadHelper.fireUploadSuccess(
            event.getFileName(), event.getMimeType(), event.getContentLength());
      } finally {
        this.uploadHelper.endUpload();
        this.uploadHelper.fireUploadFinish(
            event.getFileName(), event.getMimeType(), event.getContentLength());
      }
    }

    @Override
    public void streamingFailed(StreamVariable.StreamingErrorEvent event) {
      try {
        Exception exception = event.getException();
        if (exception instanceof NoInputStreamException) {
          this.uploadHelper.fireNoInputStream(event.getFileName(), event.getMimeType(), 0);
        } else if (exception instanceof NoOutputStreamException) {
          this.uploadHelper.fireNoOutputStream(event.getFileName(), event.getMimeType(), 0);
        } else {
          this.uploadHelper.fireUploadInterrupted(
              event.getFileName(), event.getMimeType(), event.getBytesReceived(), exception);
        }
      } finally {
        this.uploadHelper.endUpload();
        this.uploadHelper.fireUploadFinish(
            event.getFileName(), event.getMimeType(), event.getContentLength());
      }
    }
  }
}
