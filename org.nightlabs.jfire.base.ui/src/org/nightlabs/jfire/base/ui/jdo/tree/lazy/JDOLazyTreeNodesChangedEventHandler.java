/**
 *
 */
package org.nightlabs.jfire.base.ui.jdo.tree.lazy;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Event handler that applies the changes found in an {@link JDOLazyTreeNodesChangedEvent} to a {@link TreeViewer}.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JDOLazyTreeNodesChangedEventHandler {
	private static final Logger logger = Logger.getLogger(JDOLazyTreeNodesChangedEventHandler.class);

	protected JDOLazyTreeNodesChangedEventHandler() { }

	private static final long DEFER_REFRESH_MSEC = 250;
	private static Job deferredRefreshJob = null;
	private static Set<TreeViewer> treeViewersWaitingForRefresh = new HashSet<TreeViewer>();

	public static void handle(TreeViewer treeViewer, final JDOLazyTreeNodesChangedEvent<? extends ObjectID, ? extends JDOObjectLazyTreeNode> changedEvent) {
		final Display display = Display.getCurrent();
		if (display == null)
			throw new IllegalStateException("This method must be called on the SWT UI thread!!! Wrong thread: " + Thread.currentThread()); //$NON-NLS-1$

		if (treeViewer.getTree().isDisposed())
			return;

		treeViewersWaitingForRefresh.add(treeViewer);

		if (deferredRefreshJob == null) {
			Job job = new Job(Messages.getString("org.nightlabs.jfire.base.ui.jdo.tree.lazy.JDOLazyTreeNodesChangedEventHandler.job.deferredRefresh")) { //$NON-NLS-1$
				@Override
				protected IStatus run(ProgressMonitor monitor) throws Exception {
					try { Thread.sleep(DEFER_REFRESH_MSEC); } catch (InterruptedException x) { } // ignore InterruptedException

					display.asyncExec(new Runnable() {
						public void run() {
							deferredRefreshJob = null;
							TreeViewer[] treeViewers = treeViewersWaitingForRefresh.toArray(new TreeViewer[treeViewersWaitingForRefresh.size()]);
							treeViewersWaitingForRefresh.clear();

							for (TreeViewer treeViewer : treeViewers) {
								if (treeViewer.getTree().isDisposed())
									continue;

								if (logger.isDebugEnabled())
									logger.debug("handle.deferredRefreshJob.run: Calling treeViewer.refresh() now: " + treeViewer); //$NON-NLS-1$

								// WORKAROUND BEGIN - otherwise it refreshes only a few items - not all
								treeViewer.getTree().getParent().layout(true);
								// WORKAROUND END

								long start = System.currentTimeMillis();
//								treeViewer.refresh();
								treeViewer.refresh(true); // a tree with SWT.VIRTUAL needs to be always refreshed completely (since it refreshes the visible items only, anyway).
//								for (JDOObjectLazyTreeNode node : changedEvent.getParentsToRefresh()) {
//									treeViewer.refresh(node, true);
//								}
								if (logger.isDebugEnabled()) {
									long duration = System.currentTimeMillis() - start;
									logger.debug("treeViewer.refresh() took "+duration+" ms");
								}
							}

						}
					});
					return Status.OK_STATUS;
				}
			};
			deferredRefreshJob = job;
			job.setPriority(Job.SHORT);
			job.schedule();
		}
	}

}
