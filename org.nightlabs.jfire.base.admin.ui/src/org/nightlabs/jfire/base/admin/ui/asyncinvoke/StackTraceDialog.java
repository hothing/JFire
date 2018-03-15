package org.nightlabs.jfire.base.admin.ui.asyncinvoke;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.eclipse.ui.dialog.ResizableTrayDialog;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeProblem;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;

public class StackTraceDialog
		extends ResizableTrayDialog
{
	private AsyncInvokeProblem asyncInvokeProblem;

	public StackTraceDialog(Shell parentShell, AsyncInvokeProblem asyncInvokeProblem)
	{
		super(parentShell, Messages.RESOURCE_BUNDLE);
		this.asyncInvokeProblem = asyncInvokeProblem;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Button createButton(Composite parent, int id, String label,
			boolean defaultButton)
	{
		if (id == CANCEL)
			return null;

		return super.createButton(parent, id, label, defaultButton);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite area = (Composite) super.createDialogArea(parent);
		Text stackTrace = new Text(area, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		stackTrace.setLayoutData(new GridData(GridData.FILL_BOTH));
		if (asyncInvokeProblem.getLastError() == null)
			stackTrace.setText("*** asyncInvokeEnvelopeID=" + asyncInvokeProblem.getAsyncInvokeEnvelopeID() + " ***\n\n{Sorry! No error information available (probably not serializable)! Check the log file!}\n");
		else
			stackTrace.setText("*** asyncInvokeEnvelopeID=" + asyncInvokeProblem.getAsyncInvokeEnvelopeID() + " ***\n\n" + asyncInvokeProblem.getLastError().getErrorStackTrace());
		return area;
	}
}
