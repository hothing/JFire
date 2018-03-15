package org.nightlabs.jfire.base.ui.overview;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.nightlabs.base.ui.action.registry.editor.XEditorActionBarContributor;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.form.AbstractBaseFormPage;
import org.nightlabs.base.ui.message.IErrorMessageDisplayer;
import org.nightlabs.base.ui.message.MessageType;
import org.nightlabs.base.ui.part.ControllablePart;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.login.part.ICloseOnLogoutEditorPart;
import org.nightlabs.jfire.base.ui.login.part.LSDPartController;
import org.nightlabs.jfire.base.ui.resource.Messages;

/**
 * Editor displaying the {@link EntryViewer} of an {@link Entry}.
 * It therefore requires an {@link OverviewEntryEditorInput} as input.
 * Each editor instance will create its own {@link EntryViewer} instance.
 *
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class OverviewEntryEditor
	extends FormEditor
	implements ControllablePart, ICloseOnLogoutEditorPart
{
	private static final Logger logger = Logger.getLogger(OverviewEntryEditor.class);

	public OverviewEntryEditor() {
		super();

		// Register the view at the view-controller
		LSDPartController.sharedInstance().registerPart(this);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
	throws PartInitException
	{
		setSite(site);
		setInput(input);
		if (input instanceof OverviewEntryEditorInput) {
			OverviewEntryEditorInput entryInput = (OverviewEntryEditorInput) input;
			entryViewer = entryInput.getEntry().createEntryViewer();
		}
		getSite().getPage().addPartListener(partListener);
	}

	private EntryViewer entryViewer;
	public EntryViewer getEntryViewer() {
		return entryViewer;
	}

	private Composite composite;
	public void createPartContents(Composite parent)
	{
		addPages();
	}

	@Override
	public void setFocus() {
		if (composite != null && !composite.isDisposed())
			composite.setFocus();
	}

	protected void updateContextMenu()
	{
		EditorActionBarContributor actionBarContributor =
			(EditorActionBarContributor) getEditorSite().getActionBarContributor();
		if (actionBarContributor != null && entryViewer != null) {
			MenuManager menuManager = entryViewer.getMenuManager();
			if (menuManager != null) {
				if (actionBarContributor instanceof XEditorActionBarContributor) {
					XEditorActionBarContributor xEditorActionBarContributor = (XEditorActionBarContributor) actionBarContributor;

					if (xEditorActionBarContributor.getActionRegistry() == null)
						logger.info("updateContextMenu: xEditorActionBarContributor.getActionRegistry() returned null!"); //$NON-NLS-1$
					else {
						xEditorActionBarContributor.getActionRegistry().contributeToContextMenu(menuManager);
						if (logger.isDebugEnabled())
							logger.debug("updateContextMenu, Number of entries = "+menuManager.getItems().length+", actionBarContributor = "+actionBarContributor); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else {
					actionBarContributor.contributeToMenu(menuManager);
				}
			}
		}
	}

	protected void removeContextMenu()
	{
		EditorActionBarContributor actionBarContributor =
			(EditorActionBarContributor) getEditorSite().getActionBarContributor();
		if (actionBarContributor != null && entryViewer != null) {
			MenuManager menuManager = entryViewer.getMenuManager();
			if (menuManager != null) {
				menuManager.removeAll();
				menuManager.updateAll(true);
				if (logger.isDebugEnabled())
					logger.debug("removeContextMenu, Number of entries = "+menuManager.getItems().length+", actionBarContributor = "+actionBarContributor); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	protected void updateToolbar()
	{
		EditorActionBarContributor actionBarContributor =
			(EditorActionBarContributor) getEditorSite().getActionBarContributor();
		if (actionBarContributor != null && entryViewer != null) {
			ToolBarManager toolbarManager = entryViewer.getToolBarManager();
			if (toolbarManager != null) {
				if (actionBarContributor instanceof XEditorActionBarContributor) {
					XEditorActionBarContributor xEditorActionBarContributor = (XEditorActionBarContributor) actionBarContributor;

					if (xEditorActionBarContributor.getActionRegistry() == null)
						logger.info("updateToolbar: xEditorActionBarContributor.getActionRegistry() returned null!"); //$NON-NLS-1$
					else {
						xEditorActionBarContributor.getActionRegistry().contributeToToolBar(toolbarManager);
						if (logger.isDebugEnabled())
							logger.debug("updateToolbar, Number of entries = "+toolbarManager.getItems().length+", actionBarContributor = "+actionBarContributor); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else {
					actionBarContributor.contributeToToolBar(toolbarManager);
				}
			}
		}
	}

	protected void removeToolbar()
	{
		EditorActionBarContributor actionBarContributor =
			(EditorActionBarContributor) getEditorSite().getActionBarContributor();
		if (actionBarContributor != null && entryViewer != null) {
			ToolBarManager toolbarManager = entryViewer.getToolBarManager();
			if (toolbarManager != null) {
				toolbarManager.removeAll();
				toolbarManager.update(true);
				if (logger.isDebugEnabled())
					logger.debug("removeToolbar, Number of entries = "+toolbarManager.getItems().length+", actionBarContributor = "+actionBarContributor); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	private IPartListener partListener = new IPartListener(){
		public void partOpened(IWorkbenchPart part) {
		}
		public void partDeactivated(IWorkbenchPart part) {
			removeContextMenu();
			removeToolbar();
		}
		public void partClosed(IWorkbenchPart part) {
			editorDisposed();
		}
		public void partBroughtToTop(IWorkbenchPart part) {
		}
		public void partActivated(IWorkbenchPart part) {
			updateContextMenu();
			updateToolbar();
		}
	};

	protected void editorDisposed() {
		getSite().getPage().removePartListener(partListener);
		if (entryViewer != null)
			getSite().setSelectionProvider(entryViewer.getSelectionProvider());
	}

	private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener(){
		public void selectionChanged(SelectionChangedEvent event) {
			logger.debug("selection changed "+event.getSelection()); //$NON-NLS-1$
			fireSelectionChanged(event);
		}
	};

	private ListenerList listeners = new ListenerList();
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}
	protected void fireSelectionChanged(SelectionChangedEvent event) {
		for (int i=0; i<listeners.size(); i++) {
			ISelectionChangedListener listener = (ISelectionChangedListener) listeners.getListeners()[i];
			listener.selectionChanged(event);
		}
	}

	/**
	 * Returns the name of the search page.
	 * @return the name of the search page.
	 */
	protected String getSearchPageName()
	{
		if (entryViewer == null)
			return ""; //$NON-NLS-1$

		return entryViewer.getEntry().getEntryFactory().getName();
	}

	@Override
	protected void addPages()
	{
		if (entryViewer != null) {
			FormPage searchPage = new AbstractBaseFormPage(this, Messages.getString("org.nightlabs.jfire.base.ui.overview.OverviewEntryEditor.searchPageTitle"), getSearchPageName()) //$NON-NLS-1$
			{
				@Override
				protected void createFormContent(IManagedForm managedForm)
				{
					final ScrolledForm form = managedForm.getForm();
					Composite formBody = form.getBody();
					formBody.setLayout(XComposite.getLayout(LayoutMode.ORDINARY_WRAPPER));
					composite = entryViewer.createComposite(form.getBody());
					GridData resultData = new GridData(SWT.FILL, SWT.FILL, true, true);
					composite.setLayoutData(resultData);
					entryViewer.setErrorMessageDisplayer(new IErrorMessageDisplayer()
					{
						@Override
						public void setErrorMessage(String errorMessage)
						{
							setMessage(errorMessage, IMessageProvider.ERROR);
						}

						@Override
						public void setMessage(String message, MessageType type)
						{
							setMessage(message, type.ordinal());
						}

						@Override
						public void setMessage(String message, int type)
						{
							form.setMessage(message, type);
						}
					});

					if (entryViewer.getSelectionProvider() != null) {
						getSite().setSelectionProvider(entryViewer.getSelectionProvider());
						entryViewer.getSelectionProvider().addSelectionChangedListener(selectionChangedListener);
					}

					if (getEditorSite().getActionBarContributor() != null &&
						getEditorSite().getActionBarContributor() instanceof XEditorActionBarContributor)
					{
						XEditorActionBarContributor editorActionBarContributor =
							(XEditorActionBarContributor) getEditorSite().getActionBarContributor();
						addSelectionChangedListener(editorActionBarContributor);
					}

					updateContextMenu();
					updateToolbar();
				}

				@Override
				protected boolean includeFixForVerticalScrolling()
				{
					return true;
				}
			};

			try
			{
				addPage(searchPage);
			}
			catch (PartInitException e)
			{
				throw new RuntimeException("Couldn't initialise searchFormPage!", e); //$NON-NLS-1$
			}
		}

    // Ensures that this editor will only display the page's tab
    // area if there are more than one page
    //
    getContainer().addControlListener
      (new ControlAdapter()
       {
        boolean guard = false;
        @Override
        public void controlResized(ControlEvent event)
        {
          if (!guard)
          {
            guard = true;
            hideTabs();
            guard = false;
          }
        }
       });
	}

	@Override
	public boolean canDisplayPart()
	{
		return Login.isLoggedIn();
	}

  /**
   * If there is just one page in the multi-page editor part,
   * this hides the single tab at the bottom.
   */
  protected void hideTabs()
  {
    if (getPageCount() <= 1)
    {
      setPageText(0, ""); //$NON-NLS-1$
      if (getContainer() instanceof CTabFolder)
      {
        ((CTabFolder)getContainer()).setTabHeight(1);
        Point point = getContainer().getSize();
        getContainer().setSize(point.x, point.y + 6);
      }
    }
  }

  /**
   * If there is more than one page in the multi-page editor part,
   * this shows the tabs at the bottom.
   */
  protected void showTabs()
  {
    if (getPageCount() > 1)
    {
      setPageText(0, getSearchPageName());
      if (getContainer() instanceof CTabFolder)
      {
        ((CTabFolder)getContainer()).setTabHeight(SWT.DEFAULT);
        Point point = getContainer().getSize();
        getContainer().setSize(point.x, point.y - 6);
      }
    }
  }
}
