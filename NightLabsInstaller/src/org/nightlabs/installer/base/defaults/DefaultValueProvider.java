package org.nightlabs.installer.base.defaults;

import java.util.Properties;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ValueProvider;

/**
 * @version $Revision: 923 $ - $Date: 2007-05-22 16:46:41 +0200 (Di, 22 Mai 2007) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DefaultValueProvider extends DefaultWorker implements ValueProvider
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.ValueProvider#getValues()
	 */
	public Properties getValues() throws InstallationException
	{
		Properties defaultValues = null;
		String defaultValue = getInstallationEntity().getConfig().getProperty(Constants.DEFAULT);
		if(defaultValue != null) {
			defaultValues = new Properties();
			defaultValues.setProperty(Constants.RESULT, defaultValue);
		}
		return defaultValues;
	}
}
