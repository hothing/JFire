package org.nightlabs.jfire.base.admin.ui.editor.authoritytype;

import javax.jdo.FetchPlan;

import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditor;
import org.nightlabs.jfire.base.ui.login.part.ICloseOnLogoutEditorPart;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.dao.AuthorityTypeDAO;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.progress.ProgressMonitor;

public class AuthorityTypeEditor extends ActiveEntityEditor
implements ICloseOnLogoutEditorPart {
	@Override
	protected String getEditorTitleFromEntity(Object entity) {
		return entity instanceof AuthorityType ? ((AuthorityType)entity).getName().getText() : null;
	}

	@Override
	protected Object retrieveEntityForEditorTitle(ProgressMonitor monitor) {
		AuthorityTypeID authorityTypeID = (AuthorityTypeID) ((JDOObjectEditorInput<?>)getEditorInput()).getJDOObjectID();
		assert authorityTypeID != null;
		return AuthorityTypeDAO.sharedInstance().getAuthorityType(authorityTypeID, new String[] { FetchPlan.DEFAULT, AuthorityType.FETCH_GROUP_NAME }, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

//	@Override
//	public String getTitle() {
//		if(getEditorInput() == null)
//			return super.getTitle();
//
//		Job loadTitleJob = new Job(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authoritytype.AuthorityTypeEditor.job.loadingAuthorityType")) { //$NON-NLS-1$
//			@Override
//			protected IStatus run(ProgressMonitor monitor) throws Exception {
//				final String title = AuthorityTypeDAO.sharedInstance().getAuthorityType(
//						(AuthorityTypeID) ((JDOObjectEditorInput<?>)getEditorInput()).getJDOObjectID(),
//						new String[] { FetchPlan.DEFAULT, AuthorityType.FETCH_GROUP_NAME },
//						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor).getName().getText();
//				Display.getDefault().asyncExec(new Runnable() {
//					public void run() {
//						setPartName(title);
//					}
//				});
//				return Status.OK_STATUS;
//			}
//		};
//		loadTitleJob.schedule();
//
//		return super.getTitle();
//	}

}
