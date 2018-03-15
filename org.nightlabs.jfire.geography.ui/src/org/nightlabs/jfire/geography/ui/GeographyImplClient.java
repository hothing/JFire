package org.nightlabs.jfire.geography.ui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

import org.eclipse.core.runtime.ListenerList;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleListener;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.ui.jdo.notification.JDOLifecycleAdapterJob;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.geography.CSV;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.GeographyImplResourceCSV;
import org.nightlabs.jfire.geography.GeographyManagerRemote;
import org.nightlabs.jfire.geography.ui.resource.Messages;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.SimpleLifecycleListenerFilter;
import org.nightlabs.jfire.security.SecurityReflector;

public class GeographyImplClient
extends GeographyImplResourceCSV
{
	private ListenerList geoChangeListeners = new ListenerList();

	public GeographyImplClient()
	{
		JDOLifecycleManager.sharedInstance().addLifecycleListener(myLifecycleListener);
	}

	/**
	 * This method sets the system property {@link Geography#PROPERTY_KEY_GEOGRAPHY_CLASS}
	 * to the fully qualified class name of <code>GeographyImplResourceCSV</code>. This method
	 * does not create a shared instance!
	 */
	public static void register()
	{
		System.setProperty(PROPERTY_KEY_GEOGRAPHY_CLASS, GeographyImplClient.class.getName());
	}

	/**
	 * This method creates a new instance of <code>GeographyImplResourceCSV</code> and sets it
	 * as shared instance. Therefore, a subsequent call to {@link Geography#sharedInstance()} will
	 * return this instance (if it is not overridden by other code). Note, that there is one
	 * shared instance per organisation. The organisationID is determined by {@link SecurityReflector}.
	 */
	public static void createSharedInstance()
	{
		register();
		setSharedInstance(new GeographyImplClient());
	}

	public static InputStream createCSVInputStream(final String csvType, String countryID)
	{
		try {
			GeographyManagerRemote gm = JFireEjb3Factory.getRemoteBean(GeographyManagerRemote.class,
				Login.getLogin().getInitialContextProperties());
			byte[] data = gm.getCSVData(csvType, countryID);
			if (data == null)
				return null;

			return new InflaterInputStream(new ByteArrayInputStream(data));
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	protected InputStream createCountryCSVInputStream()
	{
		return createCSVInputStream(CSV.CSV_TYPE_COUNTRY, ""); //$NON-NLS-1$
	}

	@Override
	protected InputStream createRegionCSVInputStream(String countryID)
	{
		return createCSVInputStream(CSV.CSV_TYPE_REGION, countryID);
	}

	@Override
	protected InputStream createCityCSVInputStream(String countryID)
	{
		return createCSVInputStream(CSV.CSV_TYPE_CITY, countryID);
	}

	@Override
	protected InputStream createZipCSVInputStream(String countryID)
	{
		return createCSVInputStream(CSV.CSV_TYPE_ZIP, countryID);
	}

	@Override
	protected InputStream createDistrictCSVInputStream(String countryID)
	{
		return createCSVInputStream(CSV.CSV_TYPE_DISTRICT, countryID);
	}

	@Override
	protected InputStream createLocationCSVInputStream(String countryID)
	{
		return createCSVInputStream(CSV.CSV_TYPE_LOCATION, countryID);
	}

	public void addGeographyTemplateDataChangedListener(GeographyTemplateDataChangedListener listener) {
		synchronized(geoChangeListeners) {
			geoChangeListeners.add(listener);
		}
	}

	public void removeGeographyTemplateDataChangedListener(GeographyTemplateDataChangedListener listener) {
		synchronized(geoChangeListeners) {
			geoChangeListeners.remove(listener);
		}
	}

	private void notifyChangeListeners(JDOLifecycleEvent event) {
		Object[] listeners;
		synchronized(geoChangeListeners) {
			listeners = geoChangeListeners.getListeners();
		}

		for (Object listener : listeners)
			((GeographyTemplateDataChangedListener)listener).geographyTemplateDataChanged(event);
	}

	private JDOLifecycleListener myLifecycleListener = new JDOLifecycleAdapterJob(Messages.getString("org.nightlabs.jfire.geography.ui.GeographyImplClient.loadingCSVJob")) { //$NON-NLS-1$
		private IJDOLifecycleListenerFilter filter = new SimpleLifecycleListenerFilter(
				CSV.class,
				true,
				new JDOLifecycleState[]{JDOLifecycleState.DIRTY, JDOLifecycleState.NEW});

		public IJDOLifecycleListenerFilter getJDOLifecycleListenerFilter()
		{
			return filter;
		}

		public void notify(JDOLifecycleEvent event)
		{
			clearCache(); // TODO improve this - should only clear those parts of the cache that really become invalid
			notifyChangeListeners(event);
		}
	};
}
