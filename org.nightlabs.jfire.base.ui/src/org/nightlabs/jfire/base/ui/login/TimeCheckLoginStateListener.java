package org.nightlabs.jfire.base.ui.login;

import java.util.Date;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.login.LoginState;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.server.ServerManagerRemote;
import org.nightlabs.l10n.DateFormatter;
import org.nightlabs.progress.ProgressMonitor;

/**
 * This {@link LoginStateListener} implementation checks whether the client and the server have
 * roughly the same time (<= 2 minutes difference). If the time differs a log (more than the tolerance),
 * a warning dialog is shown.
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class TimeCheckLoginStateListener implements LoginStateListener
{
	private static final Logger logger = Logger.getLogger(TimeCheckLoginStateListener.class);

	@Override
	public void loginStateChanged(LoginStateChangeEvent event) {
		if (event.getNewLoginState() != LoginState.LOGGED_IN)
			return;

		final Display display = Display.getCurrent();
		if (display == null)
			throw new IllegalStateException("What the hell?! A LoginStateListener should always be triggered on the SWT UI thread!"); //$NON-NLS-1$

		Job job = new Job(Messages.getString("org.nightlabs.jfire.base.ui.login.TimeCheckLoginStateListener.timeCheckJob.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception
			{
				if (!Login.isLoggedIn()) { // not online anymore => silently return
					logger.warn("Already logged out. Cannot check time anymore!"); //$NON-NLS-1$
					return Status.CANCEL_STATUS;
				}

				ServerManagerRemote m = JFireEjb3Factory.getRemoteBean(ServerManagerRemote.class, Login.getLogin().getInitialContextProperties());
				long startServerRequest = System.currentTimeMillis();
				final Date serverTime = m.getServerTime();
				long stopServerRequest = System.currentTimeMillis();
				final Date localTime = new Date();

				if (Math.abs(serverTime.getTime() - localTime.getTime()) <= 2 * 60 * 1000) { // TODO make this configurable?!
					logger.info("Time difference between client and server is small enough to work with. localTime=" + localTime + " serverTime=" + serverTime + " requestDuration=" + (stopServerRequest - startServerRequest)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				else {
					logger.warn("Time difference between client and server is significant! localTime=" + localTime + " serverTime=" + serverTime + " requestDuration=" + (stopServerRequest - startServerRequest)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

					display.asyncExec(new Runnable() {
						public void run() {
							if (display.isDisposed())
								return;

							String localTimeString = DateFormatter.formatDateLongTimeHMS(localTime, false);
							String serverTimeString = DateFormatter.formatDateLongTimeHMS(serverTime, false);

							MessageDialog.openError(
									RCPUtil.getActiveShell(),
									Messages.getString("org.nightlabs.jfire.base.ui.login.TimeCheckLoginStateListener.timeDifferenceDialog.title"), //$NON-NLS-1$
									String.format(
											Messages.getString("org.nightlabs.jfire.base.ui.login.TimeCheckLoginStateListener.timeDifferenceDialog.message"), //$NON-NLS-1$
											localTimeString,
											serverTimeString,
											localTime,
											serverTime
									)
							);
						}
					});
				}

				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.LONG);
		job.schedule();
	}

}
