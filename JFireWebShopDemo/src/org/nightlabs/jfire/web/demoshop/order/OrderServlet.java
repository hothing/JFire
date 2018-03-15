package org.nightlabs.jfire.web.demoshop.order;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.AccountingManagerRemote;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.InvoiceLocal;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.accounting.pay.ModeOfPaymentConst;
import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.pay.PaymentData;
import org.nightlabs.jfire.accounting.pay.PaymentDataCreditCard;
import org.nightlabs.jfire.accounting.pay.PaymentResult;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessorCreditCardDummyForClientPayment;
import org.nightlabs.jfire.accounting.pay.id.PaymentID;
import org.nightlabs.jfire.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.simpletrade.SimpleTradeManagerRemote;
import org.nightlabs.jfire.simpletrade.store.prop.SimpleProductTypeStruct;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.SegmentType;
import org.nightlabs.jfire.trade.TradeManagerRemote;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.id.SegmentID;
import org.nightlabs.jfire.trade.id.SegmentTypeID;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.jfire.voucher.VoucherManagerRemote;
import org.nightlabs.jfire.web.demoshop.BaseServlet;
import org.nightlabs.jfire.web.demoshop.WebShopException;
import org.nightlabs.jfire.web.demoshop.WebShopServlet;
import org.nightlabs.jfire.web.demoshop.resource.Messages;
import org.nightlabs.jfire.web.demoshop.shoppingcart.ShoppingCart;
import org.nightlabs.jfire.web.demoshop.shoppingcart.ShoppingCartItem;
import org.nightlabs.jfire.web.demoshop.shoppingcart.ShoppingCartItemSimpleProductType;
import org.nightlabs.jfire.web.demoshop.shoppingcart.ShoppingCartItemVoucherType;
import org.nightlabs.jfire.web.login.Login;
import org.nightlabs.jfire.web.webshop.WebShopRemote;
import org.nightlabs.jfire.webshop.id.WebCustomerID;

/**
 * @author Khaled Soliman - khaled[at]nightlabs[dot]de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Attapol Thomprasert - attapol[at]nightlabs[dot]de
 */
public class OrderServlet extends WebShopServlet {

	private static final String ACTION_ORDER = "order";
	private static final long serialVersionUID = 1L;

	/**
	 * 2007/06/29
	 * Please note that the complete ordering/trade procedures are currently hard coded and are handled within
	 * this method.This will change soon.
	 */
	private void doOrder() throws WebShopException
	{
		ShoppingCart shoppingCart = getShoppingCart();
		if( (!shoppingCart.hasItems() ) )
			throw new WebShopException(Messages.getString("error.noitemfound"));
		try {
			WebShopRemote webShop = JFireEjb3Factory.getRemoteBean(WebShopRemote.class, Login.getLogin().getInitialContextProperties());

			AnchorID customerID = webShop.getWebCustomerLegalEntityID(
					WebCustomerID.create(Login.getLogin().getOrganisationID(),
							requireLoggedInCustomerId()
					)
			);
			if (customerID == null) {
				throw new IllegalStateException("The WebCustomer does not have a LegalEntity assigned.");
			}
			CurrencyID currencyID = CurrencyID.create(BaseServlet.getConfig().getCurrencyId());
			Hashtable environment = null; // currently, we are within the same server, hence we use null.
			TradeManagerRemote tm = JFireEjb3Factory.getRemoteBean(TradeManagerRemote.class, environment);
			Order order = tm.createSaleOrder(
					customerID,
					null, // the server will use the default prefix according to its configuration
					currencyID,
					new SegmentTypeID[] { SegmentType.DEFAULT_SEGMENT_TYPE_ID },
					new String[] {
							FetchPlan.DEFAULT,
							Order.FETCH_GROUP_SEGMENTS
							},
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

			OrderID orderID = (OrderID) JDOHelper.getObjectId(order);
			SegmentID segmentID = (SegmentID) JDOHelper.getObjectId(order.getSegments().iterator().next());

			SimpleTradeManagerRemote stm = JFireEjb3Factory.getRemoteBean(SimpleTradeManagerRemote.class, environment);
			VoucherManagerRemote vm = JFireEjb3Factory.getRemoteBean(VoucherManagerRemote.class, environment);

			Set<Article> articles = new HashSet<Article>();
			//getGroupedItems gives back all products that have the same type /price & tariffs
			for (Map.Entry<ShoppingCartItem, Integer> me : shoppingCart.getGroupedItems().entrySet()) {
				ShoppingCartItem shoppingCartItem = me.getKey();

				int quantity = me.getValue().intValue();

				if(shoppingCartItem instanceof ShoppingCartItemSimpleProductType){
					ShoppingCartItemSimpleProductType shoppingCartItemSimpleProductType = (ShoppingCartItemSimpleProductType)shoppingCartItem;
					articles.addAll(
							stm.createArticles(
									segmentID,
									null, // offerID - there is no offer yet - it will be implicitely created
									shoppingCartItemSimpleProductType.getProductTypeID(),//productTypeID
									quantity,
									shoppingCartItemSimpleProductType.getTariffID(),//tariffID,
									true, // allocate
									true, // allocateSynchronously
									new String[] {FetchPlan.DEFAULT},
									NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT));
				}

				if(shoppingCartItem instanceof ShoppingCartItemVoucherType){
					ShoppingCartItemVoucherType shoppingCartItemVoucherType = (ShoppingCartItemVoucherType)shoppingCartItem;
					articles.addAll(
							vm.createArticles(segmentID,
									null,
									shoppingCartItemVoucherType.getProductTypeID(),
									quantity,
									new String[] {FetchPlan.DEFAULT},
									NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT));
				}
			}

			AccountingManagerRemote am = JFireEjb3Factory.getRemoteBean(AccountingManagerRemote.class, environment);
			Invoice invoice = am.createInvoice(
					orderID,
					null, // invoiceIDPrefix - null means use the default from the configuration
					true,
					new String[] {
							FetchPlan.DEFAULT, Invoice.FETCH_GROUP_INVOICE_LOCAL,
							Invoice.FETCH_GROUP_PRICE, Price.FETCH_GROUP_THIS_PRICE
						},
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			InvoiceLocal invoiceLocal = invoice.getInvoiceLocal();

			Set<InvoiceID> invoiceIDs = new HashSet<InvoiceID>(1);
			invoiceIDs.add((InvoiceID) JDOHelper.getObjectId(invoice));

			Payment payment = new Payment(IDGenerator.getOrganisationID(), IDGenerator.nextID(Payment.class));
			PaymentID paymentID = PaymentID.create(payment.getOrganisationID(), payment.getPaymentID());
			payment.setAmount(invoiceLocal.getAmountToPay());
			payment.setInvoiceIDs(invoiceIDs);
			payment.setCurrencyID(currencyID);
			payment.setPartnerID(customerID);
			payment.setPaymentDirection(Payment.PAYMENT_DIRECTION_INCOMING);
			//payment.setReasonForPayment("desired webshop product - go and fuck yourself"); // necessary?!

			//TODO the below stuff should be dependent on the customer's choice
			PaymentDataCreditCard paymentDataCC = new PaymentDataCreditCard(payment);
			payment.setModeOfPaymentFlavourID(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_VISA);
			//ServerPaymentProcessor.getServerPaymentProcessorsForOneModeOfPaymentFlavour(pm, dummyFlavourID);
			payment.setClientPaymentProcessorFactoryID("I_Have_A_Lot_Of_Fun");
			payment.setServerPaymentProcessorID(ServerPaymentProcessorID.create(Organisation.DEV_ORGANISATION_ID, ServerPaymentProcessorCreditCardDummyForClientPayment.class.getName()));
			//TODO the above stuff should be dependent on the customer's choice

			PaymentData paymentData = paymentDataCC;
			boolean forceRollback = false; // should be set to true, if an error occurs

			// client - stage 1
			PaymentResult payBeginClientResult = new PaymentResult(
					PaymentResult.CODE_APPROVED_NO_EXTERNAL, null, null);

			payment.setPayBeginClientResult(payBeginClientResult);

			// server - stage 1
			PaymentResult payBeginServerResult = am.payBegin(paymentData);
			if (payBeginServerResult.isFailed())
				forceRollback = true;

			// client - stage 2
			PaymentResult payDoWorkClientResult = new PaymentResult(
					PaymentResult.CODE_PAID_NO_EXTERNAL, null, null);

			// server - stage 2
			PaymentResult payDoWorkServerResult = am.payDoWork(paymentID, payDoWorkClientResult, forceRollback);
			if (payDoWorkServerResult.isFailed())
				forceRollback = true;

			// client - stage 3
			PaymentResult payEndClientResult = new PaymentResult(
					PaymentResult.CODE_COMMITTED_NO_EXTERNAL, null, null);

			// server - stage 3
			am.payEnd(paymentID, payEndClientResult, forceRollback);
		} catch(WebShopException e) {
			throw new WebShopException(e);
		}
		catch(Exception e) {
			throw new WebShopException(Messages.getString("error.orderfailed"));
		}

		// below is temporary ; it will only notify the client that the order has been
		// quick work delete all items from the shoppingcart
		for (Map.Entry<ShoppingCartItem, Integer> me : shoppingCart.getGroupedItems().entrySet()) {
			ShoppingCartItem shoppingCartItem = me.getKey();
			int quantity = me.getValue().intValue();
			for(int i=0; i<quantity; i++)
				shoppingCart.removeItem(shoppingCartItem);
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String action = getAction();
		if(ACTION_ORDER.equals(action)) {
			try {
				doOrder();
				showPage("/jsp-order/orderSuccess.jsp");
			} catch (WebShopException e) {
				addError(e);
				showPage("/jsp-order/orderError.jsp");
			}
		} else {
			// set this for the shopping cart overview image:
			setAttribute("structFieldIDSmallImage", SimpleProductTypeStruct.IMAGES_SMALL_IMAGE.toString());

			Map<ShoppingCartItem, Integer> simpleProductGroupItem = new HashMap<ShoppingCartItem, Integer>();
			for (ShoppingCartItem item : getShoppingCart().getItems()) {
				if(item instanceof ShoppingCartItemSimpleProductType)
					simpleProductGroupItem.put(item, getShoppingCart().getGroupedItems().get(item));
			}
			setAttribute("simpleProductGroupItem", simpleProductGroupItem);

			Map<ShoppingCartItem, Integer> voucherGroupItem = new HashMap<ShoppingCartItem, Integer>();
			for (ShoppingCartItem item : getShoppingCart().getItems()) {
				if(item instanceof ShoppingCartItemVoucherType)
					voucherGroupItem.put(item, getShoppingCart().getGroupedItems().get(item));
			}
			setAttribute("voucherGroupItem", voucherGroupItem);

			showPage("/jsp-order/orderOverview.jsp");
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}
