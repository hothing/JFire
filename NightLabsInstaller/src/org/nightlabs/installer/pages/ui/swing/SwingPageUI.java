package org.nightlabs.installer.pages.ui.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.InstallationManager;
import org.nightlabs.installer.Messages;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.Navigator.Navigation;
import org.nightlabs.installer.base.defaults.DefaultUI;
import org.nightlabs.installer.base.ui.NavigationUI;
import org.nightlabs.installer.base.ui.SwingUI;
import org.nightlabs.installer.elements.ui.swing.SwingElementUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class SwingPageUI extends DefaultUI implements ActionListener
{
	private JPanel contentPane = null;

	private JPanel emptyContentPane = null;

	private Set<SwingElementUI> elements = null;

	private boolean waiting;

	private JPanel spacer;
	private GridBagConstraints spacerGBC;
	private int lastGridY;
	private int maxRelativeGridY;
	private SwingElementUI lastSource;
	Navigation lastNavigation = null;

	private void init()
	{
		contentPane = null;
		elements = null;
		lastGridY = 0;
		maxRelativeGridY = -1;
		lastSource = null;
		SwingUI.setActivePage(this);
	}

	@Override
	public void renderBefore() throws InstallationException
	{
		super.renderBefore();
		init();
	}

	protected JPanel getContentPane()
	{
		if(contentPane == null) {
			contentPane = new JPanel();
			contentPane.setLayout(new GridBagLayout());
		}
		return contentPane;
	}

	public void add(Component c, GridBagConstraints gbc, SwingElementUI source)
	{
		if(source != lastSource) {
			lastGridY += maxRelativeGridY + 1;
			maxRelativeGridY = 0;
			if(elements == null)
				elements = new HashSet<SwingElementUI>();
			elements.add(source);

			lastSource = source;
		}
		if(gbc.gridy == 0)
			gbc.insets.top += 15;

		maxRelativeGridY = Math.max(maxRelativeGridY, gbc.gridy);

		gbc.gridy = lastGridY + gbc.gridy;
		getContentPane().add(c, gbc);
	}

	public void actionPerformed(ActionEvent e)
	{
		try {
			lastNavigation = null;
			if(e.getSource() == SwingUI.getInstallerFrame().getNextButton() ||
				 e.getSource() == SwingUI.getInstallerFrame().getFinishButton()) {
				lastNavigation = Navigation.next;
				NavigationUI.setNavigation(getInstallationEntity().getParent(), lastNavigation);
				SwingUI.getInstallerFrame().getBackButton().setEnabled(true);
				waiting = false;
			} else if(e.getSource() == SwingUI.getInstallerFrame().getBackButton()) {
				lastNavigation = Navigation.back;
				NavigationUI.setNavigation(getInstallationEntity().getParent(), lastNavigation);
				SwingUI.getInstallerFrame().getNextButton().setEnabled(true);
				waiting = false;
			} else if(e.getSource() == SwingUI.getInstallerFrame().getCancelButton()) {
				int result = JOptionPane.showOptionDialog(
						SwingUI.getInstallerFrame(),
						Messages.getString("SwingPageUI.cancelQuestion"),  //$NON-NLS-1$
						Messages.getString("SwingPageUI.cancelTitle"),  //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null, null, null
						);
				if(result == JOptionPane.YES_OPTION)
					System.exit(0);
			}
		} catch (InstallationException e1) {
			InstallationManager.getInstallationManager().getErrorHandler().handle(e1);
		}
		// stop waiting on the GUI thread
	}

	private static JButton[] listenerButtons = null;

	private static JButton[] getListenerButtons()
	{
		if(listenerButtons == null) {
			listenerButtons = new JButton[] {
					SwingUI.getInstallerFrame().getNextButton(),
					SwingUI.getInstallerFrame().getBackButton(),
					SwingUI.getInstallerFrame().getCancelButton(),
					SwingUI.getInstallerFrame().getFinishButton(),
			};
		}
		return listenerButtons;
	}

	private void enableListeners(boolean enable)
	{
		for (JButton button : getListenerButtons()) {
			if(enable)
				button.addActionListener(this);
			else
				button.removeActionListener(this);
		}
	}

	protected void waitForPage()
	{
		lastNavigation = null;
		waiting = true;
		enableListeners(true);
		// wait on the main thread
		while(waiting) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		enableListeners(false);
	}

	private JPanel getSpacer()
	{
		if(spacer == null) {
			spacer = new JPanel();
		}
		return spacer;
	}
	private GridBagConstraints getSpacerGBC()
	{
		if(spacerGBC == null) {
			spacerGBC = SwingUI.getGBC(0, lastGridY + maxRelativeGridY + 1);
			spacerGBC.weighty = 50.0;
			spacerGBC.gridwidth = 3;
			spacerGBC.fill = GridBagConstraints.BOTH;
		}
		return spacerGBC;
	}

	protected Component getContentPaneWithSpacer()
	{
		JPanel contentPane = getContentPane();
		contentPane.add(getSpacer(), getSpacerGBC());
		return contentPane;
	}

	private JPanel getEmptyContentPane()
	{
		if(emptyContentPane == null) {
			emptyContentPane = new JPanel();
		}
		return emptyContentPane;
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultUI#renderAfter()
	 */
	@Override
	public void renderAfter() throws InstallationException
	{
		SwingUI.getInstallerFrame().setPageContent(getContentPaneWithSpacer());
		SwingUI.getInstallerFrame().setVisible(true);
		waitForPage();
		SwingUI.getInstallerFrame().setPageContent(getEmptyContentPane());
		// only set results if navigation was next
		if(lastNavigation == Navigation.next) {
			if(elements != null) {
				for (SwingElementUI elementUI : elements) {
					String result = elementUI.getResult();
					if(result != null) {
						//Logger.out.println("Setting result for "+elementUI.getInstallationEntity().getId()+": "+result);
						elementUI.getInstallationEntity().setResult(Constants.RESULT, result);
					}
				}
			}
		}
	}
}
