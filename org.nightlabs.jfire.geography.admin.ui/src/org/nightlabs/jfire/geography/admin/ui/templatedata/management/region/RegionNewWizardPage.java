package org.nightlabs.jfire.geography.admin.ui.templatedata.management.region;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.wizard.WizardHopPage;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.admin.ui.templatedata.editor.name.GeographyNameTableComposite;
import org.nightlabs.util.NLLocale;

public class RegionNewWizardPage
extends WizardHopPage // Marco: even if you don't use a WizardHop now, this page should support to be added to one
{
	private Country preselectedCountry;

	private XComboComposite<Country> countryCombo;

	private GeographyNameTableComposite geographyNameTableComposite;
	/**
	 * @param preselectedRegionID The ID of the currently selected region or <code>null</code> if none is preselected.
	 */
	public RegionNewWizardPage(Country preselectedCountry)
	{
		super(RegionNewWizardPage.class.getName());
		this.preselectedCountry = preselectedCountry;
		setTitle(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.region.RegionNewWizardPage.wizardPageTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.region.RegionNewWizardPage.wizardPageDescription")); //$NON-NLS-1$
	}

// Marco: because the default implementation is error-prone (you easily forget to call setControl(...), our
// API uses a different abstract method which has to return the used control. => less error-prone
	@Override
	public Control createPageContents(Composite parent)
	{
		XComposite wizardPageComposite = new XComposite(parent, SWT.NONE | SWT.READ_ONLY); // Marco: you should always use our XComposite as it has some additional features
		wizardPageComposite.getGridLayout().numColumns = 1; // Marco: for example, it uses a GridLayout by default ;-)
		
		geographyNameTableComposite = new GeographyNameTableComposite(wizardPageComposite, SWT.NONE, false);
		GridData gd = new GridData(GridData.FILL_BOTH);
		geographyNameTableComposite.setLayoutData(gd);

		LabelProvider countryLabelProvider = new LabelProvider() {
			@Override
			public String getText(Object element)
			{
				return ((Country)element).getName().getText();
			}
		};

		countryCombo = new XComboComposite<Country>(wizardPageComposite, SWT.NONE | SWT.READ_ONLY, Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.region.RegionNewWizardPage.countryComboLabelText"), countryLabelProvider); //$NON-NLS-1$
		countryCombo.addElements(Geography.sharedInstance().getCountriesSorted(NLLocale.getDefault())); // Marco: we don't need to pass these as parameter to the constructor as Geography is readily available everywhere.
		countryCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event)
			{
				countryComboSelectionChanged();
			}
		});

		if (preselectedCountry != null)
			countryCombo.setSelection(preselectedCountry);

		countryComboSelectionChanged();
		
		return wizardPageComposite;
	}

	private Country selectedCountry;

	private void countryComboSelectionChanged()
	{
		selectedCountry = countryCombo.getSelectedElement();
	}

// Marco: the following getters are probably needed in the Wizard when it creates the new Region
	public Country getSelectedCountry()
	{
		return selectedCountry;
	}

	@Override
	public boolean isPageComplete()
	{
		return true;
	}
	
	protected GeographyNameTableComposite getGeographyNameTableComposite(){
		return geographyNameTableComposite;
	}
}