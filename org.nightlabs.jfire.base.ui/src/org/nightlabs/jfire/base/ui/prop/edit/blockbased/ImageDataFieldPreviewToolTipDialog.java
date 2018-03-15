package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.eclipse.ui.dialog.ToolTipDialogSupport;
import org.nightlabs.jfire.prop.datafield.IContentDataField;
import org.nightlabs.jfire.prop.datafield.ImageDataField;

public class ImageDataFieldPreviewToolTipDialog extends Dialog
{
	private static final Logger logger = Logger.getLogger(ImageDataFieldPreviewToolTipDialog.class);

	private ToolTipDialogSupport toolTipDialogSupport = new ToolTipDialogSupport(this);
	private ImageDataFieldEditor imageDataFieldEditor;

	private Label imageLabel;

	public ImageDataFieldPreviewToolTipDialog(Shell parentShell, ImageDataFieldEditor imageDataFieldEditor) {
		super(parentShell);

		this.imageDataFieldEditor = imageDataFieldEditor;
		toolTipDialogSupport.setEnabled(false);
	}

	public ToolTipDialogSupport getToolTipDialogSupport() {
		return toolTipDialogSupport;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		Point cursorLocation = newShell.getDisplay().getCursorLocation();
		Rectangle bounds = newShell.getBounds();
		bounds.x = cursorLocation.x;
		bounds.y = cursorLocation.y;
		bounds.width = newShell.getDisplay().getBounds().width / 2;
		bounds.height = newShell.getDisplay().getBounds().height / 2;
		newShell.setBounds(bounds);
		newShell.setText(String.format("%s: %s", imageDataFieldEditor.getStructField().getName().getText(), imageDataFieldEditor.getDataField().getFileName()));
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return null;
	}

	private Composite page;

	@Override
	protected Control createDialogArea(Composite parent) {
		page = (Composite) super.createDialogArea(parent);

		imageLabel = new Label(page, SWT.BORDER);
		GridData imageGD = new GridData(GridData.FILL_BOTH);
//		imageGD.horizontalAlignment = SWT.CENTER;
//		imageGD.verticalAlignment = SWT.CENTER;
		imageLabel.setLayoutData(imageGD);
		imageLabel.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imageLabel.getImage() != null)
					imageLabel.getImage().dispose();
			}
		});

		parent.getShell().layout(true, true);

		displayImage();
//		parent.getDisplay().asyncExec(new Runnable() {
//			@Override
//			public void run() {
//				displayImage();
//			}
//		});

		return page;
	}

	private void displayImage()
	{
		ImageDataField dataField = imageDataFieldEditor.getDataField();
		if (!dataField.isEmpty()) {
			ImageData id = null;
			ByteArrayInputStream inPlain = new ByteArrayInputStream(dataField.getContent());
			InputStream in;
			if(dataField.getContentEncoding().equals(IContentDataField.CONTENT_ENCODING_PLAIN))
				in = inPlain;
			else if(dataField.getContentEncoding().equals(IContentDataField.CONTENT_ENCODING_DEFLATE))
				in = new InflaterInputStream(inPlain);
			else
				throw new RuntimeException("Unsupported content encoding: "+dataField.getContentEncoding()); //$NON-NLS-1$
			try {
				// TODO: try loading image with Java Image API if loading with SWT fails as in org.nightlabs.eclipse.ui.fckeditor.file.image.ImageUtil - marc
				id = new ImageData(in);
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
						logger.error(e);
					}
			}
			displayImage(id);
		}
		else
			displayImage(null);
	}

	private void displayImage(ImageData id) {
		if (imageLabel.getImage() != null)
			imageLabel.getImage().dispose();

//		GridData imageLabelGD = ((GridData)imageLabel.getLayoutData());
		if(id != null) {
			int width = id.width;
			int height = id.height;
			double factor = 1.0;
			int maxWidth = page.getClientArea().width;
			int maxHeight = page.getClientArea().height;
			if (width > maxWidth || height > maxHeight) {
//				factor *= height > width ? 1.0*imageLabel.getBounds().height/height : 1.0*imageLabel.getBounds().width/width;
				double factorX = (double) maxWidth / width;
				double factorY = (double) maxHeight / height;
				factor = Math.min(factorX, factorY);

				if (logger.isDebugEnabled())
					logger.debug("displayImage: width=" + width + " height=" + height + " maxWidth=" + maxWidth + " maxHeight=" + maxHeight + " factorX=" + factorX + " factorY=" + factorY + " factor=" + factor);
			}

			id = id.scaledTo((int) (factor*width), (int) (factor*height));
			Image image = new Image(imageLabel.getDisplay(), id);
			imageLabel.setImage(image);
			getShell().setSize(
					id.width + (getShell().getSize().x - maxWidth),
					id.height + (getShell().getSize().y - maxHeight)
			);
//			imageLabelGD.heightHint = SWT.DEFAULT;
//			imageLabelGD.widthHint = SWT.DEFAULT;
		} else {
			imageLabel.setImage(null);
//			imageLabelGD.heightHint = 0;
//			imageLabelGD.widthHint = 0;
		}

		// re-layout the top level container
		Composite top = imageLabel.getParent();
		while (top.getParent() != null)
			top = top.getParent();
		top.layout(true, true);
	}
}
