package org.nightlabs.installer.base;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class VerificationException extends InstallationException
{
	/**
	 * The serial version.
	 */
	private static final long serialVersionUID = 1L;

	public static enum Severity
	{
		ERROR,
		WARNING
	}

	private Severity severity = Severity.ERROR;

	/**
	 * Create a new VerificationException.
	 */
	public VerificationException()
	{
	}

	/**
	 * Create a new VerificationException.
	 * @param message
	 */
	public VerificationException(String message)
	{
		super(message);
	}

	/**
	 * Create a new VerificationException.
	 * @param cause
	 */
	public VerificationException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Create a new VerificationException.
	 * @param message
	 * @param cause
	 */
	public VerificationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Get the severity.
	 * @return the severity
	 */
	public Severity getSeverity()
	{
		return severity;
	}

	/**
	 * Set the severity.
	 * @param severity the severity to set
	 */
	public void setSeverity(Severity severity)
	{
		this.severity = severity;
	}
}
