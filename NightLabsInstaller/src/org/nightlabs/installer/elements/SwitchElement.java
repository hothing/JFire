package org.nightlabs.installer.elements;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.Messages;
import org.nightlabs.installer.base.defaults.DefaultElement;
import org.nightlabs.installer.util.Util;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class SwitchElement extends DefaultElement
{
	private SortedMap<String, String> options = null;
	
	/**
	 * Get the options map. Option identifier to option value.
	 * @return The options map. Option identifier to option value.
	 */
	public SortedMap<String, String> getOptions()
	{
		if(options == null) {
			Collection<Matcher> matches = Util.getPropertyKeyMatches(getConfig(), Pattern.compile("^"+Pattern.compile(Constants.SWITCH_OPTION)+Pattern.compile(Constants.SEPARATOR)+"(.+)$")); //$NON-NLS-1$ //$NON-NLS-2$
			options = new TreeMap<String, String>();
			for (Matcher m : matches)
				options.put(m.group(1), getConfig().getProperty(m.group(0)));
			if(options.isEmpty())
				throw new IllegalStateException(Messages.getString("SwitchElement.switchConfigError")+getId()); //$NON-NLS-1$
		}
		return options;
	}

	/**
	 * Get the defaultOption.
	 * @return the defaultOption
	 */
	public String getDefaultOption()
	{
		return getResult(Constants.RESULT);
	}
	
	public String getOptionValue(String optionID)
	{
		return getConfig().getProperty(Constants.SWITCH_OPTION+Constants.SEPARATOR+optionID);
	}
}
