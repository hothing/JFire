package org.nightlabs.installer.base;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class RenderException extends InstallationException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new RenderException.
	 */
	public RenderException()
	{
	}

	/**
	 * Create a new RenderException.
	 * @param message
	 */
	public RenderException(String message)
	{
		super(message);
	}

	/**
	 * Create a new RenderException.
	 * @param cause
	 */
	public RenderException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Create a new RenderException.
	 * @param message
	 * @param cause
	 */
	public RenderException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
