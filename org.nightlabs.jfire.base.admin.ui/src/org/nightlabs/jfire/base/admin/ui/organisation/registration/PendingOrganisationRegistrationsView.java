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

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.login.part.LSDViewPart;
import org.nightlabs.jfire.organisation.OrganisationManagerRemote;
import org.nightlabs.jfire.organisation.RegistrationStatus;
import org.nightlabs.l10n.NumberFormatter;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class PendingOrganisationRegistrationsView
extends LSDViewPart
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(PendingOrganisationRegistrationsView.class);

	public static final String ID_VIEW = PendingOrganisationRegistrationsView.class.getName();

	private TableViewer viewer;
  private RegistrationTableContentProvider contentProvider;
  private RegistrationTableLabelProvider labelProvider;

// TODO these buttons should be view-actions!
  private Button acceptButton;
  private Button rejectButton;
  private Button cancelButton;
  private Button ackButton;
  private Button reloadButton;

  private RegistrationStatus[] selectedRegistrationStati = null;

	public PendingOrganisationRegistrationsView()
	{
	}

	protected SelectionListener acceptButton_selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent arg0)
		{
			try {
				logger.debug("accept button clicked!"); //$NON-NLS-1$

				if (selectedRegistrationStati != null) {
					for (int i = 0; i < selectedRegistrationStati.length; ++i) {
						RegistrationStatus rs = selectedRegistrationStati[i];
						if (rs.getCloseDT() == null &&
								RegistrationStatus.DIRECTION_INCOMING.equals(rs.getDirection()))
						{
							OrganisationManagerRemote organisationManager = JFireEjb3Factory.getRemoteBean(OrganisationManagerRemote.class, Login.getLogin().getInitialContextProperties());
							organisationManager.acceptRegistration(rs.getOrganisationID());
						}
					}
				}

				reload();
			} catch (Throwable x) {
				ExceptionHandlerRegistry.asyncHandleException(x);
			}
		}
	};

	protected SelectionListener cancelButton_selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent arg0)
		{
			try {
				logger.debug("cancel button clicked!"); //$NON-NLS-1$

				if (selectedRegistrationStati != null) {
					for (int i = 0; i < selectedRegistrationStati.length; ++i) {
						RegistrationStatus rs = selectedRegistrationStati[i];
						if (rs.getCloseDT() == null &&
								RegistrationStatus.DIRECTION_OUTGOING.equals(rs.getDirection()))
						{
							OrganisationManagerRemote organisationManager = JFireEjb3Factory.getRemoteBean(OrganisationManagerRemote.class, Login.getLogin().getInitialContextProperties());
							organisationManager.cancelRegistration(rs.getOrganisationID());
						}
					}
				}

				reload();
			} catch (Throwable x) {
				ExceptionHandlerRegistry.asyncHandleException(x);
			}
		}
	};

	protected SelectionListener rejectButton_selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent arg0)
		{
			try {
				logger.debug("reject button clicked!"); //$NON-NLS-1$

				if (selectedRegistrationStati != null) {
					for (int i = 0; i < selectedRegistrationStati.length; ++i) {
						RegistrationStatus rs = selectedRegistrationStati[i];
						if (rs.getCloseDT() == null &&
								RegistrationStatus.DIRECTION_INCOMING.equals(rs.getDirection()))
						{
							OrganisationManagerRemote organisationManager = JFireEjb3Factory.getRemoteBean(OrganisationManagerRemote.class, Login.getLogin().getInitialContextProperties());
							organisationManager.rejectRegistration(rs.getOrganisationID());
						}
					}
				}

				reload();
			} catch (Throwable x) {
				ExceptionHandlerRegistry.asyncHandleException(x);
			}
		}
	};

	protected SelectionListener ackButton_selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent arg0)
		{
			try {
				logger.debug("ack button clicked!"); //$NON-NLS-1$

				if (selectedRegistrationStati != null) {
					for (int i = 0; i < selectedRegistrationStati.length; ++i) {
						RegistrationStatus rs = selectedRegistrationStati[i];
						if (rs.getCloseDT() != null) {
							OrganisationManagerRemote organisationManager = JFireEjb3Factory.getRemoteBean(OrganisationManagerRemote.class, Login.getLogin().getInitialContextProperties());
							organisationManager.ackRegistration(rs.getOrganisationID());
						}
					}
				}

				reload();
			} catch (Throwable x) {
				ExceptionHandlerRegistry.asyncHandleException(x);
			}
		}
	};

	protected SelectionListener reloadButton_selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent arg0)
		{
			try {
				logger.debug("reload button clicked!"); //$NON-NLS-1$
				reload();
			} catch (Throwable x) {
				ExceptionHandlerRegistry.asyncHandleException(x);
			}
		}
	};

	protected void reload()
	{
		contentProvider.loadData();
		viewer.setInput(contentProvider);
		selectionChanged();
	}

	protected void selectionChanged()
	{
		int direction_we_count = 0;
		int direction_they_count = 0;
		int closed_count = 0;

		if (selectedRegistrationStati != null) {
			for (int i = 0; i < selectedRegistrationStati.length; ++i) {
				RegistrationStatus rs = selectedRegistrationStati[i];
				if (rs.getCloseDT() != null)
					closed_count++;
				else if (RegistrationStatus.DIRECTION_OUTGOING.equals(rs.getDirection()))
					direction_we_count++;
				else
					direction_they_count++;
			}
		}

		acceptButton.setEnabled(closed_count == 0 && direction_they_count > 0 && direction_we_count == 0);
		rejectButton.setEnabled(acceptButton.getEnabled());
		cancelButton.setEnabled(closed_count == 0 && direction_we_count > 0 && direction_they_count == 0);
		ackButton.setEnabled(closed_count != 0 && direction_we_count == 0 && direction_they_count == 0);
	}

	protected ISelectionChangedListener viewer_selectionChangedListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event)
		{
			try {
				logger.debug("selection changed: "+event.getSelection().getClass().getName()+": "+event.getSelection()); //$NON-NLS-1$ //$NON-NLS-2$
				StructuredSelection ss = (StructuredSelection) event.getSelection();
				Object[] oa = ss.toArray();
				selectedRegistrationStati = new RegistrationStatus[oa.length];
				System.arraycopy(oa, 0, selectedRegistrationStati, 0, oa.length);
				PendingOrganisationRegistrationsView.this.selectionChanged();
			} catch (Throwable x) {
				ExceptionHandlerRegistry.asyncHandleException(x);
			}
		}
	};

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
//	public void createPartControl(Composite parent)
	public void createPartContents(Composite parent)
	{
		try {
			Composite composite = parent;
	//		TabFolder composite = new TabFolder(parent, SWT.NULL);
			composite.setLayout(new GridLayout());

			Composite buttonBar = new Composite(composite, SWT.NULL);
			buttonBar.setLayout(new GridLayout(5, false));

			acceptButton = new Button(buttonBar, SWT.NULL);
			acceptButton.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.PendingOrganisationRegistrationsView.button.text.accept")); //$NON-NLS-1$
			acceptButton.addSelectionListener(acceptButton_selectionListener);

			rejectButton = new Button(buttonBar, SWT.NULL);
			rejectButton.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.PendingOrganisationRegistrationsView.button.text.reject")); //$NON-NLS-1$
			rejectButton.addSelectionListener(rejectButton_selectionListener);

			cancelButton = new Button(buttonBar, SWT.NULL);
			cancelButton.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.PendingOrganisationRegistrationsView.button.text.cancel")); //$NON-NLS-1$
			cancelButton.addSelectionListener(cancelButton_selectionListener);

			ackButton = new Button(buttonBar, SWT.NULL);
			ackButton.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.PendingOrganisationRegistrationsView.button.text.acknowledge")); //$NON-NLS-1$
			ackButton.addSelectionListener(ackButton_selectionListener);

			reloadButton = new Button(buttonBar, SWT.NULL);
			reloadButton.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.PendingOrganisationRegistrationsView.button.text.reload")); //$NON-NLS-1$
			reloadButton.addSelectionListener(reloadButton_selectionListener);

			contentProvider = new RegistrationTableContentProvider();
			labelProvider = new RegistrationTableLabelProvider();
			viewer = new TableViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
			viewer.setContentProvider(contentProvider);
			viewer.setLabelProvider(labelProvider);

			viewer.addSelectionChangedListener(viewer_selectionChangedListener);

			Table t = viewer.getTable();
			t.setHeaderVisible(true);
			t.setLinesVisible(true);

			GridData tgd = new GridData(GridData.FILL_BOTH);
			tgd.horizontalSpan = 2;
			tgd.verticalSpan = 1;

			t.setLayoutData(tgd);
			t.setLayout(new WeightedTableLayout(new int[] {1, 1, 1, 1, 1, 1, 1}));

			// Add the columns to the table
			new TableColumn(t, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.PendingOrganisationRegistrationsView.organisationIDTableColumn")); //$NON-NLS-1$
			new TableColumn(t, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.PendingOrganisationRegistrationsView.directionTableColumn")); //$NON-NLS-1$
			new TableColumn(t, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.PendingOrganisationRegistrationsView.statusTableColumn")); //$NON-NLS-1$
			new TableColumn(t, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.PendingOrganisationRegistrationsView.openDTTableColumn")); //$NON-NLS-1$
			new TableColumn(t, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.PendingOrganisationRegistrationsView.openUserTableColumn")); //$NON-NLS-1$
			new TableColumn(t, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.PendingOrganisationRegistrationsView.closeDTTableColumn")); //$NON-NLS-1$
			new TableColumn(t, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.organisation.registration.PendingOrganisationRegistrationsView.closeUserTableColumn")); //$NON-NLS-1$

			// This method MUST be called as last - otherwise not all columns are shown!
			viewer.setInput(contentProvider);
			selectionChanged();
		} catch (Throwable x) {
			ExceptionHandlerRegistry.asyncHandleException(x);
			throw new RuntimeException(x);
		}

		// DEBUG ///////////////////////////////
		if (logger.isDebugEnabled()) {
			logger.debug(NumberFormatter.formatInt(2384, 10));
			logger.debug(NumberFormatter.formatFloat(2349.95752, 3));
		}
		// end DEBUG ///////////////////////////////
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus()
	{
	}

}
