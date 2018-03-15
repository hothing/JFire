package org.nightlabs.installer.elements.ui.swing;

import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.ui.SwingUI;

/**
 * @version $Revision: 1056 $ - $Date: 2007-09-19 19:39:48 +0200 (Mi, 19 Sep 2007) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class TextInputElementUI extends SwingElementUI
{
	private JLabel label = null;
	private JTextField textField = null;
	private JLabel annotationLabel = null;
	
	@Override
	public void render() throws InstallationException
	{
		super.render();
		SwingUI.getActivePage().add(getLabel(), getLabelConstraints(), this);
		SwingUI.getActivePage().add(getTextField(), getTextFieldConstraints(), this);
		if(haveAnnotation()) {
			//Logger.out.println("Have annotation!");
			SwingUI.getActivePage().add(getAnnotationLabel(), getAnnotationLabelConstraints(), this);
		}
	}

	protected JLabel getLabel() throws InstallationException
	{
		if(label == null)
			label = new JLabel(String.format("%s:", getString(Constants.LABEL)));
		return label;
	}
	
	protected GridBagConstraints getLabelConstraints()
	{
		GridBagConstraints gbc = SwingUI.getGBC(0, 0);
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.gridx = 0;
//	gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets.right = 8;
		return gbc;
	}

	protected boolean haveAnnotation() throws InstallationException
	{
		return haveString("annotation");
	}
	
	protected JLabel getAnnotationLabel() throws InstallationException
	{
		if(annotationLabel == null)
			annotationLabel = new JLabel(String.format("(%s)", getString(Constants.ANNOTATION)));
		return annotationLabel;
	}

	protected GridBagConstraints getAnnotationLabelConstraints()
	{
		GridBagConstraints gbc = getTextFieldConstraints();
		gbc.gridy++;
		return gbc;
	}
	
	protected GridBagConstraints getTextFieldConstraints()
	{
		GridBagConstraints gbc = SwingUI.getGBC(1, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 2.0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		return gbc;
	}

	/**
	 * Get the textField.
	 * @return the textField
	 */
	public JTextField getTextField()
	{
		if(textField == null)
			textField = new JTextField();
		String defaultValue = getInstallationEntity().getResult(Constants.RESULT);
		if(defaultValue != null) {
			//Logger.out.println("Setting text field value: "+defaultValue);
			textField.setText(defaultValue);
		}
		return textField;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.elements.ui.gui.SwingElementUI#getResult()
	 */
	@Override
	public String getResult()
	{
		// don't use the getter here: the getter sets the default value
		return textField.getText();
	}
}
