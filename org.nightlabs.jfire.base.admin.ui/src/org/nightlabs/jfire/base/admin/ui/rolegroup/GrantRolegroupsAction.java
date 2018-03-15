package org.nightlabs.jfire.base.admin.ui.rolegroup;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.id.UserID;

public class GrantRolegroupsAction implements IWorkbenchWindowActionDelegate{

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

	private Set<UserID> selectedUserIDs;

	@Override
	public void run(IAction action) {
		if (selectedUserIDs == null)
			return;

		try {
			JFireSecurityManagerRemote rm= JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, Login.getLogin().getInitialContextProperties());
			for (UserID userID : selectedUserIDs)
				rm.grantAllRoleGroupsInAllAuthorities(userID);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(false);

		if (!(selection instanceof IStructuredSelection))
			return;

		IStructuredSelection sel = (IStructuredSelection) selection;
		Set<UserID> userIDs = new HashSet<UserID>();

		for (Iterator<?> it = sel.iterator(); it.hasNext(); ) {
			Object o = it.next();
			Object oid = JDOHelper.getObjectId(o);
			if (oid instanceof UserID) {
				UserID userID = (UserID) oid;
				userIDs.add(userID);
			}
		}

		selectedUserIDs = userIDs;
		action.setEnabled(!userIDs.isEmpty());
	}

}
