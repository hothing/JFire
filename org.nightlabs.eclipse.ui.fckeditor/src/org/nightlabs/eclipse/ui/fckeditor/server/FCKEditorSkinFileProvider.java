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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.nightlabs.eclipse.ui.fckeditor.IFCKEditor;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @version $Revision: 99 $ - $Date: 2008-05-27 16:31:16 +0200 (Di, 27 Mai 2008) $
 */
public class FCKEditorSkinFileProvider extends BundleFileProvider
{
	public FCKEditorSkinFileProvider(IFCKEditor editor) {
		super(editor);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.server.FileProvider#getPath()
	 */
	@Override
	public String getPath() {
		return "/fckeditor-skin/"; //$NON-NLS-1$
	}
	
	@Override
	public InputStream getFileContents(String subUri, Properties parms)
			throws IOException
	{
		InputStream in = super.getFileContents(subUri, parms);
		if(subUri.equals("/fckeditor-skin/fck_editor.css")) { //$NON-NLS-1$
			// adapt colors
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder contents = new StringBuilder();
			while(true) {
				String line = reader.readLine();
				if(line == null)
					break;
				contents.append(line
						.replace("#abcde0", getEditor().getWidgetBackgroundColor()) //$NON-NLS-1$
						.replace("#abcde1", getEditor().getWidgetSelectedColor()) //$NON-NLS-1$
						.replace("#abcde2", getEditor().getWidgetHoverColor()) //$NON-NLS-1$
						);
			}
			in = new ByteArrayInputStream(contents.toString().getBytes("UTF-8")); //$NON-NLS-1$
		}
		return in;
	}
}
