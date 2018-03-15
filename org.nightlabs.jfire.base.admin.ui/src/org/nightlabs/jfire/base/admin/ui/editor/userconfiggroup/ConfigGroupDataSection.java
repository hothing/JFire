package org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
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
import org.nightlabs.jfire.config.ConfigGroup;

public class ConfigGroupDataSection extends RestorableSectionPart {
	private Text configGroupNameText;
	private Text configGroupKeyText;
	
	private ConfigGroup configGroup;

	public ConfigGroupDataSection(FormPage page, Composite parent, String title) {
		super(parent, page.getEditor().getToolkit(), ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		createClient(getSection(), page.getEditor().getToolkit(), title);
	}

	private void createClient(Section section, FormToolkit toolkit, String sectionDescriptionText) {
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		section.setText(sectionDescriptionText);
		
		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 1);
		createLabel(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup.ConfigGroupDataSection.label.configGroupKey"), 1); //$NON-NLS-1$
		configGroupKeyText = new Text(container, XComposite.getBorderStyle(container));
		configGroupKeyText.setLayoutData(getGridData(1));
		configGroupKeyText.setEditable(false);
		
		createLabel(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.userconfiggroup.ConfigGroupDataSection.label.ConfigGroupName"), 1); //$NON-NLS-1$
		configGroupNameText = new Text(container, XComposite.getBorderStyle(container));
		configGroupNameText.setLayoutData(getGridData(1));
		configGroupNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!refreshing)
					markDirty();
			}
		});
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
	
	private boolean refreshing = false;
	
	@Override
	public void commit(boolean onSave) {
		super.commit(onSave);
		configGroup.setName(configGroupNameText.getText());
	}

	public void setConfigGroup(final ConfigGroup configGroup) {
		this.configGroup = configGroup;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				refreshing = true;
				configGroupKeyText.setText(configGroup.getConfigKey());
				if (configGroup.getName() != null)
					configGroupNameText.setText(configGroup.getName());
				refreshing = false;
			}
		});
	}
}
