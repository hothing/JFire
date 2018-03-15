package org.nightlabs.jfire.base.admin.ui.editor.workstation;

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
import org.nightlabs.jfire.workstation.Workstation;

public class WorkstationDataSection extends RestorableSectionPart {
	private Text workstationDescText;
	private Text workstationIdText;
	
	private Workstation workstation;

	public WorkstationDataSection(FormPage page, Composite parent, String title) {
		super(parent, page.getEditor().getToolkit(), ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		createClient(getSection(), page.getEditor().getToolkit(), title);
	}

	private void createClient(Section section, FormToolkit toolkit, String sectionDescriptionText) {
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		section.setText(sectionDescriptionText);
		
		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 1);
		createLabel(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.workstation.WorkstationDataSection.label.workstationID"), 1); //$NON-NLS-1$
		workstationIdText = new Text(container, XComposite.getBorderStyle(container));
		workstationIdText.setLayoutData(getGridData(1));
		workstationIdText.setEditable(false);
		
		createLabel(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.workstation.WorkstationDataSection.label.workstationDescription"), 1); //$NON-NLS-1$
		workstationDescText = new Text(container, XComposite.getBorderStyle(container));
		workstationDescText.setLayoutData(getGridData(1));
		workstationDescText.addModifyListener(new ModifyListener() {
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
	
	public void setWorkstation(final Workstation workstation) {
		this.workstation = workstation;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				refreshing = true;
				workstationIdText.setText(workstation.getWorkstationID());
				if (workstation.getDescription() != null)
					workstationDescText.setText(workstation.getDescription());
				refreshing = false;
			}
		});
	}
	
	@Override
	public void commit(boolean onSave) {
		super.commit(onSave);
		workstation.setDescription(workstationDescText.getText());
	}
}
