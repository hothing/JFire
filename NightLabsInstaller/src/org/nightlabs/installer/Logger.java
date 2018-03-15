package org.nightlabs.installer;

import java.io.PrintStream;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class Logger
{
	private static final PrintStream noopPrintStream = new PrintStream(new NoopOutputStream());
	public static PrintStream out = noopPrintStream;
	public static PrintStream err = System.err;
	
	public static void setVerbose(boolean verbose)
	{
		out = verbose ? System.out : noopPrintStream;
	}
	
	public static boolean isVerbose()
	{
		return out != noopPrintStream;
	}
}
