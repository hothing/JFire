package org.nightlabs.jfire.base.ui.editlock;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.nightlabs.base.ui.celleditor.ComboBoxCellEditor;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableContentProvider;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.l10n.DateFormatter;

public class ProcessEditLockTable extends AbstractTableComposite<EditLockCarrier> {
	
	private class ActionEditingSupport extends EditingSupport {
		private ComboBoxCellEditor editor;
		public ActionEditingSupport() {
			super(getTableViewer());
			String[] items = new String[ProcessLockAction.values().length];
			
			ProcessLockAction[] values = ProcessLockAction.values();
			for (int i = 0; i < values.length; i++)
				items[i] = values[i].getDescription();
			
			editor = new ComboBoxCellEditor(getTable(), items) {
				@Override
				public void activate() {
					getComboBox().addSelectionListener(new SelectionListener() {
						public void widgetSelected(SelectionEvent e) {
							fireApplyEditorValue();
						}
						public void widgetDefaultSelected(SelectionEvent e) {}
					});
				}
			};
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected Object getValue(Object element) {
			return getAction((EditLockCarrier) element).getDescription();
		}

		@Override
		protected void setValue(Object element, Object value) {
			actionMap.put((EditLockCarrier) element, ProcessLockAction.getByDescription((String) value));
			getViewer().update(element, null);
		}
	};
	
	private ActionEditingSupport actionEditingSupport;
	private Map<EditLockCarrier, ProcessLockAction> actionMap;

	public ProcessEditLockTable(Composite parent, int style) {
		super(parent, style);
		
		this.actionMap = new HashMap<EditLockCarrier, ProcessLockAction>();
	}
	
	private ProcessLockAction getAction(EditLockCarrier carrier) {
		if (!actionMap.containsKey(carrier))
			actionMap.put(carrier, ProcessLockAction.REFRESH_AND_CONTINUE);
			
		return actionMap.get(carrier);
	}

	@Override
	protected void createTableColumns(TableViewer tableViewer, Table table) {
		TableColumn tc;
		
		tc = new TableColumn(table, SWT.LEFT);
		tc.setText(Messages.getString("org.nightlabs.jfire.base.ui.editlock.ProcessEditLockTable.column.description")); //$NON-NLS-1$
		
		tc = new TableColumn(table, SWT.LEFT);
		tc.setText(Messages.getString("org.nightlabs.jfire.base.ui.editlock.ProcessEditLockTable.column.created")); //$NON-NLS-1$
		
		tc = new TableColumn(table, SWT.LEFT);
		tc.setText(Messages.getString("org.nightlabs.jfire.base.ui.editlock.ProcessEditLockTable.column.lastActivity")); //$NON-NLS-1$
		
		TableViewerColumn tvc = new TableViewerColumn(tableViewer, SWT.LEFT);
		tvc.getColumn().setText(Messages.getString("org.nightlabs.jfire.base.ui.editlock.ProcessEditLockTable.column.action")); //$NON-NLS-1$
		actionEditingSupport = new ActionEditingSupport();
		tvc.setEditingSupport(actionEditingSupport);
		
		table.setLayout(new WeightedTableLayout(new int[] {1, -1, -1, -11}, new int[]{ -1, 120, 120, 130}));
	}

	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setLabelProvider(new TableLabelProvider() {
			public String getColumnText(Object element, int columnIndex) {
				if (! (element instanceof EditLockCarrier))
					throw new IllegalArgumentException("Got table element of type " + element.getClass() + "."); //$NON-NLS-1$ //$NON-NLS-2$
				
				EditLockCarrier carrier = (EditLockCarrier) element;
				switch(columnIndex) {
				case 0: return carrier.getEditLock().getDescription();
				case 1: return DateFormatter.formatDateShortTimeHMS(carrier.getEditLock().getCreateDT(), false);
				case 2: return DateFormatter.formatDateShortTimeHMS(carrier.getEditLock().getLastAcquireDT(), false);
				case 3: return getAction(carrier).getDescription();
				default: return ""; //$NON-NLS-1$
				}
			}
		});
		tableViewer.setContentProvider(new TableContentProvider());
	}
	
	@Override
	public void refresh() {
		super.refresh();
		
		for (TableItem item : getTable().getItems()) {
			item.setBackground(getTable().getColumnCount()-1, Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		}
	}
	
	Map<EditLockCarrier, ProcessLockAction> getActionMap() {
		return actionMap;
	}
}
