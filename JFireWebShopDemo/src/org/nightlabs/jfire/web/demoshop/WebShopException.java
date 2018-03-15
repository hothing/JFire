package org.nightlabs.jfire.web.demoshop;

import javax.servlet.ServletException;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class WebShopException extends ServletException
{
	/**
	 * The serial version for this class.
	 */
	private static final long serialVersionUID = 1L;
	private String messageKey;
	
	/**
	 * Create a new WebShopException.
	 */
	public WebShopException()
	{
	}

	/**
	 * Create a new WebShopException.
	 */
	public WebShopException(String message)
	{
		super(message);
	}

	/**
	 * Create a new WebShopException.
	 */
	public WebShopException(Throwable rootCause)
	{
		super(rootCause);
	}

	/**
	 * Create a new WebShopException.
	 */
	public WebShopException(String message, Throwable rootCause)
	{
		super(message, rootCause);
	}
	
	/**
	 * Create a new WebShopException.
	 */
	public WebShopException(String messageKey, String message, Throwable rootCause)
	{
		super(message, rootCause);
		this.messageKey = messageKey;
	}
	
	/**
	 * Create a new WebShopException.
	 */
	public WebShopException(String messageKey, String message)
	{
		super(message);
		this.messageKey = messageKey;
	}
	
	public String getMessageKey() {
		return messageKey;
	}
	
}
