package org.nightlabs.jfire.geography.admin.ui.templatedata.management.district;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
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

public class DistrictNewWizardPage
extends WizardHopPage
{
	private Country preselectedCountry;
	private Region preselectedRegion;
	private City preselectedCity;

	private XComboComposite<Country> countryCombo;
	private XComboComposite<Region> regionCombo;
	private XComboComposite<City> cityCombo;
	private Spinner spinnerLatitude;
	private Spinner spinnerLongitude;
	
	private GeographyNameTableComposite geographyNameTableComposite;
	
	private double longitude = 0;
	private double Latitude = 0;
	
	/**
	 * @param preselectedCity The currently selected city or <code>null</code> if none is preselected.
	 */
	public DistrictNewWizardPage(Country preselectedCountry, Region preselectedRegion, City preselectedCity)
	{
		super("wizardPage"); //$NON-NLS-1$
		setTitle(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.district.DistrictNewWizardPage.wizardPageTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.district.DistrictNewWizardPage.wizardPageDescription")); //$NON-NLS-1$
		this.preselectedCountry = preselectedCountry;

		if(preselectedRegion != null){
			this.preselectedCountry = preselectedRegion.getCountry();
			this.preselectedRegion = preselectedRegion;

			if (preselectedCity != null){
				this.preselectedCity = preselectedCity;
			}//if
			else{
				this.preselectedCity = Geography.sharedInstance().getCitiesSorted(RegionID.create(preselectedRegion), NLLocale.getDefault()).get(0);
			}//else
		}//if
		else{
			if(preselectedCountry.getRegions().size() > 0){
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

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalSpan = 2;

		Label labelLongitude = new Label(wizardPageComposite, SWT.NONE);
		labelLongitude.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.district.DistrictNewWizardPage.label.longitude")); //$NON-NLS-1$
		labelLongitude.setLayoutData(gd);
		spinnerLongitude = new Spinner (wizardPageComposite, SWT.NONE);
		spinnerLongitude.setDigits(6);
		spinnerLongitude.setMinimum(0);
		spinnerLongitude.setMaximum(180 *1000000);
		spinnerLongitude.setSelection(30*1000000);
		spinnerLongitude.setIncrement(1*1000000);
		spinnerLongitude.setPageIncrement(1*1000000);
		spinnerLongitude.setLayoutData(gd);
		spinnerLongitude.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  longitude = spinnerLongitude.getSelection();
		    	  
		        }
		      });

		Label labelLatitude = new Label(wizardPageComposite, SWT.NONE);
		labelLatitude.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.district.DistrictNewWizardPage.label.latitude")); //$NON-NLS-1$
		labelLatitude.setLayoutData(gd);
		spinnerLatitude = new Spinner (wizardPageComposite, SWT.NONE);
		spinnerLatitude.setMinimum(0);
		spinnerLatitude.setDigits(6);
		spinnerLatitude.setMaximum(90*1000000);
		spinnerLatitude.setSelection(30*1000000);
		spinnerLatitude.setIncrement(1*1000000);
		spinnerLatitude.setPageIncrement(1*1000000);
		spinnerLatitude.setLayoutData(gd);
		spinnerLatitude.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  Latitude = spinnerLatitude.getSelection();
		    	  
		        }
		      });
		
		
		
		LabelProvider countryLabelProvider = new LabelProvider() {
			@Override
			public String getText(Object element)
			{
				return ((Country)element).getName().getText();
			}
		};

		countryCombo = new XComboComposite<Country>(wizardPageComposite, SWT.NONE, Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.district.DistrictNewWizardPage.countryComboLabelText"), countryLabelProvider); //$NON-NLS-1$
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

		regionCombo = new XComboComposite<Region>(wizardPageComposite, SWT.NONE, Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.district.DistrictNewWizardPage.regionComboLabelText"), regionLabelProvider); //$NON-NLS-1$
		regionCombo.addElements(Geography.sharedInstance().getRegionsSorted(CountryID.create(preselectedCountry), NLLocale.getDefault()));
		regionCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event)
			{
				regionComboSelectionChanged();
			}
		});

		if (preselectedRegion != null)
			regionCombo.setSelection(preselectedRegion);


		LabelProvider cityLabelProvider = new LabelProvider() {
			@Override
			public String getText(Object element)
			{
				return ((City)element).getName().getText();
			}
		};

		cityCombo = new XComboComposite<City>(wizardPageComposite, SWT.NONE, Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.district.DistrictNewWizardPage.cityComboLabelText"), cityLabelProvider); //$NON-NLS-1$
		if(preselectedRegion != null)
			cityCombo.addElements(Geography.sharedInstance().getCitiesSorted(RegionID.create(preselectedRegion),
					NLLocale.getDefault()));
		cityCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event)
			{
				cityComboSelectionChanged();
			}
		});

		if (preselectedCity != null)
			cityCombo.setSelection(preselectedCity);

		countryComboSelectionChanged();
		cityComboSelectionChanged();

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
				regionCombo.setSelection(preselectedRegion);
		}//if
		else{
			countryCombo.setSelection(0);
		}//else

		regionCombo.setSelection(0);
		regionComboSelectionChanged();
	}

	private Region selectedRegion;
	private void regionComboSelectionChanged()
	{
		selectedRegion = regionCombo.getSelectedElement();
	}

	private City selectedCity;
	private void cityComboSelectionChanged()
	{
		selectedCity = cityCombo.getSelectedElement();

		if(selectedCity == null)
			setErrorMessage(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.district.DistrictNewWizardPage.errorMessage.selectCity")); //$NON-NLS-1$
		else
			setErrorMessage(null);
//		checkDataComplete();

		getWizard().getContainer().updateButtons();
	}

//	private void checkDataComplete()
//	{

//	IStatus status = null;
//	if(selectedCity == null) {
//	status = new Status(IStatus.ERROR, "not_used", 0,
//	"City cannot be null.", null);
//	}//if
//	else{
//	status = new Status(IStatus.OK, "not_used", 0, "", null);
//	}

//	String message= status.getMessage();
//	if (message.length() == 0) message= null;
//	switch (status.getSeverity()) {
//	case IStatus.ERROR:
//	setErrorMessage("City cannot be null");
//	setMessage(message, WizardPage.ERROR);
//	break;
//	case IStatus.OK:
//	setErrorMessage(null);
//	setMessage(message);
//	break;
//	}//switch
//	}

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

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return Latitude;
	}
}