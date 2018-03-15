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
package org.nightlabs.eclipse.ui.pdfviewer.extension.action.zoom;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.ArrayList;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;
import org.nightlabs.base.ui.action.CComboContributionItem;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.eclipse.ui.pdfviewer.AutoZoom;
import org.nightlabs.eclipse.ui.pdfviewer.PdfViewer;
import org.nightlabs.eclipse.ui.pdfviewer.extension.action.IPdfViewerActionOrContributionItem;
import org.nightlabs.eclipse.ui.pdfviewer.extension.action.PdfViewerActionRegistry;
import org.nightlabs.l10n.NumberFormatter;

/**
 * Creates an XComboComposite for the maintenance of zoom levels. A set of custom zoom levels between a certain range is created
 * for displaying a given PDF document on one of these zoom levels. There is also the possibility to create and use a new zoom
 * level not available yet.
 * @version $Revision: 349 $ - $Date: 2008-10-13 15:32:46 +0200 (Mo, 13 Okt 2008) $
 * @author marco schulze - marco at nightlabs dot de
 */
public class ZoomContributionItem
extends CComboContributionItem<ZoomLevel>
implements IPdfViewerActionOrContributionItem
{
	private class ZoomLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			ZoomLevel zoomLevel = (ZoomLevel) element;
			return zoomLevel.getLabel(true);
		}
	}

	/**
	 * The constructor of {@link ZoomContributionItem}.
	 * An array list of zoom levels is created for displaying a given PDF document
	 * on different zoom levels.
	 */
	public ZoomContributionItem() {
		super(ZoomContributionItem.class.getName(), null, null);
		setLabelProvider(new ZoomLabelProvider());
		zoomLevels = new ArrayList<ZoomLevel>();
		zoomLevels.add(ZoomLevel.ZOOM_TO_PAGE_WIDTH);
		zoomLevels.add(ZoomLevel.ZOOM_TO_PAGE_HEIGHT);
		zoomLevels.add(ZoomLevel.ZOOM_TO_PAGE);
		zoomLevels.add(new ZoomLevel(125));
		zoomLevels.add(new ZoomLevel(250));
		zoomLevels.add(new ZoomLevel(333));
		zoomLevels.add(new ZoomLevel(500));
		zoomLevels.add(new ZoomLevel(667));
		zoomLevels.add(new ZoomLevel(750));
		zoomLevels.add(new ZoomLevel(1000));
		zoomLevels.add(new ZoomLevel(1250));
		zoomLevels.add(new ZoomLevel(1500));
		zoomLevels.add(new ZoomLevel(2000));
		setElements(new ArrayList<ZoomLevel>(zoomLevels));
	}

	private ArrayList<ZoomLevel> zoomLevels;

	@Override
	protected XComboComposite<ZoomLevel> createControl(Composite parent) {
		if (getControl() != null)
			getControl().removeKeyListener(keyListener);

		XComboComposite<ZoomLevel> combo = super.createControl(parent);
		combo.addKeyListener(keyListener);
		combo.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				PdfViewer pdfViewer = getPdfViewer();
				if (pdfViewer == null)
					return;

				ZoomLevel selectedZoomLevel = getControl().getSelectedElement();
				if (selectedZoomLevel == null)
					return;

				if (selectedZoomLevel == ZoomLevel.ZOOM_TO_PAGE_WIDTH)
					pdfViewer.setAutoZoom(AutoZoom.pageWidth);
				else if (selectedZoomLevel == ZoomLevel.ZOOM_TO_PAGE_HEIGHT)
					pdfViewer.setAutoZoom(AutoZoom.pageHeight);
				else if (selectedZoomLevel == ZoomLevel.ZOOM_TO_PAGE)
					pdfViewer.setAutoZoom(AutoZoom.page);
				else {
					pdfViewer.setAutoZoom(AutoZoom.none);
					pdfViewer.setZoomFactorPerMill(selectedZoomLevel.getZoomFactorPerMill());
				}
			}
		});
		setComboZoomFactorPerMill();
		return combo;
	}

	private KeyListener keyListener = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e)
		{
			if (e.character == '\n' || e.character == '\r') {
				PdfViewer pdfViewer = getPdfViewer();
				if (pdfViewer == null)
					return;

				String newZoomStr = getControl().getText();
				newZoomStr = newZoomStr.replaceAll("\\s", "").replaceAll("%", "");
				double zoom = -1;
				try {
					zoom = NumberFormatter.parseFloat(newZoomStr);
					newZoomStr = null;
				} catch (ParseException x) {
					// ignore
				}

				if (newZoomStr != null) {
					// if it's not null, the value couldn't be parsed as valid zoom and thus we fall back to the current zoom instead.
					setComboZoomFactorPerMill();
				}
				else {
					pdfViewer.setAutoZoom(AutoZoom.none);
					pdfViewer.setZoomFactorPerMill((int) (zoom * 10)); // will cause a PropertyChangeEvent and thus trigger setComboZoomFactorPerMill()
				}
			}
		}
	};

	/**
	 * Searches the appropriate {@link ZoomLevel} value in the currently used list of {@link ZoomLevel}s.
	 * If the value is not available yet, a new entry for this {@link ZoomLevel} value will be created and
	 * added to the list of {@link ZoomLevel}s.
	 *
	 * @param zoomFactorPerMill the value to search for in the list of {@link ZoomLevel}s.
	 * @return the {@link ZoomLevel} that has been searched for.
	 */
	private ZoomLevel findOrCreateZoomLevel(int zoomFactorPerMill)
	{
		int nextSmallerZoomLevelIndex = -1;
		for (ZoomLevel zoomLevel : zoomLevels) {
			if (zoomLevel.getZoomFactorPerMill() < zoomFactorPerMill)
				++nextSmallerZoomLevelIndex;

			if (zoomLevel.getZoomFactorPerMill() == zoomFactorPerMill)
				return zoomLevel;
		}

		ArrayList<ZoomLevel> newZoomLevels = new ArrayList<ZoomLevel>(zoomLevels.size() + 1);
		newZoomLevels.addAll(zoomLevels);
		int index = nextSmallerZoomLevelIndex + 1;
		if (index > newZoomLevels.size())
			index = newZoomLevels.size();

		ZoomLevel newZoomLevel = new ZoomLevel(zoomFactorPerMill);
		newZoomLevels.add(index, newZoomLevel);
		setElements(newZoomLevels);
		return newZoomLevel;
	}

	/**
	 * Sets the selected element of the XComboComposite dependent on which value is set in the PDF viewer.
	 * The zoom level has to be searched for in the list of zoom levels in the case the zoom level
	 * chosen by the PDF viewer does not correspond with the zoom level constants ZOOM_TO_PAGE_WIDTH,
	 * ZOOM_TO_PAGE_HEIGHT or ZOOM_TO_PAGE.
	 */
	private void setComboZoomFactorPerMill()
	{
		PdfViewer pdfViewer = getPdfViewer();
		if (pdfViewer == null)
			return;

		ZoomLevel zoomLevel = null;
		switch (pdfViewer.getAutoZoom()) {
			case pageWidth:
				zoomLevel = ZoomLevel.ZOOM_TO_PAGE_WIDTH;
			break;
			case pageHeight:
				zoomLevel = ZoomLevel.ZOOM_TO_PAGE_HEIGHT;
			break;
			case page:
				zoomLevel = ZoomLevel.ZOOM_TO_PAGE;
			break;
			case none:
				zoomLevel = findOrCreateZoomLevel(pdfViewer.getZoomFactorPerMill());
			break;
			default:
				throw new IllegalStateException("Unknown AutoZoom: " + pdfViewer.getAutoZoom());
		}

		if (getControl() != null)
			getControl().selectElement(zoomLevel);
	}

	/**
	 * Returns the {@link PdfViewer} instance by incorporating the PDF viewer action registry.
	 * @return PdfViewer the {@link PdfViewer} instance.
	 */
	protected PdfViewer getPdfViewer()
	{
		return pdfViewerActionRegistry == null ? null : pdfViewerActionRegistry.getPdfViewer();
	}

	@Override
	protected int getComboStyle(Composite parent) {
		return XComposite.getBorderStyle(parent);
	}

	@Override
	public void fill(CoolBar parent, int index) {
		super.fill(parent, index);
//		setSize();
	}

	@Override
	public void fill(ToolBar parent, int index) {
		super.fill(parent, index);
	}

	@Override
	public void calculateEnabled() {
		setEnabled(pdfViewerActionRegistry.getPdfViewer().getPdfDocument() != null);
	}

	private PdfViewerActionRegistry pdfViewerActionRegistry;

	private PropertyChangeListener propertyChangeListenerZoom = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			setComboZoomFactorPerMill();
		}
	};

	@Override
	public PdfViewerActionRegistry getPdfViewerActionRegistry() {
		return pdfViewerActionRegistry;
	}

	@Override
	public void init(PdfViewerActionRegistry pdfViewerActionRegistry) {
		this.pdfViewerActionRegistry = pdfViewerActionRegistry;
		PdfViewer pdfViewer = getPdfViewer();
		if (pdfViewer == null)
			return;

		pdfViewer.addPropertyChangeListener(PdfViewer.PROPERTY_ZOOM_FACTOR, propertyChangeListenerZoom );
	}


//	/**
//   * Computes the width required by control
//   * @param control The control to compute width
//   * @return int The width required
//   */
//  @Override
//	protected int computeWidth(Control control)
//  {
//  	return 240;
////  	int width = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
////  	return width;
//  }

}
