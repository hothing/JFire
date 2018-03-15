package org.nightlabs.installer.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Marc Klinger - marc[at]nightlabs[dot]de
 */
public abstract class LineConsumer extends OutputStream
{
	private boolean keepLineBreaks;
	private String charsetName;
	private ByteArrayOutputStream buf = new ByteArrayOutputStream();

	public LineConsumer(boolean keepLineBreaks)
	{
		this(keepLineBreaks, Charset.defaultCharset().name());
	}

	public LineConsumer(boolean keepLineBreaks, String charsetName)
	{
		this.keepLineBreaks = keepLineBreaks;
		this.charsetName = charsetName;
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		if(keepLineBreaks || (b != '\r' && b != '\n'))
			buf.write(b);
		if(b == '\n') {
			consumeLine(buf.toString(charsetName));
			buf.reset();
		}
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close() throws IOException
	{
		if(buf.size() > 0)
			consumeLine(buf.toString(charsetName));
		super.close();
	}

	protected abstract void consumeLine(String line);
}
