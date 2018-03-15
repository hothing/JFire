/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.validation;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.validation.ExpressionDataBlockValidator;
import org.nightlabs.jfire.prop.validation.IDataBlockValidator;
import org.nightlabs.jfire.prop.validation.ScriptDataBlockValidator;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class DataBlockValidatorTable 
extends AbstractTableComposite<IDataBlockValidator> 
{
	class ContentProvider extends ArrayContentProvider {
		// do nothing
	}

	class LabelProvider extends org.eclipse.jface.viewers.LabelProvider {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element) 
		{
			if (element instanceof IDataBlockValidator) {
				IDataBlockValidator dataBlockValidator = (IDataBlockValidator) element;
				if (dataBlockValidator instanceof ScriptDataBlockValidator) {
					return Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.DataBlockValidatorTable.scriptValidator"); //$NON-NLS-1$
				}
				else if (dataBlockValidator instanceof ExpressionDataBlockValidator) {
					return Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.DataBlockValidatorTable.expressionValidator"); //$NON-NLS-1$
				}
			}
			return super.getText(element);
		}
	}
	
	/**
	 * @param parent
	 * @param style
	 * @param initTable
	 * @param viewerStyle
	 */
	public DataBlockValidatorTable(Composite parent, int style,
			boolean initTable, int viewerStyle) {
		super(parent, style, initTable, viewerStyle);
	}

	/**
	 * @param parent
	 * @param style
	 * @param initTable
	 */
	public DataBlockValidatorTable(Composite parent, int style,
			boolean initTable) {
		super(parent, style, initTable);
	}

	/**
	 * @param parent
	 * @param style
	 */
	public DataBlockValidatorTable(Composite parent, int style) {
		super(parent, style);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.table.AbstractTableComposite#createTableColumns(org.eclipse.jface.viewers.TableViewer, org.eclipse.swt.widgets.Table)
	 */
	@Override
	protected void createTableColumns(TableViewer tableViewer, Table table) 
	{
		TableColumn tc = new TableColumn(table, SWT.NONE);
		tc.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.DataBlockValidatorTable.table.column.name")); //$NON-NLS-1$
//		TableLayout tl = new TableLayout();
//		tl.addColumnData(new ColumnWeightData(1, 300, true));
		WeightedTableLayout tl = new WeightedTableLayout(new int[] {1});
		table.setLayout(tl);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.table.AbstractTableComposite#setTableProvider(org.eclipse.jface.viewers.TableViewer)
	 */
	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setContentProvider(new ContentProvider());
		tableViewer.setLabelProvider(new LabelProvider());
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
	}
	
}
