/* ************************************************************************
 * org.nightlabs.eclipse.ui.fckeditor - Eclipse RCP FCKeditor Integration *
 * Copyright (C) 2008 NightLabs - http://NightLabs.org                    *
 *                                                                        *
 * This library is free software; you can redistribute it and/or          *
 * modify it under the terms of the GNU Lesser General Public             *
 * License as published by the Free Software Foundation; either           *
 * version 2.1 of the License, or (at your option) any later version.     *
 *                                                                        *
 * This library is distributed in the hope that it will be useful,        *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU      *
 * Lesser General Public License for more details.                        *
 *                                                                        *
 * You should have received a copy of the GNU Lesser General Public       *
 * License along with this library; if not, write to the                  *
 *     Free Software Foundation, Inc.,                                    *
 *     51 Franklin St, Fifth Floor,                                       *
 *     Boston, MA  02110-1301  USA                                        *
 *                                                                        *
 * Or get it online:                                                      *
 *     http://www.gnu.org/copyleft/lesser.html                            *
 **************************************************************************/
package org.nightlabs.eclipse.ui.fckeditor.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.nightlabs.eclipse.ui.fckeditor.IFCKEditor;
import org.nightlabs.eclipse.ui.fckeditor.resource.Messages;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @version $Revision: 118 $ - $Date: 2008-06-26 19:06:11 +0200 (Do, 26 Jun 2008) $
 */
public class FCKEditorSaveDocumentProvider extends FCKEditorEditDocumentProvider {
	/**
	 * Create a new SaveDocumentFileProvider instance.
	 */
	public FCKEditorSaveDocumentProvider(IFCKEditor editor) 
	{
		super(editor);
	}

	@Override
	protected String getBundleFilename(String subUri)
	{
		return super.getBundleFilename("/edit.html"); //$NON-NLS-1$
	}
	
	@Override
	public String getPath() {
		return "/save.html"; //$NON-NLS-1$
	}
	
	@Override
	protected String getLoadingPaneText() {
		return Messages.getString("org.nightlabs.eclipse.ui.fckeditor.server.FCKEditorSaveDocumentProvider.savingText"); //$NON-NLS-1$
	}
	
	@Override
	public InputStream getFileContents(String filename, Properties parms) throws IOException {
		String contents = parms.getProperty(getEditor().getFCKEditorId());
		if(contents == null)
			throw new RuntimeException("Error saving contents. Content parameter not found.");
		contents = LinkRewriter.rewriteToPermaLinks(contents, getEditor().getBaseUrl());
		getEditor().getEditorInput().getEditorContent().setHtml(contents);
		getEditor().doReallySave();
		getEditor().setDirty(false);
		return super.getFileContents(filename, parms);
	}
}
