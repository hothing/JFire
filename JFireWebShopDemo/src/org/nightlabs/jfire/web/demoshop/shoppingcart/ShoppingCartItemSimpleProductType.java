package org.nightlabs.jfire.web.demoshop.shoppingcart;

import java.util.Collection;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.gridpriceconfig.TariffPricePair;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.simpletrade.dao.SimpleProductTypeDAO;
import org.nightlabs.jfire.simpletrade.dao.TariffPricePairDAO;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.dao.ProductTypeDAO;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.web.demoshop.BaseServlet;
import org.nightlabs.jfire.web.demoshop.Util;
import org.nightlabs.jfire.web.demoshop.WebShopException;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Attapol Thomprasert - attapol[at]nightlabs[dot]de
 */

public class ShoppingCartItemSimpleProductType extends ShoppingCartItem implements Comparable<ShoppingCartItemSimpleProductType>
{
	/**
	 * The tariff id of this item.
	 */
	private TariffID tariffID;

	/**
	 * @return the tariffID
	 */
	public TariffID getTariffID() {
		return tariffID;
	}

	public ShoppingCartItemSimpleProductType(ProductTypeID productTypeID, TariffID tariffID) {
		super(productTypeID);
		this.tariffID = tariffID;
	}

	@Override
	public Price getPrice() throws WebShopException {
		if(price == null) {
			ProductType productType = ProductTypeDAO.sharedInstance().getProductType(
					getProductTypeID(), Util.getFetchPlan(ProductType.FETCH_GROUP_PACKAGE_PRICE_CONFIG),
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());

//			PriceConfigID priceConfigId = (PriceConfigID) JDOHelper.getObjectId(productType.getPackagePriceConfig());
//			Collection<TariffPricePair> tariffPricePairs = getTariffPricePairs(productType, priceConfigId);
			Collection<TariffPricePair> tariffPricePairs = TariffPricePairDAO.sharedInstance().getTariffPricePairs(
					(ProductTypeID)JDOHelper.getObjectId(productType),
					CustomerGroupID.create(productType.getOrganisationID(), CustomerGroup.CUSTOMER_GROUP_ID_DEFAULT),
					CurrencyID.create(BaseServlet.getConfig().getCurrencyId()),
					new NullProgressMonitor());

			// iterate all prices for the given tariff
			for (TariffPricePair pair : tariffPricePairs) {
				if(getTariffID().equals(pair.getTariff().getObjectId())) {
					// found tariff
					price = pair.getPrice();
					break;
				}
			}
			// tariff was not found:
			if(price == null)
				throw new WebShopException("Price not found for shoppingcart item: "+toString());
		}
		return price;
	}

	public String getSimpleProductName() {
		SimpleProductType simpleProductType = SimpleProductTypeDAO.sharedInstance().getSimpleProductType(
				productTypeID, new String [] {FetchPlan.DEFAULT, ProductType.FETCH_GROUP_NAME},  NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				new NullProgressMonitor());
		return simpleProductType.getName().getText();
	}

//	@SuppressWarnings("unchecked")
//	private static Collection<TariffPricePair> getTariffPricePairs(ProductType productType, PriceConfigID priceConfigId)
//	{
//		return TariffPricePairDAO.sharedInstance().getTariffPricePairs(
//				priceConfigId,
//				CustomerGroupID.create(productType.getOrganisationID(), CustomerGroup.CUSTOMER_GROUP_ID_DEFAULT),
//				CurrencyID.create(BaseServlet.getConfig().getCurrencyId()),
//				new NullProgressMonitor());
//	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return productTypeID.hashCode() ^ tariffID.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "productTypeID="+productTypeID+",tariffID="+tariffID;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ShoppingCartItemSimpleProductType o)
	{
		String myIds = productTypeID+"-"+o.tariffID;
		String otherIds = o.productTypeID+"-"+o.tariffID;
		return myIds.compareTo(otherIds);
	}

	@Override
	public boolean equals(Object other)
	{
		// true if productTypeID && tariffID resolve to equal
		return
				other != null &&
				other instanceof ShoppingCartItemSimpleProductType &&
				((ShoppingCartItemSimpleProductType)other).productTypeID.equals(productTypeID) &&
				((ShoppingCartItemSimpleProductType)other).tariffID.equals(tariffID);
	}

	@Override
	public String getProductName() {
		return getSimpleProductName();
	}

}
