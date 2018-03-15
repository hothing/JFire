package org.nightlabs.jfire.web.demoshop.producttype;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.CreateException;
import javax.jdo.FetchPlan;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nightlabs.htmlcontent.IFCKEditorContentFile;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.gridpriceconfig.TariffPricePair;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.datafield.I18nTextDataField;
import org.nightlabs.jfire.prop.exception.PropertyException;
import org.nightlabs.jfire.prop.html.HTMLDataField;
import org.nightlabs.jfire.simpletrade.dao.SimpleProductTypeDAO;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.simpletrade.store.prop.SimpleProductTypeStruct;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.web.demoshop.BaseServlet;
import org.nightlabs.jfire.web.demoshop.DAUtil;
import org.nightlabs.jfire.web.demoshop.WebShopServlet;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Khaled Soliman - khaled[at]nightlabs[dot]de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class SimpleProductTypeDetailServlet extends WebShopServlet
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	public static final String[] DETAIL_FETCH_GROUPS = new String [] {
		FetchPlan.DEFAULT, ProductType.FETCH_GROUP_NAME, ProductType.FETCH_GROUP_PACKAGE_PRICE_CONFIG
	};

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.web.demoshop.BaseServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try {
			String productTypeIDStr = requireParameter("productTypeID");
			ProductTypeID productTypeID = new ProductTypeID(productTypeIDStr);

			// check for file to server:
			String fileName = getParameter("file");
			if(fileName != null && !fileName.isEmpty()) {
				// server a file...
				Pattern p = Pattern.compile("(\\d+).*");
				Matcher m = p.matcher(fileName);
				if(m.matches()) {
					long fileId = Long.parseLong(m.group(1));
					IFCKEditorContentFile infoFile = getXInfoFile(productTypeID, fileId);
					if(infoFile != null) {
						getResponse().setContentType(infoFile.getContentType());
						byte[] data = infoFile.getData();
						getResponse().setContentLength(data.length);
						getResponse().getOutputStream().write(data);
						return;
					}
				}
			}

			// server details web page:
			SimpleProductType simpleProductType = SimpleProductTypeDAO.sharedInstance().getSimpleProductType(
					productTypeID, DETAIL_FETCH_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					new NullProgressMonitor());
			ProductType productType =  simpleProductType;
			ArrayList<ProductType> arrayList = new ArrayList<ProductType>();
			arrayList.add(productType);
			Map<ProductType,Collection<TariffPricePair>> productTypesAndPrices = DAUtil.getSimpleProductPrices(arrayList ,CurrencyID.create(BaseServlet.getConfig().getCurrencyId()));

			setAttribute("tariffPricePairs",productTypesAndPrices.get(productType) );
			setAttribute("productType", simpleProductType);
			setAttribute("longDescription", getDescriptionLong(productTypeID));
			setAttribute("xInfoText", getXInfoText(productTypeID));
			setAttribute("structFieldIDSmallImage", SimpleProductTypeStruct.IMAGES_SMALL_IMAGE.toString());
			setAttribute("structFieldIDLargeImage", SimpleProductTypeStruct.IMAGES_LARGE_IMAGE.toString());

			showPage("/jsp-products/simpleProductDetails.jsp");

		} catch(ServletException e) {
			throw e;
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private I18nTextDataField getDescriptionLong(ProductTypeID productTypeID) throws PropertyException, RemoteException, CreateException, NamingException
	{
		DataField dataField = DAUtil.getDataField(productTypeID, SimpleProductTypeStruct.DESCRIPTION_LONG);
		if(!(dataField instanceof I18nTextDataField))
			throw new IllegalArgumentException("Not a I18nTextDataField instance");
		I18nTextDataField i18nTextDataField = (I18nTextDataField)dataField;
		return i18nTextDataField;
	}

	private String getXInfoText(ProductTypeID productTypeID) throws PropertyException, RemoteException, CreateException, NamingException
	{
		DataField dataField = DAUtil.getDataField(productTypeID, SimpleProductTypeStruct.XINFO_INFO);
		if(!(dataField instanceof HTMLDataField))
			throw new IllegalArgumentException("Not a HTMLDataField instance");
		HTMLDataField htmlDataField = (HTMLDataField)dataField;
		String language = getResponse().getLocale().getLanguage();
		String text = htmlDataField.getText(language);
		if(text == null && !htmlDataField.isEmpty() && !Locale.ENGLISH.getLanguage().equals(language))
			text = htmlDataField.getText(Locale.ENGLISH.getLanguage());
		if(text == null && !htmlDataField.isEmpty())
			text = htmlDataField.getTexts().values().iterator().next();
		if(text != null) {
			// rewrite file links
			try {
				String baseUrl = getRequest().getContextPath()+getRequest().getServletPath()+"?productTypeID="+URLEncoder.encode(productTypeID.toString(), "UTF-8");
				text = LinkRewriter.rewriteToLocalLinks(text, baseUrl);
			} catch (UnsupportedEncodingException e) {
				// should never happen
				throw new IllegalStateException(e);
			}
		}
		if(text == null)
			text = "";
		return text;
	}

	private IFCKEditorContentFile getXInfoFile(ProductTypeID productTypeID, long fileId) throws PropertyException, RemoteException, CreateException, NamingException
	{
		DataField dataField = DAUtil.getDataField(productTypeID, SimpleProductTypeStruct.XINFO_INFO);
		if(!(dataField instanceof HTMLDataField))
			throw new IllegalArgumentException("Not a HTMLDataField instance");
		HTMLDataField htmlDataField = (HTMLDataField)dataField;
		return htmlDataField.getFile(fileId);
	}
}