/* *****************************************************************************
 * NightLabs Editor2D - Graphical editor framework                             *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 * Project author: Daniel Mazurek <Daniel.Mazurek [at] nightlabs [dot] org>    *
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

package org.nightlabs.editor2d.ui;

import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.AlignmentAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.MatchHeightAction;
import org.eclipse.gef.ui.actions.MatchWidthAction;
import org.eclipse.gef.ui.actions.ToggleGridAction;
import org.eclipse.gef.ui.actions.ToggleRulerVisibilityAction;
import org.eclipse.gef.ui.actions.ToggleSnapToGeometryAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.gef.ui.properties.UndoablePropertySheetEntry;
import org.eclipse.gef.ui.rulers.RulerComposite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.holongate.j2d.J2DRegistry;
import org.nightlabs.base.ui.i18n.ResolutionUnitEP;
import org.nightlabs.base.ui.i18n.UnitRegistryEP;
import org.nightlabs.base.ui.io.FileEditorInput;
import org.nightlabs.base.ui.io.IOFilterRegistry;
import org.nightlabs.base.ui.io.IOFilterUIInformationProvider;
import org.nightlabs.base.ui.io.InformationProviderWizard;
import org.nightlabs.base.ui.language.LanguageManager;
import org.nightlabs.base.ui.print.page.PredefinedPageEP;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.base.ui.wizard.DynamicPathWizardDialog;
import org.nightlabs.base.ui.wizard.IWizardHop;
import org.nightlabs.editor2d.DrawComponent;
import org.nightlabs.editor2d.Editor2DFactory;
import org.nightlabs.editor2d.EditorRuler;
import org.nightlabs.editor2d.NameProvider;
import org.nightlabs.editor2d.RootDrawComponent;
import org.nightlabs.editor2d.impl.LayerImpl;
import org.nightlabs.editor2d.render.RenderModeManager;
import org.nightlabs.editor2d.render.j2d.J2DRenderContext;
import org.nightlabs.editor2d.ui.actions.DeleteAction;
import org.nightlabs.editor2d.ui.actions.EditShapeAction;
import org.nightlabs.editor2d.ui.actions.NormalSelectionAction;
import org.nightlabs.editor2d.ui.actions.ResetRotationCenterAction;
import org.nightlabs.editor2d.ui.actions.RotateAction;
import org.nightlabs.editor2d.ui.actions.SelectAllWithSameName;
import org.nightlabs.editor2d.ui.actions.ShowDefaultRenderAction;
import org.nightlabs.editor2d.ui.actions.copy.CloneAction;
import org.nightlabs.editor2d.ui.actions.copy.CopyAction;
import org.nightlabs.editor2d.ui.actions.copy.CutAction;
import org.nightlabs.editor2d.ui.actions.copy.PasteAction;
import org.nightlabs.editor2d.ui.actions.group.GroupAction;
import org.nightlabs.editor2d.ui.actions.group.UnGroupAction;
import org.nightlabs.editor2d.ui.actions.order.ChangeOrderOneDown;
import org.nightlabs.editor2d.ui.actions.order.ChangeOrderOneUp;
import org.nightlabs.editor2d.ui.actions.order.ChangeOrderToLocalBack;
import org.nightlabs.editor2d.ui.actions.order.ChangeOrderToLocalFront;
import org.nightlabs.editor2d.ui.actions.preferences.ShowFigureToolTipAction;
import org.nightlabs.editor2d.ui.actions.preferences.ShowStatusLineAction;
import org.nightlabs.editor2d.ui.actions.shape.ConvertToShapeAction;
import org.nightlabs.editor2d.ui.actions.shape.ShapeExclusiveOrAction;
import org.nightlabs.editor2d.ui.actions.shape.ShapeIntersectAction;
import org.nightlabs.editor2d.ui.actions.shape.ShapeSubtractAction;
import org.nightlabs.editor2d.ui.actions.shape.ShapeUnionAction;
import org.nightlabs.editor2d.ui.actions.zoom.ZoomAllAction;
import org.nightlabs.editor2d.ui.actions.zoom.ZoomPageAction;
import org.nightlabs.editor2d.ui.actions.zoom.ZoomSelectionAction;
import org.nightlabs.editor2d.ui.edit.RootDrawComponentEditPart;
import org.nightlabs.editor2d.ui.figures.BufferedFreeformLayer;
import org.nightlabs.editor2d.ui.outline.EditorOutlinePage;
import org.nightlabs.editor2d.ui.outline.filter.FilterManager;
import org.nightlabs.editor2d.ui.page.DocumentProperties;
import org.nightlabs.editor2d.ui.page.DocumentPropertiesRegistry;
import org.nightlabs.editor2d.ui.preferences.Preferences;
import org.nightlabs.editor2d.ui.print.EditorPrintAction;
import org.nightlabs.editor2d.ui.print.EditorPrintSetupAction;
import org.nightlabs.editor2d.ui.properties.EditorPropertyPage;
import org.nightlabs.editor2d.ui.properties.UnitManager;
import org.nightlabs.editor2d.ui.render.Draw2DRenderContext;
import org.nightlabs.editor2d.ui.resource.Messages;
import org.nightlabs.editor2d.ui.rulers.EditorRulerProvider;
import org.nightlabs.editor2d.ui.util.J2DUtil;
import org.nightlabs.editor2d.unit.DotUnit;
import org.nightlabs.editor2d.unit.UnitConstants;
import org.nightlabs.editor2d.viewer.ui.descriptor.DescriptorManager;
import org.nightlabs.editor2d.viewer.ui.render.RendererRegistry;
import org.nightlabs.i18n.unit.IUnit;
import org.nightlabs.i18n.unit.UnitRegistry;
import org.nightlabs.i18n.unit.resolution.Resolution;
import org.nightlabs.i18n.unit.resolution.ResolutionImpl;
import org.nightlabs.i18n.unit.resolution.ResolutionUnitRegistry;
import org.nightlabs.io.AbstractIOFilterWithProgress;
import org.nightlabs.io.IOFilter;
import org.nightlabs.io.IOFilterInformationProvider;
import org.nightlabs.io.IOFilterMan;
import org.nightlabs.io.IOFilterWithProgress;
import org.nightlabs.io.WriteException;
import org.nightlabs.print.page.IPredefinedPage;
import org.nightlabs.print.page.PredefinedPageRegistry;
import org.nightlabs.print.page.PredefinedPageUtil;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.NLLocale;


public abstract class AbstractEditor
//extends J2DGraphicalEditorWithFlyoutPalette
extends GraphicalEditorWithFlyoutPalette
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(Editor.class);

	private boolean savePreviouslyNeeded = false;
	private RulerComposite rulerComp;
	private TreeViewer treeViewer;
	private EditorOutlinePage outlinePage;
	private boolean editorSaving = false;
	private boolean useJava2D = true;
	
	/** Create a new Editor instance. This is called by the Workspace. */
	public AbstractEditor()
	{
		init();
		if (useJava2D) {
			initJ2DRegistry();
		}
	}

	protected void closeEditor(boolean save)
	{
		getSite().getPage().closeEditor(AbstractEditor.this, save);
//		System.gc();
	}

	public Object getModel() {
		return getRootDrawComponent();
	}

	private UnitManager unitManager = null;
	public UnitManager getUnitManager()
	{
		if (unitManager == null)
		{
			// TODO: update dotunit if resolution changes
			Collection<IUnit> units = UnitRegistryEP.sharedInstance().getUnitRegistry().getUnits(
					UnitConstants.UNIT_CONTEXT_EDITOR2D, true);
			unitManager = new UnitManager(new HashSet<IUnit>(units),
					getRootDrawComponent().getModelUnit());
		}
		return unitManager;
	}

	protected RootDrawComponent createRootDrawComponent() {
		RootDrawComponent root = getModelFactory().createRootDrawComponent(false);
		root.setNameProvider(getNameProvider());
		getModelFactory().validateRoot(root);
		return root;
	}

	private RootDrawComponent root = null;
	public RootDrawComponent getRootDrawComponent()
	{
		if (root == null) {
			root = createRootDrawComponent();
		}
		return root;
	}

	protected abstract EditPartFactory createEditPartFactory();
	private EditPartFactory editPartFactory = null;
	public EditPartFactory getEditPartFactory()
	{
		if (editPartFactory == null)
			editPartFactory = createEditPartFactory();;

		return editPartFactory;
	}

	protected abstract EditPartFactory createOutlineEditPartFactory();
	private EditPartFactory outlineEditPartFactory = null;
	public EditPartFactory getOutlineEditPartFactory()
	{
		if (outlineEditPartFactory == null)
			outlineEditPartFactory = createOutlineEditPartFactory();

		return outlineEditPartFactory;
	}

	protected abstract ContextMenuProvider createContextMenuProvider();
	private ContextMenuProvider contextMenuProvider = null;
	public ContextMenuProvider getContextMenuProvider()
	{
		if (contextMenuProvider == null)
			contextMenuProvider = createContextMenuProvider();

		return contextMenuProvider;
	}

	private PaletteRoot paletteRoot = null;
	@Override
	public PaletteRoot getPaletteRoot()
	{
		if (paletteRoot == null) {
			paletteRoot = getPaletteFactory().createPalette();
		}
		return paletteRoot;
	}

	/** KeyHandler with common bindings for both the Outline View and the Editor. */
	private KeyHandler sharedKeyHandler;

	private RenderModeManager renderMan = null;
	public RenderModeManager getRenderModeManager() {
		return renderMan;
	}

	private FilterManager filterMan = null;
	public FilterManager getFilterManager() {
		if (filterMan == null)
			filterMan = new FilterManager(getNameProvider());

		return filterMan;
	}

	protected abstract NameProvider createNameProvider();

	private NameProvider nameProvider = null;
	public NameProvider getNameProvider() {
		if (nameProvider == null)
			nameProvider = createNameProvider();
		return nameProvider;
	}

	private IOFilterMan ioFilterMan;
	public IOFilterMan getIOFilterMan()
	{
		if (ioFilterMan == null)
			ioFilterMan = IOFilterRegistry.sharedInstance().getIOFilterMan();

		return ioFilterMan;
	}

	private LanguageManager langMan;
	public LanguageManager getLanguageManager()
	{
		if (langMan == null)
			langMan = new LanguageManager();

		return langMan;
	}

	protected void init()
	{
		getPalettePreferences().setPaletteState(FlyoutPaletteComposite.STATE_PINNED_OPEN);
		getPalettePreferences().setDockLocation(PositionConstants.WEST);
//		filterMan = new FilterManager(getNameProvider()); // too late here
		configureIOFilterMan(getIOFilterMan());
	}

	@Override
	protected DefaultEditDomain getEditDomain()
	{
		 if (super.getEditDomain() == null) {
			 setEditDomain(new DefaultEditDomain(this));
		 }
		 return super.getEditDomain();
	}

	@SuppressWarnings("unchecked")
	protected void initJ2DRegistry()
	{
		// TODO: Holongate Draw2D-PreferencePage does not store values
		Map hints = new HashMap();
//		hints.put(J2DGraphics.KEY_USE_JAVA2D, Boolean.TRUE);
		hints.put(J2DUtil.get_J2DGraphics_KEY_USE_JAVA2D(), Boolean.TRUE);
		J2DRegistry.setHints(hints);
	}

	protected RootDrawComponent load(IOFilter ioFilter, InputStream input)
	{
		if (ioFilter != null)
		{
			try
			{
				RootDrawComponent root = (RootDrawComponent) ioFilter.read(input);
				root.setRenderModeManager(getRenderModeManager());
				return root;
			}
			catch (IOException e) {
				throw new RuntimeException("There occured an Error while reading with IOFilter "+ioFilter+" from InpuStream "+input, e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return null;
	}

	protected void load(FileEditorInput fileInput, IProgressMonitor monitor)
	{
		IOFilter ioFilter = getIOFilterMan().getIOFilter(fileInput.getFile());
		if (ioFilter != null)
		{
			try
			{
				if (ioFilter instanceof IOFilterWithProgress)
				{
					IOFilterWithProgress progressFilter = (IOFilterWithProgress) ioFilter;
					progressFilter.addPropertyChangeListener(progressListener);
					monitor.beginTask(Messages.getString("org.nightlabs.editor2d.ui.AbstractEditor.job.text") + " " + fileInput.getName(), progressFilter.getTotalWork()); //$NON-NLS-1$ //$NON-NLS-2$
					root = load(ioFilter, new FileInputStream(fileInput.getFile()));
					progressFilter.removePropertyChangeListener(progressListener);
					return;
				}
				else
					monitor.beginTask(Messages.getString("org.nightlabs.editor2d.ui.AbstractEditor.job.text") + " "+ fileInput.getName(), 2);	    			 //$NON-NLS-1$ //$NON-NLS-2$
				root = load(ioFilter, new FileInputStream(fileInput.getFile()));
				return;
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			} finally {
				monitor.done();
			}
		}
	}

	protected void load(FileEditorInput fileInput)
	{
		final FileEditorInput input = fileInput;
		IOFilter ioFilter = getIOFilterMan().getIOFilter(fileInput.getFile());
		prepareInformationProvider(fileInput.getFile(), ioFilter, false);
		IRunnableWithProgress runnable = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException
			{
				try {
					load(input, monitor);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		try {
			getProgressMonitor().run(false, false, runnable);
			setPartName(input.getName());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private ScalableFreeformRootEditPart rootEditPart;
	public ScalableFreeformRootEditPart getRootEditPart()
	{
		if (rootEditPart == null) 
		{
			if (useJava2D) {
//				rootEditPart = new J2DScalableFreeformRootEditPart();
				rootEditPart = J2DUtil.newJ2DScalableFreeformRootEditPart();
			}
			else {
				rootEditPart = new ScalableFreeformRootEditPart();
			}
		}
		return rootEditPart;
	}

	private ViewerManager viewerManager;
	public ViewerManager getViewerManager() {
		return viewerManager;
	}

	private DescriptorManager descriptorManager;
	public DescriptorManager getDescriptorManager() {
		return descriptorManager;
	}

	@Override
	protected void configureGraphicalViewer()
	{
		super.configureGraphicalViewer();
		ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer)getGraphicalViewer();

		List<String> zoomLevels = new ArrayList<String>(3);
		zoomLevels.add(ZoomManager.FIT_ALL);
		zoomLevels.add(ZoomManager.FIT_WIDTH);
		zoomLevels.add(ZoomManager.FIT_HEIGHT);
		getRootEditPart().getZoomManager().setZoomLevelContributions(zoomLevels);

		viewer.setRootEditPart(getRootEditPart());

		viewer.setEditPartFactory(getEditPartFactory());
		ContextMenuProvider provider = getContextMenuProvider();
		viewer.setContextMenu(provider);
		getSite().registerContextMenu("org.nightlabs.editor2d.ui.contextmenu", //$NON-NLS-1$
				provider, viewer);
		viewer.setKeyHandler(new EditorViewerKeyHandler(viewer)
			.setParent(getCommonKeyHandler()));

		loadProperties();

		// Actions
		IAction showRulers = new ToggleRulerVisibilityAction(getGraphicalViewer());
		getActionRegistry().registerAction(showRulers);

		IAction snapAction = new ToggleSnapToGeometryAction(getGraphicalViewer());
		getActionRegistry().registerAction(snapAction);

		IAction showGrid = new ToggleGridAction(getGraphicalViewer());
		getActionRegistry().registerAction(showGrid);

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				handleActivationChanged(event);
			}
		};
		getGraphicalControl().addListener(SWT.Activate, listener);
		getGraphicalControl().addListener(SWT.Deactivate, listener);

//		// TODO Workaround to fix grey bg in editor
//		getGraphicalControl().setBackground(new Color(null, 255, 255, 255));

		// ViewerManager
		viewerManager = new ViewerManager(viewer, getEditorSite().getActionBars().getStatusLineManager());
		configureViewerManager();

		getGraphicalControl().addControlListener(resizeListener);
//		getCommandStack().addCommandStackEventListener(commandStackListener);

		getGraphicalControl().addDisposeListener(disposeListener);
	}

	// should solve redraw problems when resizing the viewer
	private ControlListener resizeListener = new ControlAdapter(){
		@Override
		public void controlResized(ControlEvent e) {
			Display.getDefault().asyncExec(new Runnable(){
				public void run() {
					updateViewer();
					logger.debug("Control resized!"); //$NON-NLS-1$
				}
			});
		}
	};

	/* (non-Javadoc)
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#commandStackChanged(java.util.EventObject)
	 */
	@Override
	public void commandStackChanged(EventObject event)
	{
		if (isDirty()){
			if (!savePreviouslyNeeded()) {
				setSavePreviouslyNeeded(true);
				firePropertyChange(IEditorPart.PROP_DIRTY);
			}
		}
		else {
			setSavePreviouslyNeeded(false);
			firePropertyChange(IEditorPart.PROP_DIRTY);
		}
		super.commandStackChanged(event);
		// should solve redraw problems when undoing things
		updateViewer();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#createPaletteViewerProvider()
	 */
	@Override
	protected PaletteViewerProvider createPaletteViewerProvider() {
		return new PaletteViewerProvider(getEditDomain()) {
			@Override
			protected void configurePaletteViewer(PaletteViewer viewer) {
				super.configurePaletteViewer(viewer);
//				// create a drag source listener for this palette viewer
//				// together with an appropriate transfer drop target listener, this will enable
//				// model element creation by dragging a CombinatedTemplateCreationEntries
//				// from the palette into the editor
//				// @see ShapesEditor#createTransferDropTargetListener()
//				viewer.addDragSourceListener(new TemplateTransferDragSourceListener(viewer));
			}
		};
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	public Object getAdapter(Class type)
	{
		if (type == IContentOutlinePage.class) {
//			treeViewer = new TreeViewer();
			treeViewer = new org.nightlabs.editor2d.ui.outline.TreeViewer();
			outlinePage = new EditorOutlinePage(this, treeViewer);
			return outlinePage;
		}

		if (type == ZoomManager.class) {
			return getZoomManager();
		}

		if (type == org.eclipse.ui.views.properties.IPropertySheetPage.class) {
			PropertySheetPage page = new EditorPropertyPage(getUnitManager());
			page.setRootEntry(new UndoablePropertySheetEntry(getCommandStack()));
			return page;
		}

		if (type == RenderModeManager.class)
			return getRenderModeManager();

		if (type == RootDrawComponent.class)
			return getRootDrawComponent();

		return super.getAdapter(type);
	}

	@Override
	protected Control getGraphicalControl() {
		return rulerComp;
	}

	private ZoomManager zoomManager = null;
	protected ZoomManager getZoomManager()
	{
		if (zoomManager == null && getGraphicalViewer() != null)
		{
			Object zoomAdapter = getGraphicalViewer().getProperty(ZoomManager.class.toString());
		  if (zoomAdapter != null && zoomAdapter instanceof ZoomManager) {
  			zoomManager = ((ZoomManager)zoomAdapter);
		  }
		}
		return zoomManager;
	}

	@Override
	public void doSave(IProgressMonitor monitor)
	{
		try {
			FileEditorInput input = (FileEditorInput) getEditorInput();
			if (!input.isSaved()) {
				doSaveAs();
				return;
			}

			File file = input.getFile();
			String inputName = input.getName();
			logger.debug("inputName = "+inputName); //$NON-NLS-1$

			if (file.exists()
					|| MessageDialog.openConfirm(
							getSite().getShell(),
							Messages.getString("org.nightlabs.editor2d.ui.AbstractEditor.errorDialog.title"), //$NON-NLS-1$
							Messages.getString("org.nightlabs.editor2d.ui.AbstractEditor.errorDialog.message.part1") //$NON-NLS-1$
							+ " " //$NON-NLS-1$
							+ file.getName()
							+ " " //$NON-NLS-1$
							+ Messages.getString("org.nightlabs.editor2d.ui.AbstractEditor.errorDialog.message.part2"))) //$NON-NLS-1$
			{
				editorSaving = true;
				saveProperties();
				save(file, monitor);
				getCommandStack().markSaveLocation();
			}
		}
		catch (WriteException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public void doSaveAs()
	{
		performSaveAs();
	}

	/**
	 * Returns the KeyHandler with common bindings for both the Outline and Graphical Views.
	 * For example, delete is a common action.
	 */
	public KeyHandler getCommonKeyHandler()
	{
		if (sharedKeyHandler == null) {
			sharedKeyHandler = new KeyHandler();

			// Add key and action pairs to sharedKeyHandler
			sharedKeyHandler.put(
					KeyStroke.getPressed(SWT.DEL, 127, 0),
					getActionRegistry().getAction(ActionFactory.DELETE.getId()));
			sharedKeyHandler.put(
					KeyStroke.getPressed(SWT.F2, 0),
					getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT));
			// TODO: register more common keys
		}
		return sharedKeyHandler;
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getPalettePreferences()
	 */
	@Override
	protected FlyoutPreferences getPalettePreferences() {
		return getPaletteFactory().createPalettePreferences();
	}

	protected abstract AbstractPaletteFactory createPaletteFactory();

	private AbstractPaletteFactory paletteFactory = null;
	public AbstractPaletteFactory getPaletteFactory()
	{
		if (paletteFactory == null) {
			paletteFactory = createPaletteFactory();
		}
		return paletteFactory;
	}

	protected void handleActivationChanged(Event event)
	{
		IAction copy = null;
		if (event.type == SWT.Deactivate)
			copy = getActionRegistry().getAction(ActionFactory.COPY.getId());
		if (getEditorSite().getActionBars().getGlobalActionHandler(ActionFactory.COPY.getId())
				!= copy) {
			getEditorSite().getActionBars().setGlobalActionHandler(
					ActionFactory.COPY.getId(), copy);
			getEditorSite().getActionBars().updateActionBars();
		}
	}

	private Color bgColor = new Color(null, 255, 255, 255);
	public Color getBackgroundColor() {
		return bgColor;
	}
	public void setBackgroundColor(Color bgColor) {
		this.bgColor = bgColor;
		getGraphicalViewer().getControl().setBackground(bgColor);
	}

	/**
	 * Set up the editor's inital content (after creation).
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#initializeGraphicalViewer()
	 */
	@Override
	protected void initializeGraphicalViewer()
	{
		GraphicalViewer graphicalViewer = getGraphicalViewer();
		graphicalViewer.setContents(getModel()); // set the contents of this editor

		setBackgroundColor(bgColor);

		// DescriptorManager
		descriptorManager = new DescriptorManager();
		configureDescriptorManager();
		if (getModelRootEditPart() != null) {
			getModelRootEditPart().setDescriptorManager(getDescriptorManager());
		} else {
			logger.debug("DescriptorManager for RootDrawComponentEditPart not set, because it is null!"); //$NON-NLS-1$
		}
		viewerManager.setDescriptorManager(getDescriptorManager());

		// FilterManager
		configureFilterManager();

//		zoomAll();
	}

	private void zoomAll() {
		if (getZoomManager() != null) {
			getZoomManager().setZoomAsText(ZoomManager.FIT_ALL);
			logger.info("zoomAll"); //$NON-NLS-1$
		}
		else {
			Display.getDefault().timerExec(2000, new Runnable(){
				public void run() {
					zoomAll();
				}
			});
			logger.info("ZoomAll does not worked, zoomManager == null"); //$NON-NLS-1$
			logger.info("started Timer"); //$NON-NLS-1$
		}
	}

	@Override
	protected void initializeActionRegistry()
	{
		super.initializeActionRegistry();

		// TODO: find out why global keyBindings not work only on base of Extension-Points
		// (org.eclipse.ui.bindings + commands) nor on EditorActionBarContributor.declareGlobalActionKeys()
		IKeyBindingService keyBindingService = getSite().getKeyBindingService();
		for (Iterator<IAction> it = getActionRegistry().getActions(); it.hasNext(); ) {
			keyBindingService.registerAction(it.next());
		}
	}

	protected void configureFilterManager()
	{
//		Map<Class<? extends DrawComponent>, List<DrawComponent>> class2DrawComponents = getRootDrawComponent().getClass2DrawComponents();
//		for (Iterator<Class<? extends DrawComponent>> it = class2DrawComponents.keySet().iterator(); it.hasNext(); ) {
//			Class<? extends DrawComponent> c = it.next();
		for (Class<? extends DrawComponent> c : getRootDrawComponent().getDrawComponentClasses()) {
			getFilterManager().addFilter(c);
		}
		getRootDrawComponent().addPropertyChangeListener(getFilterManager().getTypeListener());
		getFilterManager().ignoreClass(LayerImpl.class);
	}

	/**
	 * By Default this Method does nothing, but subclasses can override this Method to define
	 * excluded EditParts, ignored classes or an exclusive class
	 *
	 * @see org.nightlabs.editor2d.ui.ViewerManager
	 */
	protected void configureViewerManager()
	{

	}

	/**
	 * By Default this Method does nothing, but subclasses can override this Method to add
	 * Descriptors for special classes to the DescriptorManager
	 *
	 * @see DescriptorManager#addDescriptor(org.nightlabs.editor2d.ui.viewer.ui.descriptor.IDrawComponentDescriptor, Class)
	 */
	protected void configureDescriptorManager()
	{

	}

	protected void configureRenderModeManager()
	{
		if (useJava2D) {
			getRenderModeManager().setCurrentRenderContextType(J2DRenderContext.RENDER_CONTEXT_TYPE_JAVA2D);	
		}
		else {
			getRenderModeManager().setCurrentRenderContextType(Draw2DRenderContext.RENDER_CONTEXT_TYPE);	
		}
	}

	protected EditorActionBarContributor getEditorActionBarContributor()
	{
		if (getEditorSite().getActionBarContributor() != null &&
				getEditorSite().getActionBarContributor() instanceof EditorActionBarContributor)
		{
			return (EditorActionBarContributor) getEditorSite().getActionBarContributor();
		}
		return null;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	protected void createActions()
	{
		super.createActions();
		ActionRegistry registry = getActionRegistry();
		IAction action;

		// Match Actions
		action = new MatchWidthAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new MatchHeightAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

//		action = new DirectEditAction((IWorkbenchPart)this);
//		registry.registerAction(action);
//		getSelectionActions().add(action.getId());

		// Alignment Actions
		action = new AlignmentAction((IWorkbenchPart)this, PositionConstants.LEFT);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart)this, PositionConstants.RIGHT);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart)this, PositionConstants.TOP);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart)this, PositionConstants.BOTTOM);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart)this, PositionConstants.CENTER);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart)this, PositionConstants.MIDDLE);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// ZoomIn
		action = new ZoomInAction(getRootEditPart().getZoomManager());
		registry.registerAction(action);
		getSite().getKeyBindingService().registerAction(action);

		// ZoomOut
		action = new ZoomOutAction(getRootEditPart().getZoomManager());
		registry.registerAction(action);
		getSite().getKeyBindingService().registerAction(action);

		// Zoom All
		action = new ZoomAllAction(getRootEditPart().getZoomManager());
		registry.registerAction(action);

		// Zoom Selection
		action = new ZoomSelectionAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// Zoom Page
		action = new ZoomPageAction(this, getRootEditPart().getZoomManager());
		registry.registerAction(action);
		getPropertyActions().add(action.getId());

		// Edit Shape Action
		action = new EditShapeAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// Rotate Action
		action = new RotateAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// Normal Selection Action
		action = new NormalSelectionAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// Reset Rotation Center Action
		action = new ResetRotationCenterAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// Show Default View (Renderer) Action
		action = new ShowDefaultRenderAction(this);
		registry.registerAction(action);

		// Select all with same name
		action = new SelectAllWithSameName(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// Clone (Duplicate) Action
		action = new CloneAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// Order Actions
		action = new ChangeOrderToLocalFront(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new ChangeOrderToLocalBack(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new ChangeOrderOneDown(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new ChangeOrderOneUp(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// Paste Action
		PasteAction pasteAction = new PasteAction(this);
		registry.registerAction(pasteAction);
		getPropertyActions().add(pasteAction.getId());
		getSite().getKeyBindingService().registerAction(pasteAction);

		// Cut Action
		CutAction cutAction = new CutAction(this);
		registry.registerAction(cutAction);
		getSelectionActions().add(cutAction.getId());
		cutAction.addPropertyChangeListener(pasteAction.cutListener);
		getSite().getKeyBindingService().registerAction(cutAction);

		// Copy Action
		CopyAction copyAction = new CopyAction(this);
		registry.registerAction(copyAction);
		getSelectionActions().add(copyAction.getId());
		copyAction.addPropertyChangeListener(pasteAction.copyListener);
		getSite().getKeyBindingService().registerAction(copyAction);

		// Tooltip Preference
		action = new ShowFigureToolTipAction(this);
		registry.registerAction(action);
		getPropertyActions().add(action.getId());

		// Status Line Preference
		action = new ShowStatusLineAction(this);
		registry.registerAction(action);
		getPropertyActions().add(action.getId());

//		// Repaint Action
//		action = new RepaintAction(this);
//		registry.registerAction(action);
//		getPropertyActions().add(action.getId());

//		// Test Viewer Action
//		action = new ViewerAction(this);
//		registry.registerAction(action);

		// Print Action
//		action = new EditorPrintAction(this);
		action = createPrintAction();
		registry.registerAction(action);
		getPropertyActions().add(action.getId());

		// Print Preview Action
//		action = new EditorPrintPreviewAction(this);
//		registry.registerAction(action);
//		getPropertyActions().add(action.getId());

//		// Print Page Setup Action
		action = new EditorPrintSetupAction(this);
		registry.registerAction(action);
		getPropertyActions().add(action.getId());

		// Group Action
		action = new GroupAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// UnGroup Action
		action = new UnGroupAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// Convert To Shape
		action = new ConvertToShapeAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// Shape Union
		action = new ShapeUnionAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// Shape Intersection
		action = new ShapeIntersectAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// Shape Subtract
		action = new ShapeSubtractAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// Shape Exclusive Or
		action = new ShapeExclusiveOrAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// Delete
		action = new DeleteAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
	}

	protected IAction createPrintAction() {
		return new EditorPrintAction(this);
//		return new PrintAction(this);
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#createGraphicalViewer(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createGraphicalViewer(Composite parent)
	{
		long start = System.currentTimeMillis();
		rulerComp = new RulerComposite(parent, SWT.NONE);
		
//		super.createGraphicalViewer(rulerComp);
		GraphicalViewer viewer = null;
		if (useJava2D) {
//			viewer = new J2DScrollingGraphicalViewer();
			viewer = J2DUtil.newJ2DScrollingGraphicalViewer();
		} else {
			viewer = new ScrollingGraphicalViewer();
		}
		viewer.createControl(rulerComp);
		setGraphicalViewer(viewer);
		configureGraphicalViewer();
		hookGraphicalViewer();
		initializeGraphicalViewer();
		
		rulerComp.setGraphicalViewer((ScrollingGraphicalViewer) getGraphicalViewer());
		if (logger.isDebugEnabled()) {
			long duration = System.currentTimeMillis() - start;
			logger.debug("createGraphicalViewer took "+duration+" ms!"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public FigureCanvas getEditor(){
		return (FigureCanvas)getGraphicalViewer().getControl();
	}

	@Override
	public boolean isDirty() {
		return isSaveOnCloseNeeded();
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		return getCommandStack().isDirty();
	}

	protected void loadProperties()
	{
		// Ruler properties
		EditorRuler ruler = getRootDrawComponent().getLeftRuler();
		RulerProvider provider = null;
		if (ruler != null) {
			provider = new EditorRulerProvider(ruler);
		}
		getGraphicalViewer().setProperty(RulerProvider.PROPERTY_VERTICAL_RULER, provider);
		ruler = getRootDrawComponent().getTopRuler();
		provider = null;
		if (ruler != null) {
			provider = new EditorRulerProvider(ruler);
		}
		getGraphicalViewer().setProperty(RulerProvider.PROPERTY_HORIZONTAL_RULER, provider);
		getGraphicalViewer().setProperty(RulerProvider.PROPERTY_RULER_VISIBILITY,
				new Boolean(getRootDrawComponent().isRulersEnabled()));

		// Snap to Geometry property
		getGraphicalViewer().setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED,
				new Boolean(getRootDrawComponent().isSnapToGeometry()));

		// Grid properties
		getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_ENABLED,
				new Boolean(getRootDrawComponent().isGridEnabled()));
		// We keep grid visibility and enablement in sync
		getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE,
				new Boolean(getRootDrawComponent().isGridEnabled()));

		// Zoom
		ZoomManager manager = (ZoomManager)getGraphicalViewer()
			.getProperty(ZoomManager.class.toString());
		if (manager != null)
			manager.setZoom(getRootDrawComponent().getZoom());


	}

	/**
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	protected boolean savePreviouslyNeeded() {
		return savePreviouslyNeeded;
	}

	protected void saveProperties()
	{
		getRootDrawComponent().setRulersEnabled(((Boolean)getGraphicalViewer()
				.getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY)).booleanValue());
		getRootDrawComponent().setGridEnabled(((Boolean)getGraphicalViewer()
				.getProperty(SnapToGrid.PROPERTY_GRID_ENABLED)).booleanValue());
		getRootDrawComponent().setSnapToGeometry(((Boolean)getGraphicalViewer()
				.getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED)).booleanValue());
		ZoomManager manager = (ZoomManager)getGraphicalViewer()
		.getProperty(ZoomManager.class.toString());
		if (manager != null)
			getRootDrawComponent().setZoom(manager.getZoom());
	}

	protected void setSavePreviouslyNeeded(boolean value) {
		savePreviouslyNeeded = value;
	}

	@Override
	public String getTitle() {
//		return super.getTitle();
		if (getEditorInput() != null)
			return getEditorInput().getName();
		else
			return super.getTitle();
	}

	protected boolean performSaveAs()
	{
		String inputFileName = getEditorInput().getName();
		String defaultFileExtension = ""; //$NON-NLS-1$
		if (getIOFilterMan().getDefaultWriteIOFilter() != null) {
			defaultFileExtension = getIOFilterMan().getDefaultWriteIOFilter().getFileExtensions()[0];
			inputFileName.concat("."+defaultFileExtension); //$NON-NLS-1$
		}

		FileDialog dialog = new FileDialog(getSite().getWorkbenchWindow().getShell(), SWT.SAVE);
		dialog.setFileName(inputFileName);

//		String[] fileExtensions = getIOFilterMan().getWriteFileExtensions(true);
//		if (fileExtensions != null) {
//			dialog.setFilterExtensions(fileExtensions);
//		}

		List<String> fileExtensions = new ArrayList<String>();
		List<String> fileExtensionDescriptions = new ArrayList<String>();
		fileExtensions.add("*.*"); //$NON-NLS-1$
		fileExtensionDescriptions.add("*.* (All Files)"); //$NON-NLS-1$
		Collection<IOFilter> writeFilters = getIOFilterMan().getWriteFilter();
		for (IOFilter writeFilter : writeFilters) {
			for (String fileExtension : writeFilter.getFileExtensions()) {
				String fileExt = "*."+fileExtension; //$NON-NLS-1$
				fileExtensions.add(fileExt);
				String description = writeFilter.getFileExtensionDescription(fileExtension).getText();
				fileExtensionDescriptions.add(fileExt + " (" + description + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		String[] extensions = fileExtensions.toArray(new String[fileExtensions.size()]);
		String[] descriptions = fileExtensionDescriptions.toArray(new String[fileExtensionDescriptions.size()]);
		String fullPath = null;

		dialog.setFilterExtensions(extensions);
		dialog.setFilterNames(descriptions);
		fullPath = dialog.open();

//		Frame frame = new Frame();
//		try {
//			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
//			SwingUtilities.updateComponentTreeUI(frame);
//		}catch ( Exception e ) {
//			System.err.println( "Could not use Look and Feel:" + e );
//		}
//		final JFileChooser fc = new JFileChooser();
//		fc.setAcceptAllFileFilterUsed(true);
//		int returnCode = fc.showSaveDialog(frame);
//		if (returnCode == JFileChooser.APPROVE_OPTION) {
//			fullPath = fc.getSelectedFile().getAbsolutePath();
//		}

		// Cancel pressed
		if (fullPath == null)
			return false;

		String fileExtension = IOUtil.getFileExtension(fullPath);
		// No fileExtension specified, add the default fileExtension
		if (fileExtension == null) {
			fullPath = fullPath.concat("."+defaultFileExtension); //$NON-NLS-1$
		}
		else {
			IOFilter ioFilter = getIOFilterMan().getIOFilter(fileExtension);
			// No ioFilter found for the given fileExtension, add the default fileExtension
			if (ioFilter == null) {
				fullPath = fullPath.concat("."+defaultFileExtension); //$NON-NLS-1$
			}
		}

		final File file = new File(fullPath);

		if (!file.exists()) {
			save(file);
		}
		else {
			if (RCPUtil.showConfirmOverwriteDialog(file.getName()))
				save(file);
			else
				return false;
		}
		try {
			// TODO should only be done when saving was successful and not aborted
			getCommandStack().markSaveLocation();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return true;
	}

	private PropertyChangeListener progressListener = new PropertyChangeListener()
	{
		public void propertyChange(PropertyChangeEvent evt)
		{
			Object newValue = evt.getNewValue();
			String propertyName = evt.getPropertyName();
			if (propertyName.equals(AbstractIOFilterWithProgress.PROGRESS_CHANGED)) {
				int work = ((Integer)newValue).intValue();
				getProgressMonitor().getProgressMonitor().internalWorked(work);
			}
			else if (propertyName.equals(AbstractIOFilterWithProgress.SUBTASK_FINISHED)) {
				String subTaskName = (String) newValue;
				getProgressMonitor().getProgressMonitor().subTask(subTaskName);
			}
		}
	};

	private ProgressMonitorDialog progressMonitor;
	protected ProgressMonitorDialog getProgressMonitor()
	{
		if (progressMonitor == null) {
			progressMonitor = new ProgressMonitorDialog(getSite().getWorkbenchWindow().getShell());
		}
		return progressMonitor;
	}

	protected void prepareInformationProvider(File f, IOFilter ioFilter, boolean write)
	{
		if (ioFilter != null)
		{
			IOFilterInformationProvider ip = ioFilter.getInformationProvider();
			if (ip != null)
			{
				URL url;
				try {
					URI uri = f.toURI();
					url = uri.toURL();
					ip.setURL(url);
					if (logger.isDebugEnabled())
						logger.debug("url = "+url); //$NON-NLS-1$

					if (ip instanceof IOFilterUIInformationProvider)
					{
						IOFilterUIInformationProvider uiip = (IOFilterUIInformationProvider) ip;
						IWizardHop hop = null;
						if (write)
							hop = uiip.getWizardHopForWrite();
						else
							hop = uiip.getWizardHopForRead();
						if (hop != null)
						{
							InformationProviderWizard wizard = new InformationProviderWizard(url);
							wizard.addDynamicWizardPage(hop.getEntryPage());
							DynamicPathWizardDialog dialog = new DynamicPathWizardDialog(wizard);
							int returnCode = dialog.open();
							if (returnCode == Window.OK) {

							}
						}
					}
				} catch (MalformedURLException e) {
					logger.warn("fileInput.getFile() "+f+" could not be transformed into ULR", e); //$NON-NLS-1$ //$NON-NLS-2$
					logger.warn("prepartion of IOFilterInformationProvider for IOFilter "+ioFilter.getName().getText(NLLocale.getDefault().getLanguage()) +" failed!"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}

	protected void save(File f)
	{
		final File file = f;
		IOFilter ioFilter = getIOFilterMan().getIOFilter(file);
		prepareInformationProvider(file, ioFilter, true);
		IRunnableWithProgress runnable = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException
			{
				saveProperties();
				try {
					save(file, monitor);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		try {
			getProgressMonitor().run(false, false, runnable);
			setPartName(file.getName());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Saves the Model under the specified path.
	 *
	 * @param file the file to save
	 * @param progressMonitor The ProgressMonitor to show the save Progress
	 */
	protected boolean save(File file, IProgressMonitor progressMonitor)
	throws WriteException
	{
		if (null == progressMonitor)
			progressMonitor = new NullProgressMonitor();

		// use IOFilterMan
		final IOFilter ioFilter = getIOFilterMan().getIOFilter(file);
		if (ioFilter != null)
		{
			final String fileExt = IOUtil.getFileExtension(file.getName());

			if (!ioFilter.supportsWrite(fileExt)) {
				// The IOFilter that was found does not support writing
				// we tell the user and call saveAs() afterwards, of course it would be better
				// to filter out the current file-extension but well...
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
								Messages.getString("org.nightlabs.editor2d.ui.AbstractEditor.formatNotSupportedDialog.title"),  //$NON-NLS-1$
								String.format(
										Messages.getString("org.nightlabs.editor2d.ui.AbstractEditor.formatNotSupportedDialog.message"),  //$NON-NLS-1$
										ioFilter.getFileExtensionDescription(fileExt).getText())
								 );
					}
				});
				doSaveAs();
				return true;
			}

			if (!reactOnSave(ioFilter)) {
				return false;
			}

			try {
				if (ioFilter instanceof IOFilterWithProgress) {
					IOFilterWithProgress progressFilter = (IOFilterWithProgress) ioFilter;
					progressMonitor.beginTask(Messages.getString("org.nightlabs.editor2d.ui.AbstractEditor.job.name") + " " + file.getName(), progressFilter.getTotalWork()); //$NON-NLS-1$ //$NON-NLS-2$
					progressFilter.addPropertyChangeListener(progressListener);
					saveFile(file, progressFilter, progressMonitor);
					progressFilter.removePropertyChangeListener(progressListener);
				}
				else {
					progressMonitor.beginTask(Messages.getString("org.nightlabs.editor2d.ui.AbstractEditor.job.name") + " " + file.getName(), 2); //$NON-NLS-1$ //$NON-NLS-2$
					saveFile(file, ioFilter, progressMonitor);
				}
			} finally {
				progressMonitor.done();
			}
		}
		return true;
	}

	protected void saveFile(File file, IOFilter ioFilter, IProgressMonitor monitor)
	throws WriteException
	{
		try {
			String fileName = file.getCanonicalPath();
			logger.info("Save File "+fileName); //$NON-NLS-1$
			FileOutputStream fos = new FileOutputStream(fileName);
			ioFilter.write(getRootDrawComponent(), fos);
		} catch (Exception e) {
//			throw new WriteException(file, "an error occured while writing", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void setInput(IEditorInput input)
	{
		long start = System.currentTimeMillis();
		super.setInput(input);
		renderMan = RendererRegistry.sharedInstance().getRenderModeManager();
		configureRenderModeManager();

		if (input instanceof FileEditorInput) {
			FileEditorInput fileInput = (FileEditorInput) input;
			root = getRootDrawComponent();
			if (!fileInput.isSaved()) {
				initialzePage();
			} else {
				try {
					load(fileInput);
				} catch (OutOfMemoryError e) {
					System.gc();
					MessageDialog.openError(RCPUtil.getActiveShell(),
							Messages.getString("org.nightlabs.editor2d.ui.AbstractEditor.notEnoughMemoryDialog.title"), Messages.getString("org.nightlabs.editor2d.ui.AbstractEditor.notEnoughMemoryDialog.message")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				zoomAll();
			}
		}
		else
			initialzePage();

		root.setRenderModeManager(getRenderModeManager());
		getRootDrawComponent().setLanguageID(getLanguageManager().getCurrentLanguageID());
		getUnitManager().setCurrentUnit(getRootDrawComponent().getModelUnit());
		if (logger.isDebugEnabled()) {
			long duration = System.currentTimeMillis() - start;
			logger.debug("setInput() took "+duration+" ms!"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected void initialzePage()
	{
		logger.debug("initialize Page!"); //$NON-NLS-1$
		root = getRootDrawComponent();
		loadAdditional();

//		String pageID = Preferences.getPreferenceStore().getString(
//				Preferences.PREF_PREDEFINED_PAGE_ID);
//		IPredefinedPage defaultPage = getPredefinedPageRegistry().getPage(pageID);
//		IUnit pageUnit = defaultPage.getUnit();
//		String resolutionUnitID = Preferences.getPreferenceStore().getString(
//				Preferences.PREF_STANDARD_RESOLUTION_UNIT_ID);
//		IResolutionUnit resUnit = getResolutionUnitRegistry().getResolutionUnit(resolutionUnitID);
//		Resolution resolution = new ResolutionImpl(resUnit,
//				Preferences.getPreferenceStore().getDouble(Preferences.PREF_DOCUMENT_RESOLUTION));

		IPredefinedPage defaultPage = null;
		Resolution resolution = null;
		Map<Class<? extends AbstractEditor>, DocumentProperties> editorClass2DocumentProperties =
			DocumentPropertiesRegistry.sharedInstance().getDocumentConfModule().getEditorClass2DocumentProperties();
		DocumentProperties documentProperties = editorClass2DocumentProperties.get(this.getClass());
		if (documentProperties != null) {
			resolution = new ResolutionImpl(documentProperties.getResolutionUnit(),
					documentProperties.getResolution());
			defaultPage = documentProperties.getPredefinedPage();
		}

		DotUnit dotUnit = (DotUnit) getUnitRegistry().getUnit(DotUnit.UNIT_ID);
		if (dotUnit == null) {
			dotUnit = new DotUnit(resolution);
			getUnitRegistry().addUnit(dotUnit, UnitConstants.UNIT_CONTEXT_EDITOR2D);
		}
		else
			dotUnit.setResolution(resolution);

		String unitID = Preferences.getPreferenceStore().getString(Preferences.PREF_STANDARD_UNIT_ID);
		getUnitManager().setCurrentUnit(getUnitRegistry().getUnit(unitID));

		Rectangle pageBounds = PredefinedPageUtil.getPageBounds(dotUnit, defaultPage);
		getRootDrawComponent().setResolution(resolution);
		getRootDrawComponent().getCurrentPage().setPageBounds(pageBounds);
	}

	protected ResolutionUnitRegistry getResolutionUnitRegistry() {
		return ResolutionUnitEP.sharedInstance().getResolutionUnitRegistry();
	}

	protected UnitRegistry getUnitRegistry() {
		return UnitRegistryEP.sharedInstance().getUnitRegistry();
	}

	protected PredefinedPageRegistry getPredefinedPageRegistry() {
		return PredefinedPageEP.sharedInstance().getPageRegistry();
	}

	protected void loadAdditional() {
		if (!editorSaving) {
			if (getGraphicalViewer() != null) {
				getGraphicalViewer().setContents(getRootDrawComponent());
				loadProperties();
			}
		}
	}

	public EditPartViewer getEditPartViewer() {
		return getGraphicalViewer();
	}

	public void updateViewer()
	{
		refreshBuffer();
		if (getGraphicalViewer() != null && getGraphicalViewer().getControl() != null) {
			getGraphicalViewer().getControl().redraw();
		}
		logger.debug("updateViewer!"); //$NON-NLS-1$
	}

	protected void refreshBuffer()
	{
		RootDrawComponentEditPart mldcEditPart = getModelRootEditPart();
		if (mldcEditPart != null) {
			BufferedFreeformLayer buffer = mldcEditPart.getBufferedFreeformLayer();
			if (buffer != null) {
//				LOGGER.debug("Buffer refreshed!");
				buffer.refresh();
			}
		}
	}

	private RootDrawComponentEditPart mldcEditPart = null;
	protected RootDrawComponentEditPart getModelRootEditPart()
	{
		if (getRootEditPart().getChildren().size() == 1) {
			EditPart editPart = (EditPart) getRootEditPart().getChildren().get(0);
			if (editPart != null) {
				if (editPart instanceof RootDrawComponentEditPart) {
					return mldcEditPart = (RootDrawComponentEditPart) editPart;
				}
			}
		}
		return mldcEditPart;
	}

//	**************** BEGIN public Methods for EditorOutlinePage ********************
	public GraphicalViewer getOutlineGraphicalViewer() {
		return getGraphicalViewer();
	}

	public SelectionSynchronizer getOutlineSelectionSynchronizer() {
		return getSelectionSynchronizer();
	}

	public DefaultEditDomain getOutlineEditDomain() {
		return getEditDomain();
	}

	public ActionRegistry getOutlineActionRegistry() {
		return getActionRegistry();
	}
//	**************** END public Methods for EditorOutlinePage **********************

	public void disposeEditor()
	{
//		super.dispose();

		if (getGraphicalControl() != null)
			getGraphicalControl().removeControlListener(resizeListener);

		// disposes RootDrawComponent
		if (root != null)
			root.dispose();
		root = null;

		if (outlinePage != null)
			outlinePage.dispose();
		outlinePage = null;

		if (rootEditPart != null)
			rootEditPart.deactivate();
		rootEditPart = null;

		if (mldcEditPart != null)
			mldcEditPart.deactivate();
		mldcEditPart = null;

		if (rulerComp != null)
			rulerComp.dispose();
		rulerComp = null;

		if (contextMenuProvider != null)
			contextMenuProvider.dispose();
		contextMenuProvider = null;

		treeViewer = null;
		viewerManager = null;
		editPartFactory = null;
		paletteRoot = null;

		freeMemory();
	}

	protected void freeMemory()
	{
		Runtime runTime = Runtime.getRuntime();
		long maxMemory = runTime.maxMemory();
		long freeMemory = runTime.freeMemory();
		long totalMemory = runTime.totalMemory();
		long usedMemory = totalMemory - freeMemory;
		long startTime = System.currentTimeMillis();

		double mb = 1024 * 1024;
		if (logger.isDebugEnabled()) {
			logger.debug("Max Memory BEFORE GC   = "+(maxMemory / mb)+" MB"); //$NON-NLS-1$ //$NON-NLS-2$
			logger.debug("Total Memory BEFORE GC = "+(totalMemory / mb)+" MB"); //$NON-NLS-1$ //$NON-NLS-2$
			logger.debug("Used Memory BEFORE GC  = "+(usedMemory / mb)+" MB"); //$NON-NLS-1$ //$NON-NLS-2$
			logger.debug("Free Memory BEFORE GC  = "+(freeMemory / mb)+" MB");     //$NON-NLS-1$ //$NON-NLS-2$
			logger.debug("GC Begin!");			 //$NON-NLS-1$
		}

		runTime.gc();
		long newTime = System.currentTimeMillis() - startTime;

		if (logger.isDebugEnabled())
			logger.debug("GC took "+newTime+" ms"); //$NON-NLS-1$ //$NON-NLS-2$

		freeMemory = runTime.freeMemory();
		totalMemory = runTime.totalMemory();
		usedMemory = totalMemory - freeMemory;

		if (logger.isDebugEnabled()) {
			logger.debug("Total Memory AFTER GC = "+(totalMemory / mb)+" MB"); //$NON-NLS-1$ //$NON-NLS-2$
			logger.debug("Used Memory AFTER GC  = "+(usedMemory / mb)+" MB"); //$NON-NLS-1$ //$NON-NLS-2$
			logger.debug("Free Memory AFTER GC  = "+(freeMemory / mb)+" MB");     //$NON-NLS-1$ //$NON-NLS-2$
			logger.debug("");  				 //$NON-NLS-1$
		}
	}

	private Editor2DFactory factory = null;
	public Editor2DFactory getModelFactory()
	{
		if (factory == null) {
			factory = createModelFactory();
		}
		return factory;
	}

	protected abstract Editor2DFactory createModelFactory();

	private DisposeListener disposeListener = new DisposeListener(){
		public void widgetDisposed(DisposeEvent e) {
			disposeEditor();
		}
	};

	/**
	 * By default empty, subclasses can override this method to configure the ioFilterMan
	 * e.g. set the default fileExtensions for reading and writing files
	 */
	protected void configureIOFilterMan(IOFilterMan ioFilterMan) {
	}

	/**
	 * By default empty, subclasses can override this method to react
	 * before saving with a iofiter e.g. to display a message before
	 *
	 * @param ioFilter the iofilter to use for saving
	 * @return true if perform save or false if not
	 */
	protected boolean reactOnSave(IOFilter ioFilter) {
		return true;
	}
}