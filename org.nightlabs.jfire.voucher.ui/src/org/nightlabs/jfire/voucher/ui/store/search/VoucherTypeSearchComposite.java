package org.nightlabs.jfire.voucher.ui.store.search;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.search.AbstractProductTypeQuery;
import org.nightlabs.jfire.trade.ui.store.search.AbstractProductTypeSearchComposite;
import org.nightlabs.jfire.voucher.store.VoucherType;
import org.nightlabs.jfire.voucher.store.search.VoucherTypeQuery;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class VoucherTypeSearchComposite
extends AbstractProductTypeSearchComposite
{
	/**
	 * @param parent
	 * @param style
	 */
	public VoucherTypeSearchComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected AbstractProductTypeQuery createNewQuery() {
		return new VoucherTypeQuery();
	}

	@Override
	protected Class<? extends AbstractProductTypeQuery> getQueryClass()
	{
		return VoucherTypeQuery.class;
	}

	@Override
	protected Class<? extends ProductType> getResultClass()
	{
		return VoucherType.class;
	}

}
