package org.nightlabs.jfire.web.demoshop.shoppingcart;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.web.demoshop.WebShopException;



/**
 * @author Attapol Thomprasert - attapol[at]nightlabs[dot]de
 */

public abstract class ShoppingCartItem
{
	/**
	 * The product type id of this item.
	 */
	protected ProductTypeID productTypeID;

	/**
	 * The price of this item. This field is only used for caching pruposes.
	 */
	protected transient Price price;

	public ShoppingCartItem(ProductTypeID productTypeID) {
		super();
		this.productTypeID = productTypeID;
	}

	/**
	 * @return the productTypeID
	 */
	public ProductTypeID getProductTypeID() {
		return productTypeID;
	}

	/**
	 * @return productName String
	 */
	public abstract String getProductName();

	/**
	 * Get the price of this shopping cart item.
	 * @throws WebShopException
	 */
	public abstract Price getPrice() throws WebShopException;

}
