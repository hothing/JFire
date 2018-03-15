package org.nightlabs.jfire.geography.admin.ui.templatedata.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.eclipse.ui.dialog.ResizableTrayDialog;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.GeographyTemplateDataManagerRemote;
import org.nightlabs.jfire.geography.Location;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.admin.ui.templatedata.CityList;
import org.nightlabs.jfire.geography.admin.ui.templatedata.CountryList;
import org.nightlabs.jfire.geography.admin.ui.templatedata.RegionList;
import org.nightlabs.jfire.geography.id.CityID;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.jfire.geography.id.RegionID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.NLLocale;

import au.com.bytecode.opencsv.CSVReader;

public class ImportCSVActionDelegate
{
	private static Logger logger = Logger.getLogger(ImportCSVActionDelegate.class);

	private static final CountryID COUNTRY_ID_CANCEL = CountryID.create("COUNTRY_ID_CANCEL_" + System.currentTimeMillis()); //$NON-NLS-1$
	private static final CountryID COUNTRY_ID_SKIP = CountryID.create("COUNTRY_ID_SKIP_" + System.currentTimeMillis()); //$NON-NLS-1$
	private static final CountryID COUNTRY_ID_SKIP_FOLLOWING = CountryID.create("COUNTRY_ID_SKIP_FOLLOWING_" + System.currentTimeMillis()); //$NON-NLS-1$

	private static final RegionID REGION_ID_CANCEL = RegionID.create("REGION_ID_CANCEL_" + System.currentTimeMillis(), "R", "R"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final RegionID REGION_ID_SKIP = RegionID.create("REGION_ID_SKIP_" + System.currentTimeMillis(), "R", "R"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final RegionID REGION_ID_SKIP_FOLLOWING = RegionID.create("REGION_ID_SKIP_FOLLOWING_" + System.currentTimeMillis(), "R", "R"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private static final CityID CITY_ID_CANCEL = CityID.create("CITY_ID_CANCEL_" + System.currentTimeMillis(), "C", "C"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final CityID CITY_ID_SKIP = CityID.create("CITY_ID_SKIP_" + System.currentTimeMillis(), "C", "C"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final CityID CITY_ID_SKIP_FOLLOWING = CityID.create("CITY_ID_SKIP_FOLLOWING_" + System.currentTimeMillis(), "C", "C"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public void run() {
		FileDialog fileDialog = new FileDialog(RCPUtil.getActiveShell());
		final String fileName = fileDialog.open();
		if (fileName == null)
			return;

		final Display display = RCPUtil.getActiveShell().getDisplay();

		Job job = new Job(org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.jobImportCSV")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception
			{
				monitor.beginTask(org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.monitorImportCSV"), 100); //$NON-NLS-1$
				try {
					Geography geography = Geography.sharedInstance();

					Map<String, CountryID> countryName2countryID = new HashMap<String, CountryID>();
					Map<String, RegionID> regionName2regionID = new HashMap<String, RegionID>();
					Map<String, CityID> cityName2cityID = new HashMap<String, CityID>();

					Map<CountryID, Map<RegionID, Map<CityID, Set<String>>>> countryID2regionID2cityID2locationNames = new HashMap<CountryID, Map<RegionID,Map<CityID,Set<String>>>>();

					int locationCountToBeStored = 0;
					File f = new File(fileName);
					Reader r = new InputStreamReader(new FileInputStream(f), IOUtil.CHARSET_UTF_8);
					try {
						CSVReader csvReader = new CSVReader(r, ';');
						try {
							String[] fields;
							int lineNumber = 0;
							iterateCSVLines: while ((fields = csvReader.readNext()) != null) {
								++lineNumber;
								if (fields.length != 4) {
									logger.error("Error in line " + lineNumber + ": Number of fields expected is 4, number of fields found: " + fields.length); //$NON-NLS-1$ //$NON-NLS-2$
									continue iterateCSVLines;
								}

								if (lineNumber == 1)
									continue iterateCSVLines;

								String countryName = fields[0];
								String regionName = fields[1];
								String cityName = fields[2];
								String locationName = fields[3];

								CountryID countryID = countryName2countryID.get(countryName);
								if (countryID == null) {
									Collection<Country> countries = geography.findCountriesByCountryName(countryName, NLLocale.getDefault(), Geography.FIND_MODE_BEGINS_WITH);
									for (Country country : countries) {
										if (countryName.equals(country.getName().getText())) {
											countryID = CountryID.create(country);
											break;
										}
									}
								}

								if (countryID == null) {
									countryID = selectCountry(display, lineNumber, fields, countryName);
									if (countryID == COUNTRY_ID_CANCEL)
										return Status.CANCEL_STATUS;

									if (countryID == COUNTRY_ID_SKIP)
										continue iterateCSVLines;

									if (countryID == COUNTRY_ID_SKIP_FOLLOWING)
										break iterateCSVLines;

									countryName2countryID.put(countryName, countryID);
								}

								RegionID regionID = regionName2regionID.get(regionName);
								if (regionID == null) {
									Collection<Region> regions = geography.findRegionsByRegionName(countryID, regionName, NLLocale.getDefault(), Geography.FIND_MODE_BEGINS_WITH);
									for (Region region : regions) {
										if (regionName.equals(region.getName().getText())) {
											regionID = RegionID.create(region);
											break;
										}
									}
								}

								if (regionID == null) {
									regionID = selectRegion(display, lineNumber, fields, countryID, regionName);
									if (regionID == REGION_ID_CANCEL)
										return Status.CANCEL_STATUS;

									if (regionID == REGION_ID_SKIP)
										continue iterateCSVLines;

									if (regionID == REGION_ID_SKIP_FOLLOWING)
										break iterateCSVLines;

									regionName2regionID.put(regionName, regionID);
								}

								CityID cityID = cityName2cityID.get(cityName);
								if (cityID == null) {
									Collection<City> cities = geography.findCitiesByCityName(regionID, cityName, NLLocale.getDefault(), Geography.FIND_MODE_BEGINS_WITH);
									for (City city : cities) {
										if (cityName.equals(city.getName().getText())) {
											cityID = CityID.create(city);
											break;
										}
									}
								}

								if (cityID == null) {
									cityID = selectCity(display, lineNumber, fields, regionID, cityName);
									if (cityID == CITY_ID_CANCEL)
										return Status.CANCEL_STATUS;

									if (cityID == CITY_ID_SKIP)
										continue iterateCSVLines;

									if (cityID == CITY_ID_SKIP_FOLLOWING)
										break iterateCSVLines;

									cityName2cityID.put(cityName, cityID);
								}

								Map<RegionID, Map<CityID, Set<String>>> regionID2cityID2locationNames = countryID2regionID2cityID2locationNames.get(countryID);
								if (regionID2cityID2locationNames == null) {
									regionID2cityID2locationNames = new HashMap<RegionID, Map<CityID,Set<String>>>();
									countryID2regionID2cityID2locationNames.put(countryID, regionID2cityID2locationNames);
								}

								Map<CityID, Set<String>> cityID2locationNames = regionID2cityID2locationNames.get(regionID);
								if (cityID2locationNames == null) {
									cityID2locationNames = new HashMap<CityID, Set<String>>();
									regionID2cityID2locationNames.put(regionID, cityID2locationNames);
								}

								Set<String> locationNames = cityID2locationNames.get(cityID);
								if (locationNames == null) {
									locationNames = new HashSet<String>();
									cityID2locationNames.put(cityID, locationNames);
								}

								locationNames.add(locationName);
								++locationCountToBeStored;
							} // while ((fields = csvReader.readNext()) != null) {
						} finally {
							csvReader.close();
						}
					} finally {
						r.close();
					}
					monitor.worked(10);


					// Filter existing locations already now - not while writing, because this would cause re-fetching the geography data hundreds of times.
					// Cache cities for same reason.
					Map<CityID, City> cityID2city = new HashMap<CityID, City>();
					ProgressMonitor subMonitor = new SubProgressMonitor(monitor, 10);
					subMonitor.beginTask(org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.monitorStoreLocations"), locationCountToBeStored); //$NON-NLS-1$
					try {
						for (Map.Entry<CountryID, Map<RegionID, Map<CityID, Set<String>>>> me1 : countryID2regionID2cityID2locationNames.entrySet()) {
							if (monitor.isCanceled())
								return Status.CANCEL_STATUS;

							for (Map.Entry<RegionID, Map<CityID, Set<String>>> me2 : me1.getValue().entrySet()) {
								if (monitor.isCanceled())
									return Status.CANCEL_STATUS;

								for (Map.Entry<CityID, Set<String>> me3 : me2.getValue().entrySet()) {
									if (monitor.isCanceled())
										return Status.CANCEL_STATUS;

									CityID cityID = me3.getKey();
									City city = geography.getCity(cityID, true);
									cityID2city.put(cityID, city);
									iterateNewLocationNames: for (Iterator<String> itLocationName = me3.getValue().iterator(); itLocationName.hasNext(); ) {
										String locationName = itLocationName.next();

										if (monitor.isCanceled())
											return Status.CANCEL_STATUS;

										Collection<Location> locations = geography.findLocationsByLocationName(cityID, locationName, NLLocale.getDefault(), Geography.FIND_MODE_BEGINS_WITH);
										for (Location location : locations) {
											if (locationName.equals(location.getName().getText())) {
												logger.info("Location \"" + locationName + "\" already exists in city \"" + city.getName().getText() + "\". Removing it from candidates."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
												itLocationName.remove();
												--locationCountToBeStored;
												subMonitor.worked(1);
												continue iterateNewLocationNames; // exists already - don't register it again
											}
										}

										subMonitor.worked(1);
									}
								}
							}
						}
					} finally {
						subMonitor.done();
					}

					if (locationCountToBeStored < 0) {
						logger.warn("locationCountToBeStored < 0", new Exception("StackTrace")); //$NON-NLS-1$ //$NON-NLS-2$
						locationCountToBeStored = 0;
					}

					subMonitor = new SubProgressMonitor(monitor, 80);
					subMonitor.beginTask(org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.subMonitorStoreLocations"), locationCountToBeStored); //$NON-NLS-1$
					try {
						GeographyTemplateDataManagerRemote m = JFireEjb3Factory.getRemoteBean(GeographyTemplateDataManagerRemote.class, Login.getLogin().getInitialContextProperties());
						for (Map.Entry<CountryID, Map<RegionID, Map<CityID, Set<String>>>> me1 : countryID2regionID2cityID2locationNames.entrySet()) {
							if (monitor.isCanceled())
								return Status.CANCEL_STATUS;

							for (Map.Entry<RegionID, Map<CityID, Set<String>>> me2 : me1.getValue().entrySet()) {
								if (monitor.isCanceled())
									return Status.CANCEL_STATUS;

								for (Map.Entry<CityID, Set<String>> me3 : me2.getValue().entrySet()) {
									if (monitor.isCanceled())
										return Status.CANCEL_STATUS;

									CityID cityID = me3.getKey();
//									City city = geography.getCity(cityID, true);
									City city = cityID2city.get(cityID);
									if (city == null)
										throw new IllegalStateException("cityID2city.get(cityID) returned null! " + cityID); //$NON-NLS-1$

									for (String locationName : me3.getValue()) {
										if (monitor.isCanceled())
											return Status.CANCEL_STATUS;

//										Collection<Location> locations = geography.findLocationsByLocationName(cityID, locationName, NLLocale.getDefault(), Geography.FIND_MODE_BEGINS_WITH);
//										for (Location location : locations) {
//											if (locationName.equals(location.getName().getText())) {
//												logger.info("Location \"" + locationName + "\" already exists in city \"" + city.getName().getText() + "\".");
//												subMonitor.worked(1);
//												continue iterateNewLocationNames; // exists already - don't register it again
//											}
//										}
										logger.info("Creating location \"" + locationName + "\" in city \"" + city.getName().getText() + "\"."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

										Location location = new Location(IDGenerator.getOrganisationID(), ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(Location.class)), city);
										location.getName().setText(NLLocale.getDefault().getLanguage(), locationName);
										m.storeGeographyTemplateLocationData(location);
										subMonitor.worked(1);
									}
								}
							}
						}
					} finally {
						subMonitor.done();
					}

					return Status.OK_STATUS;
				} finally {
					monitor.done();
				}
			}
		};
		job.setUser(true);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}

	private CountryID selectCountry(Display display, final int csvLineNumber, final String[] fields, final String countryName) {
		final CountryID[] res = new CountryID[1];
		display.syncExec(new Runnable() {
			public void run() {
				SelectCountryDialog selectCountryDialog = new SelectCountryDialog(RCPUtil.getActiveShell(), csvLineNumber, fields, countryName);
				if (Dialog.OK != selectCountryDialog.open())
					res[0] = COUNTRY_ID_CANCEL;
				else
					res[0] = selectCountryDialog.getCountryID();
			}
		});
		return res[0];
	}

	private RegionID selectRegion(Display display, final int csvLineNumber, final String[] fields, final CountryID countryID, final String regionName) {
		final RegionID[] res = new RegionID[1];
		display.syncExec(new Runnable() {
			public void run() {
				SelectRegionDialog selectRegionDialog = new SelectRegionDialog(RCPUtil.getActiveShell(), csvLineNumber, fields, countryID, regionName);
				if (Dialog.OK != selectRegionDialog.open())
					res[0] = REGION_ID_CANCEL;
				else
					res[0] = selectRegionDialog.getRegionID();
			}
		});
		return res[0];
	}

	private CityID selectCity(Display display, final int csvLineNumber, final String[] fields, final RegionID regionID, final String cityName) {
		final CityID[] res = new CityID[1];
		display.syncExec(new Runnable() {
			public void run() {
				SelectCityDialog selectCityDialog = new SelectCityDialog(RCPUtil.getActiveShell(), csvLineNumber, fields, regionID, cityName);
				if (Dialog.OK != selectCityDialog.open())
					res[0] = CITY_ID_CANCEL;
				else
					res[0] = selectCityDialog.getCityID();
			}
		});
		return res[0];
	}

	private static String fieldsToString(String[] fields)
	{
		StringBuilder sb = new StringBuilder();

		for (String field : fields) {
			if (sb.length() > 0)
				sb.append(" | "); //$NON-NLS-1$

			sb.append(field);
		}

		return sb.toString();
	}

	private class SelectCountryDialog extends ResizableTrayDialog
	{
		private CountryList countryList;
		private String countryName;
		private CountryID countryID;
		private String[] fields;
		private int csvLineNumber;

		public SelectCountryDialog(Shell shell, int csvLineNumber, String[] fields, String countryName) {
			super(shell, Messages.RESOURCE_BUNDLE);
			this.countryName = countryName;
			this.fields = fields;
			this.csvLineNumber = csvLineNumber;
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.newShell.text")); //$NON-NLS-1$
		}

		@Override
		protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
			Button b = super.createButton(parent, id, label, defaultButton);
			if (id == OK)
				b.setEnabled(false);
			return b;
		}

		private static final int SKIP = 1000;
		private static final int SKIP_FOLLOWING = 1001;

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			createButton(parent, SKIP, org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectCountryDialog.button.skip"), false); //$NON-NLS-1$
			createButton(parent, SKIP_FOLLOWING, org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectCountryDialog.button.skipAllFollowing"), false); //$NON-NLS-1$
		}

		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == SKIP) {
				countryID = COUNTRY_ID_SKIP;
				close();
				return;
			}
			if (buttonId == SKIP_FOLLOWING) {
				countryID = COUNTRY_ID_SKIP_FOLLOWING;
				close();
				return;
			}

			super.buttonPressed(buttonId);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite comp = (Composite) super.createDialogArea(parent);
			new Label(comp, SWT.NONE).setText(org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectCountryDialog.label.csvLineNumber") + csvLineNumber); //$NON-NLS-1$
			new Label(comp, SWT.NONE).setText(org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectCountryDialog.label.fields") + fieldsToString(fields)); //$NON-NLS-1$
			new Label(comp, SWT.NONE).setText(org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectCountryDialog.label.countryName") + countryName); //$NON-NLS-1$
			countryList = new CountryList(comp);
			countryList.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					Country country = countryList.getFirstSelectedCountry();
					if (country == null)
						countryID = null;
					else
						countryID = CountryID.create(country);

					getButton(OK).setEnabled(countryID != null);
				}
			});
			return comp;
		}

		public CountryID getCountryID() {
			return countryID;
		}
	}

	private class SelectRegionDialog extends ResizableTrayDialog
	{
		private RegionList regionList;
		private CountryID countryID;
		private String regionName;
		private RegionID regionID;
		private String[] fields;
		private int csvLineNumber;

		public SelectRegionDialog(Shell shell, int csvLineNumber, String[] fields, CountryID countryID, String regionName) {
			super(shell, Messages.RESOURCE_BUNDLE);
			this.countryID = countryID;
			this.regionName = regionName;
			this.fields = fields;
			this.csvLineNumber = csvLineNumber;
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectRegionDialog.newShell.text")); //$NON-NLS-1$
		}

		@Override
		protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
			Button b = super.createButton(parent, id, label, defaultButton);
			if (id == OK)
				b.setEnabled(false);
			return b;
		}

		private static final int SKIP = 1000;
		private static final int SKIP_FOLLOWING = 1001;

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			createButton(parent, SKIP, org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectRegionDialog.button.skip"), false); //$NON-NLS-1$
			createButton(parent, SKIP_FOLLOWING, org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectRegionDialog.button.skipAllFollowing"), false); //$NON-NLS-1$
		}

		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == SKIP) {
				regionID = REGION_ID_SKIP;
				close();
				return;
			}
			if (buttonId == SKIP_FOLLOWING) {
				regionID = REGION_ID_SKIP_FOLLOWING;
				close();
				return;
			}

			super.buttonPressed(buttonId);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite comp = (Composite) super.createDialogArea(parent);
			new Label(comp, SWT.NONE).setText(org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectRegionDialog.label.csvLineNumber") + csvLineNumber); //$NON-NLS-1$
			new Label(comp, SWT.NONE).setText(org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectRegionDialog.label.fields") + fieldsToString(fields)); //$NON-NLS-1$
			new Label(comp, SWT.NONE).setText(org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectRegionDialog.label.regionName") + regionName); //$NON-NLS-1$
			regionList = new RegionList(comp);
			regionList.setCountryID(countryID);
			regionList.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					Region region = regionList.getFirstSelectedRegion();
					if (region == null)
						regionID = null;
					else
						regionID = RegionID.create(region);

					getButton(OK).setEnabled(regionID != null);
				}
			});
			return comp;
		}

		public RegionID getRegionID() {
			return regionID;
		}
	}

	private class SelectCityDialog extends ResizableTrayDialog
	{
		private CityList cityList;
		private RegionID regionID;
		private String cityName;
		private CityID cityID;
		private String[] fields;
		private int csvLineNumber;

		public SelectCityDialog(Shell shell, int csvLineNumber, String[] fields, RegionID regionID, String cityName) {
			super(shell, Messages.RESOURCE_BUNDLE);
			this.regionID = regionID;
			this.cityName = cityName;
			this.fields = fields;
			this.csvLineNumber = csvLineNumber;
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectCityDialog.newShell.text")); //$NON-NLS-1$
		}

		@Override
		protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
			Button b = super.createButton(parent, id, label, defaultButton);
			if (id == OK)
				b.setEnabled(false);
			return b;
		}

		private static final int SKIP = 1000;
		private static final int SKIP_FOLLOWING = 1001;

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			createButton(parent, SKIP, org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectCityDialog.button.skip"), false); //$NON-NLS-1$
			createButton(parent, SKIP_FOLLOWING, org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectCityDialog.button.skipAllFollowing"), false); //$NON-NLS-1$
		}

		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == SKIP) {
				cityID = CITY_ID_SKIP;
				close();
				return;
			}
			if (buttonId == SKIP_FOLLOWING) {
				cityID = CITY_ID_SKIP_FOLLOWING;
				close();
				return;
			}

			super.buttonPressed(buttonId);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite comp = (Composite) super.createDialogArea(parent);
			new Label(comp, SWT.NONE).setText(org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectCityDialog.label.csvLineNumber") + csvLineNumber); //$NON-NLS-1$
			new Label(comp, SWT.NONE).setText(org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectCityDialog.label.fields") + fieldsToString(fields)); //$NON-NLS-1$
			new Label(comp, SWT.NONE).setText(org.nightlabs.jfire.geography.admin.ui.resource.Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.action.ImportCSVActionDelegate.SelectCityDialog.label.cityName") + cityName); //$NON-NLS-1$
			cityList = new CityList(comp);
			cityList.setRegionID(regionID);
			cityList.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					City city = cityList.getFirstSelectedCity();
					if (city == null)
						cityID = null;
					else
						cityID = CityID.create(city);

					getButton(OK).setEnabled(cityID != null);
				}
			});
			return comp;
		}

		public CityID getCityID() {
			return cityID;
		}
	}
}
