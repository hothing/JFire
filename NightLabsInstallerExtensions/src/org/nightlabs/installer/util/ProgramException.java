package org.nightlabs.installer.util;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ProgramException extends Exception
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new ProgramException.
	 */
	public ProgramException()
	{
		super();
	}

	/**
	 * Create a new ProgramException.
	 * @param  message the detail message (which is saved for later retrieval
	 *         by the {@link #getMessage()} method).
	 */
	public ProgramException(String message)
	{
		super(message);
	}

	/**
	 * Create a new ProgramException.
	 * @param  message the detail message (which is saved for later retrieval
	 *         by the {@link #getMessage()} method).
	 * @param  cause the cause (which is saved for later retrieval by the
	 *         {@link #getCause()} method).  (A <tt>null</tt> value is
	 *         permitted, and indicates that the cause is nonexistent or
	 *         unknown.)
	 */
	public ProgramException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Create a new ProgramException.
	 * @param  cause the cause (which is saved for later retrieval by the
	 *         {@link #getCause()} method).  (A <tt>null</tt> value is
	 *         permitted, and indicates that the cause is nonexistent or
	 *         unknown.)
	 */
	public ProgramException(Throwable cause)
	{
		super(cause);
	}
}
