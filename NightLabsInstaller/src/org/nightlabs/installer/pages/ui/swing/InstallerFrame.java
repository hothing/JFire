package org.nightlabs.installer.pages.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;

import org.nightlabs.installer.Messages;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class InstallerFrame extends JFrame
{
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JPanel headerPanel = null;

	private JPanel buttonPanel = null;

	private JPanel mainPanel = null;

	private JEditorPane headerTextArea = null;

	private JButton backButton = null;

	private JButton nextButton = null;

	private JButton finishButton = null;

	private JButton cancelButton = null;

	private JLabel iconLabel = null;

	/**
	 * This is the default constructor
	 */
	public InstallerFrame()
	{
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize()
	{
		this.setBounds(50, 80, 700, 550);
		this.setContentPane(getJContentPane());
		this.setTitle(Messages.getString("InstallerFrame.frameTitle")); //$NON-NLS-1$
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane()
	{
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getHeaderPanel(), BorderLayout.NORTH);
			jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
			jContentPane.add(getMainPanel(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes headerPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getHeaderPanel()
	{
		if (headerPanel == null) {
			GridBagConstraints gbc;
			headerPanel = new JPanel();
			headerPanel.setLayout(new GridBagLayout());
			headerPanel.setBackground(Color.WHITE);

			gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.weightx = 1.0;
			headerPanel.add(getHeaderTextField(), gbc);

			gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.NONE;
			gbc.anchor = GridBagConstraints.SOUTHEAST;
			gbc.weightx = 0.0;
			headerPanel.add(getIconLabel(), gbc);
		}
		return headerPanel;
	}

	private JLabel getIconLabel()
	{
		if(iconLabel == null) {
			iconLabel = new JLabel();
		}
		return iconLabel;
	}

	/**
	 * This method initializes buttonPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPanel()
	{
		if (buttonPanel == null) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
			buttonPanel = new JPanel();
			buttonPanel.setLayout(flowLayout);
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			buttonPanel.add(getBackButton(), null);
			buttonPanel.add(getNextButton(), null);
			buttonPanel.add(getFinishButton(), null);
			buttonPanel.add(getCancelButton(), null);
		}
		return buttonPanel;
	}

	/**
	 * This method initializes mainPanel
	 *
	 * @return javax.swing.JComponent
	 */
	private JPanel getMainPanel()
	{
		if (mainPanel == null) {
			mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			mainPanel.setBorder(new CompoundBorder(
					new CompoundBorder(
							new CompoundBorder(
								BorderFactory.createMatteBorder(1, 0, 0, 0, mainPanel.getBackground().darker()),
								BorderFactory.createMatteBorder(1, 0, 0, 0, mainPanel.getBackground().brighter())
							),
							new CompoundBorder(
								BorderFactory.createMatteBorder(0, 0, 1, 0, mainPanel.getBackground().brighter()),
								BorderFactory.createMatteBorder(0, 0, 1, 0, mainPanel.getBackground().darker())
							)),
						BorderFactory.createEmptyBorder(10, 20, 10, 20)
						));
		}
		return mainPanel;
	}

	public void setPageContent(Component content)
	{
		mainPanel.removeAll();
		mainPanel.add(content, BorderLayout.CENTER);
		validate();
		repaint();
	}

//	private static void debugComponentTree(Component c)
//	{
//		Logger.out.println("\nCOMPONENT:");
//		debugComponentTree(c, "");
//	}
//	private static void debugComponentTree(Component c, String indent)
//	{
//		Logger.out.println(indent+c.toString());
//		if(c instanceof Container) {
//			for (Component sc : ((Container)c).getComponents()) {
//				debugComponentTree(sc, indent+"  ");
//			}
//		}
//	}

	public void setPageIcon(Icon icon)
	{
		if(icon != getIconLabel().getIcon()) {
			getIconLabel().setIcon(icon);
			validate();
			repaint();
		}
	}

	/**
	 * This method initializes headerTextArea
	 *
	 * @return javax.swing.JEditorPane
	 */
	private JEditorPane getHeaderTextField()
	{
		if (headerTextArea == null) {
			headerTextArea = new JEditorPane();
			headerTextArea.setEditable(false);
			headerTextArea.setBackground(Color.WHITE);
			headerTextArea.setBorder(BorderFactory.createEmptyBorder());
//			headerTextArea.setPreferredSize(new Dimension(headerTextArea.getPreferredSize().width, 64));
		}
		return headerTextArea;
	}

	public void setHeaderText(String text, String annotation)
	{
		StringBuffer htmlText = new StringBuffer();
		htmlText.append("<html><body><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td style=\"width: 10px;\"></td><td style=\"font-family:arial,sans-serif; height:64px; vertical-align:middle\">"); //$NON-NLS-1$
		htmlText.append("<div style=\"font-weight: bold;\">"); //$NON-NLS-1$
		htmlText.append(text);
		htmlText.append("</div>"); //$NON-NLS-1$
		if(annotation != null && !"".equals(annotation)) { //$NON-NLS-1$
			htmlText.append("<div style=\"margin-top: 5px\">"); //$NON-NLS-1$
			htmlText.append(annotation);
			htmlText.append("</div>"); //$NON-NLS-1$
		}
		htmlText.append("</td></tr></table></body></html>"); //$NON-NLS-1$
		JEditorPane headerTextField = getHeaderTextField();
		headerTextField.setContentType("text/html"); //$NON-NLS-1$
		headerTextField.setText(htmlText.toString());
	}

	/**
	 * This method initializes backButton
	 *
	 * @return javax.swing.JButton
	 */
	public JButton getBackButton()
	{
		if (backButton == null) {
			backButton = new JButton();
			backButton.setText(Messages.getString("InstallerFrame.backButtonLabel")); //$NON-NLS-1$
			backButton.setMnemonic(Messages.getString("InstallerFrame.backButtonMnemonic").charAt(0)); //KeyEvent.VK_B); //$NON-NLS-1$
			backButton.setEnabled(false);
		}
		return backButton;
	}

	/**
	 * This method initializes nextButton
	 *
	 * @return javax.swing.JButton
	 */
	public JButton getNextButton()
	{
		if (nextButton == null) {
			nextButton = new JButton();
			nextButton.setText(Messages.getString("InstallerFrame.nextButtonLabel")); //$NON-NLS-1$
			nextButton.setMnemonic(Messages.getString("InstallerFrame.nextButtonMnemonic").charAt(0)); //KeyEvent.VK_N); //$NON-NLS-1$
		}
		return nextButton;
	}

	/**
	 * This method initializes finishButton
	 *
	 * @return javax.swing.JButton
	 */
	public JButton getFinishButton()
	{
		if (finishButton == null) {
			finishButton = new JButton();
			finishButton.setText(Messages.getString("InstallerFrame.finishButtonText")); //$NON-NLS-1$
			finishButton.setMnemonic(Messages.getString("InstallerFrame.finishButtonMnemonic").charAt(0)); //KeyEvent.VK_F); //$NON-NLS-1$
			finishButton.setEnabled(false);
		}
		return finishButton;
	}

	/**
	 * This method initializes cancelButton
	 *
	 * @return javax.swing.JButton
	 */
	public JButton getCancelButton()
	{
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText(Messages.getString("InstallerFrame.cancelButtonText")); //$NON-NLS-1$
			cancelButton.setMnemonic(Messages.getString("InstallerFrame.cancelButtonMnemonic").charAt(0)); //KeyEvent.VK_C); //$NON-NLS-1$
		}
		return cancelButton;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
