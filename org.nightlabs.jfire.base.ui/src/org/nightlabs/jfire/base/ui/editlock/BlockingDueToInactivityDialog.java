package org.nightlabs.jfire.base.ui.editlock;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.eclipse.ui.dialog.ResizableTrayDialog;
import org.nightlabs.jfire.base.ui.resource.Messages;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class BlockingDueToInactivityDialog extends ResizableTrayDialog {
	private ProcessEditLockTable editLockTable;
	private EditLockMan editLockMan;
	private Set<EditLockCarrier> editLockCarriers = new HashSet<EditLockCarrier>();
	
	public BlockingDueToInactivityDialog(EditLockMan editLockMan, Shell parentShell) {
		super(parentShell, null);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.editLockMan = editLockMan;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		XComposite comp = new XComposite(parent, SWT.NONE);
		Label label = new Label(comp, SWT.WRAP);
		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, label);
		label.setText(Messages.getString("org.nightlabs.jfire.base.ui.editlock.BlockingDueToInactivityDialog.label.editLockNotUsed")); //$NON-NLS-1$
		
		label = new Label(comp, SWT.WRAP);
		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA_HORIZONTAL, label);
		label.setText(Messages.getString("org.nightlabs.jfire.base.ui.editlock.BlockingDueToInactivityDialog.label.choose")); //$NON-NLS-1$
		
		editLockTable = new ProcessEditLockTable(comp, SWT.NONE);
		
		editLockTable.setInput(editLockCarriers);
		return comp;
	}
	
	public void addEditLockCarrier(EditLockCarrier editLockCarrier) {
		if (editLockCarriers.add(editLockCarrier)) {
			editLockTable.refresh();
		}
	}
	
	@Override
	public boolean close() {
		if (super.close()) {
			editLockMan.onCloseBlockingDueToInactivityDialog();
			return true;
		}
		return false;
	}
	
	@Override
	protected void okPressed() {
		super.okPressed();
		
		Map<EditLockCarrier, ProcessLockAction> actionMap = editLockTable.getActionMap();
		
		for (Map.Entry<EditLockCarrier, ProcessLockAction> entry : actionMap.entrySet()) {
			EditLockCarrier carrier = entry.getKey();
			ProcessLockAction action = entry.getValue();
			editLockMan.processEditLockAction(carrier, action);
		}
	}
	
	@Override
	protected void cancelPressed() {
		super.cancelPressed();
		
		for (EditLockCarrier carrier : editLockCarriers) {
			editLockMan.processEditLockAction(carrier, ProcessLockAction.REFRESH_AND_CONTINUE);
		}
	}
}
