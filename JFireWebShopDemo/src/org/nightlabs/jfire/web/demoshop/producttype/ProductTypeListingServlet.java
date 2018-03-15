package org.nightlabs.jfire.web.demoshop.producttype;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.dao.PriceFragmentTypeDAO;
import org.nightlabs.jfire.accounting.gridpriceconfig.TariffPricePair;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.datafield.I18nTextDataField;
import org.nightlabs.jfire.simpletrade.store.prop.SimpleProductTypeStruct;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.voucher.accounting.VoucherPriceConfig;
import org.nightlabs.jfire.web.demoshop.BaseServlet;
import org.nightlabs.jfire.web.demoshop.DAUtil;
import org.nightlabs.jfire.web.demoshop.WebShopServlet;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Khaled Soliman - khaled[at]nightlabs[dot]de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Attapol Thomprasert - attapol[at]nightlabs[dot]de
 */
public class ProductTypeListingServlet extends WebShopServlet
{
	/**
	 * The serial version of this class
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Map<ProductType,Collection<TariffPricePair>> simpleProductTypesAndPrices = DAUtil.getSimpleProductPrices(DAUtil.getSaleableSimpleProductTypes(),CurrencyID.create(BaseServlet.getConfig().getCurrencyId()));
		request.setAttribute("simpleProductTypesAndPrices", simpleProductTypesAndPrices);
		request.setAttribute("structFieldIDSmallImage", SimpleProductTypeStruct.IMAGES_SMALL_IMAGE.toString());
		Iterator it = simpleProductTypesAndPrices.entrySet().iterator();
		Map<ProductType,I18nTextDataField> productTypesAndI18nTextDataField = new HashMap<ProductType, I18nTextDataField>();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			ProductTypeID productTypeID = (ProductTypeID) JDOHelper.getObjectId(pairs.getKey());
			try {
				DataField dataField = DAUtil.getDataField(productTypeID, SimpleProductTypeStruct.DESCRIPTION_SHORT);
				if(!(dataField instanceof I18nTextDataField))
					throw new IllegalArgumentException("Not a I18nTextDataField instance");
				I18nTextDataField i18nTextDataField = (I18nTextDataField)dataField;
				productTypesAndI18nTextDataField.put((ProductType) pairs.getKey(),i18nTextDataField);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		request.setAttribute("structFieldIDDescriptionShort", productTypesAndI18nTextDataField);

		ArrayList<ProductType> prodCollection = DAUtil.getSaleableVoucherTypes();
		Map<ProductType, Price> voucherTypesAndPrices = new HashMap<ProductType, Price>();

		for (ProductType productType : prodCollection) {
			VoucherPriceConfig voucherPriceConfig = (VoucherPriceConfig)productType.getPackagePriceConfig();
			CurrencyID currencyID = CurrencyID.create(BaseServlet.getConfig().getCurrencyId());
			org.nightlabs.jfire.accounting.Currency currency = null;
			long priceValue = 0;
			if (voucherPriceConfig != null && voucherPriceConfig.getPrices() != null) {
				for (Map.Entry<org.nightlabs.jfire.accounting.Currency, Long> me : voucherPriceConfig.getPrices().entrySet()) {
					if (JDOHelper.getObjectId(me.getKey()).equals(currencyID)) {
						currency = me.getKey();
						priceValue = me.getValue().longValue();
						break;
					}
				}

				Price price = new Price(
						"", -1, // this Price instance will never be persisted => we can thus use -1
//						voucherPriceConfig.getOrganisationID(),
//						voucherPriceConfig.getPriceConfigID(),
//						voucherPriceConfig.createPriceID(),
						currency
				);
				price.setAmount(PriceFragmentTypeDAO.sharedInstance().getPriceFragmentType(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL,
						new String[] { FetchPlan.DEFAULT },
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new NullProgressMonitor())
						,priceValue);

				voucherTypesAndPrices.put(productType, price);
			}
		}

		request.setAttribute("voucherTypesAndPrices", voucherTypesAndPrices);

		showPage("/jsp-products/productListing.jsp");
	}
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request,response);
	}
}
