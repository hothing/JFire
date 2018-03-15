package org.nightlabs.installer.pages.ui.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.nightlabs.installer.ExecutionProgressEvent;
import org.nightlabs.installer.ExecutionProgressListener;
import org.nightlabs.installer.InstallationManager;
import org.nightlabs.installer.Messages;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.SwingUI;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ExecutionPageUI extends TitlePageUI
{
	private static final String INSTALLATION_CONTENT_PANE = "installationContentPane"; //$NON-NLS-1$
	private static final String START_CONTENT_PANE = "startContentPane"; //$NON-NLS-1$
	private JPanel contentPane = null;
	private JPanel startContentPane = null;
	private JScrollPane startTextScrollPane = null;
	private JTextArea startTextArea = null;
	private JPanel installationContentPane = null;
	private JTextArea progressList = null;
	private JScrollPane progressListScrollPane = null;
	private JProgressBar progressBar = null;
	private ExecutionProgressListener progressListener = null;
	private CardLayout contentPaneLayout = null;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultUI#render()
	 */
	@Override
	public void render() throws InstallationException
	{
		super.render();
		InstallationManager.getInstallationManager().addExecutionProgressListener(getProgressListener());
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.pages.ui.swing.SwingPageUI#renderAfter()
	 */
	@Override
	public void renderAfter() throws InstallationException
	{
		InstallerFrame installerFrame = SwingUI.getInstallerFrame();
		installerFrame.getFinishButton().setEnabled(true);
		installerFrame.getNextButton().setEnabled(false);
		installerFrame.setPageContent(getContentPane());
		installerFrame.setVisible(true);
		waitForPage();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.pages.ui.swing.SwingPageUI#getContentPane()
	 */
	@Override
	protected JPanel getContentPane()
	{
		if(contentPane == null) {
			contentPane = new JPanel();
			contentPane.setLayout(getContentPaneLayout());
			contentPane.add(getStartContentPane(), START_CONTENT_PANE);
			contentPane.add(getInstallationContentPane(), INSTALLATION_CONTENT_PANE);
		}
		return contentPane;
	}

	private CardLayout getContentPaneLayout()
	{
		if(contentPaneLayout == null) {
			contentPaneLayout = new CardLayout();
		}
		return contentPaneLayout;
	}
	
	private JPanel getStartContentPane()
	{
		if(startContentPane == null) {
			startContentPane = new JPanel();
			startContentPane.setLayout(new BorderLayout());
			startContentPane.add(getStartTextScrollPane(), BorderLayout.CENTER);
		}
		return startContentPane;
	}
	
	private JScrollPane getStartTextScrollPane()
	{
		if(startTextScrollPane == null) {
	    startTextScrollPane = new JScrollPane();
	    startTextScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    startTextScrollPane.setBorder(BorderFactory.createEmptyBorder());
	    startTextScrollPane.setBackground(SwingUI.getInstallerFrame().getBackground());
	    startTextScrollPane.setViewportView(getStartTextArea());
		}
		return startTextScrollPane;
	}
	
	private JTextArea getStartTextArea()
	{
		if(startTextArea == null) {
			startTextArea = new JTextArea();
			startTextArea.setLineWrap(true);
			startTextArea.setWrapStyleWord(true);
			startTextArea.setEditable(false);
			startTextArea.setText(Messages.getString("ExecutionPageUI.startText")); //$NON-NLS-1$
			startTextArea.setBorder(BorderFactory.createEmptyBorder());
			startTextArea.setFont(SwingUI.getInstallerFrame().getFont());
			startTextArea.setBackground(SwingUI.getInstallerFrame().getBackground());
		}
		return startTextArea;
	}
	
	private JPanel getInstallationContentPane()
	{
		if(installationContentPane == null) {
			installationContentPane = new JPanel();
			installationContentPane.setLayout(new GridBagLayout());
			GridBagConstraints gbc = SwingUI.getGBC(0, 0);
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weighty = 100.0;
//			add(getProgressListScrollPane(), gbc, null);
			installationContentPane.add(getProgressListScrollPane(), gbc);
			
			GridBagConstraints gbc2 = SwingUI.getGBC(0, 1);
			gbc2.fill = GridBagConstraints.HORIZONTAL;
//			add(getProgressBar(), gbc2, null);
			installationContentPane.add(getProgressBar(), gbc2);
		}
		return installationContentPane;
	}

	/**
	 * Get the progressBar.
	 * @return the progressBar
	 */
	public JProgressBar getProgressBar()
	{
		if(progressBar == null) {
			progressBar = new JProgressBar();
			progressBar.setEnabled(false);
		}
		return progressBar;
	}

	/**
	 * Get the progressListScrollPane.
	 * @return the progressListScrollPane
	 */
	public JScrollPane getProgressListScrollPane()
	{
		if(progressListScrollPane == null) {
			progressListScrollPane = new JScrollPane();
			progressListScrollPane.setViewportView(getProgressList());
			progressListScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			progressListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}
		return progressListScrollPane;
	}

	private JTextArea getProgressList()
	{
		if(progressList == null) {
			progressList = new JTextArea();
			progressList.setEditable(false);
		}
		return progressList;
	}

	private ExecutionProgressListener getProgressListener()
	{
		if(progressListener == null) {
			progressListener = new ExecutionProgressListener() {
				public void executionProgress(final ExecutionProgressEvent e)
				{
//						try {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() 
							{
								//Logger.out.println("PROGRESS: "+e.getType()+" - "+e.getDescription());
								JButton finishButton = SwingUI.getInstallerFrame().getFinishButton();
								JTextArea progressList = getProgressList();
								try {
									Document document = progressList.getDocument();
									document.insertString(document.getLength(), e.getDescription()+"\n", null); //$NON-NLS-1$
								} catch (BadLocationException e1) {
									e1.printStackTrace();
								}
								
								if(e.getWorkDone() >= 0 && e.getSource().getTotalWork() >= 0) {
									JProgressBar progressBar = getProgressBar();
									progressBar.setIndeterminate(false);
									progressBar.setMaximum(e.getSource().getTotalWork());
									progressBar.setValue(e.getWorkDone());
								}
								
								if(e.getType() == ExecutionProgressEvent.Type.starting) {
									SwingUI.getInstallerFrame().getBackButton().setEnabled(false);
									SwingUI.getInstallerFrame().getNextButton().setEnabled(false);
									SwingUI.getInstallerFrame().getCancelButton().addActionListener(ExecutionPageUI.this);
									finishButton.setEnabled(false);
									JProgressBar progressBar = getProgressBar();
									progressBar.setIndeterminate(true);
									progressBar.setEnabled(true);
								} else if(e.getType() == ExecutionProgressEvent.Type.done) {
									SwingUI.getInstallerFrame().getCancelButton().setEnabled(false);
									finishButton.setText(Messages.getString("ExecutionPageUI.closeButtonLabel")); //$NON-NLS-1$
									finishButton.setMnemonic(Messages.getString("ExecutionPageUI.closeButtonMnemonic").charAt(0)); //$NON-NLS-1$
									finishButton.setEnabled(true);
									finishButton.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e)
										{
											System.exit(0);
										}
									});
									JProgressBar progressBar = getProgressBar();
									progressBar.setIndeterminate(false);
									progressBar.setValue(progressBar.getMaximum());
								}
							}
						});
				}
			};
		}
		return progressListener;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.pages.ui.swing.SwingPageUI#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		super.actionPerformed(e);
		if(e.getSource() == SwingUI.getInstallerFrame().getFinishButton())
			getContentPaneLayout().show(getContentPane(), INSTALLATION_CONTENT_PANE);
		else if(e.getSource() == SwingUI.getInstallerFrame().getCancelButton())
			super.actionPerformed(e);
	}
}
