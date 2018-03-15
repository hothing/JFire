package org.nightlabs.installer.base;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public interface ErrorHandler extends Configurable
{
	/**
	 * Handle the error and exit execution.
	 * @param e The error
	 */
	void handle(Throwable e);
	
	/**
	 * Handle the error and exit execution only if
	 * exit is set to <code>true</code>.
	 * @param e The error
	 * @param exit Whether to exit execution after andling the error.
	 */
	void handle(Throwable e, boolean exit);
	
	/**
	 * Set this error handler to the verbose mode. In verbose mode,
	 * all error information should be dumped to stdout.
	 * @param verbose <code>true</code> to set the error handler verbose -
	 * 		<code>false</code> otherwise.
	 */
	void setVerbose(boolean verbose);
}
