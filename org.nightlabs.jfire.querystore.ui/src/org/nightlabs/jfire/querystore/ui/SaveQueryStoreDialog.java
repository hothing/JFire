package org.nightlabs.jfire.querystore.ui;

import java.util.Collection;

import javax.jdo.FetchPlan;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.query.store.BaseQueryStore;
import org.nightlabs.jfire.query.store.QueryStore;
import org.nightlabs.jfire.querystore.ui.resource.Messages;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.util.NLLocale;

/**
 * FIXME: It should not be possible to press OK after this dialog is created!
 *
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class SaveQueryStoreDialog extends TitleAreaDialog
{
	private QueryStore selectedQueryConfiguration;
	private BaseQueryStoreInActiveTableComposite storeTable;
	private Collection<BaseQueryStore> existingQueries;
	private boolean errorMsgSet = false;

	private static final int CREATE_NEW_QUERY = IDialogConstants.CLIENT_ID + 1;

	public SaveQueryStoreDialog(Shell parentShell, Collection<BaseQueryStore> existingQueries)
	{
		super(parentShell);
		assert existingQueries != null;
		this.existingQueries = existingQueries;
	}

	@Override
	protected int getShellStyle()
	{
		return super.getShellStyle() | SWT.RESIZE;
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText(Messages.getString("org.nightlabs.jfire.querystore.ui.SaveQueryStoreDialog.ShellTitle")); //$NON-NLS-1$
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		XComposite wrapper = new XComposite(parent, SWT.NONE);
		storeTable = new BaseQueryStoreInActiveTableComposite(wrapper, SWT.NONE, true,
			AbstractTableComposite.DEFAULT_STYLE_SINGLE_BORDER);
		storeTable.addDoubleClickListener(new IDoubleClickListener()
		{
			@Override
			public void doubleClick(DoubleClickEvent event)
			{
				okPressed();
			}

		});
		storeTable.addSelectionChangedListener(new ISelectionChangedListener()
		{
			private QueryStore currentlySelectedElement;

			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				final QueryStore newSelection = storeTable.getFirstSelectedElement();

				// set okButton status
				okButton.setEnabled(newSelection != null);

				// if we clicked on the already selected element
				if (currentlySelectedElement != null && currentlySelectedElement == newSelection)
				{
					return;
				}

				currentlySelectedElement = storeTable.getFirstSelectedElement();

				if (errorMsgSet)
				{
					setErrorMessage(null);
				}
			}
		});
		storeTable.setInput(existingQueries);

		setTitle(Messages.getString("org.nightlabs.jfire.querystore.ui.SaveQueryStoreDialog.title")); //$NON-NLS-1$
		setMessage(Messages.getString("org.nightlabs.jfire.querystore.ui.SaveQueryStoreDialog.description")); //$NON-NLS-1$
		return wrapper;
	}

	private Button okButton;

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, CREATE_NEW_QUERY, Messages.getString("org.nightlabs.jfire.querystore.ui.SaveQueryStoreDialog.createNewQuery"), false); //$NON-NLS-1$
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,	false);
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
	}

	@Override
	protected void buttonPressed(int buttonId)
	{
		super.buttonPressed(buttonId);
		if (CREATE_NEW_QUERY == buttonId)
		{
			QueryStore createdStore = createNewQueryStore();
			if (createdStore == null)
				return;

			okPressed();
		}
	}

	private QueryStore createNewQueryStore()
	{
		final User owner = UserDAO.sharedInstance().getUser(
			SecurityReflector.getUserDescriptor().getUserObjectID(),
			new String[] { FetchPlan.DEFAULT, User.FETCH_GROUP_NAME },
			NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor()
			);

		final BaseQueryStore queryStore = new BaseQueryStore(owner,
			IDGenerator.nextID(BaseQueryStore.class), null);

		queryStore.getName().setText(NLLocale.getDefault().getLanguage(), Messages.getString("org.nightlabs.jfire.querystore.ui.SaveQueryStoreDialog.standardNewQueryName")); //$NON-NLS-1$

		existingQueries.add(queryStore);
		storeTable.setInput(existingQueries);
		storeTable.setSelection(new StructuredSelection(queryStore));
		return queryStore;
	}

	private boolean checkForRights(QueryStore selectedStore)
	{
		if (! selectedStore.getOwnerID().equals(SecurityReflector.getUserDescriptor().getUserObjectID()) )
		{
			errorMsgSet = true;
			setErrorMessage(Messages.getString("org.nightlabs.jfire.querystore.ui.SaveQueryStoreDialog.cannotChangeNotOwnerError")); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	private boolean changeName(QueryStore selectedStore)
	{
		final QueryStoreEditDialog inputDialog = new QueryStoreEditDialog(
			getShell(), selectedStore);

		if (inputDialog.open() != Window.OK)
			return false;

		selectedStore.setPubliclyAvailable(inputDialog.isPubliclyAvailable());
		return true;
	}

	@Override
	protected void okPressed()
	{
		selectedQueryConfiguration = storeTable.getFirstSelectedElement();
		if (! checkForRights(selectedQueryConfiguration))
			return;

		if (! changeName(selectedQueryConfiguration))
			return;

		super.okPressed();
	}

	public QueryStore getSelectedQueryStore()
	{
		return selectedQueryConfiguration;
	}
}