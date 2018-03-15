package org.nightlabs.jfire.base.admin.ui.asyncinvoke;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeProblem;
import org.nightlabs.jfire.asyncinvoke.dao.AsyncInvokeProblemDAO;
import org.nightlabs.jfire.asyncinvoke.id.AsyncInvokeProblemID;
import org.nightlabs.jfire.base.ui.jdo.ActiveJDOObjectController;
import org.nightlabs.progress.ProgressMonitor;

public class ActiveAsyncInvokeProblemController
		extends ActiveJDOObjectController<AsyncInvokeProblemID, AsyncInvokeProblem>
{

	@Override
	protected Class<? extends AsyncInvokeProblem> getJDOObjectClass()
	{
		return AsyncInvokeProblem.class;
	}

	private static final String[] FETCH_GROUPS_ASYNC_INVOKE_PROBLEM = {
			FetchPlan.DEFAULT,
			AsyncInvokeProblem.FETCH_GROUP_ASYNC_INVOKE_ENVELOPE,
			AsyncInvokeProblem.FETCH_GROUP_ERROR_COUNT,
			AsyncInvokeProblem.FETCH_GROUP_LAST_ERROR
	};

	@Override
	protected Collection<AsyncInvokeProblem> retrieveJDOObjects(Set<AsyncInvokeProblemID> objectIDs, ProgressMonitor monitor)
	{
		return AsyncInvokeProblemDAO.sharedInstance().getAsyncInvokeProblems(objectIDs, FETCH_GROUPS_ASYNC_INVOKE_PROBLEM, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	@Override
	protected Collection<AsyncInvokeProblem> retrieveJDOObjects(ProgressMonitor monitor)
	{
		return AsyncInvokeProblemDAO.sharedInstance().getAsyncInvokeProblems(FETCH_GROUPS_ASYNC_INVOKE_PROBLEM, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	@Override
	protected void sortJDOObjects(List<AsyncInvokeProblem> objects)
	{
		Collections.sort(objects, new Comparator<AsyncInvokeProblem>()
		{
			@Override
			public int compare(AsyncInvokeProblem o1, AsyncInvokeProblem o2)
			{
				return ((AsyncInvokeEnvelope) o1.getAsyncInvokeEnvelope()).getCreateDT().compareTo(
						((AsyncInvokeEnvelope) o2.getAsyncInvokeEnvelope()).getCreateDT());
			}
		});
	}
}
