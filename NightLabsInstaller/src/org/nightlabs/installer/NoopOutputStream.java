package org.nightlabs.installer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class NoopOutputStream extends OutputStream
{
	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException
	{
	}
}
