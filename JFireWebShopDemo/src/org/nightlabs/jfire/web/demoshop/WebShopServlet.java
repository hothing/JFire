package org.nightlabs.jfire.web.demoshop;

import org.nightlabs.jfire.web.demoshop.shoppingcart.ShoppingCart;


/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Attapol Thomprasert - attapol[at]nightlabs[dot]de
 */
public class WebShopServlet extends BaseServlet
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	private static final String PARAMETER_ACTION = "action";
	private static final String SESSION_SHOPPING_CART = "shoppingCart";
//	private static final String SESSION_VOUCHER_SHOPPING_CART = "voucherShoppingCart";
	private static final String SESSION_CUSTOMER_ID = "customerId";
	
	
	public String getAction()
	{
		return getParameter(PARAMETER_ACTION);
	}
	
	/**
	 * This method will return the session shoppingcart
	 * and if not found within the request object it will create a new shoppingcart instance and
	 * write it into the session
	 * @return A {@link ShoppingCart} instance for the current session.
	 */
	public ShoppingCart getShoppingCart()
	{
		ShoppingCart shoppingCart = (ShoppingCart)getSessionAttribute(SESSION_SHOPPING_CART);
		if(shoppingCart == null) {
			shoppingCart = new ShoppingCart();
			setSessionAttribute(SESSION_SHOPPING_CART, shoppingCart);
		}
		return shoppingCart;
	}
	
//	public ShoppingCart getShoppingCartForVoucher()
//	{
//		ShoppingCart voucherShoppingCart = (ShoppingCart)getSessionAttribute(SESSION_VOUCHER_SHOPPING_CART);
//		if(voucherShoppingCart == null) {
//			voucherShoppingCart = new ShoppingCart();
//			setSessionAttribute(SESSION_VOUCHER_SHOPPING_CART, voucherShoppingCart);
//		}
//		return voucherShoppingCart;
//	}
	
	public String getLoggedInCustomerId()
	{
		return (String)getSessionAttribute(SESSION_CUSTOMER_ID);
	}

	public String requireLoggedInCustomerId() throws WebShopException
	{
		String loggedInCustomerId = getLoggedInCustomerId();
		if(loggedInCustomerId == null)
			throw new WebShopException("No customer is logged in");
		return loggedInCustomerId;
	}
	
	public boolean isCustomerLoggedIn()
	{
		return getLoggedInCustomerId() != null;
	}
	
	public void setLoggedInCustomerId(String customerId)
	{
		setSessionAttribute(SESSION_CUSTOMER_ID, customerId);
	}
	
}
