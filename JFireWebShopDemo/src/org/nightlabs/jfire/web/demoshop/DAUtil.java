package org.nightlabs.jfire.web.demoshop;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.naming.NamingException;
import javax.servlet.ServletException;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.accounting.gridpriceconfig.TariffPricePair;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.exception.PropertyException;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.simpletrade.SimpleTradeManagerRemote;
import org.nightlabs.jfire.simpletrade.dao.TariffPricePairDAO;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.simpletrade.store.search.SimpleProductTypeQuery;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.dao.ProductTypeDAO;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.voucher.accounting.VoucherPriceConfig;
import org.nightlabs.jfire.voucher.store.VoucherType;
import org.nightlabs.jfire.voucher.store.search.VoucherTypeQuery;
import org.nightlabs.jfire.web.login.Login;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.util.CollectionUtil;

public class DAUtil
{
	private DAUtil() {}

	public static Map<ProductType,Collection<TariffPricePair>> getSimpleProductPrices(ArrayList<ProductType>prodCollection,CurrencyID currencyId) throws ServletException
	{
		Map<ProductType,Collection<TariffPricePair>> result = new HashMap<ProductType, Collection<TariffPricePair>>();
		Collection<TariffPricePair> pricePairs;
		for (ProductType productType : prodCollection) {
			pricePairs = TariffPricePairDAO.sharedInstance().getTariffPricePairs(
					(ProductTypeID)JDOHelper.getObjectId(productType),
					CustomerGroupID.create(productType.getOrganisationID(), CustomerGroup.CUSTOMER_GROUP_ID_DEFAULT),
					currencyId,
					new NullProgressMonitor());
			result.put(productType, pricePairs);
		}
		return result;
	}

	public static ArrayList<ProductType> getSaleableSimpleProductTypes() throws ServletException
	{
		SimpleProductTypeQuery searchFilter = new SimpleProductTypeQuery();
		Collection<SimpleProductType> productTypes = searchProductTypes(searchFilter);
		ArrayList<ProductType> prodCollection = new ArrayList<ProductType>();
		for (SimpleProductType productType : productTypes) {
			if(productType.isSaleable()) {
				prodCollection.add(productType);
			}
		}
		return prodCollection;
	}

	public static ArrayList<ProductType> getSaleableVoucherTypes() throws ServletException
	{
		VoucherTypeQuery searchFilter = new VoucherTypeQuery();
		Collection<ProductType> productTypes = searchVoucherTypes(searchFilter);
		ArrayList<ProductType> prodCollection = new ArrayList<ProductType>();
		for (ProductType productType : productTypes) {
			if(productType.isSaleable()) {
				prodCollection.add(productType);
			}
		}
		return prodCollection;
	}

	public static Collection<SimpleProductType> searchProductTypes(SimpleProductTypeQuery searchQuery) throws ServletException
	{
		try {
			QueryCollection<AbstractSearchQuery> qc = new QueryCollection<AbstractSearchQuery>(SimpleProductType.class);
			qc.add(searchQuery);

			List<ProductType> productTypes = ProductTypeDAO.sharedInstance().queryProductTypes(qc,
					new String[] { FetchPlan.DEFAULT, ProductType.FETCH_GROUP_NAME,SimpleProductType.FETCH_GROUP_PROPERTY_SET, ProductType.FETCH_GROUP_PACKAGE_PRICE_CONFIG},
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					new NullProgressMonitor());
			return CollectionUtil.castList(productTypes);
//			return JFireEjbFactory.getBean(StoreManager.class, Login.getLogin().getInitialContextProperties()).searchProductTypes(
//					searchFilter,
//					new String[] { FetchPlan.DEFAULT, ProductType.FETCH_GROUP_NAME,SimpleProductType.FETCH_GROUP_PROPERTY_SET, ProductType.FETCH_GROUP_PACKAGE_PRICE_CONFIG},
//					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
//				);
		} catch (Exception e) {
			throw new ServletException("Failed to get search simpleproduct types", e);
		}
	}

	public static Collection<ProductType> searchVoucherTypes(VoucherTypeQuery searchQuery) throws ServletException
	{
		try {
			QueryCollection<AbstractSearchQuery> qc = new QueryCollection<AbstractSearchQuery>(VoucherType.class);
			qc.add(searchQuery);

			List<ProductType> productTypes = ProductTypeDAO.sharedInstance().queryProductTypes(qc,
					new String[] {FetchPlan.DEFAULT, ProductType.FETCH_GROUP_NAME, ProductType.FETCH_GROUP_PACKAGE_PRICE_CONFIG, VoucherPriceConfig.FETCH_GROUP_PRICES},
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					new NullProgressMonitor());
			return CollectionUtil.castList(productTypes);
		} catch (Exception e) {
			throw new ServletException("Failed to get search voucher types", e);
		}
	}

	public static DataField getDataField(ProductTypeID productTypeID, StructFieldID structFieldID) throws RemoteException, CreateException, NamingException, PropertyException
	{
		Set<ProductTypeID> productTypesIDs = new HashSet<ProductTypeID>(1);
		productTypesIDs.add(productTypeID);
		Set<StructFieldID> structFieldIDs = new HashSet<StructFieldID>(1);
		structFieldIDs.add(structFieldID);

		// FIXME: dao
		SimpleTradeManagerRemote stm = JFireEjb3Factory.getRemoteBean(SimpleTradeManagerRemote.class, Login.getLogin().getInitialContextProperties());
		Map<ProductTypeID, PropertySet> simpleProductTypesPropertySets = stm
				.getSimpleProductTypesPropertySets(
				productTypesIDs, structFieldIDs, new String[] {FetchPlan.DEFAULT, PropertySet.FETCH_GROUP_FULL_DATA, PropertySet.FETCH_GROUP_DATA_FIELDS}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		PropertySet propertySet = simpleProductTypesPropertySets.get(productTypeID);
		if(propertySet == null)
			throw new IllegalStateException("getSimpleProductTypesPropertySets failed");

		StructLocal structLocal = StructLocalDAO.sharedInstance().getStructLocal(
				propertySet.getStructLocalObjectID(),
				new NullProgressMonitor());
		propertySet.inflate(structLocal);
		DataField dataField = propertySet.getDataField(structFieldID);
		return dataField;
	}

//	public static Order getOrder () {
//		TradeManagerRemote tm = null;
//		try {
//			tm = JFireEjb3Factory.getRemoteBean(TradeManagerRemote.class, Login.getLogin().getInitialContextProperties());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
////		AnchorID customerID = getCustomerID();
////		Order order = tm.createOrder(
////				customerID, null, currencyID,
////				new SegmentTypeID[] {null}, // null here is a shortcut for default segment type
////				OrderRootTreeNode.FETCH_GROUPS_ORDER, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
//		return null;
//	}

}
