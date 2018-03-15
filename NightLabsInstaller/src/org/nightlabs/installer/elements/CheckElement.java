package org.nightlabs.installer.elements;

import java.util.Properties;
import java.util.SortedMap;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.Messages;
import org.nightlabs.installer.base.InstallationException;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class CheckElement extends SwitchElement
{
	private static final String OPTION_YES = "option.01_yes"; //$NON-NLS-1$
	private static final String OPTION_NO = "option.02_no"; //$NON-NLS-1$
	private boolean initialized = false;

	void init()
	{
		if(initialized)
			return;
		initialized = true;
		Properties config = getConfig();
		config.setProperty(OPTION_YES, Constants.CHECK_TRUE);
		config.setProperty(OPTION_NO, Constants.CHECK_FALSE);
//		if(getResult(Constants.RESULT) == null) {
		if(!haveDefault(Constants.RESULT)) {
			try {
				boolean selected = Boolean.parseBoolean(config.getProperty(Constants.CHECK_SELECTED, Constants.CHECK_FALSE));
				// set default instead of result:
//				setResult(Constants.RESULT, String.valueOf(selected));
				setDefault(Constants.RESULT, String.valueOf(selected));
			} catch(Throwable e) {
				try {
					// set default instead of result:
					//setResult(Constants.RESULT, Constants.CHECK_FALSE);
					setDefault(Constants.RESULT, Constants.CHECK_FALSE);
				} catch (InstallationException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.elements.SwitchElement#getDefaultOption()
	 */
	@Override
	public String getDefaultOption()
	{
		init();
		return super.getDefaultOption();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.elements.SwitchElement#getOptions()
	 */
	@Override
	public SortedMap<String, String> getOptions()
	{
		init();
		return super.getOptions();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.elements.SwitchElement#getOptionValue(java.lang.String)
	 */
	@Override
	public String getOptionValue(String optionID)
	{
		init();
		return super.getOptionValue(optionID);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultInstallationEntity#getString(java.lang.String)
	 */
	@Override
	public String getString(String key) throws InstallationException
	{
		if(OPTION_YES.equals(key))
			return Messages.getString("CheckElement.yes"); //$NON-NLS-1$
		if(OPTION_NO.equals(key))
			return Messages.getString("CheckElement.no"); //$NON-NLS-1$
		return super.getString(key);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultInstallationEntity#haveString(java.lang.String)
	 */
	@Override
	public boolean haveString(String key) throws InstallationException
	{
		if(OPTION_YES.equals(key))
			return true;
		if(OPTION_NO.equals(key))
			return true;
		return super.haveString(key);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultInstallationEntity#getResult(java.lang.String)
	 */
	@Override
	public String getResult(String key)
	{
		init();
		return super.getResult(key);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultInstallationEntity#haveResult(java.lang.String)
	 */
	@Override
	public boolean haveResult(String key)
	{
		init();
		return super.haveResult(key);
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultInstallationEntity#getDefault(java.lang.String)
	 */
	@Override
	public String getDefault(String key)
	{
		init();
		return super.getDefault(key);
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultInstallationEntity#haveDefault(java.lang.String)
	 */
	@Override
	public boolean haveDefault(String key)
	{
		init();
		return super.haveDefault(key);
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultInstallationEntity#run(boolean)
	 */
	@Override
	public boolean run(boolean visible) throws InstallationException
	{
		init();
		return super.run(visible);
	}
}
