package org.nightlabs.installer.elements.ui.console;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.Messages;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.ConsoleFormatter;
import org.nightlabs.installer.elements.SwitchElement;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class SwitchElementUI extends AnnotationElementUI
{
	private Map<Integer, String> indexToId;
	private int defaultIdx = 0;
	private String input;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultUI#renderBefore()
	 */
	@Override
	public void renderBefore() throws InstallationException
	{
		super.renderBefore();
		SwitchElement switchElement = (SwitchElement)getInstallationEntity();
		SortedMap<String, String> options = switchElement.getOptions();
		String defaultOption = switchElement.getDefaultOption();
		int idx = 1;
		indexToId = new HashMap<Integer, String>(options.size());
		for (String optionID : options.keySet()) {
			indexToId.put(idx, optionID);
			if(switchElement.getOptionValue(optionID).equals(defaultOption))
				defaultIdx = idx;
			idx++;
		}
		idx = 1;
		ConsoleFormatter f = getConsoleFormatter();
		for (String optionID : options.keySet()) {
			String defaultSign = " "; 
			if(idx == defaultIdx)
				defaultSign = ">";
			f.println(String.format(" %s[% 2d]: %s", defaultSign, idx, getString("option."+optionID)));
//				f.println(" >["+idx+"]: "+getString("option."+optionID));
//			else
//				f.println("  ["+idx+"]: "+getString("option."+optionID));
			idx++;
		}
		f.print("      : ");	
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultUI#render()
	 */
	@Override
	public void render() throws InstallationException
	{
		input = getConsoleFormatter().read(String.valueOf(defaultIdx));
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultUI#renderAfter()
	 */
	@Override
	public void renderAfter() throws InstallationException
	{
		SwitchElement switchElement = (SwitchElement)getInstallationEntity();
		ConsoleFormatter f = getConsoleFormatter();
		int optionIdx;
		try {
			optionIdx = Integer.parseInt(input);
		} catch(NumberFormatException e) {
			// handle error here - it is much easier
			getConsoleFormatter().println();
			getConsoleFormatter().println(String.format(Messages.getString("SwitchElementUI.illegalInput"), 1, indexToId.size())); //$NON-NLS-1$
			getConsoleFormatter().println();
			renderBefore();
			render();
			renderAfter();
			return;
		}
		String chosenOptionID = indexToId.get(optionIdx);
		f.println("      : "+getString("option."+chosenOptionID));
		f.println();
		getInstallationEntity().setResult(Constants.RESULT, switchElement.getOptionValue(chosenOptionID));
	}
}
