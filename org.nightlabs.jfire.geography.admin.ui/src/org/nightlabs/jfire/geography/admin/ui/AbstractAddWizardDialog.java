/**
 * 
 */
package org.nightlabs.jfire.geography.admin.ui;

import javax.naming.InitialContext;
import javax.security.auth.login.LoginException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.base.ui.wizard.DynamicPathWizardDialog;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public abstract class AbstractAddWizardDialog 
extends DynamicPathWizardDialog 
{
	public static final int ID_CREATE_NEW = IDialogConstants.CLIENT_ID + 1;
	private static Boolean isRootOrganisation = null;
	private String newButtonText;
	
	/**
	 * @param shell
	 * @param wizard
	 */
	public AbstractAddWizardDialog(Shell shell, DynamicPathWizard wizard, String newButtonText) {
		super(shell, wizard);
		this.newButtonText = newButtonText;
	}

	@Override
	public void create() 
	{
		// Must be done synchronous because otherwise, createButtonsForButtonBar can be called before check has been performed
		checkForRootOrganisation();
		super.create();
	}

	protected void checkForRootOrganisation() 
	{
		// we should perform the check only once, thats why we use a static field 
		if (isRootOrganisation == null) {
			try {
				final Login login = Login.getLogin();
				final InitialContext initialContext = login.createInitialContext();
				// do we have an root organisation, then must check for it
				if (Organisation.hasRootOrganisation(initialContext)) {
					if (login.getOrganisationID().equals(Organisation.getRootOrganisationID(initialContext))) 
					{
						isRootOrganisation = Boolean.TRUE;
					}					
				}
				// if we do not have an root organisation you can always create new geo information
				else {
					isRootOrganisation = Boolean.TRUE;
				}
			} catch (LoginException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (isRootOrganisation != null && isRootOrganisation.equals(Boolean.TRUE)) {
			// creating of new geography data may only be done by the root organisation
			createButton(parent, ID_CREATE_NEW, newButtonText, false);			
		}
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		if (buttonId == ID_CREATE_NEW) {
			newButtonPressed();
		}
	}

	protected abstract void newButtonPressed();
}
