package org.nightlabs.jfire.base.admin.ui.editor.authoritytype;

import javax.jdo.JDOHelper;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.editor.MessageSectionPart;
import org.nightlabs.jfire.base.admin.ui.editor.authority.AuthorityTable;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.id.AuthorityTypeID;

public class AuthorityListSection extends MessageSectionPart
{
	private AuthorityTable authorityTable;

	public AuthorityListSection(IFormPage page, Composite parent) {
		super(page, parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED | ExpandableComposite.TWISTIE, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authoritytype.AuthorityListSection.title.authorities")); //$NON-NLS-1$

		createDescriptionControl(getSection(), page.getEditor().getToolkit());

		authorityTable = new AuthorityTable(getContainer());
	}

	public void setAuthorityType(AuthorityType authorityType) {
		authorityTable.setAuthorityTypeID((AuthorityTypeID) JDOHelper.getObjectId(authorityType));
	}

	private void createDescriptionControl(Section section, FormToolkit toolkit)
	{
		FormText text = toolkit.createFormText(section, true);
		text.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authoritytype.AuthorityListSection.text.text"), false, false); //$NON-NLS-1$
		section.setDescriptionControl(text);
	}

}
