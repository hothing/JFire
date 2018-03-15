/* *****************************************************************************
 * org.nightlabs.base.ui - NightLabs Eclipse utilities                            *
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
package org.nightlabs.base.ui.entity.editor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.composite.Fadeable;
import org.nightlabs.base.ui.composite.FadeableComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.editor.IFormPartDirtyStateProxy;
import org.nightlabs.base.ui.editor.IFormPartDirtyStateProxyListener;
import org.nightlabs.base.ui.editor.RestorableSectionPart;
import org.nightlabs.base.ui.editor.UndirtyBehaviour;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.progress.CompoundProgressMonitor;
import org.nightlabs.base.ui.progress.ProgressMonitorWrapper;
import org.nightlabs.base.ui.progress.SaveProgressMonitorPart;
import org.nightlabs.base.ui.resource.Messages;
import org.nightlabs.base.ui.toolkit.IToolkit;
import org.nightlabs.progress.ProgressMonitor;

/**
 * <p>An editor page to be used when you need to load data (with the editors controller)
 * in the background and want to provide progress feedback to the user</p>
 *
 * <p>The page hooks a Composite with a stack layout into its parent Form.
 * One entry in the stack will be an implementation of {@link IProgressMonitor}
 * the other the page's actual content. You can switch the vision
 * by {@link #switchToContent()} and {@link #switchToProgress()}.</p>
 *
 *
 * <p>On the creation of its contents ({@link #createFormContent(IManagedForm)})
 * this FormPage will start a job that tries to access the
 * {@link IEntityEditorPageController} associated to this page. It will therefore
 * assume that the page is embedded in an {@link EntityEditor} and a page
 * controller was created by the {@link IEntityEditorPageFactory} of this
 * page. <br/>
 * Within the job the page will load the controllers data. if a controller was found.
 * Then the {@link #handleControllerObjectModified(EntityEditorPageControllerModifyEvent)}
 * method will be invoked (still on the jobs thread) by the page-controller listener
 * mechanism, to let implementations react on to the loading. Implementations could fill the gui with
 * the obtained data, for example. The callback is on the jobs thread, however,
 * in order to enable subclasses to extend the background job with own tasks.
 * Implementors should also not forget to switch to the content view when
 * the loading is finished.</p>
 *
 * <p>Also on creation of its contents this FormPage will call several
 * methods that are intended to override or obliged to implement.
 * These methods let subclasses create the page's actual content and
 * configure the wrapping elements of this page. <br/>
 * Note, that its recommended when using this class to use parts ({@link Section}s)
 * that do not access the controller when they are created, but in contrast
 * have the ability to be "filled" with data from the {@link #asyncCallback()}.
 * </p>
 *
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public abstract class EntityEditorPageWithProgress extends FormPage implements Fadeable, IEntityEditorPage
{
	/**
	 * Wrapper that holds the stack layout.
	 */
	private FadeableComposite wrapper;
	/**
	 * Wrapper for the page's real content
	 */
//	protected ScrolledForm pageWrapper;
	protected Composite pageWrapper;
	/**
	 * Wrapper for the progress monitor
	 */
	protected XComposite progressWrapper;
	/**
	 * the progress monitor (used implementation {@link SaveProgressMonitorPart}).
	 */
	protected IProgressMonitor progressMonitorPart;
	/**
	 * The stack layout to switch views.
	 */
	private StackLayout stackLayout;

	private IFormPartDirtyStateProxyListener dirtyStateProxyListener = new IFormPartDirtyStateProxyListener() {
		private Set<IFormPart> dirtyParts = null;
		@Override
		public void markDiry(IFormPart formPart) {
			if(UndirtyBehaviour.ENABLED) {
				if(dirtyParts == null)
					dirtyParts = new HashSet<IFormPart>();
				dirtyParts.add(formPart);
			}
			getPageController().markDirty();
		}
		@Override
		public void markUndirty(IFormPart formPart) {
			if(UndirtyBehaviour.ENABLED) {
				if(dirtyParts != null) {
					dirtyParts.remove(formPart);
					if(dirtyParts.isEmpty())
						dirtyParts = null;
				}
				// Don't call markUndirty here as this would mark the complete Controller undirty and
				// this might not be true as only some aspect was set dirty.
				// Later we could manage the dirty-state in the controller per part also than
				// we can delegate the markUndirty to the controller as well.
				if(dirtyParts == null)
					getPageController().markUndirty();
			}
		}
	};

	/**
	 * Create a new editor page with progress.
	 *
	 * @param editor The page's editor.
	 * @param id The page's id.
	 * @param name The page's name.
	 */
	public EntityEditorPageWithProgress(FormEditor editor, String id, String name) {
		super(editor, id, name);
	}

	/**
	 * The job that loads with the help of this page's controller.
	 * If the controller is an instance of {@link EntityEditorPageController}
	 * the job will use its special method to join the background-loading-job that
	 * might already run.
	 * After loading the job will call {@link #asyncCallback()} to notify
	 * the page of its end.
	 * <p>
	 * Note that this Job also registered listeners to the controller that will
	 * cause {@link #handleControllerObjectModified(EntityEditorPageControllerModifyEvent)}
	 * to be invoked, when the listener gets notified.
	 * </p>
	 */
	private Job asyncLoadJob = new Job(Messages.getString("org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress.loadJob.name")) { //$NON-NLS-1$
		@Override
		protected IStatus run(ProgressMonitor monitor) {
			final IEntityEditorPageController controller = getPageController();
			if (controller != null && !controller.isLoaded()) { // added ' && !controller.isLoaded()' because it otherwise forgets local changes when switching pages in a scenario where multiple pages share the same controller. Marco, 2009-10-28.

				CompoundProgressMonitor compoundMonitor = new CompoundProgressMonitor(new ProgressMonitorWrapper(progressMonitorPart), monitor);
				if (controller instanceof EntityEditorPageController) {
					((EntityEditorPageController)controller).load(compoundMonitor);
				} // (controller instanceof EntityEditorPageController)
				else
					controller.doLoad(compoundMonitor);
			}
			return Status.OK_STATUS;
		}
	};

	/**
	 * This method is invoked when the {@link IEntityEditorPageController} associated to this page
	 * notifies that one of its objects has changed. This happens when the controller (re-)loaded
	 * or saved its model. This method is invoked on the thread that caused the notification
	 * so it is normally called on a non-ui thread, so please remember that ui
	 * operations have to be done on the {@link Display} thread.
	 * <p>
	 * The default implementation passes the new object
	 * (from {@link EntityEditorPageControllerModifyEvent#getNewObject()})
	 * to the managed-form as input (via {@link IManagedForm#setInput(Object)}).
	 * The default behaviour of an {@link IManagedForm} is to delegate the input-object
	 * to its form parts via {@link IFormPart#setFormInput(Object)}.
	 * </p>
	 * <p>
	 * It's recommended to subclass {@link RestorableSectionPart} which already behaves
	 * correctly in its {@link RestorableSectionPart#setFormInput(Object)}-implementation
	 * for most use-cases.
	 * </p>
	 * <p>
	 * After setting the managed-form's input, the default implementation of <code>handleControllerObjectModified(...)</code>
	 * calls {@link IManagedForm#refresh()}. For all stale form-parts (i.e. sections), this will
	 * result in a call to {@link IFormPart#refresh()} - load the data of your input into your
	 * UI elements, there.
	 * </p>
	 *
	 * @param modifyEvent The event fired by the associated controller.
	 *
	 * @see IManagedForm#setInput(Object)
	 * @see RestorableSectionPart#setFormInput(Object)
	 * @see RestorableSectionPart#refresh()
	 * @see RestorableSectionPart#commit(boolean)
	 */
	protected void handleControllerObjectModified(final EntityEditorPageControllerModifyEvent modifyEvent) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (getManagedForm().getForm().isDisposed())
					return;

				getManagedForm().setInput(modifyEvent.getNewObject());
				getManagedForm().refresh();
			}
		});
	}


	/**
	 * Get the {@link IEntityEditorPageController} for this page.
	 * Assumes the editor of this page is an instance of
	 * {@link EntityEditor}.
	 *
	 * Note that page controllers should not be accessed from their associated
	 * pages in their constructor, as the controller registration
	 * will be initialized immediately after the page was created.
	 *
	 * @return The page controller for this page.
	 */
	public IEntityEditorPageController getPageController() {
		return ((EntityEditor)getEditor()).getController().getPageController(this);
	}

	/**
	 * {@inheritDoc}
	 *
	 * This implementation will create a stack layout an put a progress monitor
	 * and the real content wrapped into a new Form on the stack.
	 * Besides {@link #addSections(Composite)}, where subclasses add their real content,
	 * several callback methods exist to configure the rest of the page.
	 *
	 * @see #addSections(Composite)
	 * @see #createProgressMonitorPart(Composite)
	 * @see #configureBody(Composite)
	 * @see #configureProgressWrapper(XComposite)
	 * @see #configurePageWrapper(Composite)	 *
	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm)
	{
		ScrolledForm form = managedForm.getForm();
		form.setExpandHorizontal(true);
		form.setExpandVertical(true);
		IToolkit toolkit = (IToolkit) getEditor().getToolkit(); // CommitableFormEditor uses NightlabsFormToolkit
		String formText = getPageFormTitle();
//		form.setText(formText == null ? "" : formText);  //$NON-NLS-1$
		form.setText(formText); // passing null causes the title to be completely hidden (no space allocated) - thus we must not convert null to an empty string. marco.
		fillBody(managedForm, toolkit);
//		getEditor().getToolkit().decorateFormHeading(form.getForm());
	}

	/**
	 * This method is called when the page's body is created.
	 * Add the page's sections here. Remember not to
	 * access the controllers data here, but provide the possibility
	 * to assign the data to the part created at a later time.
	 *
	 * @param parent The parent given here is a new Form created for the page and configured by {@link #configurePageWrapper(Composite)}.
	 */
	protected abstract void addSections(Composite parent);

	/**
	 * Return the title of this page's main form
	 * @return the title of this page's main form
	 */
	protected abstract String getPageFormTitle();

	/**
	 * Returns the progress monitor of this page.
	 * The progress monitor is created by {@link #createProgressMonitorPart(Composite)}
	 * so this getter is not intended to be overridden.
	 *
	 * @return the progress monitor of this page.
	 */
	public IProgressMonitor getProgressMonitor() {
		return progressMonitorPart;
	}

	/**
	 * Configure (the layout of) the composite wrapping the progress monitor.
	 *
	 * @param pWrapper The composite wrapping the progress monitor.
	 */
	protected void configureProgressWrapper(XComposite pWrapper) {
		pWrapper.getGridLayout().marginWidth = 20;
		pWrapper.getGridLayout().marginHeight = 20;
	}

	/**
	 * Create the gui representation of an {@link IProgressMonitor}
	 * to the given parent. This method is intended to be overridden,
	 * the default implementation will use an {@link SaveProgressMonitorPart}.
	 *
	 * @param progressWrapper The parent of the progress monitor.
	 * @return A new gui representation of an {@link IProgressMonitor}.
	 */
	protected IProgressMonitor createProgressMonitorPart(Composite progressWrapper) {
		SaveProgressMonitorPart monitor = new SaveProgressMonitorPart(progressWrapper, new GridLayout());
		GridData gridData = new GridData(300, 150);
		monitor.setLayoutData(gridData);
		return monitor;
	}

	/**
	 * Configure the (layout of the) Composite wrapping the page's
	 * real content.
	 * The default implementation will assign a {@link GridLayout}
	 * with moderate indenting.
	 *
	 * @param pageWrapper The Composite wrapping the page's
	 * real content.
	 */
	protected void configurePageWrapper(Composite pageWrapper) {
		GridLayout layout = new GridLayout();
		layout.marginBottom = 10;
		layout.marginTop = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.numColumns = 1;
		layout.horizontalSpacing = 10;
		pageWrapper.setLayout(layout);
	}

	/**
	 * Configure (the layout of) this page's body (it's form's body).
	 * The default implementation will assign a
	 * {@link GridLayout} with zero margins and spacing.
	 *
	 * @param body This page's body.
	 */
	protected void configureBody(Composite body) {
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		body.setLayout(layout);
	}

	/**
	 * Configure the (root) form of this page.
	 * The default implementation does nothing.
	 *
	 * @param form The form to configure
	 */
	protected void configureForm(ScrolledForm form) {

	}

	/**
	 * Configure what view should be visible initally, progress or content.
	 * Default implementation will set progress.
	 */
	protected void configureInitialStack() {
		stackLayout.topControl = progressWrapper;
	}

	protected void registerToDirtyStateProxies() {
		if (getManagedForm() != null) {
			// check for dirty state proxies and register
			IFormPart[] parts = getManagedForm().getParts();
			for (IFormPart formPart : parts) {
				if (formPart instanceof IFormPartDirtyStateProxy) {
					((IFormPartDirtyStateProxy) formPart).addFormPartDirtyStateProxyListener(dirtyStateProxyListener);
				}
			}
		}
	}

	protected Composite createPageWrapper(Composite parent)
	{
		return new Composite(parent, SWT.NONE);
	}

	/**
	 * Creates stack layout, progress-stack-item and the page's content
	 * with the help of the configure and create callbacks.
	 *
	 * @param managedForm The managed form
	 * @param toolkit The tookit to use
	 */
	private void fillBody(IManagedForm managedForm, IToolkit toolkit)
	{
		Composite body = managedForm.getForm().getBody();
		configureForm(managedForm.getForm());
		configureBody(body);

		wrapper = new FadeableComposite(body, SWT.NONE, XComposite.LayoutMode.TIGHT_WRAPPER);
		wrapper.setToolkit(toolkit);

		stackLayout = new StackLayout();
		stackLayout.marginHeight = 0;
		stackLayout.marginWidth = 0;
		wrapper.setLayout(stackLayout);

		// WORKAROUND: this is a workaround for growing tables in FromPages.
		// more information about this can be found at: https://bugs.eclipse.org/bugs/show_bug.cgi?id=215997#c4
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 1;
		if (includeFixForVerticalScrolling())
		{
			gd.heightHint = 1;
		}
		wrapper.setLayoutData(gd);

		progressWrapper = new XComposite(wrapper, SWT.NONE);
		configureProgressWrapper(progressWrapper);
		progressMonitorPart = createProgressMonitorPart(progressWrapper);
		progressWrapper.adaptToToolkit();

//		pageWrapper = new Composite(wrapper, SWT.NONE);
		pageWrapper = createPageWrapper(wrapper);

		configurePageWrapper(pageWrapper);

		asyncLoadJob.schedule();

		addSections(pageWrapper);
		registerToDirtyStateProxies();
		configureInitialStack();

		// this will notify immediately, in case there was already an event.
		getPageController().addModifyListener(new IEntityEditorPageControllerModifyListener() {
			public void controllerObjectModified(EntityEditorPageControllerModifyEvent modifyEvent) {
				switchToContent();
				handleControllerObjectModified(modifyEvent);
			}
		});
	}

	/**
	 * Indicates whether the fix for vertically growing pages should be applied. As a side-effect,
	 * this fix will prohibit vertical scroll bars and, hence should only be used if really necessary.
	 * @return <code>true</code> if vertically growing tables shall be prevented (and no vertical
	 * 	scrollbars shall be shown), <code>false</code> otherwise.
	 */
	protected boolean includeFixForVerticalScrolling()
	{
		return true;
	}

	/**
	 * Switch to view the progress monitor.
	 * Note that this will always be called asynchronously on the {@link Display} thread.
	 */
	public void switchToProgress() {
		Runnable runnable = new Runnable() {
			public void run() {
				stackLayout.topControl = progressWrapper;
				wrapper.layout(true, true);
			}
		};

		if (Display.getCurrent() != null)
			runnable.run();
		else
			Display.getDefault().syncExec(runnable);
	}

	/**
	 * Switch to view the page's content.
	 * Note, that this will always on the {@link Display} thread, but synchronously (blocking).
	 * Hence, you can call it from any thread you want.
	 */
	public void switchToContent() {
		Runnable runnable = new Runnable() {
			public void run() {
				if (wrapper == null || wrapper.isDisposed())
					return;

				if (stackLayout.topControl == pageWrapper)
					return;

				stackLayout.topControl = pageWrapper;
				wrapper.layout(true, true);
//				pageWrapper.reflow(true);
				pageWrapper.layout(true, true);
				getManagedForm().getForm().redraw();
				getManagedForm().getForm().getBody().layout(true, true);
//				pageWrapper.refresh();
			}
		};

		if (Display.getCurrent() != null)
			runnable.run();
		else
			Display.getDefault().syncExec(runnable);
	}

	/**
	 * {@inheritDoc}
	 * Will delegate to the fadable wrapper.
	 *
	 * @see org.nightlabs.base.ui.composite.Fadeable#setFaded(boolean)
	 */
	public void setFaded(boolean faded) {
		wrapper.setFaded(faded);
	}

	public boolean isDisposed()
	{
		return wrapper.isDisposed();
	}

	public void setMenu(Menu menu) {
		wrapper.getParent().setMenu(menu);
	}
}
