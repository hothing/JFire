package org.nightlabs.jfire.base.ui.exceptionhandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.composite.ListComposite;
import org.nightlabs.base.ui.exceptionhandler.DefaultErrorDialog;
import org.nightlabs.base.ui.exceptionhandler.ErrorItem;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.Role;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.progress.ProgressMonitor;

public class InsufficientPermissionDialog extends DefaultErrorDialog
{
	private static ThreadLocal<InsufficientPermissionDialogContext> contextThreadLocal = new ThreadLocal<InsufficientPermissionDialogContext>();

	/**
	 * Since the {@link InsufficientPermissionHandler} cannot pass any object here directly, we use
	 * a {@link ThreadLocal} to assign a context before the {@link #showError(String, String, Throwable, Throwable)} method
	 * is called.
	 *
	 * @param context the current context.
	 */
	protected static void setInsufficientPermissionDialogContext(InsufficientPermissionDialogContext context)
	{
		contextThreadLocal.set(context);
	}

	/**
	 * Get the context assigned to the current thread or <code>null</code> if there is none.
	 *
	 * @return the current context or <code>null</code>.
	 */
	protected static InsufficientPermissionDialogContext getInsufficientPermissionDialogContext()
	{
		return contextThreadLocal.get();
	}

	/**
	 * Removes the context from the {@link ThreadLocal}.
	 * Called by the {@link InsufficientPermissionHandler} after
	 * {@link #showError(String, String, Throwable, Throwable)} has been triggered.
	 */
	protected static void removeInsufficientPermissionDialogContext()
	{
		contextThreadLocal.remove();
	}

	private ListComposite<RoleGroup> requiredRoleGroupList;

	@Override
	protected Control createCustomArea(Composite parent) {
		requiredRoleGroupList = new ListComposite<RoleGroup>(parent, SWT.NONE);
		requiredRoleGroupList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((RoleGroup)element).getName().getText();
			}
		});
		populateRequiredRoleGroupList();
		return super.createCustomArea(parent);
	}

	// this method is called within showError, the context is there
	@Override
	protected ErrorItem creatErrorItem(String dialogTitle, String message, Throwable thrownException, Throwable triggerException) {
		return new InsufficientPermissionErrorItem(message, thrownException, triggerException, getInsufficientPermissionDialogContext());
	}

	private InsufficientPermissionErrorItem currentErrorItem;

	// this method is called within showError the first time, i.e. when the context is there
	// but it is additionally called whenever an error is selected by the user.
	@Override
	protected void setErrorItem(ErrorItem errorItem) {
		currentErrorItem = (InsufficientPermissionErrorItem) errorItem;
		super.setErrorItem(errorItem);
		populateRequiredRoleGroupList();
	}

	private void populateRequiredRoleGroupList()
	{
		final InsufficientPermissionErrorItem errorItem = currentErrorItem;

		if (requiredRoleGroupList == null)
			return;

		requiredRoleGroupList.removeAll();
		if (errorItem.getContext().getRequiredRoles() != null) { // we got the info directly in the exception => no need to obtain it asynchronously
			Set<RoleGroup> roleGroupSet = new HashSet<RoleGroup>();
			for (Role role : errorItem.getContext().getRequiredRoles()) {
				roleGroupSet.addAll(role.getRoleGroups());
			}

			List<RoleGroup> roleGroupList = new ArrayList<RoleGroup>(roleGroupSet);
			Collections.sort(roleGroupList, roleGroupComparator);

			requiredRoleGroupList.addElements(roleGroupList);

			getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					// recalculate the size of the dialog (make it beautiful)
					Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
					getShell().setSize(newSize);
				}
			});
		}
		else {
			RoleGroup dummy = new RoleGroup("dummy"); //$NON-NLS-1$
			dummy.getName().setText(Locale.getDefault().getLanguage(), Messages.getString("org.nightlabs.jfire.base.ui.exceptionhandler.InsufficientPermissionDialog.name.loadingRoleGroups")); //$NON-NLS-1$
			requiredRoleGroupList.addElement(dummy);

			loadRoleGroupsJob = new Job(Messages.getString("org.nightlabs.jfire.base.ui.exceptionhandler.InsufficientPermissionDialog.job.loadingRoleGroups.name")) { //$NON-NLS-1$
				@Override
				protected IStatus run(ProgressMonitor monitor)
				throws Exception
				{
					monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.ui.exceptionhandler.InsufficientPermissionDialog.job.loadingRoleGroups.name"), 1); //$NON-NLS-1$
					try {
						Set<Role> roles = getRoles(errorItem.getContext().getRequiredRoleIDs());

						Set<RoleGroup> roleGroupSet = new HashSet<RoleGroup>();
						for (Role role : roles) {
							roleGroupSet.addAll(role.getRoleGroups());
						}

						final List<RoleGroup> roleGroupList = new ArrayList<RoleGroup>(roleGroupSet);
						Collections.sort(roleGroupList, roleGroupComparator);

						final Job thisJob = this;
						requiredRoleGroupList.getDisplay().asyncExec(new Runnable() {
							public void run() {
								if (loadRoleGroupsJob != thisJob)
									return;

								if (requiredRoleGroupList.isDisposed())
									return;

								requiredRoleGroupList.removeAll();
								requiredRoleGroupList.addElements(roleGroupList);

								// recalculate the size of the dialog (make it beautiful)
								Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
								getShell().setSize(newSize);
							}
						});

						return Status.OK_STATUS;
					} finally {
						monitor.worked(1);
						monitor.done();
					}
				}
			};
			loadRoleGroupsJob.setPriority(Job.SHORT);
			loadRoleGroupsJob.schedule();
		}
	}

	private Set<Role> getRoles(Set<RoleID> roleIDs)
	throws Exception
	{
		roleIDs = new HashSet<RoleID>(roleIDs); // copy, because we remove items from it
		Set<Role> roles = new HashSet<Role>(roleIDs.size());

		for (Iterator<RoleID> iterator = roleIDs.iterator(); iterator.hasNext();) {
			RoleID roleID = iterator.next();
			Role role = (Role) Cache.sharedInstance().get(InsufficientPermissionDialog.class.getName(), roleID, (String[])null, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			if (role != null) {
				roles.add(role);
				iterator.remove();
			}
		}

		if (!roleIDs.isEmpty()) {
			JFireSecurityManagerRemote m = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, Login.getLogin().getInitialContextProperties());
			Set<Role> retrievedRoles = m.getRolesForRequiredRoleIDs(roleIDs);
			roles.addAll(retrievedRoles);
			Cache.sharedInstance().putAll(InsufficientPermissionDialog.class.getName(), retrievedRoles, (String[])null, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		}

		return roles;
	}

	private Job loadRoleGroupsJob;

	private Comparator<RoleGroup> roleGroupComparator = new Comparator<RoleGroup>() {
		@Override
		public int compare(RoleGroup o1, RoleGroup o2) {
			return o1.getName().getText().compareTo(o2.getName().getText());
		}
	};
}
