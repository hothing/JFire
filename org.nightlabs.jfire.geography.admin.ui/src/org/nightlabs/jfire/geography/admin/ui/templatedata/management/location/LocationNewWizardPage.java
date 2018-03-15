package org.nightlabs.jfire.geography.admin.ui.templatedata.management.location;

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
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.admin.ui.templatedata.editor.name.GeographyNameTableComposite;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.jfire.geography.id.RegionID;
import org.nightlabs.util.NLLocale;
import org.nightlabs.util.Util;

public class LocationNewWizardPage
extends WizardHopPage
{
	private Country preselectedCountry;
	private Region preselectedRegion;
	private City preselectedCity;

	private XComboComposite<Country> countryCombo;
	private XComboComposite<Region> regionCombo;
	private XComboComposite<City> cityCombo;
//	private ComboComposite<District> districtCombo;

	private GeographyNameTableComposite geographyNameTableComposite;

	/**
	 * @param preselectedRegion The currently selected region or <code>null</code> if none is preselected.
	 */
	public LocationNewWizardPage(Country preselectedCountry, Region preselectedRegion, City preselectedCity)
	{
		super(LocationNewWizardPage.class.getName());
		setTitle(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.location.LocationNewWizardPage.wizardPageTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.location.LocationNewWizardPage.wizardPageDescription")); //$NON-NLS-1$
		this.preselectedCountry = preselectedCountry;
		if(preselectedRegion != null){
//			this.preselectedCountry = preselectedRegion.getCountry();
			this.preselectedRegion = preselectedRegion;
			
			if (preselectedCity != null){
				this.preselectedCity = preselectedCity;
			}//if
			else{
				this.preselectedCity = Geography.sharedInstance().getCitiesSorted(RegionID.create(preselectedRegion), NLLocale.getDefault()).get(0);
			}//else
		}//if
		else{
			if (preselectedCountry != null && preselectedCountry.getRegions().size() > 0){
				this.preselectedRegion = Geography.sharedInstance().getRegionsSorted(CountryID.create(preselectedCountry), NLLocale.getDefault()).get(0);
				if(this.preselectedRegion.getCities().size() > 0)
					this.preselectedCity = Geography.sharedInstance().getCitiesSorted(RegionID.create(this.preselectedRegion), NLLocale.getDefault()).get(0);
			}//if
		}//else
	}

	@Override
	public Control createPageContents(Composite parent)
	{
		XComposite wizardPageComposite = new XComposite(parent, SWT.NONE); // Marco: you should always use our XComposite as it has some additional features
		wizardPageComposite.getGridLayout().numColumns = 1; // Marco: for example, it uses a GridLayout by default ;-)
		wizardPageComposite.getGridLayout().verticalSpacing = 9;
		
		geographyNameTableComposite = new GeographyNameTableComposite(wizardPageComposite, SWT.NONE, false);
		GridData gd = new GridData(GridData.FILL_BOTH);
		geographyNameTableComposite.setLayoutData(gd);

		//Country
		LabelProvider countryLabelProvider = new LabelProvider() {
			@Override
			public String getText(Object element)
			{
				return ((Country)element).getName().getText();
			}
		};

		countryCombo = new XComboComposite<Country>(wizardPageComposite, SWT.NONE | SWT.READ_ONLY, Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.location.LocationNewWizardPage.countryComboLabelText"), countryLabelProvider); //$NON-NLS-1$
		countryCombo.addElements(Geography.sharedInstance().getCountriesSorted(NLLocale.getDefault()));
		countryCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event)
			{
				countryComboSelectionChanged();
			}
		});

		if (preselectedCountry != null)
			countryCombo.setSelection(preselectedCountry);

		//Region
		LabelProvider regionLabelProvider = new LabelProvider() {
			@Override
			public String getText(Object element)
			{
				return ((Region)element).getName().getText();
			}
		};

		regionCombo = new XComboComposite<Region>(wizardPageComposite, SWT.NONE | SWT.READ_ONLY, Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.location.LocationNewWizardPage.regionComboLabelText"), regionLabelProvider); //$NON-NLS-1$
		if (preselectedCountry != null) {
			regionCombo.addElements(Geography.sharedInstance().getRegionsSorted(CountryID.create(preselectedCountry), NLLocale.getDefault()));
		}
		regionCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event)
			{
				regionComboSelectionChanged();
			}
		});
		
		//City
		LabelProvider cityLabelProvider = new LabelProvider() {
			@Override
			public String getText(Object element)
			{
				return ((City)element).getName().getText();
			}
		};

		cityCombo = new XComboComposite<City>(wizardPageComposite, SWT.NONE | SWT.READ_ONLY, Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.location.LocationNewWizardPage.cityComboLabelText"), cityLabelProvider); //$NON-NLS-1$
		if(preselectedRegion != null) {
			cityCombo.addElements(Geography.sharedInstance().getCitiesSorted(RegionID.create(preselectedRegion), NLLocale.getDefault()));
		}
		cityCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event)
			{
				cityComboSelectionChanged();
			}
		});

		countryComboSelectionChanged();
		
		return wizardPageComposite;
	}

	private Country selectedCountry;
	private void countryComboSelectionChanged()
	{
		selectedCountry = countryCombo.getSelectedElement();

		regionCombo.removeAll();

		if (selectedCountry != null) {
			regionCombo.addElements(Geography.sharedInstance().getRegionsSorted(CountryID.create(selectedCountry),
					NLLocale.getDefault()));

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
//		((LocationNewWizard)getWizard()).needRefreshAll();
		
		cityCombo.removeAll();

		if (selectedRegion != null) {
			cityCombo.addElements(Geography.sharedInstance().getCitiesSorted(RegionID.create(selectedRegion),
					NLLocale.getDefault()));

			if (preselectedCity != null && Util.equals(preselectedRegion, selectedRegion)) // Marco: this Utils method is null-safe - there can never be a NPE - that's not necessary here, because we're sure that selectedCountryID can't be null, but I wanted to show you this method ;-)
				cityCombo.setSelection(preselectedCity);
			else
				cityCombo.setSelection(0);
		}//if
		else{
			regionCombo.setSelection(0);
		}//else

		cityComboSelectionChanged();
	}

	private City selectedCity;
	private void cityComboSelectionChanged()
	{
		selectedCity = cityCombo.getSelectedElement();
	}
	
	public Country getSelectedCountry()
	{
		return selectedCountry;
	}
	public Region getSelectedRegion()
	{
		return selectedRegion;
	}
	public City getSelectedCity()
	{
		return selectedCity;
	}

	@Override
	public boolean isPageComplete() {
		if (getErrorMessage() != null){
			return false;
		}//if
		return true;
	}
	
	protected GeographyNameTableComposite getGeographyNameTableComposite(){
		return geographyNameTableComposite;
	}
}