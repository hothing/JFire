package org.nightlabs.jfire.base.admin.ui.timer.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.nightlabs.jfire.base.admin.ui.timer.TaskDetailView;

public class SubmitTaskDetailViewAction
implements IViewActionDelegate
{
	private TaskDetailView taskDetailView;

	public void init(IViewPart view)
	{
		taskDetailView = (TaskDetailView) view;
	}

	public void run(IAction action)
	{
		taskDetailView.getTaskDetailComposite().submit();
	}

	public void selectionChanged(IAction action, ISelection selection)
	{
		// for whatever reason "enablesFor" doesn't work
//		action.setEnabled(!selection.isEmpty());
		// update: enablesFor works!!! The problem was the usual timing with SelectionProviders and LSDViews => using SelectionProviderProxy in TaskDetailView now!
	}

}
