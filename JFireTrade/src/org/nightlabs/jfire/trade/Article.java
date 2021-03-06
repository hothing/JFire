/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.trade;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.DetachCallback;
import javax.jdo.listener.StoreCallback;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ReceptionNote;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.store.id.ReceptionNoteID;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.Util;
import org.nightlabs.util.reflect.ReflectUtil;

/**
 * An instance of <tt>Article</tt> occurs in the context of a {@link Segment}. It represents a
 * {@link Product} or a {@link ProductType} within an {@link Order}, an {@link Offer},
 * an {@link org.nightlabs.jfire.accounting.Invoice} or a {@link org.nightlabs.jfire.store.DeliveryNote}.
 * So you could understand an <tt>Article</tt> as a line on your <tt>Invoice</tt>, but depending
 * on the GUI, a whole <tt>Segment</tt> could be rendered differently, not showing the <tt>Article</tt>s
 * in detail.
 *
 * @version $Revision: 17137 $ - $Date: 2010-03-17 22:36:00 +0100 (Mi, 17 Mrz 2010) $
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.ArticleID"
 *		detachable="true"
 *		table="JFireTrade_Article"
 *
 * @jdo.version strategy="version-number"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, articleID"
 *
 * @jdo.fetch-group name="Article.articleLocal" fields="articleLocal"
 * @jdo.fetch-group name="Article.segment" fields="segment"
 * @jdo.fetch-group name="Article.currency" fields="currency"
 * @jdo.fetch-group name="Article.productType" fields="productType"
 * @jdo.fetch-group name="Article.product" fields="product"
 * @jdo.fetch-group name="Article.tariff" fields="tariff"
 * @jdo.fetch-group name="Article.price" fields="price"
 * @jdo.fetch-group name="Article.reversedArticle" fields="reversedArticle"
 * @jdo.fetch-group name="Article.reversingArticle" fields="reversingArticle"
 * @jdo.fetch-group name="Article.order" fields="order"
 * @jdo.fetch-group name="Article.offer" fields="offer"
 * @jdo.fetch-group name="Article.invoice" fields="invoice"
 * @jdo.fetch-group name="Article.deliveryNote" fields="deliveryNote"
 * @jdo.fetch-group name="Article.receptionNote" fields="receptionNote"
 * @!jdo.fetch-group name="Article.delivery" fields="delivery"
 *
 * @jdo.fetch-group name="Order.articles" fields="order"
 * @jdo.fetch-group name="Offer.articles" fields="offer"
 * @jdo.fetch-group name="Invoice.articles" fields="invoice"
 * @jdo.fetch-group name="DeliveryNote.articles" fields="deliveryNote"
 * @jdo.fetch-group name="ReceptionNote.articles" fields="receptionNote"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fields="articleLocal, segment, productType, product, tariff, price"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fields="articleLocal, segment, productType, product, tariff, price"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fields="articleLocal, segment, productType, product, tariff, price"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fields="articleLocal, segment, productType, product, tariff, price"
 *
 * @jdo.fetch-group
 * 		name="FetchGroupsTrade.articleCrossTradeReplication"
 * 		fields="
 * 			order, offer, invoice, deliveryNote, receptionNote,
 * 			price, currency, tariff, reversedArticle, reversingArticle,
 * 			segment, productType, product, createUser
 * 		"
 *
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fields="segment, productType, product, tariff, price, order, offer, invoice, deliveryNote"
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fields="segment, productType, product, tariff, price, order, invoice, deliveryNote"
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fields="segment, productType, product, tariff, price, order, offer, deliveryNote"
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fields="segment, productType, product, tariff, price, order, offer, invoice"
 *
 * @jdo.query
 *		name="getAllocatedArticleByOfferAndProduct"
 *		query="SELECT UNIQUE WHERE this.offer == :offer && this.product == :product && this.allocated"
 *
 * @jdo.query
 *		name="getArticlesByProduct"
 *		query="SELECT WHERE this.product == :product"
 */
@PersistenceCapable(
	objectIdClass=ArticleID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_Article")
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@FetchGroups({
	@FetchGroup(
		name=Article.FETCH_GROUP_ARTICLE_LOCAL,
		members=@Persistent(name="articleLocal")),
	@FetchGroup(
		name=Article.FETCH_GROUP_SEGMENT,
		members=@Persistent(name="segment")),
	@FetchGroup(
		name=Article.FETCH_GROUP_CURRENCY,
		members=@Persistent(name="currency")),
	@FetchGroup(
		name=Article.FETCH_GROUP_PRODUCT_TYPE,
		members=@Persistent(name="productType")),
	@FetchGroup(
		name=Article.FETCH_GROUP_PRODUCT,
		members=@Persistent(name="product")),
	@FetchGroup(
		name=Article.FETCH_GROUP_TARIFF,
		members=@Persistent(name="tariff")),
	@FetchGroup(
		name=Article.FETCH_GROUP_PRICE,
		members=@Persistent(name="price")),
	@FetchGroup(
		name=Article.FETCH_GROUP_REVERSED_ARTICLE,
		members=@Persistent(name="reversedArticle")),
	@FetchGroup(
		name=Article.FETCH_GROUP_REVERSING_ARTICLE,
		members=@Persistent(name="reversingArticle")),
	@FetchGroup(
		name=Article.FETCH_GROUP_ORDER,
		members=@Persistent(name="order")),
	@FetchGroup(
		name=Article.FETCH_GROUP_OFFER,
		members=@Persistent(name="offer")),
	@FetchGroup(
		name=Article.FETCH_GROUP_INVOICE,
		members=@Persistent(name="invoice")),
	@FetchGroup(
		name=Article.FETCH_GROUP_DELIVERY_NOTE,
		members=@Persistent(name="deliveryNote")),
	@FetchGroup(
		name=Article.FETCH_GROUP_RECEPTION_NOTE,
		members=@Persistent(name="receptionNote")),
	@FetchGroup(
		name="Order.articles",
		members=@Persistent(name="order")),
	@FetchGroup(
		name="Offer.articles",
		members=@Persistent(name="offer")),
	@FetchGroup(
		name="Invoice.articles",
		members=@Persistent(name="invoice")),
	@FetchGroup(
		name="DeliveryNote.articles",
		members=@Persistent(name="deliveryNote")),
	@FetchGroup(
		name="ReceptionNote.articles",
		members=@Persistent(name="receptionNote")),
	@FetchGroup(
		name="FetchGroupsTrade.articleInOrderEditor",
		members={@Persistent(name="articleLocal"), @Persistent(name="segment"), @Persistent(name="productType"), @Persistent(name="product"), @Persistent(name="tariff"), @Persistent(name="price"), @Persistent(name="endCustomer")}),
	@FetchGroup(
		name="FetchGroupsTrade.articleInOfferEditor",
		members={@Persistent(name="articleLocal"), @Persistent(name="segment"), @Persistent(name="productType"), @Persistent(name="product"), @Persistent(name="tariff"), @Persistent(name="price"), @Persistent(name="endCustomer")}),
	@FetchGroup(
		name="FetchGroupsTrade.articleInInvoiceEditor",
		members={@Persistent(name="articleLocal"), @Persistent(name="segment"), @Persistent(name="productType"), @Persistent(name="product"), @Persistent(name="tariff"), @Persistent(name="price"), @Persistent(name="endCustomer")}),
	@FetchGroup(
		name="FetchGroupsTrade.articleInDeliveryNoteEditor",
		members={@Persistent(name="articleLocal"), @Persistent(name="segment"), @Persistent(name="productType"), @Persistent(name="product"), @Persistent(name="tariff"), @Persistent(name="price"), @Persistent(name="endCustomer")}),
	@FetchGroup(
		name="FetchGroupsTrade.articleCrossTradeReplication",
		members={@Persistent(name="order"), @Persistent(name="offer"), @Persistent(name="invoice"), @Persistent(name="deliveryNote"), @Persistent(name="receptionNote"), @Persistent(name="price"), @Persistent(name="currency"), @Persistent(name="tariff"), @Persistent(name="reversedArticle"), @Persistent(name="reversingArticle"), @Persistent(name="segment"), @Persistent(name="productType"), @Persistent(name="product"), @Persistent(name="createUser")}),
	@FetchGroup(
		name=Article.FETCH_GROUP_END_CUSTOMER,
		members=@Persistent(name="endCustomer"))
})
@Queries({
	@javax.jdo.annotations.Query(
		name="getAllocatedArticleByOfferAndProduct",
		value="SELECT UNIQUE WHERE this.offer == :offer && this.product == :product && this.allocated"),
	@javax.jdo.annotations.Query(
		name="getArticlesByProduct",
		value="SELECT WHERE this.product == :product")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Article
implements Serializable, DeleteCallback, AttachCallback, DetachCallback, StoreCallback
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Article.class);

	public static final String FETCH_GROUP_ARTICLE_LOCAL = "Article.articleLocal";
	public static final String FETCH_GROUP_SEGMENT = "Article.segment";
	public static final String FETCH_GROUP_CURRENCY = "Article.currency";
	public static final String FETCH_GROUP_PRODUCT_TYPE = "Article.productType";
	public static final String FETCH_GROUP_PRODUCT = "Article.product";
	public static final String FETCH_GROUP_PRICE = "Article.price";
	public static final String FETCH_GROUP_REVERSED_ARTICLE = "Article.reversedArticle";
	public static final String FETCH_GROUP_REVERSING_ARTICLE = "Article.reversingArticle";
	public static final String FETCH_GROUP_ORDER = "Article.order";
	public static final String FETCH_GROUP_OFFER = "Article.offer";
	public static final String FETCH_GROUP_INVOICE = "Article.invoice";
	public static final String FETCH_GROUP_DELIVERY_NOTE = "Article.deliveryNote";
	public static final String FETCH_GROUP_RECEPTION_NOTE = "Article.receptionNote";
	public static final String FETCH_GROUP_TARIFF = "Article.tariff";
//	public static final String FETCH_GROUP_DELIVERY = "Article.delivery";
	public static final String FETCH_GROUP_END_CUSTOMER = "Article.endCustomer";

	// the following fetch-groups are virtual and processed in the detach callback
	// all of them are included virtually in the FetchGroupsTrade.articleInXXX fetch-groups
	public static final String FETCH_GROUP_ORDER_ID = "Article.orderID";
	public static final String FETCH_GROUP_OFFER_ID = "Article.offerID";
	public static final String FETCH_GROUP_INVOICE_ID = "Article.invoiceID";
	public static final String FETCH_GROUP_DELIVERY_NOTE_ID = "Article.deliveryNoteID";
	public static final String FETCH_GROUP_RECEPTION_NOTE_ID = "Article.receptionNoteID";
	public static final String FETCH_GROUP_REVERSED_ARTICLE_ID = "Article.reversedArticleID";
	public static final String FETCH_GROUP_REVERSING_ARTICLE_ID = "Article.reversingArticleID";
	public static final String FETCH_GROUP_VENDOR_ID = "Article.vendorID";
	public static final String FETCH_GROUP_CUSTOMER_ID = "Article.customerID";
	public static final String FETCH_GROUP_END_CUSTOMER_ID = "Article.endCustomerID";

//	/**
//	 * @param pm The {@link PersistenceManager} used to access the datastore.
//	 * @param offerID The ID of the {@link Offer}.
//	 * @param productID The ID of the {@link Product}.
//	 * @return Either <code>null</code>, if the specified IDs don't match an {@link Article} or the {@link Article} which references the
//	 *		specified <code>Product</code> within the specified <code>Offer</code>. Note, that <code>null</code> is returned, too, if no
//	 *		<code>Offer</code> with the specified <code>offerID</code> exists, or no <code>Product</code> with the specified <code>productID</code> exists.
//	 */
//	public static Article getArticle(PersistenceManager pm, OfferID offerID, ProductID productID)
//	{
//		pm.getExtent(Offer.class); pm.getExtent(Product.class);
//		Offer offer; Product product;
//		try {
//			offer = (Offer) pm.getObjectById(offerID);
//			product = (Product) pm.getObjectById(productID);
//		} catch (JDOObjectNotFoundException x) {
//			return null;
//		}
//		return getArticle(pm, offer, product);
//	}

	/**
	 * @param pm The {@link PersistenceManager} used to access the datastore.
	 * @param offer The {@link Offer} containing the searched <code>Article</code>.
	 * @param product The {@link Product} referenced by the searched <code>Article</code>.
	 * @return Either <code>null</code>, if no allocated {@link Article} can be found matching the
	 *		specified <code>Product</code> within the specified <code>Offer</code>.
	 */
	public static Article getAllocatedArticle(PersistenceManager pm, Offer offer, Product product)
	{
		Query q = pm.newNamedQuery(Article.class, "getAllocatedArticleByOfferAndProduct");
		return (Article) q.execute(offer, product);
	}

	/**
	 * Returns the Set of {@link Article}s where the given {@link Product} is referenced
	 * or an empty Set, if no {@link Article}s can be found matching the
	 * specified <code>Product</code>.
	 *
	 * @param pm The {@link PersistenceManager} used to access the datastore.
	 * @param product The {@link Product} referenced by the searched <code>Article</code>s.
	 * @return Either <code>null</code>, if no {@link Article}s can be found matching the
	 *		specified <code>Product</code>.
	 */
	@SuppressWarnings("unchecked")
	public static Set<Article> getArticles(PersistenceManager pm, Product product)
	{
		Query q = pm.newNamedQuery(Article.class, "getArticlesByProduct");
		return new HashSet<Article>((Collection<Article>) q.execute(product));
	}

	public static Map<Class<? extends ProductType>, Set<Article>> getProductTypeClass2articleSetMapFromArticleContainers(Collection<? extends ArticleContainer> articleContainers)
	{
		Map<Class<? extends ProductType>, Set<Article>> result = new HashMap<Class<? extends ProductType>, Set<Article>>();
		for (ArticleContainer articleContainer : articleContainers) {
			populateProductTypeClass2articleSetMap(result, articleContainer.getArticles());
		}
		return result;
	}

	public static Map<Class<? extends ProductType>, Set<Article>> getProductTypeClass2articleSetMap(Collection<? extends Article> articles)
	{
		Map<Class<? extends ProductType>, Set<Article>> result = new HashMap<Class<? extends ProductType>, Set<Article>>();
		populateProductTypeClass2articleSetMap(result, articles);
		return result;
	}

	public static void populateProductTypeClass2articleSetMap(Map<Class<? extends ProductType>, Set<Article>> productTypeClass2articleSetMap, Collection<? extends Article> articles)
	{
		if (productTypeClass2articleSetMap == null)
			throw new IllegalArgumentException("productTypeClass2articleSetMap is null!");

		for (Article article : articles) {
			Class<? extends ProductType> clazz = article.getProductType().getClass();
			Set<Article> articleSet = productTypeClass2articleSetMap.get(clazz);
			if (articleSet == null) {
				articleSet = new HashSet<Article>();
				productTypeClass2articleSetMap.put(clazz, articleSet);
			}
			articleSet.add(article);
		}
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long articleID;

	public static long createArticleID()
	{
		return IDGenerator.nextID(Article.class);
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="article"
	 */
	@Persistent(
		mappedBy="article",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private ArticleLocal articleLocal;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Segment segment;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Order order;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Offer offer;

	/**
	 * An <tt>Article</tt> can only be part of none or exactly one <tt>Invoice</tt>.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Invoice invoice = null;

	/**
	 * An <tt>Article</tt> can only be in one <tt>DeliveryNote</tt> (or in none).
	 * If the <tt>Article</tt> shall be returned, a new reverse-<tt>Article</tt>
	 * needs to be created. Hence, <tt>Invoice</tt> and <tt>DeliveryNote</tt> handling
	 * is quite the same.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DeliveryNote deliveryNote = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ReceptionNote receptionNote = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ProductType productType;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Product product = null;

	/**
	 * Creation date of this Invoice.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date createDT;

	/**
	 * The user who created this Invoice.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private User createUser = null;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private OrderID orderID = null;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean orderID_detached = false;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private OfferID offerID = null;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean offerID_detached = false;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private InvoiceID invoiceID = null;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean invoiceID_detached = false;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private DeliveryNoteID deliveryNoteID = null;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean deliveryNoteID_detached = false;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private ReceptionNoteID receptionNoteID = null;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean receptionNoteID_detached = false;


	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private LegalEntity endCustomer;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private AnchorID endCustomerID = null;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean endCustomerID_detached = false;


	public OrderID getOrderID()
	{
		if (orderID == null && !orderID_detached)
			orderID = (OrderID) JDOHelper.getObjectId(order);

		return orderID;
	}
	public OfferID getOfferID()
	{
		if (offerID == null && !offerID_detached)
			offerID = (OfferID) JDOHelper.getObjectId(offer);

		return offerID;
	}
	public InvoiceID getInvoiceID()
	{
		if (invoiceID == null && !invoiceID_detached)
			invoiceID = (InvoiceID) JDOHelper.getObjectId(invoice);

		return invoiceID;
	}
	public DeliveryNoteID getDeliveryNoteID()
	{
		if (deliveryNoteID == null && !deliveryNoteID_detached)
			deliveryNoteID = (DeliveryNoteID) JDOHelper.getObjectId(deliveryNote);

		return deliveryNoteID;
	}

	public ReceptionNoteID getReceptionNoteID()
	{
		if (receptionNoteID == null && !receptionNoteID_detached)
			receptionNoteID = (ReceptionNoteID) JDOHelper.getObjectId(receptionNote);

		return receptionNoteID;
	}

	public ArticleID getReversedArticleID()
	{
		if (reversedArticleID == null && !reversedArticleID_detached)
			reversedArticleID = (ArticleID) JDOHelper.getObjectId(reversedArticle);

		return reversedArticleID;
	}
	public ArticleID getReversingArticleID()
	{
		if (reversingArticleID == null && !reversingArticleID_detached)
			reversingArticleID = (ArticleID) JDOHelper.getObjectId(reversingArticle);

		return reversingArticleID;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private AnchorID vendorID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean vendorID_detached = false;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private AnchorID customerID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean customerID_detached = false;

	/**
	 * @return Returns the ID of the vendor, which is either obtained via {@link Order#getVendorID()} or
	 *		manually detached in {@link #jdoPostDetach(Object)}.
	 */
	public AnchorID getVendorID()
	{
		if (vendorID == null && !vendorID_detached)
			vendorID = order.getVendorID();

		return vendorID;
	}

	/**
	 * @return Returns the ID of the customer, which is either obtained via {@link Order#getCustomerID()} or
	 *		manually detached in {@link #jdoPostDetach(Object)}.
	 */
	public AnchorID getCustomerID()
	{
		if (customerID == null && !customerID_detached)
			customerID = order.getCustomerID();

		return customerID;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Currency currency;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	@Persistent(
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private ArticlePrice price = null;

	/**
	 * This is set according to the result of PriceConfig.isDependentOnOffer().
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean priceDependentOnOffer;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Tariff tariff = null;

	/**
	 * Whether or not this Article has already been reversed. This is <code>true</code>,
	 * if {@link #reversingArticle} is assigned.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean reversed = false;

	/**
	 * Whether or not this Article reverses another one. This is <code>true</code>,
	 * if {@link #reversedArticle} is assigned.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean reversing = false;

	/**
	 * This flag is set <code>true</code>, if the reversing offer is somehow cancelled (rejected, expired, revoked, aborted etc.).
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean isReversingAborted = false;

	/**
	 * If this <tt>Article</tt> reverses (i.e. refunds) a previously sold Article, this member points
	 * to the original Article.
	 *
	 * @see #reverse
	 * @see #reversingArticle
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Article reversedArticle = null;

	/**
	 * If this <tt>Article</tt> is reversed, the new Article (which reverses this one),
	 * will be referenced here.
	 *
	 * @see #reversedArticle
	 *
	 * @jdo.field persistence-modifier="persistent"
	 * @! mapped-by="reversedArticle"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Article reversingArticle = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private ArticleID reversedArticleID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean reversedArticleID_detached = false;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private ArticleID reversingArticleID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean reversingArticleID_detached = false;

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private Article referencedArticle = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean allocationPending = false;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean releasePending = false;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean allocated = false;

	/**
	 * This is usually <tt>null</tt> or the fully qualified class name of the root exception.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="255"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=255)
	private String allocationExceptionClass = null;

	/**
	 * This is usually <tt>null</tt> or the exception message (no stacktrace) of the root exception.
	 *
	 * @jdo.field persistence-modifier="persistent" default-fetch-group="false"
	 * @jdo.column sql-type="CLOB"
	 */
	@Persistent(
		defaultFetchGroup="false",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="CLOB")
	private String allocationExceptionMessage = null;

	/**
	 * This is usually <tt>null</tt> or the result of {@link StackTraceElement#toString()}
	 * specifying where the root exception happened.
	 *
	 * @jdo.field persistence-modifier="persistent" default-fetch-group="false"
	 * @jdo.column sql-type="CLOB"
	 */
	@Persistent(
		defaultFetchGroup="false",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="CLOB")
	private String allocationExceptionStackTrace = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean allocationAbandoned = false;

	/**
	 * This is usually <tt>null</tt> or the fully qualified class name of the root exception.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="255"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=255)
	private String releaseExceptionClass = null;

	/**
	 * This is usually <tt>null</tt> or the exception message (no stacktrace) of the root exception.
	 *
	 * @jdo.field persistence-modifier="persistent" default-fetch-group="false"
	 * @jdo.column sql-type="CLOB"
	 */
	@Persistent(
		defaultFetchGroup="false",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="CLOB")
	private String releaseExceptionMessage = null;

	/**
	 * This is usually <tt>null</tt> or the result of {@link StackTraceElement#toString()}
	 * specifying where the root exception happened.
	 *
	 * @jdo.field persistence-modifier="persistent" default-fetch-group="false"
	 * @jdo.column sql-type="CLOB"
	 */
	@Persistent(
		defaultFetchGroup="false",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="CLOB")
	private String releaseExceptionStackTrace = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean releaseAbandoned = false;

	/**
	 * This field describes the estimated delivery date for the article,
	 * which can be determined in the offer.
	 * This field may be null, if no delivery date was specified.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date deliveryDateOffer = null;

	/**
	 * This field describes the estimated delivery date for the article,
	 * which can be determined in the delivery note.
	 * This field may be null, if no delivery date was specified.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date deliveryDateDeliveryNote = null;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected Article() { }

	/**
	 * This method is used by all constructors to check common parameters and to
	 * initialize the related fields.
	 */
	protected void init(User user, Offer offer, Segment segment, long articleID)
	{
		if (offer == null)
			throw new NullPointerException("offer");

		if (segment == null)
			throw new NullPointerException("segment");

		if (articleID < 0)
			throw new IllegalArgumentException("articleID < 0!");

		this.createUser = user;
		this.createDT = new Date();
		this.offer = offer;
		this.order = offer.getOrder();
		this.segment = segment;
		this.organisationID = offer.getOrganisationID();
		this.articleID = articleID;
		this.primaryKey = getPrimaryKey(organisationID, articleID);
		this.currency = offer.getCurrency();
	}

//	/**
//	 * This constructor creates a referencing article. This is used to cancel
//	 * an Invoice or a DeliveryNote. In this case, all Articles within the cancelled
//	 * Invoice/DeliveryNote are replaced by referencingArticles.
//	 *
//	 * @see #createReferencingArticle(User)
//	 */
//	protected Article(User user, long articleID, Article referencedArticle)
//	{
//		init(user, referencedArticle.getOffer(), referencedArticle.getSegment(), articleID);
//
//		this.setReferencedArticle(referencedArticle);
//		this.productType = referencedArticle.getProductType();
//		this.product = referencedArticle.getProduct();
//		this.tariff = referencedArticle.getTariff();
//		this.price = referencedArticle.getPrice(); // the price is immutable, so we can share it
//
//		this.allocated = referencedArticle.isAllocated();
//		this.allocationPending = referencedArticle.isAllocationPending();
//		this.releasePending = referencedArticle.isReleasePending();
//	}

	/**
	 * This constructor is used to create a new Article which reverses a previously
	 * sold one.
	 * <p>
	 * Do not use this constructor directly, but call
	 * {@link Trader#reverseArticle(User, Offer, Article)}! <code>Trader</code>
	 * delegates to {@link #reverseArticle(User, Offer, long)} which means, the
	 * <code>reversedArticle</code> is in charge of creating its reversing <code>Article</code>.
	 */
	protected Article(User user, Offer offer, long articleID, Article reversedArticle)
	{
		init(user, offer, reversedArticle.getSegment(), articleID);

		if (reversedArticle == null)
			throw new IllegalArgumentException("reversedArticle == null!");

		if (!this.offer.getOrder().getPrimaryKey().equals(reversedArticle.getOffer().getOrder().getPrimaryKey()))
			throw new IllegalArgumentException("An article can only be refunded within the same order!");

		if (reversedArticle.reversingArticle != null)
			throw new IllegalArgumentException("The passed article (" + reversedArticle.getPrimaryKey() + ") cannot be reversed, because it has already been reversed before!");

		if (reversedArticle.reversedArticle != null)
			throw new IllegalArgumentException("The passed article (" + reversedArticle.getPrimaryKey() + ") cannot be reversed, because it is itself reversing another Article!");

		// DataNucleus WORKAROUND: Maps/Collections that are populated in the constructor are not properly handled - their contents are not saved in the DB!
		PersistenceManager pm = NLJDOHelper.getThreadPersistenceManager(false);
		if (pm != null)
			pm.makePersistent(this);
		// end workaround

		this.setReversedArticle(reversedArticle);
		reversedArticle.setReversingArticle(this);
		this.productType = reversedArticle.getProductType();
		this.product = reversedArticle.getProduct();
		this.tariff = reversedArticle.getTariff();

		ArticlePrice tmpAP = reversedArticle.getPrice().createReversingArticlePrice();
		if (pm != null)
			tmpAP = pm.makePersistent(tmpAP);

		this.price = tmpAP;

//		JDOHelper.makeDirty(this, "price"); // DataNucleus WORKAROUND: Without this, the price is not assigned (field is written as null to the DB). Marco.

		// copy status
		this.allocated = reversedArticle.isAllocated();
		this.allocationPending = reversedArticle.isAllocationPending();
		this.releasePending = reversedArticle.isReleasePending();

		// DataNucleus WORKAROUND: Without this, the price is not assigned (field is written as null to the DB) and other fields
		// are written incompletely/wrong, too. Marco.
//		NLJDOHelper.makeDirtyAllFieldsRecursively(this); // This fails with an exception, because making things dirty with fully qualified name seems not to work while it is attached (simple names work) :-(
		// Since we don't need it recursively anyway, we use the following code instead
		workaround_makeFieldsDirty_noRecursion(this);
		workaround_handleArticlePriceRecursively(this.price);
		if (pm != null)
			pm.flush();
	}

	private void workaround_handleArticlePriceRecursively(ArticlePrice price)
	{
		workaround_makeFieldsDirty_noRecursion(price);
		for (ArticlePrice nestedPrice : price.getNestedArticlePrices())
			workaround_handleArticlePriceRecursively(nestedPrice);
	}

	private void workaround_makeFieldsDirty_noRecursion(Object o)
	{
		List<Field> fields = ReflectUtil.collectAllFields(o.getClass(), true);
		for (Field field : fields) {
			try {
				JDOHelper.makeDirty(o, field.getName());
			} catch (JDOUserException x) { } // silently ignore non-persistent fields causing an exception
		}
	}

	/**
	 * This constructor is used to create an <tt>Article</tt> with a <tt>Product</tt>.
	 * The product will not be allocated by this action! This must be done by outside logic.
	 * <p>
	 * Do not use this constructor directly, but use the {@link Trader} to create an
	 * <tt>Article</tt>!
	 * <p>
	 * The new article will not yet have a price! This must be set afterwards to allow
	 * subclassing of <tt>Article</tt> and making prices dependent on the new fields
	 * of the subclass.
	 */
	public Article(User user, Offer offer, Segment segment, long articleID, Product product, Tariff tariff)
	{
		this(user, offer, segment, articleID, product.getProductType(), product, tariff);
	}

	/**
	 * This constructor is used to create an <tt>Article</tt> with a <tt>ProductType</tt>, means
	 * the allocation of the <tt>Product</tt> will be done later (e.g. after the customer
	 * accepted the <tt>Offer</tt>).
	 * <p>
	 * Do not use this constructor directly, but use the {@link Trader} to create an
	 * <tt>Article</tt>!
	 * <p>
	 * The new article will not yet have a price! This must be set afterwards to allow
	 * subclassing of <tt>Article</tt> and making prices dependent on the new fields
	 * of the subclass.
	 */
	public Article(User user, Offer offer, Segment segment, long articleID, ProductType productType, Tariff tariff)
	{
		this(user, offer, segment, articleID, productType, null, tariff);
	}

	/**
	 * @param offer must not be null
	 * @param segment must not be null
	 * @param articleID must be >= 0
	 * @param productType must not be null
	 * @param product can be null
	 * @param currency must not be null
	 */
	protected Article(
			User user, Offer offer, Segment segment, long articleID, ProductType productType,
			Product product, Tariff tariff)
	{
		init(user, offer, segment, articleID);

		if (productType == null)
			throw new NullPointerException("productType");

		if (productType.isPackageInner()) // TODO wie machen wir das bei inter-organisations-handel? Wir MÜSSEN innerproducts handeln können!
			throw new IllegalArgumentException("productType \""+productType.getPrimaryKey()+"\" is an inner part of a package! Cannot make an article out of it!");

		IPackagePriceConfig packagePriceConfig = productType.getPackagePriceConfig();
		if (packagePriceConfig == null)
			throw new IllegalStateException("packagePriceConfig of productType \""+productType.getPrimaryKey()+"\" is undefined!");

		this.productType = productType;
		this.product = product;
		this.tariff = tariff;

//		PersistenceManager pm = JDOHelper.getPersistenceManager(offer);
//		if (pm == null)
//			throw new IllegalStateException("offer is currently not persistent! Can only create an article with a persistent offer.");
//
//		Accounting accounting = Accounting.getAccounting(pm);
//		AccountingPriceConfig accountingPriceConfig = accounting.getAccountingPriceConfig();

//		this.price = new ArticlePrice(
//				packagePriceConfig.getArticlePrice(this),
//				accounting.getOrganisationID(),
//				accountingPriceConfig.getPriceConfigID(),
//				accountingPriceConfig.createPriceID(), false);
		// The price is set after creation to allow subclassing of Article and making the
		// price dependent on new fields of the subclass.
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the articleID.
	 */
	public long getArticleID()
	{
		return articleID;
	}
	/**
	 * @return Returns the articleID converted to a String using {@link ObjectIDUtil#longObjectIDFieldToString(long)}.
	 */
	public String getArticleIDAsString() {
		return ObjectIDUtil.longObjectIDFieldToString(articleID);
	}
	public static String getPrimaryKey(String organisationID, long articleID)
	{
		return organisationID + '/' + ObjectIDUtil.longObjectIDFieldToString(articleID);
	}
	public String getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * Returns the creation {@link Date} of this {@link Article}.
	 *
	 * @return The creation {@link Date} of this {@link Article}.
	 */
	public Date getCreateDT() {
		return createDT;
	}

	/**
	 * Returns the {@link User} who created this {@link Article}
	 *
	 * @return The {@link User} who created this {@link Article}
	 */
	public User getCreateUser() {
		return createUser;
	}

	/**
	 * @return Returns the productType.
	 */
	public ProductType getProductType()
	{
		return productType;
	}
	/**
	 * @param productType The productType to set.
	 */
	protected void setProductType(ProductType productType)
	{
		this.productType = productType;
	}
	/**
	 * @return Returns the product.
	 */
	public Product getProduct()
	{
		return product;
	}
	/**
	 * @return Returns the price.
	 */
	public ArticlePrice getPrice()
	{
		return price;
	}
	/**
	 * @param price The price to set.
	 */
	public void setPrice(ArticlePrice price)
	{
		// WORKAROUND JPOX should remove dependent objects, but currently it doesn't
		if (this.price != null) {
			PersistenceManager pm = getPersistenceManager();
			if (pm == null)
				throw new IllegalStateException("I thought, this method would only be called in the server?! Marco.");

			ArticlePrice oldPrice = this.price;
			this.price = null;
			pm.deletePersistent(oldPrice);
		}

		this.price = price;
	}
	/**
	 * @return Returns the priceDependentOnOffer.
	 */
	public boolean isPriceDependentOnOffer()
	{
		return priceDependentOnOffer;
	}
	/**
	 * @param priceDependentOnOffer The priceDependentOnOffer to set.
	 */
	protected void setPriceDependentOnOffer(boolean priceDependentOnOffer)
	{
		this.priceDependentOnOffer = priceDependentOnOffer;
	}
	/**
	 * @return Returns the offer.
	 */
	public Offer getOffer()
	{
		return offer;
	}

	/**
	 * @return Returns either <code>null</code> or the <code>Article</code> that is reversed by this instance.
	 */
	public Article getReversedArticle()
	{
		return reversedArticle;
	}
	/**
	 * @return Returns either <code>null</code> or the <code>Article</code> which reverses this instance.
	 */
	public Article getReversingArticle()
	{
		return reversingArticle;
	}

//	/**
//	 * @return Returns the allocated.
//	 */
//	public boolean isAllocated()
//	{
//		return allocated;
//	}
	/**
	 * @return Returns the deliveryNote.
	 */
	public DeliveryNote getDeliveryNote()
	{
		return deliveryNote;
	}
	/**
	 * @param deliveryNote The deliveryNote to set.
	 */
	public void setDeliveryNote(DeliveryNote deliveryNote)
	{
		this.deliveryNote = deliveryNote;
	}

	public ReceptionNote getReceptionNote()
	{
		return receptionNote;
	}
	public void setReceptionNote(ReceptionNote receptionNote)
	{
		this.receptionNote = receptionNote;
	}

	/**
	 * @return Returns the segment.
	 */
	public Segment getSegment()
	{
		return segment;
	}
	/**
	 * @param segment The segment to set.
	 */
	protected void setSegment(Segment segment)
	{
		this.segment = segment;
	}
//	/**
//	 * This method returns the date and time when the Article will
//	 * automatically be released. To change this, you need to call
//	 * <tt>allocate(..)</tt>.
//	 *
//	 * @return Returns the autoReleaseDT.
//	 * @see #allocate(User, Date)
//	 */
//	public Date getAutoReleaseDT()
//	{
//		return autoReleaseDT;
//	}

	// TODO logic for allocate and release must be in the Trader (and not here)
//	/**
//	 * This method allocates the product which is embraced by this Article.
//	 * The allocation work is delegated to the Trader, because it is necessary
//	 * to create Order-s & Offer-s to get the requirement-products.
//	 * @throws ModuleException
//	 */
//	public void allocate(User user, Date autoReleaseDT)
//			throws ModuleException
//	{
//		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//		if (pm == null)
//			throw new IllegalStateException("This instance of Article is currently not persistent!");
//
//		Trader trader = Trader.getTrader(pm);
//		trader.allocateArticle(user, this, autoReleaseDT);
//
//		this.autoReleaseDT = autoReleaseDT;
//		this.allocated = true;
//	}
//
//	public void release(User user)
//			throws ModuleException
//	{
//		if (!allocated)
//			return;
//
//		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//		if (pm == null)
//			throw new IllegalStateException("This instance of Article is currently not persistent!");
//
//		Trader trader = Trader.getTrader(pm);
//		trader.releaseArticle(user, this);
//		this.allocated = false;
//	}

	public Invoice getInvoice() {
		return invoice;
	}
	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	/**
	 * @return Returns the tariff.
	 */
	public Tariff getTariff()
	{
		return tariff;
	}
	/**
	 * @param tariff The tariff to set.
	 */
	public void setTariff(Tariff tariff)
	{
		this.tariff = tariff;
	}
	/**
	 * @return Returns the currency.
	 */
	public Currency getCurrency()
	{
		return currency;
	}

	public boolean isAllocationPending()
	{
		return allocationPending;
	}
	protected void _setAllocationPending(boolean allocationPending)
	{
		this.allocationPending = allocationPending;
	}
	protected void setAllocationPending(boolean allocationPending)
	{
		this.allocationPending = allocationPending;

		if (isReversed())
			getReversingArticle()._setAllocationPending(allocationPending);

		if (isReversing())
			getReversedArticle()._setAllocationPending(allocationPending);
	}
	public boolean isReleasePending()
	{
		return releasePending;
	}

	protected void _setReleasePending(boolean releasePending)
	{
		this.releasePending = releasePending;
	}

	protected void setReleasePending(boolean releasePending)
	{
		this.releasePending = releasePending;

		if (isReversed())
			getReversingArticle()._setReleasePending(releasePending);

		if (isReversing())
			getReversedArticle()._setReleasePending(releasePending);
	}
	public boolean isAllocated()
	{
		return allocated;
	}
	protected void _setAllocated(boolean allocated)
	{
		this.allocated = allocated;
	}
	protected void setAllocated(boolean allocated)
	{
		this.allocated = allocated;

		if (isReversed())
			getReversingArticle()._setAllocated(allocated);

		if (isReversing())
			getReversedArticle()._setAllocated(allocated);
	}

	/**
	 * @return Returns the order.
	 */
	public Order getOrder()
	{
		return order;
	}

	/**
	 * This method is called by {@link Trader#reverseArticles(User, Offer, Collection)}.
	 * When inheriting <code>Article</code>, you MUST override this method, because it
	 * MUST return an instance of the same class as the reversed <code>Article</code>.
	 * <p>
	 * Override this method and create a new instance of your <code>Article</code> implementation
	 * which reverses this Article (if possible).
	 * </p>
	 * <p>
	 * This method should be a one-liner! You should put all your logic into the constructor
	 * (see {@link #Article(User, Offer, long, Article)}).
	 * </p>
	 * @param user The responsible user.
	 * @param offer The <code>Offer</code> into which the new <code>Article</code> will be created.
	 * @param articleID A new (not yet used) unique id within the current organisation for the new <code>Article</code>, that will be created.
	 * @return Returns the new Article that reverses this one.
	 */
	protected Article reverseArticle(User user, Offer offer, long articleID)
	{
		if (this.getClass() != Article.class)
			throw new IllegalStateException("You did not override Article.reverseArticle(...)! When inheriting Article, you must override this method and return an instance of your new class!");

		return new Article(user, offer, articleID, this);
	}

	protected void setReversedArticle(Article reversedArticle)
	{
		this.reversedArticle = reversedArticle;
		this.reversing = reversedArticle != null;
	}
	protected void setReversingArticle(Article reversingArticle)
	{
		this.reversingArticle = reversingArticle;
		this.reversed = reversingArticle != null;
	}

//	protected void setReferencedArticle(Article referencedArticle)
//	{
//		if (this.referencedArticle != null)
//			throw new IllegalStateException("this.referencedArticle != null !!! Cannot re-assign.");
//
//		this.referencedArticle = referencedArticle;
//	}
//
//	public Article createReferencingArticle(User user)
//	{
//		return new Article(user, IDGenerator.nextID(Article.class), this);
//	}
//
//	public Article getReferencedArticle()
//	{
//		return referencedArticle;
//	}

	/**
	 * @return Whether or not this Article has already been reversed. Returns <code>true</code>,
	 *         if {@link #getReversingArticle()} is assigned.
	 */
	public boolean isReversed()
	{
		return reversed;
	}
	/**
	 * @return Whether or not this Article reverses another one. Returns <code>true</code>,
	 *         if {@link #getReversedArticle()} is assigned.
	 */
	public boolean isReversing()
	{
		return reversing;
	}
	/**
	 * @return This flag is set <code>true</code>, if the reversing offer is
	 *         somehow cancelled (rejected, expired, revoked, aborted etc.).
	 */
	public boolean isReversingAborted()
	{
		return isReversingAborted;
	}
	protected void setReversingAborted()
	{
		this.isReversingAborted = true;

		// This article will never be updated because the reversed article is not pointing to this "defunct" reversing article anymore.
		// Therefore it probably looks better, if we mark it as not allocated. The UI should somehow show this status.
		this._setAllocated(false);
		this.setReleasePending(false);
		this.setAllocationPending(false);
	}

	public void jdoPreDelete()
	{
		if (reversedArticle != null)
			reversedArticle.setReversingArticle(null);

		ArticleLocal tmpArticleLocal = this.articleLocal;
		this.articleLocal = null;
		getPersistenceManager().deletePersistent(tmpArticleLocal);
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Article has currently no PersistenceManager assigned!");

		return pm;
	}

	public void jdoPreDetach()
	{
	}

	public void jdoPostDetach(Object _attached)
	{
		Article attached = (Article)_attached;
		Article detached = this;
		Set<?> fetchGroups = attached.getPersistenceManager().getFetchPlan().getGroups();

		boolean fetchGroupsArticleInEditor =
			fetchGroups.contains(FetchGroupsTrade.FETCH_GROUP_ARTICLE_IN_ORDER_EDITOR) ||
			fetchGroups.contains(FetchGroupsTrade.FETCH_GROUP_ARTICLE_IN_OFFER_EDITOR) ||
			fetchGroups.contains(FetchGroupsTrade.FETCH_GROUP_ARTICLE_IN_INVOICE_EDITOR) ||
			fetchGroups.contains(FetchGroupsTrade.FETCH_GROUP_ARTICLE_IN_DELIVERY_NOTE_EDITOR) ||
			fetchGroups.contains(FetchGroupsTrade.FETCH_GROUP_ARTICLE_IN_RECEPTION_NOTE_EDITOR);

// The following lines (nulling all the IDs) is not really necessary, but in case the JDO impl
// has a bug that causes them to be copied, we're sure they're always null - despite of any bug
		detached.orderID = null;
		detached.offerID = null;
		detached.invoiceID = null;
		detached.deliveryNoteID = null;

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_ORDER_ID)) {
			detached.orderID = attached.getOrderID();
			detached.orderID_detached = true;
		}

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_OFFER_ID)) {
			detached.offerID = attached.getOfferID();
			detached.offerID_detached = true;
		}

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_INVOICE_ID)) {
			detached.invoiceID = attached.getInvoiceID();
			detached.invoiceID_detached = true;
		}

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_DELIVERY_NOTE_ID)) {
			detached.deliveryNoteID = attached.getDeliveryNoteID();
			detached.deliveryNoteID_detached = true;
		}

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_RECEPTION_NOTE_ID)) {
			detached.receptionNoteID = attached.getReceptionNoteID();
			detached.receptionNoteID_detached = true;
		}

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_REVERSED_ARTICLE_ID)) {
			detached.reversedArticleID = attached.getReversedArticleID();
			detached.reversedArticleID_detached = true;
		}

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_REVERSING_ARTICLE_ID)) {
			detached.reversingArticleID = attached.getReversingArticleID();
			detached.reversingArticleID_detached = true;
		}

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_VENDOR_ID)) {
			detached.vendorID = attached.getVendorID();
			detached.vendorID_detached = true;
		}

		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_CUSTOMER_ID)) {
			detached.customerID = attached.getCustomerID();
			detached.customerID_detached = true;
		}

		if (fetchGroups.contains(FETCH_GROUP_END_CUSTOMER_ID)) {
			detached.endCustomerID = attached.getEndCustomerID();
			detached.endCustomerID_detached = true;
		}
	}

	@Override
	public void jdoPreAttach() {
		LegalEntity endCustomer;
		try {
			endCustomer = this.getEndCustomer();
		} catch (JDODetachedFieldAccessException x) {
			endCustomer = null;
		}

		if (endCustomer != null) {
			PersistenceManager pm = NLJDOHelper.getThreadPersistenceManager();
			if (this.organisationID.equals(LocalOrganisation.getLocalOrganisation(pm).getOrganisationID())) {
				ArticleID articleID = (ArticleID) JDOHelper.getObjectId(this);
				Article persistentArticle = null;
				try {
					persistentArticle = (Article) pm.getObjectById(articleID);
				} catch (JDOObjectNotFoundException x) {
					// ignore
				}
				if (persistentArticle != null && !endCustomer.equals(persistentArticle.getEndCustomer())) {
					User user = SecurityReflector.getUserDescriptor().getUser(pm);
					pm.makePersistent(new ArticleEndCustomerHistoryItem(this, persistentArticle.getEndCustomer(), endCustomer, user));
				}
			}
		}
	}

	@Override
	public void jdoPostAttach(Object o) {
	}

	public void jdoPreStore()
	{
		if (primaryKey == null) {
			primaryKey = getPrimaryKey(organisationID, articleID);
			logger.info("Seems, the JPOX bug still exists: primaryKey == null! Resetting it to " + primaryKey);
		}

		if (JDOHelper.isNew(this) && endCustomer != null) {
			PersistenceManager pm = getPersistenceManager();
			User user = SecurityReflector.getUserDescriptor().getUser(pm);
			pm.makePersistent(new ArticleEndCustomerHistoryItem(this, null, endCustomer, user));
		}
	}

	public void setAllocationException(String allocationExceptionClass, String allocationExceptionMessage, String allocationExceptionStackTrace)
	{
		this.allocationExceptionClass = allocationExceptionClass;
		this.allocationExceptionMessage = allocationExceptionMessage;
		this.allocationExceptionStackTrace = allocationExceptionStackTrace;
	}

	public void setAllocationException(Throwable t)
	{
		if (t == null) {
			this.allocationExceptionClass = null;
			this.allocationExceptionMessage = null;
			this.allocationExceptionStackTrace = null;
			return;
		}

		Throwable root = ExceptionUtils.getRootCause(t);
		if (root == null)
			root = t;
//		StackTraceElement ste = root.getStackTrace()[0];

		this.allocationExceptionClass = root.getClass().getName();
		this.allocationExceptionMessage = root.getLocalizedMessage();
//		this.allocationExceptionStackTrace = ste.toString();
		this.allocationExceptionStackTrace = Util.getStackTraceAsString(root);
	}

	public void setReleaseException(String releaseExceptionClass, String releaseExceptionMessage, String releaseExceptionStackTrace)
	{
		this.releaseExceptionClass = releaseExceptionClass;
		this.releaseExceptionMessage = releaseExceptionMessage;
		this.releaseExceptionStackTrace = releaseExceptionStackTrace;
	}

	public void setReleaseException(Throwable t)
	{
		if (t == null) {
			this.releaseExceptionClass = null;
			this.releaseExceptionMessage = null;
			this.releaseExceptionStackTrace = null;
			return;
		}

		Throwable root = ExceptionUtils.getRootCause(t);
		if (root == null)
			root = t;
		StackTraceElement ste = root.getStackTrace()[0];

		this.releaseExceptionClass = root.getClass().getName();
		this.releaseExceptionMessage = root.getLocalizedMessage();
		this.releaseExceptionStackTrace = ste.toString();
	}

	public String getAllocationExceptionClass()
	{
		return allocationExceptionClass;
	}
	public String getAllocationExceptionMessage()
	{
		return allocationExceptionMessage;
	}
	public String getAllocationExceptionStackTrace()
	{
		return allocationExceptionStackTrace;
	}
	public boolean isAllocationAbandoned()
	{
		return allocationAbandoned;
	}
	public void setAllocationAbandoned(boolean allocationAbandoned)
	{
		this.allocationAbandoned = allocationAbandoned;
	}

	public String getReleaseExceptionClass()
	{
		return releaseExceptionClass;
	}
	public String getReleaseExceptionMessage()
	{
		return releaseExceptionMessage;
	}
	public String getReleaseExceptionStackTrace()
	{
		return releaseExceptionStackTrace;
	}
	public boolean isReleaseAbandoned()
	{
		return releaseAbandoned;
	}
	public void setReleaseAbandoned(boolean releaseAbandoned)
	{
		this.releaseAbandoned = releaseAbandoned;
	}

	public ArticleLocal getArticleLocal()
	{
		return articleLocal;
	}
	protected void setArticleLocal(ArticleLocal articleLocal)
	{
		this.articleLocal = articleLocal;
	}

	public ArticleLocal createArticleLocal(User user)
	{
		if (articleLocal != null)
			throw new IllegalStateException("There is already an instance of ArticleLocal assigned to this Article: " + getPrimaryKey());

		ArticleLocal al = new ArticleLocal(this);
		setArticleLocal(al);
		return al;
	}


	/**
	 * Returns the estimated delivery date for this article specified in the corresponding {@link Offer}.
	 * @return the deliveryDateOffer (may be null)
	 */
	public Date getDeliveryDateOffer() {
		return deliveryDateOffer;
	}

	/**
	 * Sets the estimated delivery date for this article specified in the corresponding {@link Offer}.
	 * This may only be set until the corresponding {@link Offer} where this article is contained in, is not yet finalized,
	 * otherwise an exception is thrown.
	 * @param deliveryDateOffer the deliveryDateOffer to set
	 */
	public void setDeliveryDateOffer(Date deliveryDateOffer) {
		if (offer.isFinalized()) {
			throw new IllegalStateException("Once the offer of an article is finalized, the deliveryDateOffer for the article can not be changed anymore!");
		}
		this.deliveryDateOffer = deliveryDateOffer;
	}

	/**
	 * Returns the estimated delivery date for this article specified in the corresponding {@link DeliveryNote}.
	 * @return the deliveryDateDeliveryNote (may be null)
	 */
	public Date getDeliveryDateDeliveryNote() {
		return deliveryDateDeliveryNote;
	}

	/**
	 * Sets the estimated delivery date for this article specified in the corresponding {@link DeliveryNote}.
	 * This may only be set until the corresponding {@link DeliveryNote} where this article is contained in, is not yet finalized,
	 * otherwise an exception is thrown.
	 * @param deliveryDateDeliveryNote the deliveryDateDeliveryNote to set
	 */
	public void setDeliveryDateDeliveryNote(Date deliveryDateDeliveryNote) {
		if (deliveryNote.isFinalized()) {
			throw new IllegalStateException("Once the delivery note of an article is finalized, the deliveryDateDeliveryNote for the article can not be changed anymore!");
		}
		this.deliveryDateDeliveryNote = deliveryDateDeliveryNote;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + ObjectIDUtil.longObjectIDFieldToString(articleID) + ']' + "(version " + JDOHelper.getVersion(this) + ')';
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof Article))
			return false;

		Article o = (Article)obj;

		return
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.articleID, o.articleID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) ^ Util.hashCode(articleID);
	}

	/**
	 * This is a temporary method until this problem is solved: http://www.jpox.org/servlet/forum/viewthread?thread=4718
	 */
	public void makeAllDirty()
	{
		if (!JDOHelper.isDetached(this))
			throw new IllegalStateException("This method may only be called while this object is detached!");

		try {
			DeliveryNote d = deliveryNote;
			deliveryNote = null;
			deliveryNote = d;
		} catch (JDODetachedFieldAccessException x) {
			// ignore
		}

		try {
			Invoice i = invoice;
			invoice = null;
			invoice = i;
		} catch (JDODetachedFieldAccessException x) {
			// ignore
		}

		try {
			Offer o = offer;
			offer = null;
			offer = o;
		} catch (JDODetachedFieldAccessException x) {
			// ignore
		}

		try {
			Article a = reversedArticle;
			reversedArticle = null;
			reversedArticle = a;
		} catch (JDODetachedFieldAccessException x) {
			// ignore
		}

		try {
			Article a = reversingArticle;
			reversingArticle = null;
			reversingArticle = a;
		} catch (JDODetachedFieldAccessException x) {
			// ignore
		}

		try {
			boolean r = reversed;
			reversed = !r;
			reversed = r;
		} catch (JDODetachedFieldAccessException x) {
			// ignore
		}

		try {
			boolean r = reversing;
			reversing = !r;
			reversing = r;
		} catch (JDODetachedFieldAccessException x) {
			// ignore
		}

		try {
			ArticlePrice p = price;
			price = null;
			price = p;
		} catch (JDODetachedFieldAccessException x) {
			// ignore
		}
	}

	/**
	 * This is a temporary method until this problem is solved: http://www.jpox.org/servlet/forum/viewthread?thread=4718
	 */
	public void checkReversing()
	{
		Article reversedArticle = this.getReversedArticle();
		if (reversedArticle != null) {
			if (reversedArticle.getReversingArticle() == null)
				reversedArticle.setReversingArticle(this);
		}
	}

	public LegalEntity getEndCustomer() {
		return endCustomer;
	}

	public void setEndCustomer(LegalEntity endCustomer) {
		if (Util.equals(this.endCustomer, endCustomer))
			return;

		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm != null) {
			User user = SecurityReflector.getUserDescriptor().getUser(pm);
			pm.makePersistent(new ArticleEndCustomerHistoryItem(this, this.endCustomer, endCustomer, user));
		}

		this.endCustomer = endCustomer;

		this.endCustomerID = null;
		this.endCustomerID_detached = false;
	}

	public AnchorID getEndCustomerID()
	{
		if (endCustomerID == null && !endCustomerID_detached)
			endCustomerID = (AnchorID) JDOHelper.getObjectId(endCustomer);

		return endCustomerID;
	}
}
