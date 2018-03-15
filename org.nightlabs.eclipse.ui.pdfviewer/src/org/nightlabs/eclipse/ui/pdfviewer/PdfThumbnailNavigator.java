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
package org.nightlabs.eclipse.ui.pdfviewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.eclipse.ui.pdfviewer.internal.PdfThumbnailNavigatorComposite;

/**
 * A navigator showing thumbnails of all pages and allowing to navigate
 * by scrolling and clicking.
 *
 * @version $Revision: 347 $ - $Date: 2008-10-10 20:05:57 +0200 (Fr, 10 Okt 2008) $
 * @author marco schulze - marco at nightlabs dot de
 * @author frederik loeser - frederik at nightlabs dot de
 */
public class PdfThumbnailNavigator implements ContextElement<PdfThumbnailNavigator>
{
	public static final ContextElementType<PdfThumbnailNavigator> CONTEXT_ELEMENT_TYPE = new ContextElementType<PdfThumbnailNavigator>(PdfThumbnailNavigator.class);
	private PdfThumbnailNavigatorComposite pdfThumbnailNavigatorComposite;
	private PdfViewer pdfViewer;
	private PdfDocument pdfDocument;
	private String contextElementId;


	public PdfThumbnailNavigator(PdfViewer pdfViewer) {
		this(pdfViewer, null);
	}

	public PdfThumbnailNavigator(PdfViewer pdfViewer, String contextElementId) {
		assertValidThread();

		if (pdfViewer == null)
			throw new IllegalArgumentException("pdfViewer must not be null!"); //$NON-NLS-1$

		this.pdfViewer = pdfViewer;
		this.contextElementId = contextElementId;
		pdfViewer.registerContextElement(this);

		// PDF thumbnail navigator will be notified in the case PDF simple navigator or PDF viewer itself has changed current page.
		// The event is not fired again (see below).
		pdfViewer.addPropertyChangeListener(PdfViewer.PROPERTY_CURRENT_PAGE, propertyChangeListenerCurrentPage);
		// PDF thumbnail navigator will be notified in the case PDF viewer has loaded a PDF document.
		pdfViewer.addPropertyChangeListener(PdfViewer.PROPERTY_PDF_DOCUMENT, propertyChangeListenerPdfDocument);
//		pdfViewer.addPropertyChangeListener(PdfViewer.PROPERTY_COMPONENT_RESIZED, propertyChangeListenerComponentResized);
		setPdfDocument(pdfViewer.getPdfDocument());
	}

	public Control createControl(Composite parent, int style) {
		assertValidThread();

		if (this.pdfThumbnailNavigatorComposite != null) {
			this.pdfThumbnailNavigatorComposite.dispose();
			this.pdfThumbnailNavigatorComposite = null;
		}

		this.pdfThumbnailNavigatorComposite = new PdfThumbnailNavigatorComposite(parent, style, this);

		this.pdfThumbnailNavigatorComposite.getThumbnailPdfViewer().addPropertyChangeListener(PdfViewer.PROPERTY_CURRENT_PAGE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				PdfThumbnailNavigator.this.pdfViewer.setCurrentPage((Integer) evt.getNewValue());
			}
		});
//		this.pdfThumbnailNavigatorComposite.getThumbnailPdfViewer().addPropertyChangeListener(PdfViewer.PROPERTY_COMPONENT_RESIZED, propertyChangeListenerComponentResized);

		return this.pdfThumbnailNavigatorComposite;
	}

//	private PropertyChangeListener propertyChangeListenerComponentResized = new PropertyChangeListener() {
//		@Override
//		public void propertyChange(PropertyChangeEvent event) {
//			pdfThumbnailNavigatorComposite.zoomToPageWidth();
//		}
//	};

	private PropertyChangeListener propertyChangeListenerCurrentPage = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			int newCurrentPage = (Integer)event.getNewValue();
			if (pdfThumbnailNavigatorComposite != null && !pdfThumbnailNavigatorComposite.isDisposed())
				pdfThumbnailNavigatorComposite.setCurrentPage(newCurrentPage);
		}
	};

	private PropertyChangeListener propertyChangeListenerPdfDocument = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			setPdfDocument((PdfDocument)event.getNewValue());
		}
	};

	/**
	 * Checks if a given method is called on the SWT UI thread.
	 */
	private static void assertValidThread()	{
		if (Display.getCurrent() == null)
			throw new IllegalStateException("Wrong thread! This method must be called on the SWT UI thread!"); //$NON-NLS-1$
	}

//	public PdfViewer getThumbnailPdfViewer() {
//		if (pdfThumbnailNavigatorComposite != null)
//			return pdfThumbnailNavigatorComposite.getThumbnailPdfViewer();
//		else
//			return null;
//	}

	@Override
	public PdfViewer getPdfViewer() {
		return pdfViewer;
	}

	@Override
	public ContextElementType<PdfThumbnailNavigator> getContextElementType() {
		return CONTEXT_ELEMENT_TYPE;
	}
	@Override
	public String getContextElementId() {
		return contextElementId;
	}

	@Override
	public void onUnregisterContextElement() {
		pdfViewer.removePropertyChangeListener(PdfViewer.PROPERTY_PDF_DOCUMENT, propertyChangeListenerPdfDocument);
	    pdfViewer.removePropertyChangeListener(PdfViewer.PROPERTY_CURRENT_PAGE, propertyChangeListenerCurrentPage);
//	    pdfViewer.removePropertyChangeListener(PdfViewer.PROPERTY_COMPONENT_RESIZED, propertyChangeListenerComponentResized);
	    pdfViewer = null; // ensure we can't do anything with it anymore - the pdfViewer forgot this instance already - so we forget it, too.
	}

	protected void setPdfDocument(PdfDocument pdfDocument) {
	    this.pdfDocument = pdfDocument;
	    if (pdfThumbnailNavigatorComposite != null && !pdfThumbnailNavigatorComposite.isDisposed())
	    	pdfThumbnailNavigatorComposite.setPdfDocument(pdfDocument);
    }

	public PdfDocument getPdfDocument() {
	    return pdfDocument;
    }
}
