package org.nightlabs.installer.util;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class StringBufferLineConsumer extends LineConsumer
{
	private StringBuffer buf;

	/**
	 * Create a new StringBufferLineConsumer instance.
	 */
	public StringBufferLineConsumer()
	{
		super(true);
		buf = new StringBuffer();
	}

	/**
	 * Create a new StringBufferLineConsumer instance.
	 * @param keepLineBreaks
	 * @param charsetName
	 */
	public StringBufferLineConsumer(boolean keepLineBreaks, String charsetName)
	{
		super(keepLineBreaks, charsetName);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.util.LineConsumer#consumeLine(java.lang.String)
	 */
	@Override
	protected void consumeLine(String line)
	{
		buf.append(line);
	}

	public StringBuffer getOutputBuffer()
	{
		return buf;
	}

	public String getOutputString()
	{
		return buf.toString();
	}
}
