package org.nightlabs.jfire.base.admin.ui.timer;

import javax.jdo.JDOHelper;
import javax.security.auth.login.LoginException;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.notification.SelectionManager;
import org.nightlabs.base.ui.selection.SelectionProviderProxy;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.login.part.LSDViewPart;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.notification.NotificationAdapterCallerThread;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;

public class TaskDetailView
extends LSDViewPart
{
	public static final String ID_VIEW = TaskDetailView.class.getName();

	private TaskDetailComposite taskDetailComposite;
	private SelectionProviderProxy selectionProviderProxy = new SelectionProviderProxy();

	@Override
	public void createPartControl(Composite parent)
	{
		super.createPartControl(parent);
		getSite().setSelectionProvider(selectionProviderProxy);
	}

	@Override
	public void createPartContents(Composite parent)
	{
		try {
			Login.getLogin();
		} catch (LoginException e1) {
			throw new RuntimeException(e1);
		}

		this.taskDetailComposite = new TaskDetailComposite(parent);
		SelectionManager.sharedInstance().addNotificationListener(TaskListView.ID_VIEW, Task.class, selectionListener);
		taskDetailComposite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e)
			{
				SelectionManager.sharedInstance().removeNotificationListener(TaskListView.ID_VIEW, Task.class, selectionListener);
			}
		});
//		getSite().setSelectionProvider(taskDetailComposite);
		selectionProviderProxy.addRealSelectionProvider(taskDetailComposite);
	}

	private NotificationListener selectionListener = new NotificationAdapterCallerThread() {
		public void notify(NotificationEvent notificationEvent)
		{
			if (notificationEvent.getSubjects().size() != 1) {
				taskDetailComposite.setTaskID(null);
				return;
			}

			TaskID taskID = (TaskID) JDOHelper.getObjectId(notificationEvent.getFirstSubject());
			taskDetailComposite.setTaskID(taskID);
		}
	};

	@Override
	public void setFocus()
	{
	}

	public TaskDetailComposite getTaskDetailComposite()
	{
		return taskDetailComposite;
	}
}
