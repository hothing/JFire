package org.nightlabs.installer.base;

/**
 * The base exception for all errors that may happen 
 * during installation.
 * 
 * @version $Revision: 895 $ - $Date: 2007-05-15 11:58:45 +0200 (Di, 15 Mai 2007) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class InstallationException extends Exception
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new InstallationException.
	 */
	public InstallationException()
	{
	}

	/**
	 * Create a new InstallationException.
	 * @param message
	 */
	public InstallationException(String message)
	{
		super(message);
	}

	/**
	 * Create a new InstallationException.
	 * @param cause
	 */
	public InstallationException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Create a new InstallationException.
	 * @param message
	 * @param cause
	 */
	public InstallationException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
