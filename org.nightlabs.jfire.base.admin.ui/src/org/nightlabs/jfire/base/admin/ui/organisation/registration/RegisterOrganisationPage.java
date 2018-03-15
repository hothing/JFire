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

package org.nightlabs.jfire.base.admin.ui.organisation.registration;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.table.AbstractInvertableTableSorter;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.base.ui.table.TableSortSelectionListener;
import org.nightlabs.base.ui.wizard.DynamicPathWizardPage;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.OrganisationManagerRemote;
import org.nightlabs.jfire.organisation.dao.OrganisationDAO;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.server.Server;
import org.nightlabs.jfire.server.ServerManagerRemote;
import org.nightlabs.jfire.servermanager.config.J2eeServerTypeRegistryConfigModule;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.jfire.servermanager.config.J2eeServerTypeRegistryConfigModule.J2eeRemoteServer;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class RegisterOrganisationPage extends DynamicPathWizardPage
{
	public RegisterOrganisationPage()
	{
		super(RegisterOrganisationPage.class.getName(), Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.RegisterOrganisationPage.title"), null); //$NON-NLS-1$
	}

	private TableViewer organisationTable;
	private ArrayContentProvider organisationTableContentProvider = new ArrayContentProvider() {
		private Organisation[] organisations = null;
		private String[] messages = null;
		@Override
		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{
			organisations = null;
			messages = null;
			if (newInput instanceof String[])
				messages = (String[]) newInput;
			else {
				organisations = CollectionUtil.collection2TypedArray((Collection<Organisation>) newInput, Organisation.class);
			}
		}
		@Override
		public Object[] getElements(Object inputElement)
		{
			if (messages != null)
				return messages;

			if (organisations != null)
				return organisations;

			return new Organisation[]{};
		}
	};

	private TableLabelProvider organisationTableLabelProvider = new TableLabelProvider() {
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof String) {
				if (columnIndex == 0)
					return (String) element;

				return ""; //$NON-NLS-1$
			}

			Organisation organisation = (Organisation) element;

			switch (columnIndex) {
				case 0:
					return organisation.getPerson().getDisplayName();
				case 1:
					return organisation.getOrganisationID();
				default:
					return ""; //$NON-NLS-1$
			}
		}
	};

	private Text anonymousInitialContextFactory;
	private Text initialContextURL;
	private Text organisationID;

	private Organisation myOrganisation;
	private Server myServer;

	protected static class OrganisationViewerSorter_PersonName
	extends AbstractInvertableTableSorter<Organisation>
	{
		@Override
		protected int _compare(Viewer viewer, Organisation orga1, Organisation orga2)
		{
			if (orga1 == UNKNOWN_ORGANISATION)
				return 1;

			if (orga2 == UNKNOWN_ORGANISATION)
				return -1;

			return getComparator().compare(
					orga1.getPerson().getDisplayName(),
					orga2.getPerson().getDisplayName());

//			return getCollator().compare(
//					orga1.getPerson().getDisplayName(),
//					orga2.getPerson().getDisplayName());
		}
	}

	protected static class OrganisationViewerSorter_OrganisationID
	extends AbstractInvertableTableSorter<Organisation>
	{
		@Override
		protected int _compare(Viewer viewer, Organisation orga1, Organisation orga2)
		{
			if (orga1 == UNKNOWN_ORGANISATION)
				return 1;

			if (orga2 == UNKNOWN_ORGANISATION)
				return -1;

			return getComparator().compare(
					orga1.getOrganisationID(),
					orga2.getOrganisationID());

//			return getCollator().compare(
//					orga1.getOrganisationID(),
//					orga2.getOrganisationID());
		}
	}

	private void createOrganisationTable(Composite parent)
	{
		organisationTable = new TableViewer(parent, SWT.SINGLE);
		organisationTable.setContentProvider(organisationTableContentProvider);
		organisationTable.setLabelProvider(organisationTableLabelProvider);
		organisationTable.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		organisationTable.getTable().setHeaderVisible(true);

		TableColumn column = new TableColumn(organisationTable.getTable(), SWT.NULL);
		column.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.RegisterOrganisationPage.organisationNameTableColumn.text")); //$NON-NLS-1$
		TableSortSelectionListener tsslPersonName = new TableSortSelectionListener(organisationTable, column, new OrganisationViewerSorter_PersonName(), SWT.UP);

		column = new TableColumn(organisationTable.getTable(), SWT.NULL);
		column.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.RegisterOrganisationPage.organisationIDTableColumn.text")); //$NON-NLS-1$
		new TableSortSelectionListener(organisationTable, column, new OrganisationViewerSorter_OrganisationID(), SWT.UP);

		organisationTable.getTable().setLayout(new WeightedTableLayout(new int[] { 50, 50 }));
		tsslPersonName.chooseColumnForSorting();

		organisationTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event)
			{
				organisationSelected();
			}
		});

		organisationTable.setInput(new String[] { Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.RegisterOrganisationPage.organisationTable.input_loading") }); //$NON-NLS-1$
		organisationTable.setSelection(new StructuredSelection(new Organisation[0]));

		new Job(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.RegisterOrganisationPage.loadOrganisationsJob.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				try {
					ServerManagerRemote serverManager = JFireEjb3Factory.getRemoteBean(ServerManagerRemote.class, Login.getLogin().getInitialContextProperties());
					j2eeRemoteServers = serverManager.getJ2eeRemoteServers();
					serverType2j2eeRemoteServerMap = new HashMap<String, J2eeRemoteServer>(j2eeRemoteServers.size());
					for (J2eeServerTypeRegistryConfigModule.J2eeRemoteServer remoteServer : j2eeRemoteServers)
						serverType2j2eeRemoteServerMap.put(remoteServer.getJ2eeServerType(), remoteServer);

					OrganisationManagerRemote organisationManager = JFireEjb3Factory.getRemoteBean(OrganisationManagerRemote.class, Login.getLogin().getInitialContextProperties());
					final Collection<Organisation> orgs = organisationManager.getOrganisationsFromRootOrganisation(
							true, FETCH_GROUPS_ORGANISATION, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
					orgs.add(UNKNOWN_ORGANISATION);

					final Organisation _myOrganisation = OrganisationDAO.sharedInstance().getOrganisation(
							OrganisationID.create(Login.getLogin().getOrganisationID()),
							FETCH_GROUPS_ORGANISATION,
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
							new NullProgressMonitor() // TODO proper handling of monitor
					);

					Display.getDefault().asyncExec(new Runnable() {
						public void run()
						{
							myOrganisation = _myOrganisation;
							myServer = myOrganisation.getServer();
							organisationTable.setInput(orgs);
						}
					});
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				return Status.OK_STATUS;
			}
		}.schedule();
	}

	private List<J2eeServerTypeRegistryConfigModule.J2eeRemoteServer> j2eeRemoteServers;
	private Map<String, J2eeServerTypeRegistryConfigModule.J2eeRemoteServer> serverType2j2eeRemoteServerMap;

	private static final Organisation UNKNOWN_ORGANISATION = new Organisation("jfire.other.organisation.com"); //$NON-NLS-1$
	static {
		ServerCf serverCf = new ServerCf("jfire.company.com"); //$NON-NLS-1$
		serverCf.setServerName("JFire Server"); //$NON-NLS-1$
		serverCf.init();

		Server unknownServer = new Server(serverCf.getServerID());
		serverCf.copyTo(unknownServer);
		UNKNOWN_ORGANISATION.setServer(unknownServer);

		Person person = new Person(UNKNOWN_ORGANISATION.getOrganisationID(), 0);
		person.setDisplayName(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.RegisterOrganisationPage.manualRegistration")); //$NON-NLS-1$
		UNKNOWN_ORGANISATION.setPerson(person);
	}

	private static final String[] FETCH_GROUPS_ORGANISATION = new String[]{ FetchPlan.ALL }; // TODO fix this!

	@Override
	public Control createPageContents(Composite parent)
	{
		XComposite page = new XComposite(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER);
		createOrganisationTable(page);

		XComposite compTexts = new XComposite(page, SWT.NONE, LayoutMode.ORDINARY_WRAPPER);
		compTexts.getGridLayout().numColumns = 2;
//		page.getGridLayout().verticalSpacing = 9;

		Label label = new Label(compTexts, SWT.NONE);
		label.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.RegisterOrganisationPage.anonymousInitialContextFactoryLabel.text")); //$NON-NLS-1$

		anonymousInitialContextFactory = new Text(compTexts, SWT.BORDER | SWT.SINGLE);
		anonymousInitialContextFactory.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(compTexts, SWT.NONE);
		label.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.RegisterOrganisationPage.initialContextURL.text")); //$NON-NLS-1$

		initialContextURL = new Text(compTexts, SWT.BORDER | SWT.SINGLE);
		initialContextURL.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));


		label = new Label(compTexts, SWT.NONE);
		label.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.RegisterOrganisationPage.organisationIDLabel.text")); //$NON-NLS-1$

		organisationID = new Text(compTexts, SWT.BORDER | SWT.SINGLE);
		organisationID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		organisationSelected();
		return page;
	}

	private Organisation selectedOrganisation;

	private void organisationSelected()
	{
		selectedOrganisation = UNKNOWN_ORGANISATION;

		IStructuredSelection sel = (IStructuredSelection) organisationTable.getSelection();
		if (!sel.isEmpty()) {
			Object object = sel.getFirstElement();
			if (object instanceof Organisation)
				selectedOrganisation = (Organisation) object;
		}

		if (organisationID == null)
			return;

		String protocol;
		if (selectedOrganisation == UNKNOWN_ORGANISATION)
			protocol = Server.PROTOCOL_HTTPS;
		else {
			if (myServer.getDistinctiveDataCentreID().equals(selectedOrganisation.getServer().getDistinctiveDataCentreID()))
				protocol = Server.PROTOCOL_JNP;
			else
				protocol = Server.PROTOCOL_HTTPS;
		}

		J2eeServerTypeRegistryConfigModule.J2eeRemoteServer remoteServer = null;
		if (serverType2j2eeRemoteServerMap != null)
			 remoteServer = serverType2j2eeRemoteServerMap.get(selectedOrganisation.getServer().getJ2eeServerType());

		if (remoteServer == null)
			anonymousInitialContextFactory.setText("your.anonymous.jndi.NamingContextFactory"); // "org.jnp.interfaces.NamingContextFactory"; //$NON-NLS-1$
		else
			anonymousInitialContextFactory.setText(remoteServer.getAnonymousInitialContextFactory(protocol, true));

		anonymousInitialContextFactory.setEditable(selectedOrganisation == UNKNOWN_ORGANISATION);
		String initialContextURLString = selectedOrganisation.getServer().getInitialContextURL(protocol, false);
		if (initialContextURLString == null || initialContextURLString.isEmpty()) {
			initialContextURLString = "";
			MessageDialog.openError(
					getShell(),
					"No initial-context-URL!",
					String.format(
							"The server \"%3$s\" (on which the selected organisation \"%2$s\" is hosted) doesn't have an initial-context-URL configured for the protocol \"%1$s\"! Tell the server administrator to configure his server correctly and to propagate this information to the root organisation!",
							protocol,
							selectedOrganisation.getOrganisationID(),
							selectedOrganisation.getServer().getServerID())
			);
		}
		initialContextURL.setText(initialContextURLString);
		initialContextURL.setEditable(selectedOrganisation == UNKNOWN_ORGANISATION);
		organisationID.setText(selectedOrganisation.getOrganisationID());
		organisationID.setEditable(selectedOrganisation == UNKNOWN_ORGANISATION);
	}

	public String getAnonymousInitialContextFactory()
	{
		return anonymousInitialContextFactory.getText();
	}
	public String getInitialContextURL()
	{
		return initialContextURL.getText();
	}
	public String getOrganisationID()
	{
		return organisationID.getText();
	}

}
