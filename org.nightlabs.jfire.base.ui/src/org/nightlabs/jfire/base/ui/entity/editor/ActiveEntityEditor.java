package org.nightlabs.jfire.base.ui.entity.editor;

import java.util.Collection;

import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.notification.NotificationAdapterJob;
import org.nightlabs.base.ui.part.PartAdapter;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.ui.login.part.ICloseOnLogoutEditorPart;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.progress.ProgressMonitor;

public abstract class ActiveEntityEditor
extends EntityEditor
implements ICloseOnLogoutEditorPart
{
	public ActiveEntityEditor()
	{
		workbenchPage = RCPUtil.getActiveWorkbenchPage();
		workbenchPage.addPartListener(partListenerEditorClosed);
	}

	private IPartListener2 partListenerEditorClosed = new PartAdapter() {
		@Override
		public void partClosed(IWorkbenchPartReference reference) {
			if (workbenchPage == null) // Should never happen that this listener is triggered twice, but better safe than sorry.
				return;

			IWorkbenchPart part = reference.getPart(false);
			if (ActiveEntityEditor.this != part)
				return;

			removeChangeListener();
			workbenchPage.removePartListener(partListenerEditorClosed);
			partListenerEditorClosed = null;
			workbenchPage = null;
		}
	};

	private IWorkbenchPage workbenchPage;
	private NotificationListener changeListener = null;
	private Class<?> entityClass = null;
	private Object entityObjectID = null;

	private synchronized void addChangeListener(Object entity)
	{
		removeChangeListener();

		if (entity == null)
			return;

		changeListener = new NotificationAdapterJob() {
			@Override
			public void notify(NotificationEvent event) {
				Object eoid = entityObjectID;
				if (eoid == null)
					return;

				@SuppressWarnings("unchecked")
				Collection<? extends DirtyObjectID> c = event.getSubjects();

				for (DirtyObjectID dirtyObjectID : c) {
					if (eoid.equals(dirtyObjectID.getObjectID())) {
						scheduleLoadTitleJob();
						break;
					}
				}
			}
		};
		entityClass = entity.getClass();
		entityObjectID = JDOHelper.getObjectId(entity);
		if (entityObjectID == null)
			throw new IllegalStateException("Entity has no object-id assigned! " + entity); //$NON-NLS-1$

		JDOLifecycleManager.sharedInstance().addNotificationListener(
				entityClass, changeListener
		);
	}

	private synchronized void removeChangeListener()
	{
		if (entityClass == null)
			return;

		JDOLifecycleManager.sharedInstance().removeNotificationListener(
				entityClass, changeListener
		);

		entityObjectID = null;
		changeListener = null;
		entityClass = null;
	}

	@Override
	public String getTitle() {
		if (entityTitle != null)
			return entityTitle;

		if(getEditorInput() == null)
			return super.getTitle();

		if (loadTitleJob == null)
			scheduleLoadTitleJob();

		return super.getTitle();
	}

	@Override
	public String getTitleToolTip() {
		if (entityTooltip != null)
			return entityTooltip;

		if(getEditorInput() == null)
			return super.getTitle();

		if (loadTitleJob == null)
			scheduleLoadTitleJob();

		return super.getTitleToolTip();
	}

	private String entityTitle = null;
	private String entityTooltip = null;

	private volatile Job loadTitleJob = null;
	private void scheduleLoadTitleJob()
	{
		Job job = new Job(Messages.getString("org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditor.job.loadEntity")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				final Job thisJob = this;
				if (loadTitleJob != thisJob)
					return Status.CANCEL_STATUS;

				Object entity = retrieveEntityForEditorTitle(monitor);
				addChangeListener(entity);
				if (entity != null) {
					final String title = getEditorTitleFromEntity(entity);
					final String tooltip = getEditorTooltipFromEntity(entity);
					if (title != null || tooltip != null) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								if (loadTitleJob != thisJob)
									return;

								entityTitle = title;
								entityTooltip = tooltip;
								if (title != null)
									setPartName(title);
								if (tooltip != null)
									setTitleToolTip(tooltip);
								loadTitleJob = null;
							}
						});
					}
				}

				return Status.OK_STATUS;
			}
		};
		job.setRule(schedulingRule);
		this.loadTitleJob = job;
		job.schedule();
	}

	private ISchedulingRule schedulingRule = new ISchedulingRule() {
		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	};

	/**
	 * Get the title for this editor from the {@link #retrieveEntityForEditorTitle(ProgressMonitor) previously retrieved} entity.
	 * If this method returns <code>null</code>, the editor-title is not changed.
	 * <p>
	 * Override this method, as well as {@link #retrieveEntityForEditorTitle(ProgressMonitor)}, to
	 * determine a nice editor-title.
	 * </p>
	 *
	 * @param entity the object that was previously returned by {@link #retrieveEntityForEditorTitle(ProgressMonitor)}; never <code>null</code>.
	 * @return <code>null</code> or the title for this editor.
	 * @see #retrieveEntityForEditorTitle(ProgressMonitor)
	 */
	protected abstract String getEditorTitleFromEntity(Object entity);

	/**
	 * Get the tooltip for this editor from the {@link #retrieveEntityForEditorTitle(ProgressMonitor) previously retrieved} entity.
	 * If this method returns <code>null</code>, the editor-tooltip is not changed.
	 * <p>
	 * This implementation of {@link #getEditorTooltipFromEntity(Object)} returns <code>null</code> meaning that the
	 * tooltip retrieved from the editors input is set by default.
	 * </p>
	 * <p>
	 * To change the default tooltip override this method and return a custom tooltip.
	 * </p>
	 *
	 * @param entity the object that was previously returned by {@link #retrieveEntityForEditorTitle(ProgressMonitor)}; never <code>null</code>.
	 * @return <code>null</code> or the title for this editor.
	 * @see #retrieveEntityForEditorTitle(ProgressMonitor)
	 */
	protected String getEditorTooltipFromEntity(Object entity) {
		return null;
	}

	/**
	 * Obtain the data that is needed for displaying the correct editor title.
	 * This is usually the entity for which this editor is opened, detached with minimal
	 * fetch-groups (only those necessary for determining the title).
	 * <p>
	 * If this method returns <code>null</code>, no further action is done by the framework.
	 * Otherwise, if the returned object is a JDO-object, the framework registers a listener
	 * on it and re-triggers this method whenever the object changed. Furthermore, the method
	 * {@link #getEditorTitleFromEntity(Object)} is called for every non-<code>null</code> result.
	 * </p>
	 * <p>
	 * Override this method, as well as {@link #getEditorTitleFromEntity(Object)}, to
	 * determine a nice editor-title.
	 * </p>
	 *
	 * @param monitor a {@link ProgressMonitor} for progress feedback.
	 * @return <code>null</code> or the entity from which the title will be obtained in the
	 *		{@link #getEditorTitleFromEntity(Object)} method.
	 * @see #getEditorTitleFromEntity(Object)
	 */
	protected abstract Object retrieveEntityForEditorTitle(ProgressMonitor monitor);

}
