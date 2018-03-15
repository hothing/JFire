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
package org.nightlabs.eclipse.ui.fckeditor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.nightlabs.eclipse.ui.fckeditor.file.IImageProvider;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @version $Revision: 116 $ - $Date: 2008-06-26 17:07:22 +0200 (Do, 26 Jun 2008) $
 */
public interface IFCKEditor
{
    /**
     * The property id for <code>isDirty</code>.
     */
    public static final int PROP_DIRTY = IWorkbenchPartConstants.PROP_DIRTY;
    
	void createControl(Composite parent);
	void init(Shell shell, IFCKEditorInput input) throws PartInitException;
	IFCKEditorInput getEditorInput();
	String getBaseUrl();
	String getWidgetBackgroundColor();
	String getWidgetSelectedColor();
	String getWidgetHoverColor();
	boolean isDirty();
	void setDirty(boolean dirty);
	String getFCKEditorId();

	void print();
//	void copy();
//	void paste();

	void setEnabled(boolean enabled);

	IImageProvider getImageProvider();

	/**
	 * Save the contents of the editor. This is an extra method because
	 * org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 * is needed for the two-way browser widget communication.
	 */
	void doReallySave();

	void commit();

	Shell getShell();

    void addPropertyListener(IPropertyListener l);
    void removePropertyListener(IPropertyListener l);
}
