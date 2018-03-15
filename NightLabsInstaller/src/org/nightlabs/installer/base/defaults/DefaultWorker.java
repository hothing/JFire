package org.nightlabs.installer.base.defaults;

import org.nightlabs.installer.base.InstallationEntity;
import org.nightlabs.installer.base.InstallationEntityAssigned;
import org.nightlabs.installer.base.Installer;


/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DefaultWorker extends DefaultConfigurable implements InstallationEntityAssigned
{
	private InstallationEntity installationEntity;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.InstallationEntityAssigned#getInstallationEntity()
	 */
	public InstallationEntity getInstallationEntity()
	{
		return installationEntity;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.InstallationEntityAssigned#setInstallationEntity(org.nightlabs.installer.InstallationEntity)
	 */
	public void setInstallationEntity(InstallationEntity installationEntity)
	{
		this.installationEntity = installationEntity;
	}

	protected Installer getInstaller()
	{
		InstallationEntity e = getInstallationEntity();
		while(e.getParent() != null)
			e = e.getParent();
		return (Installer)e;
	}
}
