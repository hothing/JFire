package org.nightlabs.jfire.base.admin.ui.editor.authority;

import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityTypeID;

public class AssignAuthorityWizard extends DynamicPathWizard
{
	private AuthorityTypeID authorityTypeID;
	private InheritedSecuringAuthorityResolver inheritedAuthorityResolver;

	private SelectAuthorityPage selectAuthorityPage;

	public AssignAuthorityWizard(AuthorityTypeID authorityTypeID, InheritedSecuringAuthorityResolver inheritedAuthorityResolver)
	{
		if (authorityTypeID == null)
			throw new IllegalArgumentException("authorityTypeID == null"); //$NON-NLS-1$

		this.authorityTypeID = authorityTypeID;
		this.inheritedAuthorityResolver = inheritedAuthorityResolver;
	}

	@Override
	public void addPages() {
		selectAuthorityPage = new SelectAuthorityPage(authorityTypeID, inheritedAuthorityResolver);
		addPage(selectAuthorityPage);
	}

	public AuthorityTypeID getAuthorityTypeID() {
		return authorityTypeID;
	}

	public AuthorityID getAuthorityID() {
		return selectAuthorityPage.getAuthorityID();
	}

	public Authority getNewAuthority() {
		return selectAuthorityPage.getNewAuthority();
	}

	public boolean isAuthorityIDInherited()
	{
		return SelectAuthorityPage.Action.inherit == selectAuthorityPage.getAction();
	}

	@Override
	public boolean performFinish() {
		return true;
	}

}
