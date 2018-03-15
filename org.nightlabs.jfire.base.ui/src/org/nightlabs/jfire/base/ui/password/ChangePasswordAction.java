package org.nightlabs.jfire.base.ui.password;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.eclipse.ui.dialog.ChangePasswordDialog;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.login.action.LSDWorkbenchWindowActionDelegate;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.UserLocal;

public class ChangePasswordAction extends LSDWorkbenchWindowActionDelegate {

	@Override
	public void run(IAction arg0) {
		InputDialog dialog = new InputDialog(RCPUtil.getActiveShell(), Messages.getString("org.nightlabs.jfire.base.ui.password.ChangePasswordAction.dialog.title"), Messages.getString("org.nightlabs.jfire.base.ui.password.ChangePasswordAction.dialog.message"), "", null) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			@Override
			protected void okPressed() {
				try {
					if (Login.getLogin().getPassword().equals(getValue()))
						super.okPressed();
					else {
						Thread.sleep(2000);
						MessageDialog.openError(RCPUtil.getActiveShell(), Messages.getString("org.nightlabs.jfire.base.ui.password.ChangePasswordAction.dialog.wrongPassword.title"), Messages.getString("org.nightlabs.jfire.base.ui.password.ChangePasswordAction.dialog.wrongPassword.message")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};

			@Override
			protected Control createDialogArea(Composite parent) {
				Control dialogArea = super.createDialogArea(parent);
				getText().setEchoChar('*');
				return dialogArea;
			}
		};

		if (dialog.open() != Window.OK)
			return;

		IInputValidator newPasswordValidator = new IInputValidator() {
			@Override
			public String isValid(String password) {
				if (password.length() < UserLocal.MIN_PASSWORD_LENGTH)
					return Messages.getString("org.nightlabs.jfire.base.ui.password.ChangePasswordAction.passwordToShort") + UserLocal.MIN_PASSWORD_LENGTH; //$NON-NLS-1$
				if (password.matches("\\*+")) //$NON-NLS-1$
					return Messages.getString("org.nightlabs.jfire.base.ui.password.ChangePasswordAction.passwordInvalid"); //$NON-NLS-1$

				return null;
			}
		};
		String newPassword = ChangePasswordDialog.openDialog(RCPUtil.getActiveShell(), newPasswordValidator, null);
		if (newPassword == null)
			return; // User canceled or somehow else entered no new and confirmed password -> do nothing
		try {
			JFireSecurityManagerRemote um = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, SecurityReflector.getInitialContextProperties());
			um.setUserPassword(newPassword);
			Login.sharedInstance().setPassword(newPassword);
			MessageDialog.openInformation(RCPUtil.getActiveShell(), Messages.getString("org.nightlabs.jfire.base.ui.password.ChangePasswordAction.dialog.passwordChanged.title"), Messages.getString("org.nightlabs.jfire.base.ui.password.ChangePasswordAction.dialog.passwordChanged.message")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
