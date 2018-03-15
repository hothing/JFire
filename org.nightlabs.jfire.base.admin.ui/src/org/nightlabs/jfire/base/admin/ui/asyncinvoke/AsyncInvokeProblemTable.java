package org.nightlabs.jfire.base.admin.ui.asyncinvoke;

import java.io.Serializable;
import java.util.Collection;

import javax.naming.NamingException;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeProblem;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.asyncinvoke.id.AsyncInvokeProblemID;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.jdo.ActiveJDOObjectController;
import org.nightlabs.jfire.base.ui.jdo.ActiveJDOObjectTableComposite;
import org.nightlabs.l10n.DateFormatter;

public class AsyncInvokeProblemTable
		extends ActiveJDOObjectTableComposite<AsyncInvokeProblemID, AsyncInvokeProblem>
{
	private static AsyncInvokeProblem dummyLoadingDataAsyncInvokeEnvelope;

	static {
		try {
			dummyLoadingDataAsyncInvokeEnvelope = new AsyncInvokeProblem(new AsyncInvokeEnvelope(
					new Invocation() {
						private static final long serialVersionUID = 1L;

						@Override
						public Serializable invoke()
						throws Exception
						{
							return null;
						}
					},
					null,
					null,
					null
			)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public String getAsyncInvokeEnvelopeID()
				{
					return Messages.getString("org.nightlabs.jfire.base.admin.ui.asyncinvoke.AsyncInvokeProblemTable.0"); //$NON-NLS-1$
				}
			});
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	public AsyncInvokeProblemTable(Composite parent, int style)
	{
		super(parent, style);
		load();
	}

	public static boolean isLoadingData(Object element)
	{
		return dummyLoadingDataAsyncInvokeEnvelope == element;
	}

	protected static AsyncInvokeEnvelope getAsyncInvokeEnvelope(Object element)
	{
		AsyncInvokeProblem asyncInvokeProblem = (AsyncInvokeProblem) element;
		return (AsyncInvokeEnvelope) asyncInvokeProblem.getAsyncInvokeEnvelope();
	}

	@Override
	protected void createTableColumns(TableViewer tableViewer, Table table)
	{
		TableLayout tableLayout = new TableLayout();
		TableViewerColumn c;


		c = new TableViewerColumn(tableViewer, SWT.RIGHT);
		c.getColumn().setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.asyncinvoke.AsyncInvokeProblemTable.column.id")); //$NON-NLS-1$
		c.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element)
			{
				if (isLoadingData(element))
					return Messages.getString("org.nightlabs.jfire.base.admin.ui.asyncinvoke.AsyncInvokeProblemTable.loadingData"); //$NON-NLS-1$

				return getAsyncInvokeEnvelope(element).getAsyncInvokeEnvelopeID();
			}
		});
		tableLayout.addColumnData(new ColumnPixelData(100));


		c = new TableViewerColumn(tableViewer, SWT.LEFT);
		c.getColumn().setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.asyncinvoke.AsyncInvokeProblemTable.column.created")); //$NON-NLS-1$
		c.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element)
			{
				if (isLoadingData(element))
					return ""; //$NON-NLS-1$

				return DateFormatter.formatDateShortTimeHMS(getAsyncInvokeEnvelope(element).getCreateDT(), false);
			}
		});
		tableLayout.addColumnData(new ColumnPixelData(140));


		c = new TableViewerColumn(tableViewer, SWT.LEFT);
		c.getColumn().setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.asyncinvoke.AsyncInvokeProblemTable.column.invocationType")); //$NON-NLS-1$
		c.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element)
			{
				if (isLoadingData(element))
					return ""; //$NON-NLS-1$

				return getAsyncInvokeEnvelope(element).getInvocation().getClass().getName();
			}
		});
		tableLayout.addColumnData(new ColumnWeightData(30));


		c = new TableViewerColumn(tableViewer, SWT.RIGHT);
		c.getColumn().setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.asyncinvoke.AsyncInvokeProblemTable.column.errors")); //$NON-NLS-1$
		c.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element)
			{
				if (isLoadingData(element))
					return ""; //$NON-NLS-1$

				AsyncInvokeProblem asyncInvokeProblem = (AsyncInvokeProblem) element;
				return String.valueOf(asyncInvokeProblem.getErrorCount());
			}
		});
		tableLayout.addColumnData(new ColumnPixelData(40));


		c = new TableViewerColumn(tableViewer, SWT.LEFT);
		c.getColumn().setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.asyncinvoke.AsyncInvokeProblemTable.column.errortype")); //$NON-NLS-1$
		c.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element)
			{
				if (isLoadingData(element))
					return ""; //$NON-NLS-1$

				AsyncInvokeProblem asyncInvokeProblem = (AsyncInvokeProblem) element;
				if (asyncInvokeProblem.getLastError() == null)
					return ""; //$NON-NLS-1$
				return asyncInvokeProblem.getLastError().getErrorClassName();
			}
			@Override
			public String getToolTipText(Object element)
			{
				if (isLoadingData(element))
					return ""; //$NON-NLS-1$

				AsyncInvokeProblem asyncInvokeProblem = (AsyncInvokeProblem) element;
				if (asyncInvokeProblem.getLastError() == null)
					return ""; //$NON-NLS-1$
				return asyncInvokeProblem.getLastError().getErrorStackTrace();
			}
		});
		tableLayout.addColumnData(new ColumnWeightData(30));


		c = new TableViewerColumn(tableViewer, SWT.LEFT);
		c.getColumn().setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.asyncinvoke.AsyncInvokeProblemTable.column.ErrorMessage")); //$NON-NLS-1$
		c.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element)
			{
				if (isLoadingData(element))
					return ""; //$NON-NLS-1$

				AsyncInvokeProblem asyncInvokeProblem = (AsyncInvokeProblem) element;
				if (asyncInvokeProblem.getLastError() == null)
					return ""; //$NON-NLS-1$
				return asyncInvokeProblem.getLastError().getErrorMessage();
			}
			@Override
			public String getToolTipText(Object element)
			{
				if (isLoadingData(element))
					return ""; //$NON-NLS-1$

				AsyncInvokeProblem asyncInvokeProblem = (AsyncInvokeProblem) element;
				if (asyncInvokeProblem.getLastError() == null)
					return ""; //$NON-NLS-1$
				return asyncInvokeProblem.getLastError().getErrorStackTrace();
			}
		});
		tableLayout.addColumnData(new ColumnWeightData(30));


		c = new TableViewerColumn(tableViewer, SWT.LEFT);
		c.getColumn().setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.asyncinvoke.AsyncInvokeProblemTable.column.errorRootCause")); //$NON-NLS-1$
		c.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element)
			{
				if (isLoadingData(element))
					return ""; //$NON-NLS-1$

				AsyncInvokeProblem asyncInvokeProblem = (AsyncInvokeProblem) element;
				if (asyncInvokeProblem.getLastError() == null)
					return ""; //$NON-NLS-1$
				return asyncInvokeProblem.getLastError().getErrorRootCauseClassName();
			}
			@Override
			public String getToolTipText(Object element)
			{
				if (isLoadingData(element))
					return ""; //$NON-NLS-1$

				AsyncInvokeProblem asyncInvokeProblem = (AsyncInvokeProblem) element;
				if (asyncInvokeProblem.getLastError() == null)
					return ""; //$NON-NLS-1$
				return asyncInvokeProblem.getLastError().getErrorStackTrace();
			}
		});
		tableLayout.addColumnData(new ColumnWeightData(30));


		c = new TableViewerColumn(tableViewer, SWT.LEFT);
		c.getColumn().setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.asyncinvoke.AsyncInvokeProblemTable.column.status")); //$NON-NLS-1$
		c.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element)
			{
				if (isLoadingData(element))
					return ""; //$NON-NLS-1$

				AsyncInvokeProblem asyncInvokeProblem = (AsyncInvokeProblem) element;
				if (asyncInvokeProblem.isUndeliverable())
					return Messages.getString("org.nightlabs.jfire.base.admin.ui.asyncinvoke.AsyncInvokeProblemTable.undeliverable"); //$NON-NLS-1$
				else
					return Messages.getString("org.nightlabs.jfire.base.admin.ui.asyncinvoke.AsyncInvokeProblemTable.stillTrying"); //$NON-NLS-1$
			}
		});
		tableLayout.addColumnData(new ColumnWeightData(30));

		table.setLayout(tableLayout);

		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent arg0)
			{
				Collection<AsyncInvokeProblem> selectedElements = getSelectedElements();
				if (selectedElements.isEmpty())
					return;

				AsyncInvokeProblem asyncInvokeProblem = selectedElements.iterator().next();
//				MessageDialog.openInformation(RCPUtil.getActiveShell(), "Stack trace", asyncInvokeProblem.getLastError().getErrorStackTrace());
				new StackTraceDialog(RCPUtil.getActiveShell(), asyncInvokeProblem).open();
			}
		});
	}

	@Override
	protected ActiveJDOObjectController<AsyncInvokeProblemID, AsyncInvokeProblem> createActiveJDOObjectController()
	{
		return new ActiveAsyncInvokeProblemController();
	}

	@Override
	protected ITableLabelProvider createLabelProvider()
	{
		return null; // we use the new column-based API and return null here.
	}

}
