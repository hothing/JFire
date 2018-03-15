package org.nightlabs.jfire.base.ui.jdo.notification;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.nightlabs.base.ui.progress.RCPProgressMonitor;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleAdapter;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;
import org.nightlabs.progress.ProgressMonitor;

public abstract class JDOLifecycleAdapterJob
extends JDOLifecycleAdapter
implements JDOLifecycleListenerJob
{
	private String jobName = null;

	public JDOLifecycleAdapterJob() { }

	public JDOLifecycleAdapterJob(String jobName)
	{
		this.jobName = jobName;
	}

	public org.nightlabs.base.ui.job.Job getJob(JDOLifecycleEvent event)
	{
		return null;
	}

	public String getJobName()
	{
		return jobName;
	}

	private ProgressMonitor progressMonitor;

	private RCPProgressMonitor rcpProgressMonitor;

	@Override
	public void setProgressMonitor(ProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	@Override
	public ProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public RCPProgressMonitor getRCPProgressMonitor() {
		if (rcpProgressMonitor == null) {
			if (progressMonitor == null)
				throw new IllegalStateException("getRCPProgressMonitor() must not be called before setProgressMonitor(ProgressMonitor)."); //$NON-NLS-1$
			rcpProgressMonitor = new RCPProgressMonitor(progressMonitor);
		}
		return rcpProgressMonitor;
	}

	/**
	 * @deprecated Use {@link #getProgressMonitor()} instead! This method exists only for downward compatibility!
	 */
	@Deprecated
	public ProgressMonitor getProgressMonitorWrapper() {
		return getProgressMonitor();
	}

	/**
	 * @deprecated Use {@link #getProgressMonitor()} instead! This method exists only for downward compatibility!
	 */
	@Deprecated
	public ProgressMonitor getProgressMontitorWrapper() {
		return getProgressMonitor();
	}

//	/**
//	 * Returns a {@link ProgressMonitor} implementation wrapping around the
//	 * {@link IProgressMonitor} set in {@link #setProgressMonitor(ProgressMonitor)}.
//	 *
//	 * @return A {@link ProgressMonitor} implementation wrapping around the
//	 * 		{@link IProgressMonitor} set in {@link #setProgressMonitor(ProgressMonitor)}.
//	 */
//	public ProgressMonitor getProgressMontitorWrapper() {
//		if (progressMonitorWrapper == null) {
//			if (progressMonitor == null)
//				throw new IllegalStateException("getProgressMontitorWrapper must not be called before setProgressMonitor(IProgressMonitor)."); //$NON-NLS-1$
//			progressMonitorWrapper = new ProgressMonitorWrapper(progressMonitor);
//		}
//		return progressMonitorWrapper;
//	}

	/**
	 * @see org.nightlabs.base.ui.notification.NotificationListenerJob#getRule()
	 */
	public ISchedulingRule getRule()
	{
		return null;
	}

	/**
	 * The default implementation of this method returns {@link Job#SHORT}.
	 *
	 * @see org.nightlabs.base.ui.notification.NotificationListenerJob#getPriority()
	 */
	public int getPriority()
	{
		return Job.SHORT;
	}

	/**
	 * The default implementation of this method returns 0.
	 *
	 * @see org.nightlabs.base.ui.notification.NotificationListenerJob#getDelay()
	 */
	public long getDelay()
	{
		return 0;
	}

	/**
	 * The default implementation of this method returns false.
	 *
	 * @see org.nightlabs.base.ui.notification.NotificationListenerJob#isUser()
	 */
	public boolean isUser()
	{
		return false;
	}

	/**
	 * The default implementation of this method returns false.
	 *
	 * @see org.nightlabs.base.ui.notification.NotificationListenerJob#isSystem()
	 */
	public boolean isSystem()
	{
		return false;
	}
}
