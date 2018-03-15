package org.nightlabs.jfire.base.admin.ui.asyncinvoke;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeProblem;
import org.nightlabs.jfire.asyncinvoke.dao.AsyncInvokeProblemDAO;
import org.nightlabs.jfire.asyncinvoke.id.AsyncInvokeProblemID;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.progress.ProgressMonitor;

public class DeleteViewAction
implements IViewActionDelegate
{
	private AsyncInvokeProblemView view;

	@Override
	public void init(IViewPart view)
	{
		this.view = (AsyncInvokeProblemView) view;
	}

	@Override
	public void run(IAction arg0)
	{
		final Set<AsyncInvokeProblemID> asyncInvokeProblemIDs = NLJDOHelper.getObjectIDSet(view.getAsyncInvokeProblemTable().getSelectedElements());
		Job job = new Job(Messages.getString("org.nightlabs.jfire.base.admin.ui.asyncinvoke.DeleteViewAction.job.deleteAsyncInvocations")) //$NON-NLS-1$
		{
			@Override
			protected IStatus run(ProgressMonitor monitor)
					throws Exception
			{
				AsyncInvokeProblemDAO.sharedInstance().deleteAsyncInvokeProblems(asyncInvokeProblemIDs);
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.setPriority(org.eclipse.core.runtime.jobs.Job.SHORT);
		job.schedule();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection)
	{
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			for (Iterator<?> it = sel.toList().iterator(); it.hasNext(); ) {
				AsyncInvokeProblem asyncInvokeProblem = (AsyncInvokeProblem) it.next();
				if (asyncInvokeProblem.isUndeliverable()) {
					action.setEnabled(true);
					return;
				}
			}
		}

		action.setEnabled(false);
	}
}
