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

package org.nightlabs.jfire.accounting;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.nightlabs.jfire.accounting.id.InvoiceLocalID;
import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.StatableLocal;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.InvoiceLocalID"
 *		detachable="true"
 *		table="JFireTrade_InvoiceLocal"
 *
 * @jdo.version strategy="version-number"
 *
 * @jdo.implements name="org.nightlabs.jfire.jbpm.graph.def.StatableLocal"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, invoiceIDPrefix, invoiceID"
 *		include-body="id/InvoiceLocalID.body.inc"
 *
 * @jdo.fetch-group name="Invoice.invoiceLocal" fields="invoice"
 * @jdo.fetch-group name="InvoiceLocal.invoice" fields="invoice"
 * @jdo.fetch-group name="InvoiceLocal.bookUser" fields="bookUser"
 * @jdo.fetch-group name="InvoiceLocal.this" fields="invoice, bookUser, state, states"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleContainerInEditor" fetch-groups="InvoiceLocal.this"
 *
 * @jdo.fetch-group name="StatableLocal.state" fields="state"
 * @jdo.fetch-group name="StatableLocal.states" fields="states"
 *
 */
@PersistenceCapable(
	objectIdClass=InvoiceLocalID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_InvoiceLocal")
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@FetchGroups({
	@FetchGroup(
		name="Invoice.invoiceLocal",
		members=@Persistent(name="invoice")),
	@FetchGroup(
		name=InvoiceLocal.FETCH_GROUP_INVOICE,
		members=@Persistent(name="invoice")),
	@FetchGroup(
		name=InvoiceLocal.FETCH_GROUP_BOOK_USER,
		members=@Persistent(name="bookUser")),
	@FetchGroup(
		name=InvoiceLocal.FETCH_GROUP_THIS_INVOICE_LOCAL,
		members={@Persistent(name="invoice"), @Persistent(name="bookUser"), @Persistent(name="state"), @Persistent(name="states")}),
	@FetchGroup(
		name="FetchGroupsTrade.articleContainerInEditor",
		members={@Persistent(name="invoice"), @Persistent(name="bookUser"), @Persistent(name="state"), @Persistent(name="states")}),
	@FetchGroup(
		name="StatableLocal.state",
		members=@Persistent(name="state")),
	@FetchGroup(
		name="StatableLocal.states",
		members=@Persistent(name="states"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class InvoiceLocal
implements Serializable, StatableLocal
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_INVOICE = "InvoiceLocal.invoice";
	public static final String FETCH_GROUP_BOOK_USER = "InvoiceLocal.bookUser";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_INVOICE_LOCAL = "InvoiceLocal.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	@PrimaryKey
	@Column(length=50)
	private String invoiceIDPrefix;
	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long invoiceID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Invoice invoice;

	/**
	 * This member represents the amount already paid.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long amountPaid;

	/**
	 * This flag is true as long as the vendor still hopes to get the money. This means,
	 * it is set false automatically when paid becomes true or when the vendor gives up
	 * - e.g. because the customer is bankrupt.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean outstanding = true;

	/**
	 * This member stores the user who booked this Invoice.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private User bookUser = null;

	/**
	 * This member stores when this Invoice was booked.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date bookDT  = null;

	private User bookUncollectableUser = null;

	private Date bookUncollectableDT = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private State state;

	/**
	 * This is the history of <b>all</b> {@link State}s with the newest last and the oldest first.
	 * Of course, only the states known to the current organisation are linked here.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="State"
	 *		table="JFireTrade_InvoiceLocal_states"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_InvoiceLocal_states",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<State> states;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean processEnded = false;

	@Override
	public boolean isProcessEnded()
	{
		return processEnded;
	}
	@Override
	public void setProcessEnded()
	{
		processEnded = true;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected InvoiceLocal() { }

	public InvoiceLocal(Invoice invoice)
	{
		this.organisationID = invoice.getOrganisationID();
		this.invoiceIDPrefix = invoice.getInvoiceIDPrefix();
		this.invoiceID = invoice.getInvoiceID();
		this.invoice = invoice;
		this.invoiceActionHandlers = new HashSet<InvoiceActionHandler>();

		invoice.setInvoiceLocal(this);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getInvoiceIDPrefix()
	{
		return invoiceIDPrefix;
	}
	public long getInvoiceID()
	{
		return invoiceID;
	}

	/**
	 * @return the same as {@link #getStatable()}
	 */
	public Invoice getInvoice()
	{
		return invoice;
	}
	public Statable getStatable()
	{
		return invoice;
	}

	protected void incAmountPaid(long diffAmount)
	{
		this.amountPaid = amountPaid + diffAmount;

		if (getAmountToPay() == 0) {
			// Make all ArticleLocal instances dirty since they have the non-persistent flag invoicePaid
			// and require refreshing in the client.

			for (Article article : getInvoice().getArticles())
				JDOHelper.makeDirty(article.getArticleLocal(), "articleID");
		}
	}

	public long getAmountPaid() {
		return amountPaid;
	}
	public long getAmountToPay() {
		if (isBookedUncollectable()) // If it is already booked out, there is no money outstanding anymore - it's a definite loss. Marco.
			return 0;

		return getInvoice().getPrice().getAmount() - getAmountPaid();
	}

	public boolean isOutstanding() {
		return outstanding;
	}
	public void setOutstanding(boolean outstanding)
	{
		if (this.outstanding != outstanding) {
			this.outstanding = outstanding;
		}
	}

	protected void setBooked(User bookUser) {
		if (this.bookDT != null)
			return;

		this.bookUser = bookUser;
		this.bookDT = new Date();
	}
	public User getBookUser() {
		return bookUser;
	}
	public Date getBookDT() {
		return bookDT;
	}
	/**
	 * This member is set to true as soon as the money is booked on the various
	 * accounts.
	 */
	public boolean isBooked() {
		return bookDT != null;
	}


	protected void setBookUncollectableUser(User bookUncollectableUser) {
		if (bookUncollectableUser == null)
			throw new IllegalArgumentException("bookUncollectableUser must not be null!");

		if (this.bookUncollectableDT != null)
			return;

		this.bookUncollectableUser = bookUncollectableUser;
		this.bookUncollectableDT = new Date();
		this.setOutstanding(false);
	}
	public User getBookUncollectableUser() {
		return bookUncollectableUser;
	}
	public Date getBookUncollectableDT() {
		return bookUncollectableDT;
	}
	/**
	 * This member is set to true as soon as the invoice has been booked
	 * as being uncollectable.
	 */
	public boolean isBookedUncollectable() {
		return bookUncollectableDT != null;
	}


	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="InvoiceActionHandler"
	 *		table="JFireTrade_InvoiceLocal_invoiceActionHandlers"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_InvoiceLocal_invoiceActionHandlers",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<InvoiceActionHandler> invoiceActionHandlers;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Set<InvoiceActionHandler> _invoiceActionHandlers = null;

	/**
	 * @return Instances of {@link InvoiceActionHandler}.
	 */
	public Set<InvoiceActionHandler> getInvoiceActionHandlers()
	{
		if (_invoiceActionHandlers == null)
			_invoiceActionHandlers = Collections.unmodifiableSet(invoiceActionHandlers);

		return _invoiceActionHandlers;
	}

	public void addInvoiceActionHandler(InvoiceActionHandler invoiceActionHandler)
	{
		if (!invoiceActionHandlers.contains(invoiceActionHandler))
			invoiceActionHandlers.add(invoiceActionHandler);
	}

	public boolean removeInvoiceActionHandler(InvoiceActionHandler invoiceActionHandler)
	{
		return invoiceActionHandlers.remove(invoiceActionHandler);
	}

	/**
	 * This method is <b>not</b> intended to be called directly. It is called by
	 * {@link State#State(String, long, User, Statable, org.nightlabs.jfire.jbpm.graph.def.StateDefinition)}
	 * which is called automatically by {@link ActionHandlerNodeEnter}, if this <code>ActionHandler</code> is registered.
	 */
	public void setState(State currentState)
	{
		if (currentState == null)
			throw new IllegalArgumentException("state must not be null!");

		this.state = currentState;
		this.states.add(currentState);
	}

	public State getState()
	{
		return state;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient List<State> _states = null;

	public List<State> getStates()
	{
		if (_states == null)
			_states = CollectionUtil.castList(Collections.unmodifiableList(states));

		return _states;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long jbpmProcessInstanceId = -1;

	public long getJbpmProcessInstanceId()
	{
		return jbpmProcessInstanceId;
	}

	public void setJbpmProcessInstanceId(long jbpmProcessInstanceId)
	{
		this.jbpmProcessInstanceId = jbpmProcessInstanceId;
	}

}
