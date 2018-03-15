package org.nightlabs.jfire.base.admin.ui.editor.workstationgroup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.editor.RestorableSectionPart;
import org.nightlabs.base.ui.entity.editor.EntityEditorUtil;
import org.nightlabs.jfire.base.admin.ui.configgroup.ConfigGroupMembersEditComposite;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class WorkstationGroupMemberSection
extends RestorableSectionPart
{
	public WorkstationGroupMemberSection(IFormPage page, Composite parent)
	{
		super(parent, page.getEditor().getToolkit(), ExpandableComposite.TITLE_BAR);
		FormToolkit toolkit = page.getEditor().getToolkit();
		Section section = getSection();
		section.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.workstationgroup.WorkstationGroupMemberSection.section.text")); //$NON-NLS-1$
		section.setExpanded(true);
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 1);
		((GridLayout)container.getLayout()).numColumns = 1;
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		configGroupMembersEditComposite = new ConfigGroupMembersEditComposite(container, SWT.NONE, true, this, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.workstationgroup.WorkstationGroupMemberSection.title.workstations")); //$NON-NLS-1$
	}
	
	private ConfigGroupMembersEditComposite configGroupMembersEditComposite;
	public ConfigGroupMembersEditComposite getConfigGroupMembersEditComposite() {
		return configGroupMembersEditComposite;
	}
}
