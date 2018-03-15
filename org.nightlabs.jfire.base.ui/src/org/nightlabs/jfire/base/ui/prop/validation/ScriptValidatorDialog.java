/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.I18nTextEditor.EditMode;
import org.nightlabs.eclipse.ui.dialog.ResizableTitleAreaDialog;
import org.nightlabs.jfire.base.idgenerator.IDGeneratorClient;
import org.nightlabs.jfire.base.ui.prop.structedit.ValidationResultTypeCombo;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.validation.I18nValidationResult;
import org.nightlabs.jfire.prop.validation.I18nValidationResultMessage;
import org.nightlabs.jfire.prop.validation.IScriptValidator;
import org.nightlabs.jfire.prop.validation.ValidationResultType;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class ScriptValidatorDialog 
extends ResizableTitleAreaDialog
implements IScriptValidatorEditor
{
	private Text text;
	private I18nTextEditor i18nTextEditor;
	private ValidationResultTypeCombo validationResultTypeCombo;
	private Combo keyCombo;
	private IScriptValidator<?, ?> scriptValidator;
	private Map<String, I18nValidationResult> key2ValidationResult;
	private I18nValidationResult currentValidationResult;
	private IScriptValidatorHandler scriptHandler;
	private Button addTemplateButton;
	private String message;
	private Button validateScriptButton;
	
	/**
	 * @param shell
	 * @param resourceBundle
	 */
	public ScriptValidatorDialog(Shell shell, ResourceBundle resourceBundle, 
			IScriptValidator<?, ?> scriptValidator, IScriptValidatorHandler handler) 
	{
		super(shell, resourceBundle);
		if (scriptValidator == null)
			throw new IllegalArgumentException("Param scriptValidator must not be null!"); //$NON-NLS-1$

		if (handler == null)
			throw new IllegalArgumentException("Param handler must not be null!"); //$NON-NLS-1$
		
		this.scriptValidator = scriptValidator;
		this.scriptHandler = handler;
		scriptHandler.setScriptValidatorEditor(this);
		key2ValidationResult = new HashMap<String, I18nValidationResult>();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) 
	{
		setTitle(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.dialog.title")); //$NON-NLS-1$
		getShell().setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.window.title")); //$NON-NLS-1$
		message = Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.dialog.message"); //$NON-NLS-1$
		setMessage(message);
	
		Composite wrapper = new XComposite(parent, SWT.NONE);
		
		Composite comp = new XComposite(wrapper, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label keyLabel = new Label(comp, SWT.NONE);
		keyLabel.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.label.key.text")); //$NON-NLS-1$
		Composite keyWrapper = new XComposite(comp, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		XComposite.configureLayout(LayoutMode.TIGHT_WRAPPER, layout);
		keyWrapper.setLayout(layout);
		keyWrapper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		keyCombo = new Combo(keyWrapper, SWT.BORDER | SWT.READ_ONLY);
		keyCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		keyCombo.addSelectionListener(new SelectionAdapter(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectKey(keyCombo.getText());
				validateOK();
			}
		});

		Button addKeyButton = new Button(keyWrapper, SWT.NONE);
		addKeyButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.button.addKey.text")); //$NON-NLS-1$
		addKeyButton.addSelectionListener(new SelectionAdapter(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				addKeyPressed();
				validateOK();
			}
		});
		Button removeKeyButton = new Button(keyWrapper, SWT.NONE);
		removeKeyButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.button.removeKey.text")); //$NON-NLS-1$
		removeKeyButton.addSelectionListener(new SelectionAdapter(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeKeyPressed();
				validateOK();
			}
		});		
		
		Label messageLabel = new Label(comp, SWT.NONE);
		messageLabel.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.label.message.text")); //$NON-NLS-1$
		i18nTextEditor = new I18nTextEditor(comp);
		i18nTextEditor.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				validateOK();
			}
		});
		
		Label validationTypeLabel = new Label(comp, SWT.NONE);
		validationTypeLabel.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.label.validationType.text")); //$NON-NLS-1$
		validationResultTypeCombo = new ValidationResultTypeCombo(comp, SWT.READ_ONLY | SWT.BORDER);
		validationResultTypeCombo.selectElement(ValidationResultType.ERROR);		
		validationResultTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentValidationResult.setValidationResultType(validationResultTypeCombo.getSelectedElement());
			}
		});			
		
//		text = new Text(wrapper, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
		text = new Text(wrapper, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		text.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				validateOK();
			}
		});
		
		Composite buttonComp = new XComposite(wrapper, SWT.NONE,  LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);
		addTemplateButton = new Button(buttonComp, SWT.NONE);
		addTemplateButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.button.addTemplate.text")); //$NON-NLS-1$
		addTemplateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addTemplatePressed();
			}
		});			
		validateScriptButton = new Button(buttonComp, SWT.NONE);
		validateScriptButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.validateScriptButton.text")); //$NON-NLS-1$
		validateScriptButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				validateScriptPressed();
			}
		});
		
		setScriptValidator(scriptValidator);
		
		return wrapper;
	}

	public IScriptValidator<?, ?> getScriptValidator() {
		return scriptValidator;
	}
	
	private void setScriptValidator(IScriptValidator<?, ?> scriptValidator) 
	{
		if (scriptValidator == null)
			throw new IllegalArgumentException("Param scriptValidator must not be null!"); //$NON-NLS-1$
		
		this.scriptValidator = scriptValidator;
		key2ValidationResult = new HashMap<String, I18nValidationResult>();
		
		if (scriptValidator.getValidationResultKeys().size() > 0) 
		{
			if (keyCombo != null) {
				String[] items = scriptValidator.getValidationResultKeys().toArray(new String[scriptValidator.getValidationResultKeys().size()]);
				keyCombo.setItems(items);
				keyCombo.select(0);
			}
			for (String key : scriptValidator.getValidationResultKeys()) {
				I18nValidationResult result = scriptValidator.getValidationResult(key);
				if (result != null) {
					I18nValidationResult work = new I18nValidationResult(IDGeneratorClient.getOrganisationID(),
							IDGeneratorClient.nextID(I18nValidationResult.class), ValidationResultType.ERROR);
					copyI18nValidationResult(result, work);
					key2ValidationResult.put(key, work);
				}
			}
		}
		if (keyCombo != null && !keyCombo.getText().equals("")) { //$NON-NLS-1$
			selectKey(keyCombo.getText());
		}
		
		if (scriptValidator.getScript() != null && text != null) {
			text.setText(scriptValidator.getScript());	
		}
	}
		
	private void selectKey(String key) 
	{
		I18nValidationResult result = key2ValidationResult.get(key);
		if (result != null) {
			I18nValidationResultMessage message = result.getI18nValidationResultMessage();
			i18nTextEditor.setI18nText(message, EditMode.DIRECT);
			validationResultTypeCombo.selectElement(result.getResultType());
		}
		else {
			result = new I18nValidationResult(IDGeneratorClient.getOrganisationID(),
					IDGeneratorClient.nextID(I18nValidationResult.class), ValidationResultType.ERROR);
			I18nValidationResultMessage message = result.getI18nValidationResultMessage();
			i18nTextEditor.setI18nText(message, EditMode.DIRECT);
			validationResultTypeCombo.selectElement(result.getResultType());			
			key2ValidationResult.put(key, result);
		}
		currentValidationResult = result;
	}
	
	private void copyI18nValidationResult(I18nValidationResult original, I18nValidationResult copy) 
	{
		copy.setValidationResultType(original.getResultType());
		copy.getI18nValidationResultMessage().copyFrom(original.getI18nValidationResultMessage());
	}
	
	@Override
	protected void okPressed() 
	{
		scriptValidator.setScript(text.getText());
		for (Map.Entry<String, I18nValidationResult> entry : key2ValidationResult.entrySet()) {
			I18nValidationResult result = scriptValidator.getValidationResult(entry.getKey());
			if (result != null) {
				copyI18nValidationResult(entry.getValue(), result);
			} else {
				scriptValidator.addValidationResult(entry.getKey(), entry.getValue());
			}
		}
		super.okPressed();
	}
	
	private void addKeyPressed() 
	{
		InputDialog dialog = new InputDialog(getShell(), Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.inputDialog.window.title"),  //$NON-NLS-1$
			Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.inputDialog.message"), //$NON-NLS-1$
			"", new IInputValidator(){ //$NON-NLS-1$
				@Override
				public String isValid(String newText) {
					if (newText.isEmpty()) {
						return Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.validator.errorMessage"); //$NON-NLS-1$
					}
					return null;
				}
			});
		int returnCode = dialog.open();
		if (returnCode == Window.OK) {
			keyCombo.add(dialog.getValue());
			keyCombo.setText(dialog.getValue());
			selectKey(dialog.getValue());
		}
	}
	
	private void removeKeyPressed() 
	{
		String key = keyCombo.getText();
		if (!key.isEmpty()) {
			keyCombo.remove(key);
			key2ValidationResult.remove(key);			
		}
	}
	
	private void addTemplatePressed() 
	{
		if (scriptHandler != null) {
			if (!text.getText().isEmpty()) {
				boolean confirm = MessageDialog.openConfirm(getShell(), 
						Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.overrideDialog.title"),  //$NON-NLS-1$
						Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.overrideDialog.message")); //$NON-NLS-1$
				if (confirm)
					scriptHandler.addTemplate();
			}
			else {
				scriptHandler.addTemplate();	
			}
		}
	}
	
	@Override
	public String getCurrentKey() {
		return keyCombo.getText();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.IScriptValidatorEditor#getScript()
	 */
	@Override
	public String getScript() {
		return text.getText();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.IScriptValidatorEditor#setScript(java.lang.String)
	 */
	@Override
	public void setScript(String script) {
		text.setText(script);
	}
	
	private String getValidationText() 
	{
		if (keyCombo.getText().isEmpty())
			return Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.errorMessage.noKey"); //$NON-NLS-1$
		
		if (i18nTextEditor.getEditText().isEmpty())
			return Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.errorMessage.messageEmpty"); //$NON-NLS-1$
		
		if (text.getText().isEmpty())
			return Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.errorMessage.scriptEmpty")+addTemplateButton.getText(); //$NON-NLS-1$
		
//		String message = scriptHandler.validateScript(getScript());
//		if (message != null)
//			return "Script is not valid. Please press "+validateScriptButton.getText()+" for more details.";
			
		return null;
	}
	
	private void validateOK() 
	{
		String validationText = getValidationText();
		if (validationText != null)
			setMessage(validationText, IMessageProvider.INFORMATION);
		else
			setMessage(message);
		
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null)
			okButton.setEnabled(validationText == null);		
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		validateOK();
	}

	private void validateScriptPressed() 
	{
		String message = scriptHandler.validateScript(getScript());
		if (message == null)
			MessageDialog.openInformation(getShell(), Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.dialog.valid.title"), Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.dialog.valid.message")); //$NON-NLS-1$ //$NON-NLS-2$
		else 
			MessageDialog.openError(getShell(), Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog.dialog.invalid.title"), message); //$NON-NLS-1$
	}

//	private transient ScriptEngine scriptEngine;
//	private ScriptEngine getJavaScriptEngine() {
//		if (scriptEngine == null) {
//		    ScriptEngineManager mgr = new ScriptEngineManager(); 
//		    ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
//		    scriptEngine = jsEngine;
//		}
//		return scriptEngine;
//	}
}
