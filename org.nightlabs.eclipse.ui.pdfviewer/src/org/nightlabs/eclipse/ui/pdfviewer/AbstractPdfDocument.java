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

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Subclass this abstract class instead of directly implementing {@link PdfDocument}.
 * It provides default implementations for some methods
 * (currently solely {@link #getMostVisiblePage(Rectangle2D)}, but more might follow).
 * Additionally, it reduces impact on your code when {@link PdfDocument} is extended in
 * the future (because we can provide default implementations in this class instead of
 * breaking your code).
 * 
 * @version $Revision: 343 $ - $Date: 2008-10-10 00:42:14 +0200 (Fr, 10 Okt 2008) $
 * @author marco schulze - marco at nightlabs dot de
 * @author frederik loeser - frederik at nightlabs dot de
 */
public abstract class AbstractPdfDocument implements PdfDocument {

	private static final Logger logger = Logger.getLogger(AbstractPdfDocument.class);

	@Override
    public int getMostVisiblePage(Rectangle2D viewBounds)
	{
		Collection<Integer> visiblePages = getVisiblePages(viewBounds);

		if (logger.isDebugEnabled()) {
			for (Iterator<Integer> pageIterator = visiblePages.iterator(); pageIterator.hasNext();) {
				int pageNumber = pageIterator.next();
				logger.debug(pageNumber);
			}
		}
		// only return the first number in the collection (not used anymore)
/*		if (visiblePages.isEmpty())
			return -1;
		else
			return visiblePages.iterator().next();*/


		// Get the page number (1-based) of that page that fills out the largest part of the given bounds (in percentage)
		// in comparison to all other pages that are also partly or wholly visible within the given bounds.

		if (visiblePages.isEmpty())
			return -1;

		int mostVisiblePage = -1;
		int visiblePageAreaMax = 0;
		for (Iterator<Integer> pageIterator = visiblePages.iterator(); pageIterator.hasNext();) {
			int pageNumber = pageIterator.next();

			if (visiblePages.size() == 1)
				return pageNumber;

			Rectangle2D pageBounds = getPageBounds(pageNumber);
			int visiblePageArea;

        	if (viewBounds.contains(pageBounds)) {
        		visiblePageArea = (int) (pageBounds.getWidth() * pageBounds.getHeight());
        		if (logger.isDebugEnabled())
        			logger.debug("Page number: " + pageNumber + "; visible page area: " + visiblePageArea); //$NON-NLS-1$ //$NON-NLS-2$
        	}
        	else if (viewBounds.intersects(pageBounds)) {

        		double x = Math.max(pageBounds.getX(), viewBounds.getX());
        		double y = Math.max(pageBounds.getY(), viewBounds.getY());
        		double width = Math.min(pageBounds.getMaxX() - x, viewBounds.getMaxX() - x);
        		double height = Math.min(pageBounds.getMaxY() - y, viewBounds.getMaxY() - y);

        		visiblePageArea = (int) (width * height);

        		if (logger.isDebugEnabled()) {
        			logger.debug("Page number: " + pageNumber + "; visible page area: " + visiblePageArea); //$NON-NLS-1$ //$NON-NLS-2$
        		}
        	}
        	else
        		throw new IllegalStateException("Inconsistent implementation of PdfDocument! getVisiblePages(...) returned page " + pageNumber + " but this page is neither contained in the bounds, nor does it intersect them!"); //$NON-NLS-1$ //$NON-NLS-2$


			if (visiblePageArea >= visiblePageAreaMax) {

				if ((visiblePageArea == visiblePageAreaMax && pageNumber < mostVisiblePage) || (visiblePageArea > visiblePageAreaMax)) {
					mostVisiblePage = pageNumber;
					visiblePageAreaMax = visiblePageArea;
				}
			}
		}

		return mostVisiblePage;

    }

}
