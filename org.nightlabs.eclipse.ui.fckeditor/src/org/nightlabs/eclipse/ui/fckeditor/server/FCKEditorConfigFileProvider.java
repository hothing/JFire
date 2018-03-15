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

import java.util.Locale;
import java.util.Map;

import org.nightlabs.eclipse.ui.fckeditor.IFCKEditor;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @version $Revision: 99 $ - $Date: 2008-05-27 16:31:16 +0200 (Di, 27 Mai 2008) $
 */
public class FCKEditorConfigFileProvider extends BundleTemplateFileProvider {

	public FCKEditorConfigFileProvider(IFCKEditor editor) {
		super(editor);
	}

	@Override
	protected Map<String, String> getReplacements()
	{
		Map<String, String> replacements = super.getReplacements();
		replacements.put("language", Locale.getDefault().getLanguage()); //$NON-NLS-1$
		return replacements;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.eclipse.ui.fckeditor.server.FileProvider#getPath()
	 */
	@Override
	public String getPath() {
		return "/editorconfig.js"; //$NON-NLS-1$
	}
}
