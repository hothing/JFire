package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.InflaterInputStream;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.jfire.base.ui.JFireBasePlugin;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.fieldbased.FieldBasedEditor;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.datafield.IContentDataField;
import org.nightlabs.jfire.prop.datafield.ImageDataField;
import org.nightlabs.jfire.prop.structfield.ImageStructField;
import org.nightlabs.language.LanguageCf;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.NLLocale;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ImageDataFieldEditor
extends AbstractDataFieldEditor<ImageDataField>
{
	/**
	 * Use this before extension.
	 */
	private static final String EXTENSION_PREFIX = "*."; //$NON-NLS-1$

	/**
	 * Separate extension in the file dialog using this string.
	 */
	private static final String EXTENSION_SEPARATOR = ";"; //$NON-NLS-1$

	public static class Factory extends AbstractDataFieldEditorFactory<ImageDataField> {

		@Override
		public String[] getEditorTypes() {
			return new String[] {ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, FieldBasedEditor.EDITORTYPE_FIELD_BASED};
		}

//		@Override
//		public Class<? extends DataFieldEditor<ImageDataField>> getDataFieldEditorClass() {
//			return ImageDataFieldEditor.class;
//		}

		@Override
		public Class<ImageDataField> getPropDataFieldType() {
			return ImageDataField.class;
		}

		@Override
		public DataFieldEditor<ImageDataField> createPropDataFieldEditor(IStruct struct, ImageDataField data) {
			return new ImageDataFieldEditor(struct, data);
		}
	}

	private static Logger LOGGER = Logger.getLogger(ImageDataFieldEditor.class);

	private Shell shell;
	private LanguageCf language;

	private Button saveToDiskButton;
	private Text filenameTextbox;
	private Button openFileChooserButton;
	private Button clearButton;
	private Group group;
	private Label imageLabel;
	private Label sizeLabel;
	private String fileDialogFilterPath;

	private static final int maxThumbnailWidth = 200;
	private static final int maxThumbnailHeight = 200;

	private ImageDataFieldPreviewToolTipDialog imageDataFieldPreviewToolTipDialog;

	public ImageDataFieldEditor(IStruct struct, ImageDataField data) {
		super(struct, data);
		language = new LanguageCf(NLLocale.getDefault().getLanguage());
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor#setDataField(org.nightlabs.jfire.prop.DataField)
	 */
	@Override
	protected void setDataField(ImageDataField dataField) {
		super.setDataField(dataField);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(final Composite parent) {
		shell = parent.getShell();
		group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout(5, false));

		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA, group);

		saveToDiskButton = new Button(group, SWT.PUSH);
		saveToDiskButton.setEnabled(false);
		saveToDiskButton.setImage(SharedImages.getSharedImage(JFireBasePlugin.getDefault(), ImageDataFieldEditor.class, "saveToDiskButton")); //$NON-NLS-1$
		saveToDiskButton.setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.saveToDiskButton.toolTipText")); //$NON-NLS-1$
		saveToDiskButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveToDiskButtonPressed();
			}
		});

		filenameTextbox = new Text(group, XComposite.getBorderStyle(parent));
		filenameTextbox.setEditable(false);
		filenameTextbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		openFileChooserButton = new Button(group, SWT.PUSH);
//		openFileChooserButton.setText("...");
		openFileChooserButton.setImage(SharedImages.getSharedImage(JFireBasePlugin.getDefault(), ImageDataFieldEditor.class, "openFileChooserButton")); //$NON-NLS-1$
		openFileChooserButton.setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.button.openFile.tooltip")); //$NON-NLS-1$
		openFileChooserButton.setLayoutData(new GridData());
		openFileChooserButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				fileChooserButtonPressed();
			}
		});

		clearButton = new Button(group, SWT.PUSH);
//		clearButton.setText("&Clear");
		clearButton.setImage(SharedImages.getSharedImage(JFireBasePlugin.getDefault(), ImageDataFieldEditor.class, "clearButton")); //$NON-NLS-1$
		clearButton.setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.button.clear.tooltip")); //$NON-NLS-1$
		clearButton.setLayoutData(new GridData());
		clearButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				clearButtonPressed();
			}
		});
		clearButton.setEnabled(false);

		sizeLabel = new Label(group, SWT.NONE);

		imageLabel = new Label(group, SWT.NONE);
		imageLabel.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imageLabel.getImage() != null)
					imageLabel.getImage().dispose();
			}
		});
		GridData gd = new GridData();
		gd.horizontalSpan = 4;
		gd.horizontalAlignment = SWT.CENTER;
		gd.verticalAlignment = SWT.CENTER;
//		gd.verticalIndent = 10;
		gd.heightHint = 0;
		imageLabel.setLayoutData(gd);

		imageDataFieldPreviewToolTipDialog = new ImageDataFieldPreviewToolTipDialog(shell, this);
		imageDataFieldPreviewToolTipDialog.getToolTipDialogSupport().register(filenameTextbox);
		if (imageLabel != null)
			imageDataFieldPreviewToolTipDialog.getToolTipDialogSupport().register(imageLabel);

		return group;
	}

	private void displayImage(ImageData id) {
		if (imageLabel.getImage() != null)
			imageLabel.getImage().dispose();

		GridData imageLabelGD = ((GridData)imageLabel.getLayoutData());
		if(id != null) {
			int width = id.width;
			int height = id.height;
			double factor = 1.0;
			if (width > maxThumbnailWidth || height > maxThumbnailHeight) {
//				factor *= height > width ? 1.0*maxThumbnailHeight/height : 1.0*maxThumbnailWidth/width;
				double factorX = (double) maxThumbnailWidth / width;
				double factorY = (double) maxThumbnailHeight / height;
				factor = Math.min(factorX, factorY);
			}

			id = id.scaledTo((int) (factor*width), (int) (factor*height));
			Image image = new Image(imageLabel.getDisplay(), id);
			imageLabel.setImage(image);
			imageLabelGD.heightHint = SWT.DEFAULT;
			clearButton.setEnabled(true);
		} else {
			imageLabel.setImage(null);
			imageLabelGD.heightHint = 0;
			clearButton.setEnabled(false);
		}

		// re-layout the top level container
		Composite top = imageLabel.getParent();
		while (top.getParent() != null)
			top = top.getParent();
		top.layout(true, true);

		imageDataFieldPreviewToolTipDialog.getToolTipDialogSupport().setEnabled(imageLabel.getImage() != null);
	}

	private void showFileDoesNotExistDialog(File file)
	{
		MessageDialog.openError(
				shell,
				Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.fileDoesNotExistDialog.title"), //$NON-NLS-1$
				String.format(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.fileDoesNotExistDialog.text"), file.getAbsolutePath()) //$NON-NLS-1$
		);
	}

	private boolean determineSaveToDiskButtonEnabled()
	{
		boolean result = true;
		if (!isChanged() && getDataField().isEmpty())
			result = false;

		if (isChanged() && filenameTextbox.getText().isEmpty())
			result = false;

		if (saveToDiskButton.isEnabled() != result)
			saveToDiskButton.setEnabled(result);

		return result;
	}

	private void saveToDiskButtonPressed()
	{
		if (!determineSaveToDiskButtonEnabled())
			return;

		File localFile = null;
		String fileName;
		if (isChanged()) {
			localFile = new File(filenameTextbox.getText());
			if (!localFile.exists()) {
				showFileDoesNotExistDialog(localFile);
				return;
			}

			fileName = localFile.getName();
		}
		else
			fileName = getDataField().getFileName();

		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		fileDialog.setFileName(fileName);
		String saveFilePath = fileDialog.open();
		if (saveFilePath == null)
			return; // User cancelled.

		File saveFile = new File(saveFilePath);

		try {
			if (localFile != null)
				IOUtil.copyFile(localFile, saveFile);
			else
				getDataField().saveToFile(saveFile);
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.AbstractDataFieldEditor#doRefresh()
	 */
	@Override
	public void doRefresh() {
		ImageStructField imageStructField = (ImageStructField) getStructField();

		group.setText(imageStructField.getName().getText(language.getLanguageID()));

		ImageDataField dataField = getDataField();
		if (!dataField.isEmpty()) {
			filenameTextbox.setText(dataField.getFileName());
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
						LOGGER.error(e);
					}
			}
			displayImage(id);
		}
		else { //@Marco, why did you remove these 2 lines? Probably a merge-mistake during back-/forward-porting. Marco.
			filenameTextbox.setText("");
			displayImage(null);
		}

		sizeLabel.setText(
				String.format(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.sizeMaxKBLabel"), //$NON-NLS-1$
						new Object[] { new Long(imageStructField.getMaxSizeKB()) }));
		sizeLabel.pack();
		sizeLabel.getParent().layout(true, true);

		handleManagedBy(dataField.getManagedBy());
		determineSaveToDiskButtonEnabled();
	}

	protected void handleManagedBy(String managedBy)
	{
		for (Control child : group.getChildren()) {
			if (child != imageLabel)
				child.setEnabled(managedBy == null);
		}
		if (managedBy != null)
			group.setToolTipText(String.format(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.group.managedBy.tooltip"), managedBy)); //$NON-NLS-1$
		else
			group.setToolTipText(null);
	}

	/**
	 * Open the image file browse dialog.
	 * @param parent The parent shell
	 * @return the selected image file name or <code>null</code> if
	 * 		no image file was selected
	 */
	private String openImageFileDialog(Shell parent)
	{
		ImageStructField imageStructField = (ImageStructField) getStructField();
		List<String> extList = imageStructField.getImageFormats();

		String[] extensions = new String[extList.size()+1];
		String[] names = new String[extList.size()+1];
		names[0] = Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.label.imageFiles"); //$NON-NLS-1$
		int i = 1;
		for (String ext : extList) {
			String extension = EXTENSION_PREFIX + ext.toLowerCase() + EXTENSION_SEPARATOR + EXTENSION_PREFIX + ext.toUpperCase();
			if(extensions[0] == null)
				extensions[0] = extension;
			else
				extensions[0] += EXTENSION_SEPARATOR + extension;
			extensions[i] = extension;
			names[i] = String.format(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.images"), ext.toUpperCase()); //$NON-NLS-1$
			i++;
		}

		FileDialog fileDialog = new FileDialog(parent);
		fileDialog.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.fileDialog.text")); //$NON-NLS-1$
		fileDialog.setFilterNames(names);
		fileDialog.setFilterExtensions(extensions);
		fileDialog.setFilterPath(fileDialogFilterPath);
		String filename = fileDialog.open();
		if(filename != null)
			fileDialogFilterPath = filename;
		return filename;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#getControl()
	 */
	@Override
	public Control getControl() {
		return group;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor#updatePropertySet()
	 */
	@Override
	public void updatePropertySet() {
		if (!isChanged())
			return;

		ImageDataField dataField = getDataField();
		String path = filenameTextbox.getText();
		if (path == null || path.isEmpty()) {
			dataField.clear();
		} else {

			//FIXME: get content type somehow!
			final String contentType;
			final String lowerPath = path.toLowerCase();
			if(lowerPath.endsWith(".png")) //$NON-NLS-1$
				contentType = "image/png"; //$NON-NLS-1$
			else if(lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) //$NON-NLS-1$ //$NON-NLS-2$
				contentType = "image/jpeg"; //$NON-NLS-1$
			else if(lowerPath.endsWith(".gif")) //$NON-NLS-1$
				contentType = "image/gif"; //$NON-NLS-1$
			else
				contentType = "application/unknown"; //$NON-NLS-1$

			// store the image as in the data field.
			final File imageFile = new File(path);

			try {
				dataField.loadFile(imageFile, contentType);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	public LanguageCf getLanguage() {
		return language;
	}

	/**
	 * Called when the file chooser button was pressed.
	 */
	private void fileChooserButtonPressed() {
		String filename = openImageFileDialog(openFileChooserButton.getShell());
		if (filename != null) {
			File file = new File(filename);
			// check if the image fulfills the size requirements
			ImageStructField imageStructField = (ImageStructField) getStructField();
			if (!imageStructField.validateSize(file.length()/1024)) {
				MessageDialog.openError(
						openFileChooserButton.getShell(),
						Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.messageBoxImageExceedsMaxSizeKB.text"), //$NON-NLS-1$
						String.format(
								Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.messageBoxImageExceedsMaxSizeKB.message"), //$NON-NLS-1$
								new Object[] { new Long(imageStructField.getMaxSizeKB()), new Long((file.length() / 1024))})
				);
				return;
			}

			try {
				ImageData data = new ImageData(filename);
				filenameTextbox.setText(filename);
				setChanged(true);
				displayImage(data);
				// there is already layout code in displayImage()... I moved this top-level layout stuff to this method, too. Marc
//						Composite top = parent;
//						while (top.getParent() != null)
//							top = top.getParent();
//						top.layout(true, true); // this is necessary, because otherwise a bigger image doesn't cause the widgets to grow and is therefore cut
			} catch(SWTException swtex) {
				MessageDialog.openError(
						openFileChooserButton.getShell(),
						Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.messageBoxInvalidImageFile.text"), //$NON-NLS-1$
						Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.ImageDataFieldEditor.messageBoxInvalidImageFile.message") //$NON-NLS-1$
				);
			}
			determineSaveToDiskButtonEnabled();
		}
	}

	/**
	 * Called when the clear button was pressed.
	 */
	private void clearButtonPressed()
	{
		filenameTextbox.setText(""); //$NON-NLS-1$
		setChanged(true);
		displayImage(null);
		determineSaveToDiskButtonEnabled();
	}
}


