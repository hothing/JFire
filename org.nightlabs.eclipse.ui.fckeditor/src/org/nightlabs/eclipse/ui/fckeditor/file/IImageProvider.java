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
package org.nightlabs.eclipse.ui.fckeditor.file;

import org.eclipse.swt.graphics.Image;
import org.nightlabs.htmlcontent.IFCKEditorContentFile;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @version $Revision: 103 $ - $Date: 2008-06-02 15:25:08 +0200 (Mo, 02 Jun 2008) $
 */
public interface IImageProvider
{
	Image getImage(IFCKEditorContentFile file);

	Image getImage(IFCKEditorContentFile file, IImageCallback imageCallback);

	void dispose();

	void stopThumbnailing();

	int getThumbnailSize();

	void setThumbnailSize(int thumbnailSize);
}