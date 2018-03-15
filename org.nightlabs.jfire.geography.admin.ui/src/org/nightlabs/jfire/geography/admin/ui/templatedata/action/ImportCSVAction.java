package org.nightlabs.jfire.geography.admin.ui.templatedata.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class ImportCSVAction implements IWorkbenchWindowActionDelegate
{
	@Override
	public void dispose() {
		// nothing to dispose
	}

	@Override
	public void init(IWorkbenchWindow window) {
		// nothing to do
	}

	@Override
	public void run(IAction action) {
		new ImportCSVActionDelegate().run();
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
		// nothing to do
	}
}
