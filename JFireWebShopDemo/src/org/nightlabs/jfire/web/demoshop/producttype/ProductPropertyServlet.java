
package org.nightlabs.jfire.web.demoshop.producttype;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.DeflaterOutputStream;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.datafield.IContentDataField;
import org.nightlabs.jfire.prop.exception.PropertyException;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.web.demoshop.DAUtil;
import org.nightlabs.jfire.web.demoshop.WebShopException;
import org.nightlabs.jfire.web.demoshop.WebShopServlet;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Khaled Soliman - khaled[at]nightlabs[dot]de
 */
public class ProductPropertyServlet extends WebShopServlet
{
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(ProductPropertyServlet.class);

	private Set<String> getAcceptedEncodings(HttpServletRequest request)
	{
		Set<String> result = new HashSet<String>();
		String encodings = request.getHeader("accept-encoding");
		if(encodings == null)
			return result;
		StringTokenizer st = new StringTokenizer(encodings, ",");
		while(st.hasMoreTokens())
			result.add(st.nextToken().trim().toLowerCase());
		return result;
	}

	private boolean isEncodingAccepted(HttpServletRequest request, String encoding)
	{
		return getAcceptedEncodings(request).contains(encoding);
	}

	protected void outputDataFieldContent(HttpServletRequest request, HttpServletResponse response) throws ParseException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException, RemoteException, CreateException, NamingException, PropertyException, ContentEmptyException, IOException, SecurityException, IllegalArgumentException, WebShopException
	{
		ProductTypeID productTypeID = new ProductTypeID(requireParameter("productTypeID"));
		StructFieldID structFieldID = new StructFieldID(requireParameter("structFieldID"));

		DataField dataField = DAUtil.getDataField(productTypeID, structFieldID);
		if(!(dataField instanceof IContentDataField))
			throw new IllegalArgumentException("Not a IContentDataField instance");
		IContentDataField contentDataField = (IContentDataField)dataField;

		if(dataField.isEmpty())
			throw new ContentEmptyException("No content found for data field: "+contentDataField);

		String contentType = contentDataField.getContentType();
		if(contentType == null)
			contentType = "application/unknown";

		String contentEncoding = contentDataField.getContentEncoding();

		if(contentEncoding == null)
			throw new IllegalStateException("No content encoding for content data field: "+contentDataField);

		OutputStream out = response.getOutputStream();
		if(!isEncodingAccepted(request, contentEncoding)) {
			if(IContentDataField.CONTENT_ENCODING_DEFLATE.equals(contentEncoding))
				out = new DeflaterOutputStream(out);
			//else if(IContentDataField.CONTENT_ENCODING_GZIP.equals(contentEncoding))
			//	out = new GZIPOutputStream(out);
			else if(!contentEncoding.equals(IContentDataField.CONTENT_ENCODING_PLAIN))
				throw new IllegalStateException("Unknown content encoding: "+contentEncoding);
			contentEncoding = IContentDataField.CONTENT_ENCODING_PLAIN;
		}

		// send:
		response.setContentType(contentType);
		if(!contentEncoding.equals(IContentDataField.CONTENT_ENCODING_PLAIN))
			response.setHeader("Content-Encoding", contentEncoding);
		//response.setContentLength(contentDataField.getContent().length);
		out.write(contentDataField.getContent());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		log.debug("accepted encodings: "+getAcceptedEncodings(request));
		try {
			outputDataFieldContent(request, response);
		} catch(Throwable e) {
			throw new ServletException("Error in doGet()", e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		doGet(req, resp);
	}
}
