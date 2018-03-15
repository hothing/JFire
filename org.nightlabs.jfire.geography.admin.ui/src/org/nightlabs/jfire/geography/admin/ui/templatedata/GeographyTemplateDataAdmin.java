package org.nightlabs.jfire.geography.admin.ui.templatedata;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.District;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.GeographyTemplateDataManagerRemote;
import org.nightlabs.jfire.geography.Location;
import org.nightlabs.jfire.geography.Region;

public class GeographyTemplateDataAdmin {

	public void storeGeographyTemplateCountryData(Country country){
		try {
			GeographyTemplateDataManagerRemote gm = JFireEjb3Factory.getRemoteBean(GeographyTemplateDataManagerRemote.class, Login.getLogin().getInitialContextProperties());
			gm.storeGeographyTemplateCountryData(country);
			Geography.sharedInstance().clearCache();
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}

	public void storeGeographyTemplateRegionData(Region region){
		try {
			GeographyTemplateDataManagerRemote gm = JFireEjb3Factory.getRemoteBean(GeographyTemplateDataManagerRemote.class, Login.getLogin().getInitialContextProperties());
			gm.storeGeographyTemplateRegionData(region);
			Geography.sharedInstance().clearCache();
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}

	public void storeGeographyTemplateCityData(City city){
		try {
			GeographyTemplateDataManagerRemote gm = JFireEjb3Factory.getRemoteBean(GeographyTemplateDataManagerRemote.class, Login.getLogin().getInitialContextProperties());
			gm.storeGeographyTemplateCityData(city);
			Geography.sharedInstance().clearCache();
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}

	public void storeGeographyTemplateLocationData(Location location){
		try {
			GeographyTemplateDataManagerRemote gm = JFireEjb3Factory.getRemoteBean(GeographyTemplateDataManagerRemote.class, Login.getLogin().getInitialContextProperties());
			gm.storeGeographyTemplateLocationData(location);
			Geography.sharedInstance().clearCache();
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}

	public void storeGeographyTemplateDistrictData(District district){
		try {
			GeographyTemplateDataManagerRemote gm = JFireEjb3Factory.getRemoteBean(GeographyTemplateDataManagerRemote.class, Login.getLogin().getInitialContextProperties());
			gm.storeGeographyTemplateDistrictData(district);
			Geography.sharedInstance().clearCache();
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}
}
