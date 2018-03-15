package org.nightlabs.jfire.web.demoshop.producttype;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.text.ParseException;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nightlabs.jfire.prop.exception.PropertyException;
import org.nightlabs.jfire.web.demoshop.WebShopException;
import org.nightlabs.jfire.web.demoshop.resource.Messages;
import org.nightlabs.util.IOUtil;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author khaled
 */
public class ProductImagePropertyServlet extends ProductPropertyServlet
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.web.demoshop.producttype.ProductPropertyServlet#outputDataFieldContent(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void outputDataFieldContent(HttpServletRequest request, HttpServletResponse response) throws ParseException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException, RemoteException, CreateException, NamingException, PropertyException, IOException, SecurityException, IllegalArgumentException, WebShopException
	{
		try {
			super.outputDataFieldContent(request, response);
		} catch(ContentEmptyException e) {
			response.setContentType(Messages.getString("producttype.ProductImagePropertyServlet.noImageContentType")); //$NON-NLS-1$
			InputStream resourceStream = Messages.class.getResourceAsStream(Messages.getString("producttype.ProductImagePropertyServlet.noImageFilename")); //$NON-NLS-1$
			IOUtil.transferStreamData(resourceStream, response.getOutputStream());
		}
	}
}
