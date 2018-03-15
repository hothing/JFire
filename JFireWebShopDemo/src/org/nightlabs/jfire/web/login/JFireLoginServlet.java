package org.nightlabs.jfire.web.login;

import java.io.IOException;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;

public abstract class JFireLoginServlet extends HttpServlet
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(JFireLoginServlet.class);

	static {
		logger.trace("<static>: begin");
		Cache.setServerMode(true);
		String className = System.getProperty(JDOLifecycleManager.PROPERTY_KEY_JDO_LIFECYCLE_MANAGER);
		if (className == null) {
			className = JDOLifecycleManager.class.getName();
			System.setProperty(JDOLifecycleManager.PROPERTY_KEY_JDO_LIFECYCLE_MANAGER, className);
			logger.debug("<static>: System property JDOLifecycleManager.PROPERTY_KEY_JDO_LIFECYCLE_MANAGER was not set - setting it to: " + className);
		}
		else
			logger.debug("<static>: System property JDOLifecycleManager.PROPERTY_KEY_JDO_LIFECYCLE_MANAGER was already set: " + className);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		try {
			Login.getLogin().login();
		} catch (LoginException x) {
			throw new ServletException(x);
		}
		try {
			super.service(req, resp);
		} finally {
			try {
				Login.getLogin().logout();
			} catch (LoginException x) {
				throw new ServletException(x);
			}
		}
	}
}
