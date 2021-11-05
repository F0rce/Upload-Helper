package org.vaadin.addons.f0rce.uploadhelper;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import de.f0rce.ace.enums.AceTheme;

@Route("")
public class View extends Div {

	public View() {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(65536);

		class FileReceiver implements UHReceiver {

			@Override
			public OutputStream receiveUpload(String fileName, String mimeType) {
				buffer.reset();
				return buffer;
			}

		}

		AceEditor ace = new AceEditor();
		ace.setHeight("500px");
		ace.setWidth("1200px");
		ace.setTheme(AceTheme.gruvbox);
		ace.setMode(AceMode.java);

		TextField tf = new TextField("TestValue");
		tf.setWidth("300px");

		UploadHelper uploadHelper = new UploadHelper(tf, new FileReceiver());

		uploadHelper.addSucceededListener(inEvent -> {
			System.out.println(inEvent.getFileName() + " " + inEvent.getMIMEType() + " " + inEvent.getContentLength());
			tf.setValue(buffer.toString());
		});

		add(uploadHelper, tf);
	}
}
