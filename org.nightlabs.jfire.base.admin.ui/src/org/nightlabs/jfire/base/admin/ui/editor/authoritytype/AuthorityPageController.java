package org.nightlabs.jfire.base.admin.ui.editor.authoritytype;

import javax.jdo.FetchPlan;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.admin.ui.editor.authority.AuthorityPageControllerHelper;
import org.nightlabs.jfire.base.admin.ui.editor.authority.InheritedSecuringAuthorityResolver;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.dao.AuthorityTypeDAO;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.Util;

public class AuthorityPageController
extends ActiveEntityEditorPageController<AuthorityType>
{
	private static final String[] FETCH_GROUPS_AUTHORITY_TYPE = {
		FetchPlan.DEFAULT
	};

	@Override
	protected String[] getEntityFetchGroups() {
		return FETCH_GROUPS_AUTHORITY_TYPE;
	}

	public AuthorityPageController(EntityEditor editor) {
		super(editor);
	}

	private AuthorityPageControllerHelper authorityPageControllerHelper = new AuthorityPageControllerHelper() {
		@Override
		protected InheritedSecuringAuthorityResolver createInheritedSecuringAuthorityResolver() {
			return null; // no inheritance for AuthorityTypes
		}

	};

	public AuthorityPageControllerHelper getAuthorityPageControllerHelper() {
		return authorityPageControllerHelper;
	}

	@Override
	protected AuthorityType retrieveEntity(ProgressMonitor monitor) {
		AuthorityType authorityType;

		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authoritytype.AuthorityPageController.job.loadingAuthorityType"), 100); //$NON-NLS-1$
		try {
			JDOObjectEditorInput<?> input = (JDOObjectEditorInput<?>) getEntityEditor().getEditorInput();
			AuthorityTypeID authorityTypeID = (AuthorityTypeID) input.getJDOObjectID();

			authorityType = Util.cloneSerializable(
					AuthorityTypeDAO.sharedInstance().getAuthorityType(
							authorityTypeID,
							FETCH_GROUPS_AUTHORITY_TYPE,
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
							new SubProgressMonitor(monitor, 25)
					)
			);

			authorityPageControllerHelper.load(
					authorityType,
					new SubProgressMonitor(monitor, 75));

		} finally {
			monitor.done();
		}

		return authorityType;
	}

	@Override
	protected AuthorityType storeEntity(AuthorityType controllerObject, ProgressMonitor monitor) {
		authorityPageControllerHelper.store(monitor);
		return getControllerObject();
	}
}
