package org.vaadin.addons.f0rce.uploadhelper;

import java.io.InputStream;

import org.vaadin.addons.f0rce.uploadhelper.receiver.UHMultiFileMemoryBuffer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.Route;

@Route("")
public class View extends Div {

  public View() {
    UHMultiFileMemoryBuffer memoryBuffer = new UHMultiFileMemoryBuffer();

    Upload u = new Upload();

    TextField tf = new TextField("TestValue");
    tf.setWidth("300px");

    TextField tf2 = new TextField("TestValue2");
    tf2.setWidth("300px");

    Dialog dialog = new Dialog();
    dialog.setSizeFull();

    UploadHelper uploadHelper = new UploadHelper(memoryBuffer);
    uploadHelper.setMaxFiles(5);
    uploadHelper.setVisualFeedback(true);

    Button dztf = new Button("dropZone tf");
    dztf.addClickListener(
        evt -> {
          uploadHelper.setDropZone(tf);
        });

    Button dztf2 = new Button("dropZone tf2");
    dztf2.addClickListener(
        evt -> {
          uploadHelper.setDropZone(tf2);
        });

    dialog.add(tf, dztf, tf2, dztf2);
    dialog.add(uploadHelper);

    Button open = new Button("Open");
    open.addClickListener(
        evt -> {
          dialog.open();
        });

    uploadHelper.addProgressListener(
        evt -> {
          long total = evt.getContentLength();
          long progress = evt.getReadBytes();

          long percent = progress * 100 / total;
          System.out.println(percent);
        });

    uploadHelper.addSucceededListener(
        inEvent -> {
          String fileName = inEvent.getFileName();

          InputStream fileData = memoryBuffer.getInputStream(fileName);
          long contentLength = inEvent.getContentLength();
          String mimeTyle = inEvent.getMIMEType();

          System.out.println(fileName + " - " + mimeTyle);
        });

    this.add(open, dialog);
  }
}
