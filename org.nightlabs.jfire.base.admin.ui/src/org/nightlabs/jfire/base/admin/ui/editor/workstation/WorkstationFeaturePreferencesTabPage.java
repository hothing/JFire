package org.nightlabs.jfire.base.admin.ui.editor.workstation;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;
import org.nightlabs.jfire.base.admin.ui.editor.user.ConfigPreferencesController;
import org.nightlabs.jfire.base.admin.ui.editor.user.ConfigPreferencesSection;
import org.nightlabs.jfire.base.admin.ui.editor.user.IConfigSetupEditor;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.config.id.ConfigID;

public class WorkstationFeaturePreferencesTabPage
	extends FormPage
{
	public static final String ID_PAGE = WorkstationFeaturePreferencesTabPage.class.getName();

	private ConfigPreferencesSection preferencesSection;

	public static class Factory implements IEntityEditorPageFactory {

		public IFormPage createPage(FormEditor formEditor) {
			return new WorkstationFeaturePreferencesTabPage(formEditor);
		}

		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new ConfigPreferencesController(editor);
		}
	}

	public WorkstationFeaturePreferencesTabPage(FormEditor formEditor) {
		super(formEditor, ID_PAGE , Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.workstation.WorkstationFeaturePreferencesTabPage.title")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm)
	{
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		form.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.workstation.WorkstationFeaturePreferencesTabPage.form.text"));  //$NON-NLS-1$
		fillBody(managedForm, toolkit);
	}

	protected void fillBody(IManagedForm form, FormToolkit toolkit) {
		Composite body = form.getForm().getBody();
		GridLayout layout = new GridLayout();
		layout.marginBottom = 10;
		layout.marginTop = 5;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.numColumns = 1;
		layout.horizontalSpacing = 10;
		body.setLayout(layout);
		body.setLayoutData(new GridData(GridData.FILL_BOTH));

		if (! (getEditor() instanceof IConfigSetupEditor))
			throw new IllegalStateException(WorkstationFeaturePreferencesTabPage.class.getName() +
					" should only be used with a Editor implementing IConfigSetupEditor"); //$NON-NLS-1$
		ConfigID configID = ((IConfigSetupEditor)getEditor()).getConfigID();

		preferencesSection = new ConfigPreferencesSection(this, body, configID);
		getManagedForm().addPart(preferencesSection);
	}
}
