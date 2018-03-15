package org.nightlabs.jfire.geography.admin.ui.templatedata.management;


import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.nightlabs.base.ui.part.ControllablePart;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.login.part.LSDPartController;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.District;
import org.nightlabs.jfire.geography.Location;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;


public class GeographyTemplateDataInformationView extends ViewPart implements
ControllablePart{

	public final static String ID_VIEW = "org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView"; //$NON-NLS-1$

//	private Label[] geoInfoLabels = null;
//	private Label[] geoInfoData = null;

	private Label countryLbl;
	private Label regionLbl;
	private Label citiyLbl;
	private Label districtLbl;
	private Label locationLbl;

	private Label countryData;
	private Label regionData;
	private Label cityData;
	private Label districtData;
	private Label locationData;

	private GridData gridData;

//	class ViewLabelProvider extends LabelProvider /*implements ITableLabelProvider*/ {
//	public String getColumnText(Object obj, int index) {
//	return getText(obj);
//	}
//	public Image getColumnImage(Object obj, int index) {
//	return getImage(obj);
//	}
//	public Image getImage(Object obj) {
//	return null;
//	}
//	}

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public GeographyTemplateDataInformationView() {
		LSDPartController.sharedInstance().registerPart(this);
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		LSDPartController.sharedInstance().createPartControl(this, parent);
	}

	public void updateGeographyInfomation(final Object element){
		new Job(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.loadingGeographyDataJob")) { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if(element instanceof Country){
							Country country = (Country)element;
							countryLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.countryNameLabelText")); //$NON-NLS-1$
							countryData.setText(country.getName().getText());
							regionLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.numberOfRegionsLabelText")); //$NON-NLS-1$
							regionData.setText(Integer.toString(country.getRegions().size()));
							citiyLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.numberOfCitiesLabelText")); //$NON-NLS-1$

							int totalCities = 0;
							int totalLocations = 0;
							int totalDistricts = 0;
							Collection<Region> regions = country.getRegions();
							for(Region region : regions){
								Collection<City> cities = region.getCities();
								totalCities += cities.size();
								for(City city : cities){
									Collection<Location> locations = city.getLocations();
									totalLocations += locations.size();
									totalDistricts += city.getDistricts().size();
								}//for
							}//for

							cityData.setText(Integer.toString(totalCities));

							districtLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.numberOfDistrictsLabelText")); //$NON-NLS-1$
							districtData.setText(Integer.toString(totalDistricts));

							locationLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.numberOfLocationsLabelText")); //$NON-NLS-1$
							locationData.setText(Integer.toString(totalLocations));
						}//if
						else if (element instanceof Region){
							Region region = (Region)element;

							int totalLocations = 0;
							int totalDistricts = 0;
							for(City city : region.getCities()){
								Collection<Location> locations = city.getLocations();
								totalLocations += locations.size();
								totalDistricts += city.getDistricts().size();
							}//for

							countryData.setText(region.getCountry().getName().getText());
							regionLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.regionNameLabelText")); //$NON-NLS-1$
							regionData.setText(region.getName().getText());
							citiyLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.numberOfCitiesLabelText")); //$NON-NLS-1$
							cityData.setText(Integer.toString(region.getCities().size()));

							districtLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.numberOfDistrictsLabelText")); //$NON-NLS-1$
							districtData.setText(Integer.toString(totalDistricts));

							locationLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.numberOfLocationsLabelText")); //$NON-NLS-1$
							locationData.setText(Integer.toString(totalLocations));
						}//else if
						else if (element instanceof City){
							City city = (City)element;

							int totalLocations = 0;
							int totalDistricts = 0;
							Collection<Location> locations = city.getLocations();
							totalLocations += locations.size();
							totalDistricts += city.getDistricts().size();

							countryData.setText(city.getRegion().getCountry().getName().getText());
							regionLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.regionNameLabelText")); //$NON-NLS-1$
							regionData.setText(city.getRegion().getName().getText());
							citiyLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.cityNameLabelText")); //$NON-NLS-1$
							cityData.setText(city.getName().getText());

							districtLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.numberOfDistrictsLabelText")); //$NON-NLS-1$
							districtData.setText(Integer.toString(totalDistricts));

							locationLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.numberOfLocationsLabelText")); //$NON-NLS-1$
							locationData.setText(Integer.toString(totalLocations));
						}
						else if (element instanceof District){
							District district = (District)element;

							countryData.setText(district.getCity().getRegion().getCountry().getName().getText());
							regionLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.regionNameLabelText")); //$NON-NLS-1$
							regionData.setText(district.getCity().getRegion().getName().getText());
							citiyLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.cityNameLabelText")); //$NON-NLS-1$
							cityData.setText(district.getCity().getName().getText());

							districtLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.districtPositionLabelText")); //$NON-NLS-1$
							districtData.setText(String.format(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.coordinatesLabelTemplate"), district.getLatitude(), district.getLongitude())); //$NON-NLS-1$

							locationLbl.setText(""); //$NON-NLS-1$
							locationData.setText(""); //$NON-NLS-1$
						}
						else if (element instanceof Location){
							Location location = (Location)element;

							countryData.setText(location.getCity().getRegion().getCountry().getName().getText());
							regionLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.regionNameLabelText")); //$NON-NLS-1$
							regionData.setText(location.getCity().getRegion().getName().getText());
							citiyLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.cityNameLabelText")); //$NON-NLS-1$
							cityData.setText(location.getCity().getName().getText());
							if(location.getDistrict() != null){
								District district = location.getDistrict();
								districtLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.districtPositionLabelText")); //$NON-NLS-1$
								districtData.setText(String.format(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.coordinatesLabelTemplate"), district.getLatitude(), district.getLongitude())); //$NON-NLS-1$
							}//if

							locationLbl.setText(""); //$NON-NLS-1$
							locationData.setText(""); //$NON-NLS-1$
						}
					}
				});
				return Status.OK_STATUS;
			}
		}.schedule();
	}//if

	@Override
	public void setFocus() {
	}

	public boolean canDisplayPart() {
		return Login.isLoggedIn();
	}

	public void createPartContents(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayout(layout);

		gridData = new GridData();

		countryLbl = new Label(parent, SWT.LEFT);
		countryLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.countryNameLabelText")); //$NON-NLS-1$
		countryData = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		countryData.setLayoutData(gridData);
		countryLbl.setLayoutData(gridData);
		
		regionLbl = new Label(parent, SWT.LEFT);
		regionLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.numberOfRegionsLabelText")); //$NON-NLS-1$
		regionData = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		regionData.setLayoutData(gridData);

		citiyLbl = new Label(parent, SWT.LEFT);
		citiyLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.numberOfCitiesLabelText")); //$NON-NLS-1$
		cityData = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		cityData.setLayoutData(gridData);

		districtLbl = new Label(parent, SWT.LEFT);
		districtLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.numberOfDistrictsLabelText")); //$NON-NLS-1$
		districtData = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		districtData.setLayoutData(gridData);

		locationLbl = new Label(parent, SWT.LEFT);
		locationLbl.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView.numberOfLocationsLabelText")); //$NON-NLS-1$
		locationData = new Label(parent, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		locationData.setLayoutData(gridData);
	}
}