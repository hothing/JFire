package org.nightlabs.jfire.base.admin.ui.editor.authoritytype;

import javax.jdo.FetchPlan;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.dao.AuthorityTypeDAO;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.Util;

public class AuthorityTypeDetailPageController extends EntityEditorPageController
{
	private static final String[] FETCH_GROUPS_AUTHORITY_TYPE = {
		FetchPlan.DEFAULT,
		AuthorityType.FETCH_GROUP_NAME,
		AuthorityType.FETCH_GROUP_DESCRIPTION,
		AuthorityType.FETCH_GROUP_ROLE_GROUPS,
		RoleGroup.FETCH_GROUP_NAME,
		RoleGroup.FETCH_GROUP_DESCRIPTION
	};

	public AuthorityTypeDetailPageController(EntityEditor editor) {
		super(editor);
	}

	public AuthorityTypeDetailPageController(EntityEditor editor, boolean startBackgroundLoading) {
		super(editor, startBackgroundLoading);
	}

	private AuthorityType authorityType;

	@Override
	public void doLoad(ProgressMonitor monitor) {
		JDOObjectEditorInput<?> input = (JDOObjectEditorInput<?>) getEntityEditor().getEditorInput();
		AuthorityTypeID authorityTypeID = (AuthorityTypeID) input.getJDOObjectID();
		this.authorityType = Util.cloneSerializable(
				AuthorityTypeDAO.sharedInstance().getAuthorityType(
						authorityTypeID,
						FETCH_GROUPS_AUTHORITY_TYPE,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						monitor
				)
		);
		fireModifyEvent(null, this.authorityType);
	}

	public AuthorityType getAuthorityType() {
		return authorityType;
	}

	@Override
	public boolean doSave(ProgressMonitor monitor) {
		// there is nothing to save - it's all read-only.
		return true;
	}

}
