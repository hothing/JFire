package org.nightlabs.jfire.base.admin.ui.editor.authoritytype;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.entity.tree.ActiveJDOEntityTreeCategory;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.dao.AuthorityTypeDAO;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.progress.ProgressMonitor;

public class EntityTreeCategoryAuthorityType
extends ActiveJDOEntityTreeCategory<AuthorityTypeID, AuthorityType>
{
	private static final String[] FETCH_GROUPS_AUTHORITY_TYPE = {
		FetchPlan.DEFAULT,
		AuthorityType.FETCH_GROUP_NAME
	};

	@Override
	protected Class<AuthorityType> getJDOObjectClass() {
		return AuthorityType.class;
	}

	@Override
	protected Collection<AuthorityType> retrieveJDOObjects(ProgressMonitor monitor) {
		return AuthorityTypeDAO.sharedInstance().getAuthorityTypes(
				FETCH_GROUPS_AUTHORITY_TYPE,
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				monitor);
	}

	@Override
	protected Collection<AuthorityType> retrieveJDOObjects(Set<AuthorityTypeID> authorityTypeIDs, ProgressMonitor monitor) {
		return AuthorityTypeDAO.sharedInstance().getAuthorityTypes(
				authorityTypeIDs,
				FETCH_GROUPS_AUTHORITY_TYPE,
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				monitor);
	}

	@Override
	protected void sortJDOObjects(List<AuthorityType> authorityTypes) {
		Collections.sort(authorityTypes, new Comparator<AuthorityType>() {
			@Override
			public int compare(AuthorityType o1, AuthorityType o2) {
				return o1.getName().getText().compareTo(o2.getName().getText());
			}
		});
	}

	@Override
	public JDOObjectEditorInput<AuthorityTypeID> createEditorInput(Object o) {
		return new JDOObjectEditorInput<AuthorityTypeID>((AuthorityTypeID) JDOHelper.getObjectId(o));
	}

	@Override
	public ITableLabelProvider createLabelProvider() {
		return new TableLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				switch (columnIndex) {
					case 0:
						if (element instanceof AuthorityType)
							return ((AuthorityType)element).getName().getText();
						else
							return String.valueOf(element);

					default:
							return ""; //$NON-NLS-1$
				}
			}
			@Override
			public String getText(Object element) {
				if (element instanceof AuthorityType)
					return ((AuthorityType)element).getName().getText();
				else
					return String.valueOf(element);
			}
		};
	}

}
