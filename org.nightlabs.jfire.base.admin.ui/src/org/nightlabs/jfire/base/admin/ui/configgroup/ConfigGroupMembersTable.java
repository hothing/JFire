package org.nightlabs.jfire.base.admin.ui.configgroup;

import java.util.Collection;

import javax.jdo.JDOHelper;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.notification.IDirtyStateManager;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jfire.base.ui.config.ConfigSetupRegistry;
import org.nightlabs.jfire.base.ui.config.ConfigSetupVisualiser;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.dao.ConfigSetupDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.progress.NullProgressMonitor;


public class ConfigGroupMembersTable extends AbstractTableComposite<Config> {

	private IDirtyStateManager dirtyStateManager;
	private ConfigGroupModel model = null;
	private String columnLabel = null;

	public ConfigGroupMembersTable(Composite parent, int style, IDirtyStateManager dirtyStateManager, String columnLabel) {
		super(parent, SWT.NONE, false, SWT.CHECK);
		this.dirtyStateManager = dirtyStateManager;
		this.columnLabel = columnLabel;
		initTable();
	}

	@Override
	protected void createTableColumns(TableViewer tableViewer, Table table) {
		new TableColumn(table, SWT.LEFT).setText(columnLabel);
		table.setLayout(new WeightedTableLayout(new int[] { 1 }));
		table.setHeaderVisible(true);
	}

	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setLabelProvider(new TableLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				ConfigSetup configSetup = ConfigSetupDAO.sharedInstance().getConfigSetupForGroup(model.configGroupID, new NullProgressMonitor());
				ConfigSetupVisualiser visualiser = ConfigSetupRegistry.sharedInstance().getVisualiser(configSetup.getConfigSetupType());
				Config config = (Config) element;
				return visualiser.getKeyObjectName((ConfigID) JDOHelper.getObjectId(config));
			}
		});

		tableViewer.setContentProvider(new ArrayContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				ConfigID configGroupID = (ConfigID) inputElement;
				ConfigSetup configSetup = ConfigSetupDAO.sharedInstance().getConfigSetupForGroup(configGroupID, new NullProgressMonitor());
				Collection<Config> configs = configSetup.getConfigs();
				return configs.toArray(new Object[configs.size()]);
			}
		});

		addCheckStateChangedListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem tableItem = ((TableItem) e.item);
				boolean checked = tableItem.getChecked();
				Config config = (Config) tableItem.getData();
				if (checked) {
					if (model.notAssignedConfigs.contains(config))
						model.addedConfigs.add(config);
					else
						model.removedConfigs.remove(config);

				} else {
					if (model.assignedConfigs.contains(config))
						model.removedConfigs.add(config);
					else
						model.addedConfigs.remove(config);
				}

				if (! (model.addedConfigs.isEmpty() && model.removedConfigs.isEmpty()) )
					dirtyStateManager.markDirty();
				else
					dirtyStateManager.markUndirty();
			}
		});
	}

	public void setModel(ConfigGroupModel model) {
		this.model = model;
		super.setInput(model.configGroupID);
		setCheckedElements(model.assignedConfigs);
	}
}
