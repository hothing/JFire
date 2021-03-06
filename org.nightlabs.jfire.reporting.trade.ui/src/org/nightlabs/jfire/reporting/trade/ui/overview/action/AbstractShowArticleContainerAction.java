package org.nightlabs.jfire.reporting.trade.ui.overview.action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.trade.ui.JFireReportingTradePlugin;
import org.nightlabs.jfire.reporting.trade.ui.articlecontainer.detail.action.print.ArticleContainerReportActionHelper;
import org.nightlabs.jfire.reporting.trade.ui.resource.Messages;
import org.nightlabs.jfire.reporting.ui.config.ReportConfigUtil;
import org.nightlabs.jfire.reporting.ui.layout.action.view.AbstractViewReportLayoutAction;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.ui.overview.action.AbstractArticleContainerAction;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class AbstractShowArticleContainerAction
extends AbstractArticleContainerAction
{
	public static final String ID = AbstractShowArticleContainerAction.class.getName();

	public AbstractShowArticleContainerAction()
	{
		super();
		init();
	}

	protected void init() {
		setId(ID);
		setText(Messages.getString("org.nightlabs.jfire.reporting.trade.ui.overview.action.AbstractShowArticleContainerAction.text")); //$NON-NLS-1$
		setToolTipText(Messages.getString("org.nightlabs.jfire.reporting.trade.ui.overview.action.AbstractShowArticleContainerAction.tooltipText")); //$NON-NLS-1$
		setImageDescriptor(SharedImages.getSharedImageDescriptor(JFireReportingTradePlugin.getDefault(),
				AbstractShowArticleContainerAction.class));
	}

	protected AbstractViewReportLayoutAction showReportAction = new AbstractViewReportLayoutAction() {
		@Override
		protected Locale getRenderRequestLocale(ReportRegistryItemID reportID, Map<String, Object> params, ProgressMonitor monitor) {
			return ArticleContainerReportActionHelper.getArticleContainerReportLocale(getArticleContainerID(), reportID, params, monitor);
		}
	};

	@Override
	public void run()
	{
		Map <String, Object> params = new HashMap<String,Object>();
		prepareParams(params);
		ReportRegistryItemID selectedLayoutID = ReportConfigUtil.getReportLayoutID(getReportRegistryItemType());
		if (selectedLayoutID == null) {
			// the user canceled, abort
			return;
		}
		Set<ReportRegistryItemID> itemIDs = new HashSet<ReportRegistryItemID>();
		itemIDs.add(selectedLayoutID);
		showReportAction.setNextRunParams(params);
		showReportAction.runWithRegistryItemIDs(itemIDs);
	}

	/**
	 * Prepare the parameter for the ReportLayout in order to view
	 * the selected {@link ArticleContainer}.
	 * The default implementation puts {@link #getArticleContainerID()}
	 * with the key "articleContainerID" into the map.
	 * Override to customize this behaviour;
	 *
	 * @param params The params that will be passed to the {@link AbstractViewReportLayoutAction}
	 */
	protected void prepareParams(Map<String, Object> params) {
		params.put("articleContainerID", getArticleContainerID()); //$NON-NLS-1$
	}

	/**
	 * Returns the report registry type that should be used to
	 * show the selected {@link ArticleContainer}.
	 *
	 * @return The report registry type that should be used to
	 * show the selected {@link ArticleContainer}.
	 */
	protected abstract String getReportRegistryItemType();
}
