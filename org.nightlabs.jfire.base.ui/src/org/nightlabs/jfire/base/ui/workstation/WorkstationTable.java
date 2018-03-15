/**
 * 
 */
package org.nightlabs.jfire.base.ui.workstation;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableContentProvider;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.workstation.Workstation;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class WorkstationTable extends AbstractTableComposite<Workstation> {

	private class LabelProvider extends TableLabelProvider {
		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof Workstation) {
				Workstation workstation = (Workstation) element;
				if (columnIndex == 0)
					return workstation.getWorkstationID();
				else if (columnIndex == 1)
					return workstation.getDescription();
			}
			return String.valueOf(element);
		}
	}
	
	/**
	 * @param parent
	 * @param style
	 */
	public WorkstationTable(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * @param parent
	 * @param style
	 * @param initTable
	 */
	public WorkstationTable(Composite parent, int style, boolean initTable) {
		super(parent, style, initTable);
	}

	/**
	 * @param parent
	 * @param style
	 * @param initTable
	 * @param viewerStyle
	 */
	public WorkstationTable(Composite parent, int style, boolean initTable,
			int viewerStyle) {
		super(parent, style, initTable, viewerStyle);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.table.AbstractTableComposite#createTableColumns(org.eclipse.jface.viewers.TableViewer, org.eclipse.swt.widgets.Table)
	 */
	@Override
	protected void createTableColumns(TableViewer tableViewer, Table table) {
		new TableColumn(table, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.base.ui.workstation.WorkstationTable.column.workstationID")); //$NON-NLS-1$
		new TableColumn(table, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.base.ui.workstation.WorkstationTable.column.description")); //$NON-NLS-1$
		TableLayout l = new TableLayout();
		l.addColumnData(new ColumnWeightData(2));
		l.addColumnData(new ColumnWeightData(3));
		table.setLayout(l);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.table.AbstractTableComposite#setTableProvider(org.eclipse.jface.viewers.TableViewer)
	 */
	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setLabelProvider(new LabelProvider());
		tableViewer.setContentProvider(new TableContentProvider());
	}

}
