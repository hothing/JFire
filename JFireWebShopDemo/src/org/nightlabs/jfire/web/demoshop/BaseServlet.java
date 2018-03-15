package org.nightlabs.jfire.web.demoshop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;

import org.apache.log4j.Logger;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.web.demoshop.resource.Messages;
import org.nightlabs.jfire.web.demoshop.resource.WebShopConfig;
import org.nightlabs.jfire.web.login.JFireLoginServlet;

/**
 * This is a convenience Servlet base class that stores the
 * request and response data in a {@link ThreadLocal} object.
 * It provides convenience methods for accessing request
 * and response data.
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public abstract class BaseServlet extends JFireLoginServlet
{
	private static final String REQUEST_CONTENT = "pageTemplate_contentJSP";
	public static final String REQUEST_ERROR_PARAMETER_PREFIX = "error.request.parameter.";
	private static final String PARAMETER_LOCALE = "locale";
	private static final String SESSION_LOCALE = "locale";
	private static final String REQUEST_ERRORS = "request_errors";
	private static final String DEFAULT_TEMPLATE_JSP = "/jsp-includes/pageTemplate.jsp";

	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	private static WebShopConfig config = null;

	private static Logger log = Logger.getLogger(BaseServlet.class);

	public static WebShopConfig getConfig()
	{
		if(config == null)
			config = new WebShopConfig();
		return config;
	}

	private ThreadLocal<RequestScopeData> requestScope = new ThreadLocal<RequestScopeData>();

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.web.demoshop.I18nServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		requestScope.set(new RequestScopeData(request, response));
		getResponse().setContentType("text/html;charset=UTF-8");
		setLocale();
		super.service(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		doGet();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		doPost();
	}

	protected void doGet() throws ServletException, IOException
	{
		log.warn("doGet() is not implemented by overriding class");
	}

	protected void doPost() throws ServletException, IOException
	{
		log.warn("doPost() is not implemented by overriding class");
	}

	/**
	 * Forward the request to the given path.
	 * @param path The path to forward to.
	 * @throws ServletException In case of an error
	 * @throws IOException In case of an error
	 */
	protected void forward(String path) throws ServletException, IOException
	{
		log.debug("Forwarding to: "+path);
		requestScope.get().setForwarded(true);
		getServletContext().getRequestDispatcher(path).forward(getRequest(), getResponse());
	}

	/**
	 * Show a content JSP embedded in the given template JSP.
	 * The embedding is triggered using the {@link #REQUEST_CONTENT}
	 * request attribute.
	 * @param templateJSP The template JSP to use
	 * @param contentJSP The content JSP to embed
	 * @throws ServletException In case of an error
	 * @throws IOException In case of an error
	 */
	protected void showPage(String templateJSP, String contentJSP) throws ServletException, IOException
	{
		getRequest().setAttribute(REQUEST_CONTENT, contentJSP);
		forward(templateJSP);
	}

	/**
	 * Show a content JSP embedded in the default template JSP.
	 * @param contentJSP The content JSP to embed
	 * @throws ServletException In case of an error
	 * @throws IOException In case of an error
	 */
	protected void showPage(String contentJSP) throws ServletException, IOException
	{
		log.debug("Showing page: "+contentJSP);
		showPage(DEFAULT_TEMPLATE_JSP, contentJSP);
	}

	/**
	 * Get a request attribute.
	 * @param name The attributes name
	 * @return The attributes value or <code>null</code> if the
	 * 		attribute	does not exist
	 */
	protected Object getAttribute(String name)
	{
		return getRequest().getAttribute(name);
	}

	/**
	 * Set a request attribute.
	 * @param name The attributes name
	 * @param o The attributes value
	 */
	protected void setAttribute(String name, Object o)
	{
		getRequest().setAttribute(name, o);
	}

	/**
	 * Get the Servlets request object.
	 * @return the request
	 */
	protected HttpServletRequest getRequest()
	{
		return requestScope.get().getRequest();
	}

	/**
	 * Get the Servlets response object.
	 * @return the response
	 */
	protected HttpServletResponse getResponse()
	{
		return requestScope.get().getResponse();
	}

	/**
	 * Get a session attribute value.
	 * @param name The session attributes name
	 * @return The session attributes value or <code>null</code> if the
	 * 		attribute	does not exist
	 */
	protected Object getSessionAttribute(String name)
	{
		return getRequest().getSession().getAttribute(name);
	}

	/**
	 * Set a session attribute.
	 * @param name The session attributes name
	 * @param o The session attributes value
	 */
	protected void setSessionAttribute(String name, Object value)
	{
		getRequest().getSession().setAttribute(name, value);
	}

	/**
	 * Get a session attribute value.
	 * @param name The session attributes name
	 * @return The session attributes value
	 * @throws WebShopException If the session attribute does not exist
	 */
	protected Object requireSessionAttribute(String name) throws WebShopException
	{
		Object attribute = getSessionAttribute(name);
		if(attribute == null)
			throw new WebShopException("Session attribute not found: "+name);
		return attribute;
	}

	/**
	 * Does a session attribute exist?
	 * @param name The session attributes name
	 * @return <code>true</code> if the session attribute does exist - <code>false</code> otherwise
	 */
	protected boolean haveSessionAttribute(String name)
	{
		try {
			requireSessionAttribute(name);
			return true;
		} catch(Throwable e) {
			return false;
		}
	}

	/**
	 * Get a servlet parameter. This is a {@link HttpServletRequest} parameter.
	 * @param name The parameter name
	 * @return The parameter value or <code>null</code> if the parameter does not exist.
	 */
	protected String getParameter(String name)
	{
		return getRequest().getParameter(name);
	}

	/**
	 * Get a servlet parameter. This is a {@link HttpServletRequest} parameter.
	 * @param name The parameter name
	 * @param defaultValue The default value
	 * @return The parameter value or <code>defaultValue</code> if the parameter does not exist.
	 */
	protected String getParameter(String name, String defaultValue)
	{
		try {
			return requireParameter(name);
		} catch(Throwable ignore) {
			return defaultValue;
		}
	}

	protected String requireParameter(String name) throws WebShopException
	{
		String x = getParameter(name);
		if(x == null || "".equals(x))
			// FIXME:
			throw new WebShopException(getRequestErrorMessagePrefix() + name, name);
		return x;
	}

	public boolean checkParameters(String ... parameters)
	{
		boolean parametersOk = true;
		for (String name : parameters) {
			try {
				requireParameter(name);
			} catch(WebShopException e) {
				log.warn("Missing parameter: "+name);
				parametersOk = false;
				addError(e);
			}
		}
		return parametersOk;
//		List<Throwable> errors = requiredParameters(parameters);
//		if (errors != null) {
//			for (Throwable error : errors) {
//				addError(error);
//			}
//			return false;
//		}
//		return true;
	}

//	protected List<Throwable> requiredParameters(String ...name)
//	{
////		ArrayList<String> results = null;
//		ArrayList<Throwable> throwables = new ArrayList<Throwable>();
//		for (int i = 0; i < name.length; i++) {
//			String x = getParameter(name[i]);
//			if(x == null || "".equals(x)) {
//				// FIXME:
//				throwables.add(new WebShopException(getRequestErrorMessagePrefix() + name[i], name[i]));
//			}
//		}
//		if(throwables.size()>0)
//			return throwables;
//		return null;
//	}

	protected String[] getPathInfo()
	{
		String pathInfo = getRequest().getPathInfo();
		if(pathInfo == null)
			return new String[0];
		while(pathInfo.startsWith("/"))
			pathInfo = pathInfo.substring(1);
		while(pathInfo.endsWith("/"))
			pathInfo = pathInfo.substring(0, pathInfo.length()-1);
		String[] parts = pathInfo.split("/");
		return parts;
	}

	protected String getPathInfo(int idx)
	{
		String[] pathInfo = getPathInfo();
		if(pathInfo.length <= idx)
			return null;
		return pathInfo[idx];
	}

	protected String requirePathInfo(int idx) throws WebShopException
	{
		String pathInfo = getPathInfo(idx);
		if(pathInfo == null)
			// FIXME:
			throw new WebShopException(getRequestErrorMessagePrefix() + idx, String.valueOf(idx));
		return pathInfo;
	}

	/**
	 * Checks if the given name has a parameter
	 * @param parameter name
	 * @return bool
	 */
	protected boolean haveParameter(String name)
	{
		try {
			requireParameter(name);
			return true;
		} catch(Throwable e) {
			return false;
		}
	}

	/**
	 * Get a request parameter as int.
	 * @param name The parameters name
	 * @param defaultValue The default value
	 * @return The parameter value as int or <code>defaultValue</code> if the parameter
	 * 		does not exist or is not an integer value.
	 */
	protected int getParameterAsInt(String name, int defaultValue)
	{
		try {
			return requireParameterAsInt(name);
		} catch(Throwable ignore) {
			return defaultValue;
		}
	}

	/**
	 * Get a request parameter as int.
	 * @return The parameter value as int.
	 * @throws WebShopException If the parameter value does not exist or is
	 * 		not an integer value.
	 */
	protected int requireParameterAsInt(String name) throws WebShopException
	{
		try {
			return Integer.parseInt(requireParameter(name));
		} catch(Throwable e) {
			throw new WebShopException("Missing parameter value: "+name, e);
		}
	}

	/**
	 * Does a parameter exist as integer?
	 * @param name The parameters name
	 * @return <code>true</code> if the parameter exists and is an integer -
	 * 		<code>false</code> otherwise.
	 */
	protected boolean haveParameterAsInt(String name)
	{
		try {
			requireParameterAsInt(name);
			return true;
		} catch(Throwable e) {
			return false;
		}
	}

	/**
	 * Set the JSTL and response locale. The locale
	 * is taken from the request parameter locale if present
	 * (in this case it will be stored in the session)
	 * or from the browser-requested locale.
	 * @param request The request
	 * @param response The response
	 */
	private void setLocale()
	{
		setSessionLocaleFromRequest();
		Locale locale = (Locale)getSessionAttribute(SESSION_LOCALE);
		if(locale == null)
			locale = getRequest().getLocale();
		log.debug("Setting locale: "+locale.toString());
		Config.set(getRequest().getSession(), Config.FMT_LOCALE, locale);
		requestScope.get().setLocale(locale);
		getResponse().setLocale(locale);
		Messages.setThreadLocale(locale);
		I18nText.setThreadLocale(locale);
	}

	private void setSessionLocaleFromRequest()
	{
		if(haveParameter(PARAMETER_LOCALE)) {
			String localeParameter = getParameter(PARAMETER_LOCALE);
			log.debug("Have request locale: "+localeParameter);
			Pattern localePattern = Pattern.compile("^(\\w\\w)(_(\\w\\w))?$");
			Matcher m = localePattern.matcher(localeParameter);
			Locale requestLocale = null;
			if(m.matches()) {
				String language = m.group(1);
				String country = m.group(3);
				if(m.groupCount() == 3 && country != null && !"".equals(country))
					requestLocale = new Locale(language, country);
				else
					requestLocale = new Locale(language);
				setSessionAttribute(SESSION_LOCALE, requestLocale);
			} else
				log.warn("Invalid locale in request: "+localeParameter);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Throwable> getErrors()
	{
		return (List<Throwable>)getAttribute(REQUEST_ERRORS);
	}

	public void addError(String errorMessage)
	{
		addError(new WebShopException(errorMessage));
	}

	public void addError(Throwable error)
	{
		List<Throwable> errors = getErrors();
		if(errors == null)
			errors = new ArrayList<Throwable>(1);
		errors.add(error);
		setAttribute(REQUEST_ERRORS, errors);
	}


	protected Locale getLocale()
	{
		return requestScope.get().getLocale();
	}

	public String getRequestErrorMessagePrefix()
	{
		return REQUEST_ERROR_PARAMETER_PREFIX;
	}

	public boolean isForwarded()
	{
		return requestScope.get().isForwarded();
	}
}
