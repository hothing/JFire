/* ********************************************************************
 * NightLabs PDF Viewer - http://www.nightlabs.org/projects/pdfviewer *
 * Copyright (C) 2004-2008 NightLabs GmbH - http://NightLabs.org      *
 *                                                                    *
 * This library is free software; you can redistribute it and/or      *
 * modify it under the terms of the GNU Lesser General Public         *
 * License as published by the Free Software Foundation; either       *
 * version 2.1 of the License, or (at your option) any later version. *
 *                                                                    *
 * This library is distributed in the hope that it will be useful,    *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of     *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  *
 * Lesser General Public License for more details.                    *
 *                                                                    *
 * You should have received a copy of the GNU Lesser General Public   *
 * License along with this library; if not, write to the              *
 *     Free Software Foundation, Inc.,                                *
 *     51 Franklin St, Fifth Floor,                                   *
 *     Boston, MA  02110-1301  USA                                    *
 *                                                                    *
 * Or get it online:                                                  *
 *     http://www.gnu.org/copyleft/lesser.html                        *
 **********************************************************************/
package org.nightlabs.eclipse.ui.pdfviewer.extension.action;

import org.nightlabs.base.ui.action.XContributionItem;

/**
 * Implementation of IPdfViewerActionOrContributionItem.
 * @version $Revision: 347 $ - $Date: 2008-10-10 20:05:57 +0200 (Fr, 10 Okt 2008) $
 * @author marco schulze - marco at nightlabs dot de
 */
public abstract class PdfViewerContributionItem
extends XContributionItem
implements IPdfViewerActionOrContributionItem
{
	private PdfViewerActionRegistry pdfViewerActionRegistry;

	@Override
	public void init(PdfViewerActionRegistry pdfViewerActionRegistry) {
		this.pdfViewerActionRegistry = pdfViewerActionRegistry;
	}

	@Override
	public PdfViewerActionRegistry getPdfViewerActionRegistry() {
		return pdfViewerActionRegistry;
	}

	@Override
	public void calculateEnabled() {
		setEnabled(pdfViewerActionRegistry.getPdfViewer().getPdfDocument() != null);
	}
}
