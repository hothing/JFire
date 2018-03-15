package org.nightlabs.jfire.base.admin.ui.editor.user;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.editor.RestorableSectionPart;
import org.nightlabs.base.ui.entity.editor.EntityEditorUtil;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.UserSecurityGroup;

public class UserSecurityGroupDataSection extends RestorableSectionPart {

	private Text userSecurityGroupIdText;
	private Text userSecurityGroupNameText;
	private Text userSecurityGroupDescriptionText;
	
	private UserSecurityGroup userSecurityGroup;
	
	ModifyListener dirtyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			markDirty();
		}
	};
	
	/**
	 * Create an instance of UserPropertiesSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public UserSecurityGroupDataSection(FormPage page, Composite parent, String sectionDescriptionText) {
		super(parent, page.getEditor().getToolkit(), ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		createClient(getSection(), page.getEditor().getToolkit(), sectionDescriptionText);
	}

	private void createClient(Section section, FormToolkit toolkit, String sectionDescriptionText) {
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createDescriptionControl(section, toolkit, sectionDescriptionText);
		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 1);
		GridLayout layout = (GridLayout) container.getLayout();
		layout.horizontalSpacing = 10;
		layout.numColumns = 3;
		
		createLabel(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserSecurityGroupDataSection.userSecurityGroupID"), 3); //$NON-NLS-1$
		userSecurityGroupIdText = new Text(container, XComposite.getBorderStyle(container));
		userSecurityGroupIdText.setEditable(false);
		userSecurityGroupIdText.setLayoutData(getGridData(3));
		
		createLabel(container,	Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserSecurityGroupDataSection.userSecurityGroupName"), 3); //$NON-NLS-1$
		userSecurityGroupNameText = new Text(container, XComposite.getBorderStyle(container));
		userSecurityGroupNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		createLabel(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserSecurityGroupDataSection.userSecurityGroupDescription"), 3); //$NON-NLS-1$
		userSecurityGroupDescriptionText = new Text(container, XComposite.getBorderStyle(container));
		userSecurityGroupDescriptionText.setLayoutData(getGridData(3));
	}
	
	private void createLabel(Composite container, String text, int span) {
		Label label = new Label(container, SWT.NONE);
		label.setText(text);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = span;
		label.setLayoutData(gd);
	}
	
	private GridData getGridData(int span) {
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = span;
		return gd;
	}
	
	private void createDescriptionControl(Section section, FormToolkit toolkit, String sectionDescriptionText) {
		if (sectionDescriptionText == null || "".equals(sectionDescriptionText)) //$NON-NLS-1$
			return;

		section.setText(sectionDescriptionText);
	}

	public void setUserSecurityGroup(UserSecurityGroup _userSecurityGroup) {
		this.userSecurityGroup = _userSecurityGroup;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (userSecurityGroupNameText.isDisposed())
					return;
				
				userSecurityGroupNameText.removeModifyListener(dirtyListener);
				userSecurityGroupDescriptionText.removeModifyListener(dirtyListener);
				userSecurityGroupIdText.setText(userSecurityGroup.getUserSecurityGroupID());
				if (userSecurityGroup.getName() != null)
					userSecurityGroupNameText.setText(userSecurityGroup.getName());
				if (userSecurityGroup.getDescription() != null)
					userSecurityGroupDescriptionText.setText(userSecurityGroup.getDescription());
				
				userSecurityGroupNameText.addModifyListener(dirtyListener);
				userSecurityGroupDescriptionText.addModifyListener(dirtyListener);
			}
		});
	}

	@Override
	public void commit(boolean onSave) {
		super.commit(onSave);
		userSecurityGroup.setDescription(userSecurityGroupDescriptionText.getText());
		userSecurityGroup.setName(userSecurityGroupNameText.getText());
	}
}
