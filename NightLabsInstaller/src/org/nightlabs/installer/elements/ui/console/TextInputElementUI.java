package org.nightlabs.installer.elements.ui.console;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.ConsoleFormatter;

/**
 * @version $Revision: 1566 $ - $Date: 2009-01-16 16:35:53 +0100 (Fr, 16 Jan 2009) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class TextInputElementUI extends AnnotationElementUI
{
	private boolean passwordMode;
	
	public TextInputElementUI()
	{
		this(false);
	}

	public TextInputElementUI(boolean passwordMode)
	{
		this.passwordMode = passwordMode;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultUI#renderBefore()
	 */
	@Override
	public void renderBefore() throws InstallationException
	{
		super.renderBefore();
		ConsoleFormatter f = getConsoleFormatter();
		if(!passwordMode) {
			String result = getInstallationEntity().getResult(Constants.RESULT);
			if(result != null) {
				f.println(" >: "+result);
			}
		}
		f.print("  : ");
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultUI#render()
	 */
	@Override
	public void render() throws InstallationException
	{
		ConsoleFormatter f = getConsoleFormatter();
		String result;
		if(passwordMode) {
			result = f.readPassword("");
			if(result == null)
				result = "";
			getInstallationEntity().setResult(Constants.RESULT, result);
		} else {
			result = f.read(getInstallationEntity().getResult(Constants.RESULT));
			if(result != null && !"".equals(result))
				getInstallationEntity().setResult(Constants.RESULT, result);
		}
		f.println();
	}
}
