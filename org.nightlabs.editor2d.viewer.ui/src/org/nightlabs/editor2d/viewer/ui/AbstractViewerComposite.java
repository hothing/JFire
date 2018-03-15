/* *****************************************************************************
 * NightLabs Editor2D - Graphical editor framework                             *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
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

package org.nightlabs.editor2d.viewer.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.editor2d.DrawComponent;
import org.nightlabs.editor2d.render.RenderModeListener;
import org.nightlabs.editor2d.render.RenderModeManager;
import org.nightlabs.editor2d.viewer.ui.action.RenderModeContributionItem;
import org.nightlabs.editor2d.viewer.ui.action.ZoomAllAction;
import org.nightlabs.editor2d.viewer.ui.action.ZoomContributionItem;
import org.nightlabs.editor2d.viewer.ui.action.ZoomInAction;
import org.nightlabs.editor2d.viewer.ui.action.ZoomOutAction;
import org.nightlabs.editor2d.viewer.ui.event.MouseEvent;
import org.nightlabs.editor2d.viewer.ui.event.MouseMoveListener;
import org.nightlabs.editor2d.viewer.ui.preview.PreviewComposite;
import org.nightlabs.editor2d.viewer.ui.render.RendererRegistry;
import org.nightlabs.editor2d.viewer.ui.tool.IToolEntry;
import org.nightlabs.editor2d.viewer.ui.tool.MarqueeToolEntry;
import org.nightlabs.editor2d.viewer.ui.tool.SelectToolEntry;
import org.nightlabs.editor2d.viewer.ui.tool.ToolEntryManager;
import org.nightlabs.editor2d.viewer.ui.tool.ZoomToolEntry;

public abstract class AbstractViewerComposite
extends XComposite
{
//	private static final Logger logger = Logger.getLogger(AbstractViewerComposite.class);

	protected AbstractViewerComposite(Composite parent, int style, boolean showTools) {
		this(parent, style, showTools, true);
	}

	protected AbstractViewerComposite(Composite parent, int style, boolean showTools, boolean showRenderModes) {
		super(parent, style);
		this.showTools = showTools;
		this.showRenderModes = showRenderModes;
	}

	public AbstractViewerComposite(Composite parent, int style, DrawComponent dc) {
		this(parent, style, dc, true);
	}

	public AbstractViewerComposite(Composite parent, int style, DrawComponent dc,
			boolean showTools)
	{
		this(parent, style, dc, showTools, true);
	}

	public AbstractViewerComposite(Composite parent, int style, DrawComponent dc,
			boolean showTools, boolean showRenderModes)
	{
		super(parent, style);
		this.showTools = showTools;
		this.showRenderModes = showRenderModes;
		init(dc);
	}

	private boolean showTools = true;
	private DrawComponent drawComponent;
	private ToolBarManager upperToolBarMan;
	private AbstractCanvasComposite canvasComp;
	private Composite sideComp;
	private Composite bottomComp;
	protected Composite toolsComp;
	private Label mouseLabel = null;
	private ToolEntryManager toolEntryManager = null;
	private boolean showRenderModes = true;

	public DrawComponent getDrawComponent() {
		return drawComponent;
	}

	protected abstract AbstractCanvasComposite initCanvasComposite(Composite parent);

	protected void init(DrawComponent drawComponent)
	{
		if (drawComponent == null)
			throw new IllegalArgumentException("Param drawComponent must not be null!"); //$NON-NLS-1$

		this.drawComponent = drawComponent;
//		drawComponent.clearBounds();
		initRenderModeManager();
		addDisposeListener(disposeListener);
		createComposite(this);
	}

	protected void initRenderModeManager()
	{
		drawComponent.setRenderModeManager(RendererRegistry.sharedInstance().getRenderModeManager());
		getRenderModeManager().addRenderModeListener(renderModeListener);
	}

	protected void createComposite(Composite parent)
	{
		// UpperToolBar
		upperToolBarMan = new ToolBarManager();
		ToolBar upperToolBar = upperToolBarMan.createControl(parent);
		GridData upperToolBarData = new GridData(GridData.FILL_HORIZONTAL);
		upperToolBar.setLayoutData(upperToolBarData);

		// Middle Wrapper Composite (Tools + Canvas + SideToolBar)
		XComposite comp = new XComposite(parent, SWT.NONE);
		int size = 3;
		if (!showTools)
			size = 2;
		GridLayout compLayout = new GridLayout(size, false);
		comp.setLayout(compLayout);

		// Tools
		if (showTools) {
			toolsComp = new XComposite(comp, SWT.BORDER);
			GridData toolsData = new GridData(GridData.FILL_VERTICAL);
			toolsComp.setLayoutData(toolsData);
		}

		// Canvas
		canvasComp = initCanvasComposite(comp);
		GridData canvasData = new GridData(GridData.FILL_BOTH);
		canvasComp.setLayoutData(canvasData);

		// SideToolBar
		sideComp = new XComposite(comp, SWT.BORDER);
		GridData sideData = new GridData(GridData.FILL_VERTICAL);
		sideData.widthHint = 200;
		sideComp.setLayoutData(sideData);
//		sideComp.setSize(200, 200);

		// BottomToolBar
		bottomComp = new XComposite(parent, SWT.BORDER);
		GridData bottomData = new GridData(GridData.FILL_HORIZONTAL);
		bottomComp.setLayoutData(bottomData);

		initToolbars();
		getViewer().updateCanvas();

//		doZoomAll();
//		addControlListener(resizeListener);
//		getViewer().getViewport().addPropertyChangeListener(viewChangeListener);
	}

//	private ControlListener resizeListener = new ControlAdapter(){
//		public void controlResized(ControlEvent e) {
//			logger.debug("control resized!");
//			doZoomAll();
//		}
//	};
//
//	private PropertyChangeListener viewChangeListener = new PropertyChangeListener(){
//		public void propertyChange(PropertyChangeEvent evt) {
//			if (evt.getPropertyName().equals(DisplayPanel.VIEW_CHANGE)) {
//				logger.debug("view Changed!");
//				doZoomAll();
//			}
//		}
//	};

//	protected void doZoomAll()
//	{
//		Display.getDefault().asyncExec(new Runnable() {
//			public void run() {
//				zoomAll();
//			}
//		});
//	}
//
//	protected void zoomAll()
//	{
//		Rectangle realBounds = getViewer().getViewport().getInitRealBounds();
//		Rectangle viewBounds = getViewer().getViewport().getInitViewBounds();
//		ViewerUtil.zoomAll(realBounds, viewBounds, getZoomSupport());
//
////		getViewer().updateCanvas();
//
//		if (getViewer() instanceof IBufferedViewport) {
//			IBufferedViewport viewport = (IBufferedViewport) getViewer();
//			viewport.notifyChange();
//		}
//		else
//			getViewer().updateCanvas();
//	}

	protected void registerToolEntries()
	{
		toolEntryManager = getToolEntryManager();
		IToolEntry selectToolEntry = new SelectToolEntry();
		toolEntryManager.addToolEntry(selectToolEntry);
		toolEntryManager.addToolEntry(new MarqueeToolEntry());
		toolEntryManager.addToolEntry(new ZoomToolEntry());

		if (showTools) {
			toolEntryManager.createToolsComposite(toolsComp);
		}
		toolEntryManager.setDefaultToolEntry(selectToolEntry);
		toolEntryManager.setActiveToolEntry(selectToolEntry);
	}

	public ToolEntryManager getToolEntryManager()
	{
		if (toolEntryManager == null)
			toolEntryManager = new ToolEntryManager(getViewer());

		return toolEntryManager;
	}

	protected void initToolbars()
	{
		initUpperToolBar(upperToolBarMan);
		initSideComposite(sideComp);
		initBottomComposite(bottomComp);
		registerToolEntries();
	}

	protected void initUpperToolBar(ToolBarManager tbm)
	{
		Action action = new ZoomInAction(getZoomSupport());
		action.setEnabled(true);
		tbm.add(action);

		action = new ZoomOutAction(getZoomSupport());
		action.setEnabled(true);
		tbm.add(action);

		action = new ZoomAllAction(getZoomSupport());
		action.setEnabled(true);
		tbm.add(action);

//		action = new ZoomSelectionAction(getViewer());
//		action.setEnabled(false);
//		tbm.add(action);

		tbm.add(new Separator());

		ZoomContributionItem zoomItem = new ZoomContributionItem(getZoomSupport());
		zoomItem.setEnabled(true);
		tbm.add(zoomItem);

		tbm.add(new Separator());

		if (showRenderModes && getRenderModeManager().getRenderModes().size() > 1)
		{
			IContributionItem item = new RenderModeContributionItem(getRenderModeManager(), false);
			item.setVisible(true);
			tbm.add(item);
		}

//		action = new ZoomAllStateAction(getZoomSupport());
//		action.setEnabled(true);
//		tbm.add(action);

//		tbm.add(new Separator());
//		bgColorItem = new BackgroundContributionItem(getViewer());
//		bgColorItem.setVisible(true);
//		tbm.add(bgColorItem);

		tbm.update(true);
		tbm.getControl().update();
	}

//	private BackgroundContributionItem bgColorItem = null;
//	public BackgroundContributionItem getBackgroundContributionItem() {
//		return bgColorItem;
//	}

	protected void initSideComposite(Composite comp)
	{
		createPreviewComposite(comp);
	}

	protected void createPreviewComposite(Composite comp)
	{
		final PreviewComposite previewComposite = new PreviewComposite(
				getDrawComponent(), getViewer().getViewport(), comp, SWT.BORDER);
		addControlListener(new ControlAdapter(){
			@Override
			public void controlResized(ControlEvent e) {
				if (previewComposite != null) {
					previewComposite.getPreviewPanel().clearBuffer();
				}
			}
		});
		GridData previewData = new GridData(GridData.FILL_BOTH);
		previewData.minimumHeight = 150;
		previewComposite.setLayoutData(previewData);
	}

	protected void initBottomComposite(Composite comp)
	{
		mouseLabel = new Label(comp, SWT.NONE);
		mouseLabel.setText("X =      , Y =       "); //$NON-NLS-1$
		getViewer().getMouseManager().addMouseMoveListener(mouseListener);
	}

	private MouseMoveListener mouseListener = new MouseMoveListener()
	{
		@Override
		public void mouseMoved(MouseEvent me) {
			if (!mouseLabel.isDisposed()) {
				int x = getViewer().getMouseManager().getAbsoluteX();
				int y = getViewer().getMouseManager().getAbsoluteY();
				mouseLabel.setText("X = "+x+", Y = "+y); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	};

	protected RenderModeListener renderModeListener = new RenderModeListener()
	{
		public void renderModeChanges(String renderMode) {
			if (getViewer() != null)
				getViewer().updateCanvas();
		}
	};

	protected IZoomSupport getZoomSupport() {
		return canvasComp.getZoomSupport();
	}

	protected RenderModeManager getRenderModeManager() {
		return drawComponent.getRenderModeManager();
	}

	public IViewer getViewer() {
		return canvasComp;
	}

	private DisposeListener disposeListener = new DisposeListener()
	{
		public void widgetDisposed(DisposeEvent e) {
			getRenderModeManager().removeRenderModeListener(renderModeListener);
			getViewer().dispose();
		}
	};

	/**
	 * Return the showTools.
	 * @return the showTools
	 */
	public boolean isShowTools() {
		return showTools;
	}

	public boolean isShowRenderModes() {
		return showRenderModes;
	}
}
