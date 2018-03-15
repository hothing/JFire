package org.nightlabs.jfire.base.admin.ui.asyncinvoke;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.selection.SelectionProviderProxy;
import org.nightlabs.jfire.base.ui.login.part.LSDViewPart;

public class AsyncInvokeProblemView
		extends LSDViewPart
{
	public static final String ID_VIEW = AsyncInvokeProblemView.class.getName();
	
	private SelectionProviderProxy selectionProviderProxy = new SelectionProviderProxy();
	private AsyncInvokeProblemTable asyncInvokeProblemTable;

	@Override
	public void createPartControl(Composite parent)
	{
		getSite().setSelectionProvider(selectionProviderProxy);
		super.createPartControl(parent);
	}

	@Override
	public void createPartContents(Composite parent)
	{
		asyncInvokeProblemTable = new AsyncInvokeProblemTable(parent, SWT.NONE);
		selectionProviderProxy.addRealSelectionProvider(asyncInvokeProblemTable);
	}

	public AsyncInvokeProblemTable getAsyncInvokeProblemTable()
	{
		return asyncInvokeProblemTable;
	}
}
