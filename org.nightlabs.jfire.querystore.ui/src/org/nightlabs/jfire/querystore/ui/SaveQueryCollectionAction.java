package org.nightlabs.jfire.querystore.ui;

import java.util.Collection;

import javax.jdo.FetchPlan;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.nightlabs.base.ui.action.WorkbenchPartAction;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.overview.EntryViewer;
import org.nightlabs.jfire.base.ui.overview.OverviewEntryEditor;
import org.nightlabs.jfire.base.ui.overview.search.SearchEntryViewer;
import org.nightlabs.jfire.query.store.BaseQueryStore;
import org.nightlabs.jfire.query.store.QueryStore;
import org.nightlabs.jfire.query.store.dao.QueryStoreDAO;
import org.nightlabs.jfire.querystore.ui.resource.Messages;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Simple Action for saving QueryStores that has two prerequisites:
 * <ul>
 * 	<li>The set WorkbenchPart must be an {@link OverviewEntryEditor}.</li>
 * 	<li>The {@link EntryViewer} used by the {@link OverviewEntryEditor} must be of type
 * 			{@link SearchEntryViewer}.</li>
 * </ul>
 *
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class SaveQueryCollectionAction
	extends WorkbenchPartAction
{
	/**
	 * The logger used in this class.
	 */
	private static final Logger logger = Logger.getLogger(SaveQueryCollectionAction.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateEnabled()
	 */
	@Override
	public boolean calculateEnabled()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateVisible()
	 */
	@Override
	public boolean calculateVisible()
	{
		return true;
	}

	private static final String[] FETCH_GROUPS_QUERYSTORES = new String[] {
		FetchPlan.DEFAULT, BaseQueryStore.FETCH_GROUP_OWNER
	};

	@Override
	public void run()
	{
		if (! (getActivePart() instanceof OverviewEntryEditor))
		{
			logger.warn("The load QueryCollection action is called from outside an OverviewEntryEditor. This is not intended! ActivePart=" + getActivePart().getClass().getName(), //$NON-NLS-1$
					new Exception()
					);
			return;
		}

		final OverviewEntryEditor editor = (OverviewEntryEditor) getActivePart();

		if (! (editor.getEntryViewer() instanceof SearchEntryViewer))
		{
			logger.error("This Action will only work with subclasses of SearchEntryViewer, since they know what kind of objects their existingQueries will return!", new Exception()); //$NON-NLS-1$
			return;
		}

		final SearchEntryViewer<?, ?> viewer = (SearchEntryViewer<?, ?>) editor.getEntryViewer();

		final Class<?> resultType = viewer.getTargetType();

		Job fetchStoredQueries = new Job(Messages.getString("org.nightlabs.jfire.querystore.ui.SaveQueryCollectionAction.jobTitleFetchingQueryStores")) //$NON-NLS-1$
		{
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception
			{
				final Collection<BaseQueryStore> storedQueryCollections =
					QueryStoreDAO.sharedInstance().getQueryStoresByReturnType(
						resultType, false, FETCH_GROUPS_QUERYSTORES, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);

				viewer.getComposite().getDisplay().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						chooseQueryCollection(storedQueryCollections, viewer);
					}
				});
				return Status.OK_STATUS;
			}
		};
		fetchStoredQueries.setUser(true);
		fetchStoredQueries.schedule();
	}

	protected void chooseQueryCollection(Collection<BaseQueryStore> storedQueries,
		SearchEntryViewer<?, ?> viewer)
	{
		SaveQueryStoreDialog dialog = new SaveQueryStoreDialog(viewer.getComposite().getShell(), storedQueries);

		if (dialog.open() != Window.OK)
			return;

		final QueryStore queryToSave = dialog.getSelectedQueryStore();
		queryToSave.setQueryCollection(viewer.getManagedQueries());

		Job saveQueryJob = new Job(Messages.getString("org.nightlabs.jfire.querystore.ui.SaveQueryCollectionAction.jobTitleSavingQueryStore") + queryToSave.getName().getText()) //$NON-NLS-1$
		{
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception
			{
				QueryStoreDAO.sharedInstance().storeQueryStore(
					queryToSave, FETCH_GROUPS_QUERYSTORES, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, false, monitor);

				return Status.OK_STATUS;
			}
		};
		saveQueryJob.schedule();
	}
}
