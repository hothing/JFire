package org.nightlabs.jfire.base.ui.prop.validation;

import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.message.IErrorMessageDisplayer;
import org.nightlabs.base.ui.message.MessageType;
import org.nightlabs.eclipse.ui.dialog.ResizableTitleAreaDialog;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.Mode;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.validation.ValidationResultType;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class ExpressionValidatorDialog extends ResizableTitleAreaDialog
{
	private ExpressionValidatorComposite expressionValidatorComposite;
	private IExpression expression;
	private I18nText message;
	private ValidationResultType validationResultType;
	private IStruct struct;
	private IExpressionValidatorHandler handler;
	private Mode mode;
	private String dialogMessage;

	/**
	 * @param shell
	 * @param resourceBundle
	 */
	public ExpressionValidatorDialog(Shell shell, ResourceBundle resourceBundle, IExpression expression,
			IStruct struct, IExpressionValidatorHandler handler, Mode mode)
	{
		super(shell, resourceBundle);
		this.expression = expression;
		this.struct = struct;
		this.handler = handler;
		this.mode = mode;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		setTitle(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorDialog.dialog.title")); //$NON-NLS-1$
		getShell().setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorDialog.window.title")); //$NON-NLS-1$
		dialogMessage = Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorDialog.dialog.message"); //$NON-NLS-1$
		setMessage(dialogMessage);

		expressionValidatorComposite = new ExpressionValidatorComposite(parent, SWT.NONE,
				expression, struct, handler, mode, new MessageDisplayer());
		if (message != null) {
			expressionValidatorComposite.setMessage(message);
		}
		if (validationResultType != null) {
			expressionValidatorComposite.setValidationResultType(validationResultType);
		}
		return expressionValidatorComposite;
	}

	public ExpressionValidatorComposite getExpressionValidatorComposite() {
		return expressionValidatorComposite;
	}

	/**
	 * Sets the message.
	 * @param message the message to set
	 */
	public void setMessage(I18nText message) {
		// remember because usually called before #createDialogArea and can then be set there
		this.message = message;
		if (expressionValidatorComposite != null) {
			expressionValidatorComposite.setMessage(message);
		}
	}

	/**
	 * Sets the validationResultType.
	 * @param validationResultType the validationResultType to set
	 */
	public void setValidationResultType(ValidationResultType validationResultType) {
		// remember because usually called before #createDialogArea and can then be set there
		this.validationResultType = validationResultType;
		if (expressionValidatorComposite != null) {
			expressionValidatorComposite.setValidationResultType(validationResultType);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		expressionValidatorComposite.refresh();
	}

	class MessageDisplayer implements IErrorMessageDisplayer
	{
		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.message.IMessageDisplayer#setMessage(java.lang.String, int)
		 */
		@Override
		public void setMessage(String message, int type) {
			if (message != null) {
				ExpressionValidatorDialog.this.setMessage(message, type);
			}
			else {
				ExpressionValidatorDialog.this.setMessage(dialogMessage);
			}
			Button okButton = getButton(IDialogConstants.OK_ID);
			if (okButton != null)
				okButton.setEnabled(message == null);
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.message.IErrorMessageDisplayer#setErrorMessage(java.lang.String)
		 */
		@Override
		public void setErrorMessage(String errorMessage) {
			ExpressionValidatorDialog.this.setErrorMessage(errorMessage);
			Button okButton = getButton(IDialogConstants.OK_ID);
			if (okButton != null)
				okButton.setEnabled(errorMessage == null);
		}

		/*
		 * (non-Javadoc)
		 * @see org.nightlabs.base.ui.message.IMessageDisplayer#setMessage(java.lang.String, org.nightlabs.base.ui.message.MessageType)
		 */
		@Override
		public void setMessage(String message, MessageType type)
		{
			setMessage(message, type.ordinal());
		}
	}
}
