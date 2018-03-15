package org.nightlabs.jfire.web.demoshop.shoppingcart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.store.id.ProductTypeID;


/**
 * @author Khaled Soliman - khaled[at]nightlabs[dot]de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Attapol Thomprasert - attapol[at]nightlabs[dot]de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ShoppingCart
{
	// this item list is sorted
	private List<ShoppingCartItem> items = new ArrayList<ShoppingCartItem>();

	/**
	 * get the list of the current ShoppingCartItems
	 * Note that this Collection is unmodifiable,
	 * use {@link #addItem(ShoppingCartItem)} and
	 * {@link #removeItems(ShoppingCartItem, int)}
	 * 
	 * @return list of ShoppingCartItems
	 */
	public List<ShoppingCartItem> getItems()
	{
		// We expose only the unmodifiable list
		// the list has to be manipulated using
		// the add() and remove() methods!
		return Collections.unmodifiableList(items);
	}

	/**
	 * Get the shopping cart items grouped with occurency count.
	 * @return A map with item pointing to count
	 */
	public Map<ShoppingCartItem, Integer> getGroupedItems()
	{
		Map<ShoppingCartItem, Integer> groupedItems = new HashMap<ShoppingCartItem, Integer>();
		for (ShoppingCartItem item : getItems()) {
			if(!groupedItems.containsKey(item))
				groupedItems.put(item, 0);
			groupedItems.put(item, groupedItems.get(item) + 1);
		}
		return groupedItems;
	}

	/**
	 * Add quantity of specified Item into the shoppingcart
	 * @param productTypeID
	 * @param tariffID
	 * @param quantity
	 */

	public void addItem(ShoppingCartItem item) {
			items.add(item);
	}

	/**
	 * iterate over all ShoppinCartItems and get the Total price as a double
	 * @return double total
	 */
	public double getTotalAsDouble()
	{
		double total = 0;
		for (Iterator<ShoppingCartItem> iter = getItems().iterator(); iter.hasNext();) {
			ShoppingCartItem item = iter.next();
			try {
				total += ((ShoppingCartItem)item).getPrice().getAmountAsDouble();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return total;
	}

	/**
	 * Remove ShoppingCartItem from shoppingcart. If quantity of Items can not be removed
	 * a RuntimeExeption is thrown.
	 * @param itemToRemove
	 * @param quantity
	 */
	public void removeItem(ShoppingCartItem itemToRemove) {
		items.remove(itemToRemove);
	}

	/**
	 * Find the amount of the specified Item within the shoppincart
	 * @param productTypeID
	 * @param tariffID
	 * @return quantity of Items within this shoppingcart
	 */
	public int quantityOfSpecificItem(ProductTypeID productTypeID, TariffID tariffID)
	{
		int quantity = 0;
		ShoppingCartItem itemToCheck = new ShoppingCartItemSimpleProductType(productTypeID, tariffID);
		boolean found = false;
		for (Iterator<ShoppingCartItem> iter = items.iterator(); iter.hasNext();) {
			ShoppingCartItem item = iter.next();
			if(item.equals(itemToCheck)) {
				found = true;
				quantity++;
			} else if(found)
				// items are always sorted: stop iterating here
				break;
		}
		return quantity;
	}

	public int quantityOfSpecificItem(ProductTypeID productTypeID)
	{
		int quantity = 0;
		ShoppingCartItem itemToCheck = new ShoppingCartItemVoucherType(productTypeID);
		boolean found = false;
		for (Iterator<ShoppingCartItem> iter = items.iterator(); iter.hasNext();) {
			ShoppingCartItem item = iter.next();
			if(item.equals(itemToCheck)) {
				found = true;
				quantity++;
			} else if(found)
				// items are always sorted: stop iterating here
				break;
		}
		return quantity;
	}

	public boolean hasItems() {
		if(items == null) return false;
		if(items.isEmpty()) return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return items == null ? "empty" : items.toString();
	}
}
