package org.nightlabs.jfire.scripting.print.ui.transfer.delivery;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nightlabs.base.ui.wizard.IWizardHopPage;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.scripting.editor2d.ScriptRootDrawComponent;
import org.nightlabs.jfire.scripting.editor2d.util.VisibleScriptUtil;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.jfire.scripting.ui.ScriptingPlugin;
import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.store.deliver.DeliveryException;
import org.nightlabs.jfire.store.deliver.DeliveryResult;
import org.nightlabs.jfire.store.id.ProductID;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.ui.transfer.deliver.AbstractClientDeliveryProcessor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class AbstractClientDeliveryProcessorPrint
extends AbstractClientDeliveryProcessor
{
	private static final Logger logger = Logger.getLogger(AbstractClientDeliveryProcessorPrint.class);

//	private TicketDataProviderThread ticketDataProviderThread;

	private AbstractScriptDataProviderThread abstractScriptDataProviderThread;
	protected abstract AbstractScriptDataProviderThread createScriptDataProviderThread(
			AbstractClientDeliveryProcessorPrint clientDeliveryProcessor);

	/**
	 * If you inherit this class and override this method, you <b>must</b> call
	 * <code>super.init();</code> in your implementation as first statement!!!
	 */
	@Override
	public void init() {
		// currently nothing to do, but this might change!


		// if the delivery-direction is incoming (e.g. for reversed articles), there's no need to do anything
		noopAll = !Delivery.DELIVERY_DIRECTION_OUTGOING.equals(getDelivery().getDeliveryDirection());
	}

	/**
	 * It does not make sense to print tickets when we receive sth.. This can only be the case for reversed articles
	 * and hence, we simply ignore everything silently. This flag is initialised in {@link #init()}.
	 */
	private boolean noopAll = false;

	private long totalTimeMeasure = 0;

	/**
	 * When subclassing this class, you should <b>not</b> override this method
	 * but implement {@link #printBegin()} instead!
	 */
	public DeliveryResult deliverBegin() throws DeliveryException {
		DeliveryProcessorPrintDebugInfo.beginMeasure();
		totalTimeMeasure = System.currentTimeMillis();
		if (noopAll)
			return null;

		if (abstractScriptDataProviderThread != null)
			throw new IllegalStateException("this.ticketDataProviderThread is not null!!!"); //$NON-NLS-1$

		DeliveryResult result;

		// start preparing the whole document asynchronously (it will be joined in
		// deliverDoWork()
// Marco: This code does not work here anymore, because some of the relevant data we need
// is generated in the beginServer phase of the delivery process. Therefore, we must
// start the data provider thread in the doWork phase below. 2007-04-02
//		abstractScriptDataProviderThread = createScriptDataProviderThread(this);
//		boolean interruptDataProvider = true;
//		try {
//			abstractScriptDataProviderThread.start();
//
//			result = printBegin();
//
//			interruptDataProvider = false;
//		} finally {
//			if (interruptDataProvider) {
//				// ticketDataProviderThread.interrupt(); // TODO reactivate this line
//				// once everything is working
//				abstractScriptDataProviderThread = null;
//			}
//		}
		result = printBegin();
		return result;
	}

	protected abstract DeliveryResult printBegin() throws DeliveryException;

	/**
	 * When subclassing this class, you should not override this method but
	 * implement {@link #printDocuments(List, boolean)} instead.
	 */
	public DeliveryResult deliverDoWork() throws DeliveryException {
		if (noopAll)
			return null;

		long start = 0;
		if (logger.isDebugEnabled()) {
			start = System.currentTimeMillis();
			logger.debug("deliverDoWork Begin!"); //$NON-NLS-1$
		}

		// start the data provider thread here, after the necessary data (e.g. VoucherKey) has
		// been created in the beginServer phase.
		abstractScriptDataProviderThread = createScriptDataProviderThread(this);
		abstractScriptDataProviderThread.start();

		// print all the tickets
		if (getDelivery().isFailed() || getDelivery().isForceRollback())
			throw new DeliveryException(new DeliveryResult(
					DeliveryResult.CODE_FAILED,
					"Delivery failed or rollback-forced. Print aborted! failed=" //$NON-NLS-1$
							+ getDelivery().isFailed() + " forceRollback=" //$NON-NLS-1$
							+ getDelivery().isForceRollback(), null));

		if (abstractScriptDataProviderThread == null)
			throw new IllegalStateException(
					"this.scriptDataProviderThread is null!!!"); //$NON-NLS-1$

		Throwable error = abstractScriptDataProviderThread.getError();
		if (error != null)
			throw new DeliveryException(new DeliveryResult(abstractScriptDataProviderThread.getError()));

		try {
			List<ScriptRootDrawComponent> ticketDrawComponents = new LinkedList<ScriptRootDrawComponent>();
			int preferredDocumentCount = getPreferredDocumentCount();
			boolean waitUntilPreferredCountFetched = preferredDocumentCount > 0;
			while (true) {
				if (abstractScriptDataProviderThread.getError() != null)
					throw abstractScriptDataProviderThread.getError();

				ArticleID articleID = abstractScriptDataProviderThread.fetchReadyArticleID();
				if (articleID != null) {
					ProductID productID = abstractScriptDataProviderThread.getProductID(articleID, true);
//					File ticketLayoutFile = abstractScriptDataProviderThread.getLayoutFileByProductID(productID);
//					ScriptRootDrawComponent ticketDrawComponent = getScriptRootDrawComponent(ticketLayoutFile);
					ScriptRootDrawComponent ticketDrawComponent = getScriptRootDrawComponent(productID);

					Map<ScriptRegistryItemID, Object> scriptResultMap = abstractScriptDataProviderThread.getScriptResultMap(productID, true);
					// inject the data into the TicketDrawComponent
					// and evaluate all local scripts
					ticketDrawComponent.assignScriptResults(scriptResultMap);
					// evaluate visible scripts
					VisibleScriptUtil.assignVisibleScriptResults(ticketDrawComponent, scriptResultMap);

					ticketDrawComponents.add(ticketDrawComponent);
				}
				if (waitUntilPreferredCountFetched) {
					// If should wait until we have the preferred count or nothing more in the queue
					if (articleID == null || ticketDrawComponents.size() >= preferredDocumentCount) {
						// print the preferred count of documents or all left
						if (ticketDrawComponents.size() > 0)
							printDocuments(ticketDrawComponents, articleID == null);
						ticketDrawComponents.clear();
					}
				}
				else {
					if (articleID == null
							|| ticketDrawComponents.size() >= 5 // if we already have a few, we already start printing - even though we might still fetch more from the data-provider-thread. this way, the printing system can already start processing the job. This number might be changed later for performance tuning! Maybe we even make it configurable. Marco.
							|| !abstractScriptDataProviderThread.canInvokeFetchReadyArticleIDWithoutBlocking()
					)
					{
						printDocuments(ticketDrawComponents, articleID == null);
						// imho we should remove all from this list now - otherwise it would
						// be printed multiple times...
						ticketDrawComponents.clear();
					}
				}

				if (articleID == null)
					break;
			}
		} catch (Throwable e) {
			logger.error("deliverDoWork failed!", e); //$NON-NLS-1$
			throw new DeliveryException(new DeliveryResult(e));
		}

		if (logger.isDebugEnabled()) {
			long duration = System.currentTimeMillis() - start;
			logger.debug("deliverDoWork took " + duration + " ms!"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return null; // null means OK (no special ok, but ok ;-)
	}

	/**
	 * This method may be called multiple times (between printBegin and printEnd),
	 * because the data retrieval and the printing are done asynchronously in
	 * parallel.
	 *
	 * @param ticketDrawComponents
	 * @param lastEntry
	 *          determines if this is the last call for this method before
	 *          deliverEnd
	 * @throws DeliveryException
	 */
	protected abstract void printDocuments(
			List<ScriptRootDrawComponent> ticketDrawComponents,
			boolean lastEntry) throws DeliveryException;

	/**
	 * When subclassing this class, you should <b>not</b> override this method
	 * but implement {@link #printEnd()} instead!
	 */
	public DeliveryResult deliverEnd() throws DeliveryException {
		if (noopAll)
			return null;

		DeliveryResult printEndResult = printEnd();
		DeliveryProcessorPrintDebugInfo.addTime(
				DeliveryProcessorPrintDebugInfo.CAT_TOTAL_TIME,
				System.currentTimeMillis() - totalTimeMeasure
		);
		DeliveryProcessorPrintDebugInfo.print();
		return printEndResult;
	}

	protected abstract DeliveryResult printEnd() throws DeliveryException;

	public DeliveryData getDeliveryData() {
		return null;
	}

	/**
	 * @return The implementation in
	 *         <code>AbstractClientDeliveryProcessorTicketPrint</code> returns
	 *         <code>null</code> as there is no wizard page needed.
	 */
	public IWizardHopPage createDeliveryWizardPage() {
		return null;
	}

	private static final String SCOPE_SCRIPT_ROOT_DRAWCOMPONENT_BY_LAYOUT = ScriptingPlugin.class.getPackage().getName() + ".ScriptRootDrawComponents";
	private static final String[] SCRIPT_ROOT_DRAWCOMPONENT_FETCH_GROUPS = new String[] {};
	private ScriptRootDrawComponent getScriptRootDrawComponent(ProductID productID) {
		long start = System.currentTimeMillis();
		ObjectID layoutID = abstractScriptDataProviderThread.getLayoutByProductID(productID);

		// Try to get the object from the cache.
		ScriptRootDrawComponent rootDrawComponent = (ScriptRootDrawComponent) Cache.sharedInstance().get(
				SCOPE_SCRIPT_ROOT_DRAWCOMPONENT_BY_LAYOUT,
				layoutID,
				SCRIPT_ROOT_DRAWCOMPONENT_FETCH_GROUPS, -1);

		// If it was not in the cache, we load it from the file.
		if (rootDrawComponent == null) {
			File ticketLayoutFile = abstractScriptDataProviderThread.getLayoutFileByProductID(productID);
			rootDrawComponent = getScriptRootDrawComponent(ticketLayoutFile);
			Cache.sharedInstance().put(
					SCOPE_SCRIPT_ROOT_DRAWCOMPONENT_BY_LAYOUT,
					layoutID,
					rootDrawComponent,
					SCRIPT_ROOT_DRAWCOMPONENT_FETCH_GROUPS, -1);
		}

		// Cloning, because data is copied into the object during print. 
		// Using the same instance would cause problems, even though the data
		// is overwritten every time and no structural changes happen.
		// This is because certain print methods (e.g. printing on DIN-A4 with a
		// TicketCarrier) collect multiple Tickets and print them at once. If
		// we didn't clone, the data of one ticket would be overwritten before
		// the printing happens and multiple tickets with the same contents would
		// occur on the paper.
		// Hence, it's essential to clone here! Marco.
//		rootDrawComponent = Util.cloneSerializable(rootDrawComponent);
		// Using cloneSerializable(...) is slightly slower and DrawComponent.clone() works fine again.
		// Besides that, cloneSerializable(...) currently causes all images to disappear (because they're marked to be transient).
		rootDrawComponent = (ScriptRootDrawComponent) rootDrawComponent.clone();

		DeliveryProcessorPrintDebugInfo.addTime(
				DeliveryProcessorPrintDebugInfo.CAT_PROCESS_DATA_PARSE_LAYOUT_FILE, System.currentTimeMillis() - start);
		return rootDrawComponent;
	}

	protected abstract ScriptRootDrawComponent getScriptRootDrawComponent(File file);

	/**
	 * This method returns the preferred count of documents that should
	 * be passed to {@link #printDocuments(List, boolean)} at once.
	 * <p>
	 * If this returns a value greater than 0 the loop fetching the document data
	 * will wait until the preferred count of documents could be fetched or no
	 * more documents are in the queue.
	 * <p>
	 * The implementation in {@link AbstractClientDeliveryProcessorPrint} returns <code>0</code>.
	 *
	 * @return The preferred count of documents that should be passed to {@link #printDocuments(List, boolean)}
	 */
	protected int getPreferredDocumentCount() {
		return 0;
	}

}
