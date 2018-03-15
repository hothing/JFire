package org.nightlabs.jfire.geography.admin.ui.templatedata.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.District;
import org.nightlabs.jfire.geography.Location;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.admin.ui.templatedata.GeographyTemplateDataAdmin;
import org.nightlabs.util.NLLocale;

public class GeographyTemplateDataEditorDialog extends Dialog {
	private Object input = null;

	public GeographyTemplateDataEditorDialog(Shell parentShell, Object input)
	{
		super(parentShell);
		this.input = input;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditorDialog.saveButtonLabel"), false); //$NON-NLS-1$
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void buttonPressed(int id) {
		if (id == IDialogConstants.OK_ID){
			doFinish();
			close();
		}
		else
			super.buttonPressed(id);

	}

	private Text nameField;
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		if(input instanceof Country){
			Country country = (Country)input;

			final Label nameLabel = new Label(container, SWT.NONE);
			nameLabel.setLayoutData(new GridData(GridData.END,
					GridData.CENTER, false, false));
			nameLabel.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditorDialog.currentCountryNameLabelText")); //$NON-NLS-1$

			final Label oldNameLabel = new Label(container, SWT.NONE);
			oldNameLabel.setLayoutData(new GridData(GridData.FILL,
					GridData.CENTER, true, false));
			oldNameLabel.setText(country.getName().getText());

			final Label newNameLabel = new Label(container, SWT.NONE);
			final GridData gridData = new GridData(GridData.END,
					GridData.CENTER, false, false);
			gridData.horizontalIndent = 20;
			newNameLabel.setLayoutData(gridData);
			newNameLabel.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditorDialog.newCountryNameLabelText")); //$NON-NLS-1$

			nameField = new Text(container, SWT.BORDER);
			nameField.setLayoutData(new GridData(GridData.FILL,
					GridData.CENTER, true, false));

			configureShell(getShell(), Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditorDialog.editCountryDialogTitle")); //$NON-NLS-1$
		}//if
		else if(input instanceof Region){
			Region region = (Region)input;
			final Label nameLabel = new Label(container, SWT.NONE);
			nameLabel.setLayoutData(new GridData(GridData.END,
					GridData.CENTER, false, false));
			nameLabel.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditorDialog.currentRegionNameLabelText")); //$NON-NLS-1$

			final Label oldNameLabel = new Label(container, SWT.NONE);
			oldNameLabel.setLayoutData(new GridData(GridData.FILL,
					GridData.CENTER, true, false));
			oldNameLabel.setText(region.getName().getText());

			final Label newNameLabel = new Label(container, SWT.NONE);
			final GridData gridData = new GridData(GridData.END,
					GridData.CENTER, false, false);
			gridData.horizontalIndent = 20;
			newNameLabel.setLayoutData(gridData);
			newNameLabel.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditorDialog.newRegionNameLabelText")); //$NON-NLS-1$

			nameField = new Text(container, SWT.BORDER);
			nameField.setLayoutData(new GridData(GridData.FILL,
					GridData.CENTER, true, false));

			configureShell(getShell(), Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditorDialog.editRegionDialogTitle")); //$NON-NLS-1$

		}//else if
		else if(input instanceof City){
			City city = (City)input;

			final Label nameLabel = new Label(container, SWT.NONE);
			nameLabel.setLayoutData(new GridData(GridData.END,
					GridData.CENTER, false, false));
			nameLabel.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditorDialog.currentCityNameLabelText")); //$NON-NLS-1$

			final Label oldNameLabel = new Label(container, SWT.NONE);
			oldNameLabel.setLayoutData(new GridData(GridData.FILL,
					GridData.CENTER, true, false));
			oldNameLabel.setText(city.getName().getText());

			final Label newNameLabel = new Label(container, SWT.NONE);
			final GridData gridData = new GridData(GridData.END,
					GridData.CENTER, false, false);
			gridData.horizontalIndent = 20;
			newNameLabel.setLayoutData(gridData);
			newNameLabel.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditorDialog.newCityNameLabelText")); //$NON-NLS-1$

			nameField = new Text(container, SWT.BORDER);
			nameField.setLayoutData(new GridData(GridData.FILL,
					GridData.CENTER, true, false));

			configureShell(getShell(), Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditorDialog.editCityDialogTitle")); //$NON-NLS-1$
		}//else if

		else if(input instanceof Location){
			Location location = (Location)input;

			final Label nameLabel = new Label(container, SWT.NONE);
			nameLabel.setLayoutData(new GridData(GridData.END,
					GridData.CENTER, false, false));
			nameLabel.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditorDialog.currentLocationNameLabelText")); //$NON-NLS-1$

			final Label oldNameLabel = new Label(container, SWT.NONE);
			oldNameLabel.setLayoutData(new GridData(GridData.FILL,
					GridData.CENTER, true, false));
			oldNameLabel.setText(location.getName().getText());

			final Label newNameLabel = new Label(container, SWT.NONE);
			final GridData gridData = new GridData(GridData.END,
					GridData.CENTER, false, false);
			gridData.horizontalIndent = 20;
			newNameLabel.setLayoutData(gridData);
			newNameLabel.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditorDialog.newLocationNameLabelText")); //$NON-NLS-1$

			nameField = new Text(container, SWT.BORDER);
			nameField.setLayoutData(new GridData(GridData.FILL,
					GridData.CENTER, true, false));

			configureShell(getShell(), Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditorDialog.editLocationDialogTitle")); //$NON-NLS-1$
		}//else if
		else if(input instanceof District){
//			District district = (District)input;

			final Label nameLabel = new Label(container, SWT.NONE);
			nameLabel.setLayoutData(new GridData(GridData.END,
					GridData.CENTER, false, false));
			nameLabel.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditorDialog.currentDistrictNameLabelText")); //$NON-NLS-1$

			final Label oldNameLabel = new Label(container, SWT.NONE);
			oldNameLabel.setLayoutData(new GridData(GridData.FILL,
					GridData.CENTER, true, false));
//			oldNameLabel.setText(district.getName().getText());

			final Label newNameLabel = new Label(container, SWT.NONE);
			final GridData gridData = new GridData(GridData.END,
					GridData.CENTER, false, false);
			gridData.horizontalIndent = 20;
			newNameLabel.setLayoutData(gridData);
			newNameLabel.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditorDialog.newDistrictNameLabelText")); //$NON-NLS-1$

			nameField = new Text(container, SWT.BORDER);
			nameField.setLayoutData(new GridData(GridData.FILL,
					GridData.CENTER, true, false));

			configureShell(getShell(), Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditorDialog.editDistrictDialogTitle")); //$NON-NLS-1$
		}//else if
		else
			throw new IllegalArgumentException("Unknown imput object type: "+input.getClass().getName()); //$NON-NLS-1$
		
		return container;
	}

	protected void configureShell(Shell shell, String title) {
		super.configureShell(shell);
		if (title != null)
			shell.setText(title);
	}

	public boolean doFinish(){
		GeographyTemplateDataAdmin geoAdmin = new GeographyTemplateDataAdmin();
		if(input instanceof City){
			City city = (City) input;
			city.getName().setText(NLLocale.getDefault().getLanguage(), nameField.getText());

			geoAdmin.storeGeographyTemplateCityData(city);
		}//if

		return true;
	}
}
