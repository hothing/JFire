/**
 * 
 */
package org.nightlabs.clientui.ui.layout;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableLabelProvider;

/**
 * Table that displays a collection of {@link IGridDataEntry}s.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public class GridDataEntryTable extends AbstractTableComposite<IGridDataEntry> {

	class LabelProvider extends TableLabelProvider {
		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof IGridDataEntry) {
				return ((IGridDataEntry) element).getName().getText();
			}
			return String.valueOf(element);
		}
		
	}
	
	/**
	 * Construct a new {@link GridDataEntryTable}.
	 * @param parent The parent Composite.
	 * @param style The style to use for the Composite wrapping the table.
	 */
	public GridDataEntryTable(Composite parent, int style) {
		super(parent, style);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.table.AbstractTableComposite#createTableColumns(org.eclipse.jface.viewers.TableViewer, org.eclipse.swt.widgets.Table)
	 */
	@Override
	protected void createTableColumns(TableViewer tableViewer, Table table) {
		new TableColumn(table, SWT.LEFT);
		table.setHeaderVisible(false);
		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(1));
		table.setLayout(tableLayout);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.table.AbstractTableComposite#setTableProvider(org.eclipse.jface.viewers.TableViewer)
	 */
	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new LabelProvider());
	}
}
