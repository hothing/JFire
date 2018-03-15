package org.nightlabs.jfire.base.admin.ui.timer.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.nightlabs.jfire.base.admin.ui.timer.TaskDetailView;

public class CreateTimePatternViewAction
		implements IViewActionDelegate
{
	private TaskDetailView view;

	public void init(IViewPart view)
	{
		this.view = (TaskDetailView) view;
	}

	public void run(IAction action)
	{
		view.getTaskDetailComposite().createTimePattern();
	}

	public void selectionChanged(IAction action, ISelection selection)
	{
		// nothing to do - extension's "enablesFor" does all we need
	}

}
