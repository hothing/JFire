/**
 * 
 */
package org.nightlabs.jfire.base.ui.login;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.action.AbstractContributionItem;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.login.LoginState;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.jfire.base.ui.JFireBasePlugin;
import org.nightlabs.jfire.base.ui.login.action.LoginAction;
import org.nightlabs.jfire.base.ui.resource.Messages;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class LoginStateStatusLineContribution
extends AbstractContributionItem
implements LoginStateListener
{
	private static Logger logger = Logger.getLogger(LoginStateStatusLineContribution.class);

	private XComposite wrapper;
	private Label image;
	private Label text;

	public LoginStateStatusLineContribution(String name, boolean fillToolBar, boolean fillCoolBar, boolean fillMenuBar, boolean fillComposite) {
		super(LoginStateStatusLineContribution.class.getName(), name, fillToolBar, fillCoolBar, fillMenuBar, fillComposite);
		init();
	}

	public LoginStateStatusLineContribution(String name) {
		super(LoginStateStatusLineContribution.class.getName(), name);
		init();
	}

	private void init() {
		try {
			Login.getLogin(false).addLoginStateListener(this);
		} catch (LoginException e) {
			throw new RuntimeException(e);
		}
	}

	private String earlyLoginText;

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.action.AbstractContributionItem#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createControl(Composite parent) {
		wrapper = new XComposite(parent, SWT.NONE);
		StatusLineLayoutData layoutData = new StatusLineLayoutData();
		layoutData.widthHint = 200;
		wrapper.setLayoutData(layoutData);
		wrapper.getGridLayout().numColumns = 2;
		wrapper.getGridLayout().makeColumnsEqualWidth = false;
		image = new Label(wrapper, SWT.ICON);
		image.setImage(SharedImages.getSharedImage(JFireBasePlugin.getDefault(), LoginAction.class, "Login")); //$NON-NLS-1$
		image.setLayoutData(new GridData());
		text = new Label(wrapper, SWT.NONE);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setText(""); //$NON-NLS-1$
		if (earlyLoginText != null) { // if the login happened already before UI creation
			setText(earlyLoginText);
			text.setToolTipText(earlyLoginText);
			earlyLoginText = null;
		}
		wrapper.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				try {
					Login.getLogin(false).removeLoginStateListener(LoginStateStatusLineContribution.this);
				} catch (LoginException e) {
					throw new RuntimeException(e);
				}
			}
		});
		return wrapper;
	}

	private static int MAX_WIDTH = 240;
	private static String TRIPLE_DOT = "..."; //$NON-NLS-1$

	private void setText(String txt)
	{
		long start = System.currentTimeMillis();

		text.setText(txt);
		boolean appendedTripleDot = false;
		while (text.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x > MAX_WIDTH) {
			if (!appendedTripleDot) {
				txt += TRIPLE_DOT;
				appendedTripleDot = true;
			}

			txt = txt.substring(0, txt.length() - TRIPLE_DOT.length() - 1) + TRIPLE_DOT;
			text.setText(txt);
		}

		// we need to fill with spaces, because there seems to be no way to re-layout and grow the label later
		while (text.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x < MAX_WIDTH) {
			txt += ' ';
			text.setText(txt);
		}

		if (logger.isDebugEnabled())
			logger.debug("Setting text took " + (System.currentTimeMillis() - start) + " msec."); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.login.LoginStateListener#loginStateChanged(int, org.eclipse.jface.action.IAction)
	 */
	public void loginStateChanged(final LoginStateChangeEvent event)
	{
		Login login = Login.sharedInstance();

		String txt = null;

		if(event.getNewLoginState() == LoginState.LOGGED_IN)
			txt = String.format(Messages.getString("org.nightlabs.jfire.base.ui.login.LoginStateStatusLineContribution.loggedInStatus"), login.getUserID(), login.getOrganisationID(), login.getWorkstationID()); //$NON-NLS-1$
		else if(event.getNewLoginState() == LoginState.LOGGED_OUT)
			txt = Messages.getString("org.nightlabs.jfire.base.ui.login.LoginStateStatusLineContribution.loggedOutStatus"); //$NON-NLS-1$
		else
			return;

		if (text == null || text.isDisposed()) {
			earlyLoginText = txt;
			return;
		}

		if (txt != null) {
			setText(txt);
			text.setToolTipText(txt);

		}
	}

}
