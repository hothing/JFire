package org.nightlabs.jfire.web.demoshop;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ThreadLocal class representing the current request data.
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
class RequestScopeData
{
	/**
	 * Create a new RequestScopeData.
	 * @param request The current request
	 * @param response The current response
	 */
	public RequestScopeData(HttpServletRequest request, HttpServletResponse response)
	{
		this.request = request;
		this.response = response;
	}
	
	/**
	 * The current request. Set whenever {@link #service(HttpServletRequest, HttpServletResponse)}
	 * is called.
	 */
	private HttpServletRequest request;

	/**
	 * The current response. Set whenever {@link #service(HttpServletRequest, HttpServletResponse)}
	 * is called.
	 */
	private HttpServletResponse response;
	
	/**
	 * The current locale.
	 */
	private Locale locale = Locale.getDefault();
	
	/**
	 * Indicates whether there was already a forward in this request.
	 */
	private boolean forwarded = false;

	/**
	 * Get the request.
	 * @return the request
	 */
	public HttpServletRequest getRequest()
	{
		return request;
	}

	/**
	 * Get the response.
	 * @return the response
	 */
	public HttpServletResponse getResponse()
	{
		return response;
	}

	/**
	 * Get the forwarded.
	 * @return the forwarded
	 */
	public boolean isForwarded()
	{
		return forwarded;
	}

	/**
	 * Set the forwarded.
	 * @param forwarded the forwarded to set
	 */
	public void setForwarded(boolean forwarded)
	{
		this.forwarded = forwarded;
	}

	/**
	 * Get the locale.
	 * @return the locale
	 */
	public Locale getLocale()
	{
		return locale;
	}

	/**
	 * Set the locale.
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale)
	{
		this.locale = locale;
	}
}