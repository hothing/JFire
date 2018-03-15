package org.nightlabs.installer.base;

import java.util.Properties;

/**
 * @version $Revision: 895 $ - $Date: 2007-05-15 11:58:45 +0200 (Di, 15 Mai 2007) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public interface ValueProvider extends Configurable, InstallationEntityAssigned
{
	Properties getValues() throws InstallationException;
}
