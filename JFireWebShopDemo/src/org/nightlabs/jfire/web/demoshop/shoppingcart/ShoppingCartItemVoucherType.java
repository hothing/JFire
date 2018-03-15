package org.nightlabs.jfire.web.demoshop.shoppingcart;

import java.util.Map;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.dao.PriceFragmentTypeDAO;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.voucher.accounting.VoucherPriceConfig;
import org.nightlabs.jfire.voucher.dao.VoucherTypeDAO;
import org.nightlabs.jfire.voucher.store.VoucherType;
import org.nightlabs.jfire.web.demoshop.BaseServlet;
import org.nightlabs.jfire.web.demoshop.WebShopException;
import org.nightlabs.progress.NullProgressMonitor;



/**
 * @author Attapol Thomprasert - attapol[at]nightlabs[dot]de
 */

public class ShoppingCartItemVoucherType extends ShoppingCartItem implements Comparable<ShoppingCartItemVoucherType>
{
	public ShoppingCartItemVoucherType(ProductTypeID productTypeID) {
		super(productTypeID);
	}

	private Price price;

	@Override
	public Price getPrice() throws WebShopException {
		if (this.price == null) {
			
			VoucherType voucherType = VoucherTypeDAO.sharedInstance().getVoucherType(
					productTypeID, new String [] {FetchPlan.DEFAULT,
					ProductType.FETCH_GROUP_NAME,
					VoucherPriceConfig.FETCH_GROUP_PRICES},
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					new NullProgressMonitor());
			
			CurrencyID currencyID = CurrencyID.create(BaseServlet.getConfig().getCurrencyId());
			VoucherPriceConfig voucherPriceConfig = (VoucherPriceConfig)voucherType.getPackagePriceConfig();
			org.nightlabs.jfire.accounting.Currency currency = null;
			long priceValue = 0;
			for (Map.Entry<org.nightlabs.jfire.accounting.Currency, Long> me : voucherPriceConfig.getPrices().entrySet()) {
				if (JDOHelper.getObjectId(me.getKey()).equals(currencyID)) {
					currency = me.getKey();
					priceValue = me.getValue().longValue();
					break;
				}
			}

			Price price = new Price(
					"", -1, // this Price instance will never be persisted => we can thus use -1
//					voucherPriceConfig.getOrganisationID(),
//					voucherPriceConfig.getPriceConfigID(),
//					voucherPriceConfig.createPriceID(),
					currency
			);
			price.setAmount(PriceFragmentTypeDAO.sharedInstance().getPriceFragmentType(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL,
					new String[] { FetchPlan.DEFAULT }, 
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, 
					new NullProgressMonitor()) 
					,priceValue);
			this.price = price;
		}
		return this.price;
	}

	public String getVoucherName() {
		VoucherType voucherType = VoucherTypeDAO.sharedInstance().getVoucherType(
				productTypeID, new String [] {FetchPlan.DEFAULT,
				ProductType.FETCH_GROUP_NAME,
				VoucherPriceConfig.FETCH_GROUP_PRICES},
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				new NullProgressMonitor());
		return voucherType.getName().getText();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return productTypeID.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "productTypeID="+productTypeID;
	}

	@Override
	public int compareTo(ShoppingCartItemVoucherType o) {
		String myIds = productTypeID.toString();
		String otherIds = o.productTypeID.toString();
		return myIds.compareTo(otherIds);
	}

	@Override
	public boolean equals(Object other)
	{
		// true if productTypeID resolve to equal
		return
				other != null &&
				other instanceof ShoppingCartItemVoucherType &&
				((ShoppingCartItemVoucherType)other).productTypeID.equals(productTypeID);
	}

	@Override
	public String getProductName() {
		return getVoucherName();
	}
}
