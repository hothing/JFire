package org.nightlabs.jfire.base.admin.ui.timer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.notification.NotificationAdapterJob;
import org.nightlabs.base.ui.notification.NotificationListenerJob;
import org.nightlabs.base.ui.notification.SelectionManager;
import org.nightlabs.base.ui.progress.ProgressMonitorWrapper;
import org.nightlabs.base.ui.table.AbstractInvertableTableSorter;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.base.ui.table.TableSortSelectionListener;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.jdo.notification.ChangeEvent;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.dao.TaskDAO;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.l10n.DateFormatter;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.util.NLLocale;

public class TaskListComposite
		extends AbstractTableComposite<Task>
{
	protected static class TaskListContentProvider
			extends ArrayContentProvider
	{
	}

	protected static class TaskListLabelProvider
			extends TableLabelProvider
	{
		@Override
		public Image getColumnImage(Object element, int columnIndex)
		{
			switch (columnIndex) {
			case 7: // enabled/disabled
			case 9: // wait (no activeExecID) / queued (existing activeExecID, but not
							// executing) / executing
			default:
				return null;
			}
		}

		public String getColumnText(Object element, int columnIndex)
		{
			Task task = (Task) element;
			switch (columnIndex) {
			case 0:
				return task.getTaskTypeID();
			case 1:
				return task.getUser().getName() != null ? task.getUser().getName() : task.getUser().getUserID();
			case 2:
				return task.getName().getText(NLLocale.getDefault().getLanguage());
			case 3:
				return task.getDescription().getText(NLLocale.getDefault().getLanguage());
			case 4:
				return task.getLastExecDT() == null ? "" : DateFormatter //$NON-NLS-1$
						.formatDateShortTimeHMS(task.getLastExecDT(), true);
			case 5:
				return String.valueOf(task.getLastExecDurationMSec());
			case 6:
				if (task.isLastExecFailed() && task.getLastExecMessage() == null)
					return Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.TaskListLabelProvider.lastExecFailedWithoutMessage"); //$NON-NLS-1$

				return task.getLastExecMessage() == null ? "" : task //$NON-NLS-1$
						.getLastExecMessage();
			case 7:
				return task.isEnabled() ? Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.TaskListLabelProvider.enabled") : Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.TaskListLabelProvider.disabled"); //$NON-NLS-1$ //$NON-NLS-2$
			case 8:
				if (task.getNextCalculateNextExecDT() != null)
					return String.format(
							Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.TaskListLabelProvider.moreThanCalculateNextExecDTFutureYears"), //$NON-NLS-1$
							new Object[] { new Integer(Task.CALCULATE_NEXT_EXEC_DT_FUTURE_YEARS) });

				return task.getNextExecDT() == null ? "" : DateFormatter //$NON-NLS-1$
						.formatDateShortTimeHM(task.getNextExecDT(), true);
			case 9:
				if (task.isExecuting())
					return Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.TaskListLabelProvider.executing"); //$NON-NLS-1$

				return task.getActiveExecID() == null ? Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.TaskListLabelProvider.sleeping") : Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.TaskListLabelProvider.queued"); //$NON-NLS-1$ //$NON-NLS-2$
			default:
				return ""; //$NON-NLS-1$
			}
		}
	}

	public TaskListComposite(Composite parent, int style)
	{
		super(parent, style);
		try {
			Login.getLogin(); // force login to have classes available
		} catch (LoginException e) {
			// ignore
		}

		JDOLifecycleManager.sharedInstance().addNotificationListener(Task.class, taskChangedListener);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0)
			{
				JDOLifecycleManager.sharedInstance().removeNotificationListener(Task.class, taskChangedListener);
			}
		});

		getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event)
			{
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();

				Set<Task> _selectedTasks = new HashSet<Task>();
				for (Iterator<Task> it = sel.iterator(); it.hasNext(); ) {
					_selectedTasks.add(it.next());
				}
				selectedTasks = _selectedTasks;

				ArrayList<Class<Task>> classes = new ArrayList<Class<Task>>();
				classes.add(Task.class);
				SelectionManager.sharedInstance().notify(
						new ChangeEvent(TaskListComposite.this, getSelectionZone(), _selectedTasks, classes));
			}
		});
	}

	private Set<Task> selectedTasks;

	public Set<Task> getSelectedTasks()
	{
		return selectedTasks;
	}

	private NotificationListenerJob taskChangedListener = new NotificationAdapterJob(
			Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.taskChangedListener.name")) //$NON-NLS-1$
	{
		public void notify(NotificationEvent notificationEvent)
		{
			if (taskSet == null)
				return;

			for (Iterator<DirtyObjectID> it = notificationEvent.getSubjects().iterator(); it.hasNext();) {
				DirtyObjectID dirtyObjectID = it.next();
				TaskID taskID = (TaskID) dirtyObjectID.getObjectID();
//				Task task = TaskProvider.sharedInstance().getTask(taskID,
//						FETCH_GROUPS_TASKS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
				Task task = TaskDAO.sharedInstance().getTask(taskID, FETCH_GROUPS_TASKS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, getProgressMonitor());
				if (taskSet.contains(task)) {
					int idx = tasks.indexOf(task);
					if (idx >= 0) {
						tasks.remove(idx);
						tasks.add(idx, task);
					}
				}
			}

			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					if (isDisposed())
						return;

					getTableViewer().refresh();
				}
			});
		}
	};

	protected static class TaskViewerSorter_TaskType
			extends AbstractInvertableTableSorter<Task>
	{
		@Override
		protected int _compare(Viewer viewer, Task t1, Task t2)
		{
//			int res = getCollator().compare(t1.getTaskTypeID(), t2.getTaskTypeID());
			int res = getComparator().compare(t1.getTaskTypeID(), t2.getTaskTypeID());

			if (res == 0) { // sort additionally by name - that's nicer as most tasks
										// probably have the same type
				String u1Name = t1.getUser().getName() != null ? t1.getUser().getName() : ""; //$NON-NLS-1$
				String u2Name = t2.getUser().getName() != null ? t2.getUser().getName() : ""; //$NON-NLS-1$
				return getComparator().compare(u1Name, u2Name);
//				return getCollator().compare(u1Name, u2Name);
			}

			return res;
		}
	}

	protected static class TaskViewerSorter_UserName
			extends AbstractInvertableTableSorter<Task>
	{
		@Override
		protected int _compare(Viewer viewer, Task t1, Task t2)
		{
//			return getCollator().compare(t1.getUser().getName(),
//					t2.getUser().getName());
			return getComparator().compare(t1.getUser().getName(),
					t2.getUser().getName());
		}
	}

	protected static class TaskViewerSorter_TaskName
			extends AbstractInvertableTableSorter<Task>
	{
		@Override
		protected int _compare(Viewer viewer, Task t1, Task t2)
		{
//			return getCollator().compare(
//					t1.getName().getText(NLLocale.getDefault().getLanguage()),
//					t2.getName().getText(NLLocale.getDefault().getLanguage()));
			return getComparator().compare(
					t1.getName().getText(NLLocale.getDefault().getLanguage()),
					t2.getName().getText(NLLocale.getDefault().getLanguage()));
		}
	}

	protected static class TaskViewerSorter_TaskDescription
			extends AbstractInvertableTableSorter<Task>
	{
		@Override
		protected int _compare(Viewer viewer, Task t1, Task t2)
		{
//			return getCollator().compare(
//					t1.getDescription().getText(NLLocale.getDefault().getLanguage()),
//					t2.getDescription().getText(NLLocale.getDefault().getLanguage()));
			return getComparator().compare(
					t1.getDescription().getText(NLLocale.getDefault().getLanguage()),
					t2.getDescription().getText(NLLocale.getDefault().getLanguage()));
		}
	}

	protected static class TaskViewerSorter_LastExecDT
			extends AbstractInvertableTableSorter<Task>
	{
		@Override
		protected int _compare(Viewer viewer, Task t1, Task t2)
		{
			if (t1.getLastExecDT() == null)
				return t2.getLastExecDT() == null ? 0 : -1;

			if (t2.getLastExecDT() == null)
				return 1;

			return t1.getLastExecDT().compareTo(t2.getLastExecDT());
		}
	}

	protected static class TaskViewerSorter_Duration
			extends AbstractInvertableTableSorter<Task>
	{
		@Override
		protected int _compare(Viewer viewer, Task t1, Task t2)
		{
			long d1 = t1.getLastExecDurationMSec();
			long d2 = t2.getLastExecDurationMSec();

			return (d1 < d2 ? -1 : (d1 == d2 ? 0 : 1));
		}
	}

	protected static class TaskViewerSorter_LastExecMessage
			extends AbstractInvertableTableSorter<Task>
	{
		@Override
		protected int _compare(Viewer viewer, Task t1, Task t2)
		{
//			return getCollator().compare(
//					t1.getLastExecMessage() == null ? "" : t1.getLastExecMessage(), //$NON-NLS-1$
//					t2.getLastExecMessage() == null ? "" : t2.getLastExecMessage()); //$NON-NLS-1$
			return getComparator().compare(
					t1.getLastExecMessage() == null ? "" : t1.getLastExecMessage(), //$NON-NLS-1$
					t2.getLastExecMessage() == null ? "" : t2.getLastExecMessage()); //$NON-NLS-1$
		}
	}

	protected static class TaskViewerSorter_Enabled
			extends AbstractInvertableTableSorter<Task>
	{
		@Override
		protected int _compare(Viewer viewer, Task t1, Task t2)
		{
			int v1 = t1.isEnabled() ? 1 : 0;
			int v2 = t2.isEnabled() ? 1 : 0;

			return (v1 < v2 ? -1 : (v1 == v2 ? 0 : 1));
		}
	}

	protected static class TaskViewerSorter_NextExecDT
			extends AbstractInvertableTableSorter<Task>
	{
		@Override
		protected int _compare(Viewer viewer, Task t1, Task t2)
		{
			if (t1.getNextExecDT() == null)
				return t2.getNextExecDT() == null ? 0 : -1;

			if (t2.getNextExecDT() == null)
				return 1;

			return t1.getNextExecDT().compareTo(t2.getNextExecDT());
		}
	}

	protected static class TaskViewerSorter_Status
			extends AbstractInvertableTableSorter<Task>
	{
		private int getStatusInt(Task task)
		{
			if (task.isExecuting())
				return 2;

			return task.getActiveExecID() == null ? 0 : 1;
		}

		@Override
		protected int _compare(Viewer viewer, Task t1, Task t2)
		{
			int v1 = getStatusInt(t1);
			int v2 = getStatusInt(t2);

			return (v1 < v2 ? -1 : (v1 == v2 ? 0 : 1));
		}
	}

	@Override
	protected void createTableColumns(TableViewer tableViewer, Table table)
	{
		TableColumn col;
		col = new TableColumn(table, SWT.LEFT);
		col.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.typeTableColumn.text")); //$NON-NLS-1$
		TableSortSelectionListener tsslTaskType = new TableSortSelectionListener(
				tableViewer, col, new TaskViewerSorter_TaskType(), SWT.UP);

		col = new TableColumn(table, SWT.LEFT);
		col.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.userTableColumn.text")); //$NON-NLS-1$
		new TableSortSelectionListener(tableViewer, col,
				new TaskViewerSorter_UserName(), SWT.UP);

		col = new TableColumn(table, SWT.LEFT);
		col.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.nameTableColumn.text")); //$NON-NLS-1$
		new TableSortSelectionListener(tableViewer, col,
				new TaskViewerSorter_TaskName(), SWT.UP);

		col = new TableColumn(table, SWT.LEFT);
		col.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.descriptionTableColumn.text")); //$NON-NLS-1$
		new TableSortSelectionListener(tableViewer, col,
				new TaskViewerSorter_TaskDescription(), SWT.UP);

		col = new TableColumn(table, SWT.RIGHT);
		col.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.lastExecutionTableColumn.text")); //$NON-NLS-1$
		new TableSortSelectionListener(tableViewer, col,
				new TaskViewerSorter_LastExecDT(), SWT.UP);

		col = new TableColumn(table, SWT.RIGHT);
		col.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.durationTableColumn.text")); //$NON-NLS-1$
		new TableSortSelectionListener(tableViewer, col,
				new TaskViewerSorter_Duration(), SWT.UP);

		col = new TableColumn(table, SWT.LEFT);
		col.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.errorMessageTableColumn.text")); //$NON-NLS-1$
		new TableSortSelectionListener(tableViewer, col,
				new TaskViewerSorter_LastExecMessage(), SWT.UP);

		col = new TableColumn(table, SWT.LEFT);
		col.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.enabledTableColumn.text")); //$NON-NLS-1$
		new TableSortSelectionListener(tableViewer, col,
				new TaskViewerSorter_Enabled(), SWT.UP);

		col = new TableColumn(table, SWT.RIGHT);
		col.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.nextExecutionTableColumn.text")); //$NON-NLS-1$
		new TableSortSelectionListener(tableViewer, col,
				new TaskViewerSorter_NextExecDT(), SWT.UP);

		col = new TableColumn(table, SWT.LEFT);
		col.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.statusTableColumn.text")); //$NON-NLS-1$
		new TableSortSelectionListener(tableViewer, col,
				new TaskViewerSorter_Status(), SWT.UP);

		table.setLayout(new WeightedTableLayout(new int[] { -1, 20, 20, 40, -1, -1,
				40, -1, -1, -1 },
				new int[] { 50, -1, -1, -1, 140, 60, -1, 50, 120, 50 }));

		tsslTaskType.chooseColumnForSorting();
	}

	@Override
	protected void setTableProvider(TableViewer tableViewer)
	{
		tableViewer.setContentProvider(new TaskListContentProvider());
		tableViewer.setLabelProvider(new TaskListLabelProvider());
		setInput(new LinkedList<Task>());
	}

	private Set<Task> taskSet = null;

	private List<Task> tasks = null;

	public static final String[] FETCH_GROUPS_TASKS = { FetchPlan.DEFAULT,
			Task.FETCH_GROUP_NAME, Task.FETCH_GROUP_DESCRIPTION,
			Task.FETCH_GROUP_USER };

	/**
	 * This method must be called on the GUI thread. It loads the data
	 * asynchronously via a Job.
	 */
	public void loadTasks()
	{
		new Job(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskListComposite.loadTasks.job.taskName")) //$NON-NLS-1$
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
//				List<Task> _tasks = TaskProvider.sharedInstance().getTasks(
//						FETCH_GROUPS_TASKS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

				List<Task> _tasks = TaskDAO.sharedInstance().getTasks(FETCH_GROUPS_TASKS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new ProgressMonitorWrapper(monitor));

				// TODO sort

				tasks = _tasks;
				taskSet = new HashSet<Task>(tasks);

				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						if (isDisposed())
							return;

						setInput(tasks);
					}
				});
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	/**
	 * @return Returns <code>null</code>, before {@link #loadTasks()} was
	 *         called the first time.
	 */
	public List<Task> getTasks()
	{
		return tasks;
	}

	private String selectionZone = TaskListComposite.class.getName();

	public String getSelectionZone()
	{
		return selectionZone;
	}

	public void setSelectionZone(String selectionScope)
	{
		this.selectionZone = selectionScope;
	}
}
