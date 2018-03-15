package org.nightlabs.jfire.geography.admin.ui.templatedata.management.city;

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
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.admin.ui.templatedata.editor.name.GeographyNameTableComposite;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.util.NLLocale;
import org.nightlabs.util.Util;

public class CityNewWizardPage
extends WizardHopPage
{
	private Country preselectedCountry;
	private Region preselectedRegion;

	private XComboComposite<Country> countryCombo;
	private XComboComposite<Region> regionCombo;

	private GeographyNameTableComposite geographyNameTableComposite;
	/**
	 * @param preselectedRegion the currently selected region or <code>null</code> if none is preselected.
	 */
	public CityNewWizardPage(Country preselectedCountry, Region preselectedRegion)
	{
		super("wizardPage"); //$NON-NLS-1$
		setTitle(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.city.CityNewWizardPage.wizardPageTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.city.CityNewWizardPage.wizardPageDescription")); //$NON-NLS-1$
		this.preselectedCountry = preselectedCountry;
		if (preselectedRegion != null){
//			this.preselectedCountry = preselectedRegion.getCountry();
			this.preselectedRegion = preselectedRegion;
		}//if
		else{
			if (preselectedCountry != null && preselectedCountry.getRegions().size() > 0){
				this.preselectedRegion = Geography.sharedInstance().getRegionsSorted(CountryID.create(preselectedCountry), NLLocale.getDefault()).get(0);
			}//if
		}//else
	}

	@Override
	public Control createPageContents(Composite parent)
	{
		XComposite wizardPageComposite = new XComposite(parent, SWT.NONE);
		wizardPageComposite.getGridLayout().numColumns = 1;

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

		countryCombo = new XComboComposite<Country>(wizardPageComposite, SWT.NONE | SWT.READ_ONLY, Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.city.CityNewWizardPage.countryComboLabelText"), countryLabelProvider); //$NON-NLS-1$
		countryCombo.addElements(Geography.sharedInstance().getCountriesSorted(NLLocale.getDefault())); // Marco: we don't need to pass these as parameter to the constructor as Geography is readily available everywhere.
		countryCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event)
			{
				countryComboSelectionChanged();
			}
		});

		if (preselectedCountry != null)
			countryCombo.setSelection(preselectedCountry);

		LabelProvider regionLabelProvider = new LabelProvider() {
			@Override
			public String getText(Object element)
			{
				return ((Region)element).getName().getText();
			}
		};

		regionCombo = new XComboComposite<Region>(wizardPageComposite, SWT.NONE | SWT.READ_ONLY, Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.city.CityNewWizardPage.regionComboLabelText"), regionLabelProvider); //$NON-NLS-1$
		regionCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event)
			{
				regionComboSelectionChanged();
			}
		});

		countryComboSelectionChanged();
		
		if (preselectedRegion != null)
			regionCombo.setSelection(preselectedRegion);
		
		return wizardPageComposite;
	}

	private Country selectedCountry;

	private void countryComboSelectionChanged()
	{
		selectedCountry = countryCombo.getSelectedElement();

		regionCombo.removeAll();

		if (selectedCountry != null) {
			regionCombo.addElements(Geography.sharedInstance().getRegionsSorted(CountryID.create(selectedCountry), NLLocale.getDefault()));

			if (Util.equals(preselectedCountry, selectedCountry)) // Marco: this Utils method is null-safe - there can never be a NPE - that's not necessary here, because we're sure that selectedCountryID can't be null, but I wanted to show you this method ;-)
				if(preselectedRegion != null)
					regionCombo.setSelection(preselectedRegion);
		}//if
		else{
			countryCombo.setSelection(0);
		}//else

		regionComboSelectionChanged();
	}

	private Region selectedRegion;

	private void regionComboSelectionChanged()
	{
		selectedRegion = regionCombo.getSelectedElement();
		if(selectedRegion == null)
			setErrorMessage(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.city.CityNewWizardPage.errorMessage.selectRegion")); //$NON-NLS-1$
		else
			setErrorMessage(null);
////		((CityNewWizard)getWizard()).needRefreshAll();
//
//		IStatus status = null;
//		if(selectedRegion == null){
//			status = new Status(IStatus.ERROR, "not_used", 0,
//					"Region cannot be null.", null);
//		}//if
//		else{
//			status = new Status(IStatus.OK, "not_used", 0, "", null);
//		}
//
//		String message= status.getMessage();
//		if (message.length() == 0) message= null;
//		switch (status.getSeverity()) {
//		case IStatus.ERROR:
//			setErrorMessage("Region cannot be null");
//			setMessage(message, WizardPage.ERROR);
//			break;
//		case IStatus.OK:
//			setErrorMessage(null);
//			setMessage(message);
//			break;
//		}//switch

		getWizard().getContainer().updateButtons();
	}

	public Country getSelectedCountry()
	{
		return selectedCountry;
	}

	public Region getSelectedRegion()
	{
		return selectedRegion;
	}

	protected GeographyNameTableComposite getGeographyNameTableComposite(){
		return geographyNameTableComposite;
	}

	@Override
	public boolean isPageComplete() {
		if (getErrorMessage() != null){
			return false;
		}//if
		return true;
	}
}