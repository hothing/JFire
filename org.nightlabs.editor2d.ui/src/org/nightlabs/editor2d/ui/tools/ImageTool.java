/* *****************************************************************************
 * NightLabs Editor2D - Graphical editor framework                             *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 * Project author: Daniel Mazurek <Daniel.Mazurek [at] nightlabs [dot] org>    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.editor2d.ui.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.editor2d.image.RenderModeMetaData;
import org.nightlabs.editor2d.ui.command.CreateImageCommand;
import org.nightlabs.editor2d.ui.dialog.ConvertImageDialog;
import org.nightlabs.editor2d.ui.model.IModelCreationFactory;
import org.nightlabs.editor2d.ui.request.ImageCreateRequest;
import org.nightlabs.editor2d.ui.resource.Messages;

public class ImageTool
extends CreationTool
{
	private static final Logger logger = Logger.getLogger(ImageTool.class);

	// TODO should come from ImageIO
	private static final String[] fileExtensions =
		new String[] {"jpg", "png", "gif", "bmp", "pcx"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

	private boolean colorConversion = false;

	private List<RenderModeMetaData> renderModeMetaDatas = new LinkedList<RenderModeMetaData>();

	public ImageTool(IModelCreationFactory aFactory) {
		this(aFactory, false);
	}

	public ImageTool(IModelCreationFactory aFactory, boolean colorConversion) {
		super(aFactory);
		this.colorConversion = colorConversion;
	}

	/**
	 * Creates a {@link CreateRequest} and sets this tool's factory on the request.
	 * @see org.eclipse.gef.tools.TargetingTool#createTargetRequest()
	 */
	@Override
	protected Request createTargetRequest()
	{
		ImageCreateRequest request = new ImageCreateRequest();
		request.setFactory(getFactory());
		return request;
	}

	protected ImageCreateRequest getImageCreateRequest() {
		return (ImageCreateRequest) getTargetRequest();
	}

	public String[] getFileExtensions()
	{
		String[] dialogFileExtensions = new String[fileExtensions.length + 1];
		StringBuffer sb = new StringBuffer();
		String wildcard = "*."; //$NON-NLS-1$
		for (int i=0; i<fileExtensions.length; i++) {
			String fileExtension = fileExtensions[i];
			dialogFileExtensions[i+1] = wildcard + fileExtension;
			sb.append(wildcard);
			sb.append(fileExtension);
			if (i != fileExtensions.length - 1)
				sb.append(File.pathSeparatorChar + " "); //$NON-NLS-1$
		}
		dialogFileExtensions[0] = sb.toString();
		return dialogFileExtensions;
	}

	protected Shell getShell() {
		return getCurrentViewer().getControl().getShell();
	}

	protected FileDialog openFileDialog()
	{
		FileDialog dialog = new FileDialog(getShell());
		dialog.setFilterExtensions(getFileExtensions());
		dialog.setText(Messages.getString("org.nightlabs.editor2d.ui.tools.ImageTool.dialog.text.chooseImage")); //$NON-NLS-1$
		dialog.open();
		return dialog;
	}

	@Override
	protected boolean handleButtonDown(int button)
	{
		FileDialog dialog = openFileDialog();
		if (!dialog.getFileName().equals(""))  //$NON-NLS-1$
		{
			String fullPathName = dialog.getFilterPath() + File.separator + dialog.getFileName();
			String fileName = dialog.getFileName();
			getImageCreateRequest().setFileName(fullPathName);
			if (colorConversion) {
				BufferedImage originalImage;
				try {
					long start = System.currentTimeMillis();
					originalImage = ImageIO.read(new File(fullPathName));
//					originalImage = Toolkit.getDefaultToolkit().createImage(fullPathName);
					if (logger.isDebugEnabled()) {
						long duration = System.currentTimeMillis() - start;
						logger.debug("read image file "+fullPathName+" took "+duration+" ms!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
					if (originalImage != null) {
						ConvertImageDialog convertDialog = new ConvertImageDialog(getShell(), originalImage);
						if (convertDialog.open() == Window.OK) {
							renderModeMetaDatas = convertDialog.getConvertImageComposite().getRenderModeMetaDatas();
							doCreation(fullPathName, fileName);
						}
					} else {
						throw new RuntimeException(Messages.getString("org.nightlabs.editor2d.ui.tools.ImageTool.error.text.part1")+fullPathName+Messages.getString("org.nightlabs.editor2d.ui.tools.ImageTool.error.text.part2")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} catch (IOException e) {
					throw new RuntimeException(Messages.getString("org.nightlabs.editor2d.ui.tools.ImageTool.error.text.part1")+fullPathName+Messages.getString("org.nightlabs.editor2d.ui.tools.ImageTool.error.text.part2"), e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			else {
				doCreation(fullPathName, fileName);
			}
			return true;
		}
		return false;
	}

	protected void doCreation(String fullFileName, String fileName)
	{
		((CreateImageCommand)getCurrentCommand()).setFileName(fullFileName);
		((CreateImageCommand)getCurrentCommand()).setSimpleFileName(fileName);
		((CreateImageCommand)getCurrentCommand()).setRenderModeMetaData(renderModeMetaDatas);
		performCreation(1);
	}
}
