package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.action.SelectionAction;
import org.nightlabs.base.ui.composite.ChildStatusController;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.LanguageChooser;
import org.nightlabs.base.ui.language.I18nTextEditor.EditMode;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.base.ui.JFireBasePlugin;
import org.nightlabs.jfire.base.ui.prop.validation.DataFieldValidatorTable;
import org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorDialog;
import org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog;
import org.nightlabs.jfire.base.ui.prop.validation.StructFieldExpressionValidatorHandler;
import org.nightlabs.jfire.base.ui.prop.validation.StructFieldScriptValidatorHandler;
import org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.Mode;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.validation.ExpressionDataFieldValidator;
import org.nightlabs.jfire.prop.validation.IDataFieldExpression;
import org.nightlabs.jfire.prop.validation.IDataFieldValidator;
import org.nightlabs.jfire.prop.validation.IExpressionValidator;
import org.nightlabs.jfire.prop.validation.IScriptValidator;
import org.nightlabs.jfire.prop.validation.ScriptDataBlockValidator;
import org.nightlabs.jfire.prop.validation.ScriptDataFieldValidator;
import org.nightlabs.jfire.prop.validation.ValidationResultType;

public abstract class AbstractStructFieldEditor<F extends StructField>
extends AbstractStructPartEditor<F>
implements StructFieldEditor<F>
{
	class AddScriptValidatorAction extends Action
	{
		public AddScriptValidatorAction() {
			super();
			setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.AbstractStructFieldEditor.action.addScriptValidator.text")); //$NON-NLS-1$
			setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.AbstractStructFieldEditor.action.addScriptValidator.tooltip")); //$NON-NLS-1$
			setId(AddScriptValidatorAction.class.getName());
			setImageDescriptor(SharedImages.ADD_16x16);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			ScriptDataFieldValidator<?, ?> newValidator = new ScriptDataFieldValidator(
					ScriptDataBlockValidator.SCRIPT_ENGINE_NAME, "", structField); //$NON-NLS-1$
			ScriptValidatorDialog dialog = new ScriptValidatorDialog(getShell(), null, newValidator,
					new StructFieldScriptValidatorHandler(structField));
			int returnCode = dialog.open();
			if (returnCode == Window.OK) {
				IScriptValidator<?, ?> scriptValidator = dialog.getScriptValidator();
				ScriptDataFieldValidator<?, ?> validator = new ScriptDataFieldValidator(
						ScriptDataBlockValidator.SCRIPT_ENGINE_NAME, scriptValidator.getScript(), structField);
				for (String key : scriptValidator.getValidationResultKeys()) {
					validator.addValidationResult(key, scriptValidator.getValidationResult(key));
				}
				structField.addDataFieldValidator(validator);
				validatorTable.setInput(structField.getDataFieldValidators());
				setChanged();
			}
		}
	}

	class AddExpressionValidatorAction extends Action
	{
		public AddExpressionValidatorAction() {
			super();
			setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.AbstractStructFieldEditor.action.addExpressionValidator.text")); //$NON-NLS-1$
			setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.AbstractStructFieldEditor.action.addExpressionValidator.tooltip")); //$NON-NLS-1$
			setId(AddExpressionValidatorAction.class.getName());
			setImageDescriptor(SharedImages.ADD_16x16);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			ExpressionValidatorDialog dialog = new ExpressionValidatorDialog(getShell(), null, null,
					getStructEditor().getStruct(), new StructFieldExpressionValidatorHandler(getStructField()),
					Mode.STRUCT_FIELD) ;
			int returnCode = dialog.open();
			if (returnCode == Window.OK) {
				IExpression expression = dialog.getExpressionValidatorComposite().getExpression();
				I18nText message = dialog.getExpressionValidatorComposite().getMessage();
				ValidationResultType validationResultType = dialog.getExpressionValidatorComposite().getValidationResultType();
				if (expression instanceof IDataFieldExpression<?>) {
					ExpressionDataFieldValidator<?, ?> validator = new ExpressionDataFieldValidator(
							(IDataFieldExpression<?>) expression, message.getText(), validationResultType, getStructField());
					validator.getValidationResult().getI18nValidationResultMessage().copyFrom(message);
					getStructField().addDataFieldValidator(validator);
					validatorTable.setInput(getStructField().getDataFieldValidators());
					setChanged();
				}
			}
		}
	}

	class DeleteValidatorAction extends SelectionAction
	{
		public DeleteValidatorAction() {
			super();
			setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.AbstractStructFieldEditor.action.removeValidator.text")); //$NON-NLS-1$
			setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.AbstractStructFieldEditor.action.removeValidator.tooltip")); //$NON-NLS-1$
			setId(DeleteValidatorAction.class.getName());
			setImageDescriptor(SharedImages.DELETE_16x16);
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateEnabled()
		 */
		@Override
		public boolean calculateEnabled() {
			return !getSelection().isEmpty();
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateVisible()
		 */
		@Override
		public boolean calculateVisible() {
			return true;
		}

		@Override
		public void run() {
			IDataFieldValidator<?, ?> validator = (IDataFieldValidator<?, ?>) getSelectedObjects().get(0);
			structField.removeDataFieldValidator(validator);
			validatorTable.refresh();
			setChanged();
		}
	}

	class EditValidatorAction extends SelectionAction
	{
		public EditValidatorAction() {
			super();
			setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.AbstractStructFieldEditor.action.editValidator.text")); //$NON-NLS-1$
			setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.AbstractStructFieldEditor.action.editValidator.tooltip")); //$NON-NLS-1$
			setId(EditValidatorAction.class.getName());
			setImageDescriptor(SharedImages.EDIT_16x16);
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateEnabled()
		 */
		@Override
		public boolean calculateEnabled() {
			return !getSelection().isEmpty();
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateVisible()
		 */
		@Override
		public boolean calculateVisible() {
			return true;
		}

		@Override
		public void run() {
			IDataFieldValidator<?, ?> validator = (IDataFieldValidator<?, ?>) getSelectedObjects().get(0);
			if (validator instanceof IScriptValidator) {
				ScriptDataFieldValidator<?, ?> scriptValidator = (ScriptDataFieldValidator<?, ?>) validator;
				ScriptValidatorDialog dialog = new ScriptValidatorDialog(getShell(), null, scriptValidator,
						new StructFieldScriptValidatorHandler(structField));
				int returnCode = dialog.open();
				if (returnCode == Window.OK) {
//					scriptValidator.setScript(dialog.getScript());
					validatorTable.refresh();
					setChanged();
				}
			}
			if (validator instanceof IExpressionValidator) {
				IExpressionValidator expressionValidator = (IExpressionValidator) validator;
				ExpressionValidatorDialog dialog = new ExpressionValidatorDialog(getShell(), null, expressionValidator.getExpression(),
						getStructEditor().getStruct(), new StructFieldExpressionValidatorHandler(getStructField()), Mode.STRUCT_FIELD);
				dialog.setMessage(expressionValidator.getValidationResult().getI18nValidationResultMessage());
				dialog.setValidationResultType(expressionValidator.getValidationResult().getResultType());
				int returnCode = dialog.open();
				if (returnCode == Window.OK) {
					IExpression expression = dialog.getExpressionValidatorComposite().getExpression();
					I18nText message = dialog.getExpressionValidatorComposite().getMessage();
					ValidationResultType validationResultType = dialog.getExpressionValidatorComposite().getValidationResultType();
					expressionValidator.getValidationResult().getI18nValidationResultMessage().copyFrom(message);
					expressionValidator.getValidationResult().setValidationResultType(validationResultType);
					expressionValidator.setExpression(expression);
					validatorTable.refresh();
					setChanged();
				}
			}
		}
	}

	private Composite specialComposite;
	private I18nTextEditor fieldNameEditor;
	private StructEditor structEditor;
	private F structField;
	private LanguageChooser languageChooser;
	private ErrorComposite errorComp;
	private String errorMessage;
	private Group editorGroup;
	private ChildStatusController childStatusController;
	private DataFieldValidatorTable validatorTable;

	protected Shell getShell() {
		return editorGroup.getShell();
	}

	public void setChanged() {
//		getStructEditor().setChanged(true);
		notifyModifyListeners();
	}

	public Composite createComposite(Composite parent, int style, StructEditor structEditor, LanguageChooser languageChooser) {
		this.childStatusController = new ChildStatusController();
		this.languageChooser = languageChooser;
		this.structEditor = structEditor;
		editorGroup = new Group(parent, SWT.NONE);
		editorGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout gl = new GridLayout();
		XComposite.configureLayout(LayoutMode.ORDINARY_WRAPPER, gl);
		editorGroup.setLayout(gl);
		((GridLayout)editorGroup.getLayout()).marginTop = 15;

		fieldNameEditor = new I18nTextEditor(editorGroup, this.languageChooser, Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.AbstractStructFieldEditor.fieldNameEditor.caption")); //$NON-NLS-1$
		fieldNameEditor.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(editorGroup, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		specialComposite = createSpecialComposite(editorGroup, style);
		if (specialComposite != null && !specialComposite.isDisposed()) {
			specialComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}

		errorComp = new ErrorComposite(editorGroup);
		errorComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ToolBarSectionPart sectionPart = new ToolBarSectionPart(new FormToolkit(editorGroup.getDisplay()), editorGroup, Section.TITLE_BAR, Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.AbstractStructFieldEditor.section.validators.title")); //$NON-NLS-1$
		validatorTable = new DataFieldValidatorTable(sectionPart.getSection(), SWT.NONE, true, AbstractTableComposite.DEFAULT_STYLE_SINGLE);
		validatorTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		sectionPart.getSection().setClient(validatorTable);

		final EditValidatorAction editAction = new EditValidatorAction();
		sectionPart.registerAction(new AddExpressionValidatorAction(), true);
		sectionPart.registerAction(new AddScriptValidatorAction(), true);
		sectionPart.registerAction(new DeleteValidatorAction(), true);
		sectionPart.registerAction(editAction, true);
		sectionPart.setSelectionProvider(validatorTable);
		sectionPart.updateToolBarManager();

		validatorTable.addDoubleClickListener(new IDoubleClickListener(){
			@Override
			public void doubleClick(DoubleClickEvent event) {
				editAction.run();
			}
		});

//		fieldNameEditor.addModifyListener(new ModifyListener() {
//			@Override
//			public void modifyText(ModifyEvent e) {
//				setChanged();
//			}
//		});

		return editorGroup;
	}

	public void setErrorMessage(String error) {
		this.errorMessage = error;
		errorComp.setErrorMessage(error);
	}

	protected StructEditor getStructEditor() {
		return structEditor;
	}

	protected F getStructField() {
		return structField;
	}

	public I18nTextEditor getFieldNameEditor() {
		return fieldNameEditor;
	}

	public LanguageChooser getLanguageChooser() {
		return languageChooser;
	}

	public Composite getComposite() {
		return editorGroup;
	}

	public void setData(F field) {
		if (editorGroup == null)
			throw new IllegalStateException("You have to call createComposite(...) prior to calling setData(...)"); //$NON-NLS-1$

		if (field == null)
		{
			fieldNameEditor.reset();
//			fieldNameEditor.setEnabled(false);
			setEnabled(false);
			if (specialComposite != null)
				specialComposite.dispose();
			return;
		}

//		fieldNameEditor.setEnabled(true);
		setEnabled(true);
		fieldNameEditor.setI18nText(field.getName(), EditMode.DIRECT);
		StructFieldMetaData sfmd = StructFieldFactoryRegistry.sharedInstance().getFieldMetaDataMap().get(field.getClass().getName());
		if (sfmd != null) {
			String fieldName = sfmd.getFieldName();
			editorGroup.setText(fieldName);
		} else
			editorGroup.setText("Uknown field type"); //$NON-NLS-1$
		fieldNameEditor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setChanged();
			}
		});
		structField = field;
		validatorTable.setInput(structField.getDataFieldValidators());
		setSpecialData(field);
	}

	protected abstract void setSpecialData(F field);

	@Override
	public void setFocus() {
		if (!getFieldNameEditor().isDisposed())
			getFieldNameEditor().setFocus();
	}

	/**
	 * Extendors should create struct field specific gui in this method and render the
	 * data of the struct field since this method is called every time a new struct
	 * field is selected.
	 *
	 * @param parent
	 * @param style
	 * @return
	 */
	protected abstract Composite createSpecialComposite(Composite parent, int style);

	/**
	 * This method is intended to be overridden if the managed struct field supports validation.
	 * Extendors should save their data in order to restore it upon a call to {@link #restoreData()}:
	 * This happens if validation fails and the user wants to discard the changes.
	 *
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.StructFieldEditor#saveData()
	 */
	public void saveData() {
		// do nothing by default
	}

	/**
	 * This method is intended to be overriden if the managed struct field supports validation.
	 * Extendors should restore the data previously saved by {@link #saveData()}.
	 * This method is called if validation fails and the user wants to discard the changes.
	 *
	 * @see org.nightlabs.jfire.base.ui.prop.structedit.StructFieldEditor#restoreData()
	 */
	public void restoreData() {
		// do nothing by default
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * This method is intended to be overriden by struct editors that require validation
	 * and should return a boolean indicating whether the user input is valid for the
	 * type of struct field. In addition, extendors can call {@link #setErrorMessage(String)}
	 * to display a message explaining the validation error if there is one.
	 */
	public boolean validateInput() {
		return true; // no validation done by default
	}

	public void setEnabled(boolean enabled) {
		if (editorGroup != null) {
			if (editorGroup.isEnabled() != enabled) {
				childStatusController.setEnabled(editorGroup, enabled);
				editorGroup.setEnabled(enabled);
			}
		}
	}
}

class ErrorComposite extends XComposite {
	private Image errorImage;
	private Label errorLabel;
	private Label errorImageLabel;

	public ErrorComposite(Composite parent) {
		super(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);

		errorImage = JFireBasePlugin.getImageDescriptor("icons/Validation_error.gif").createImage(); //$NON-NLS-1$

		errorImageLabel = new Label(this, SWT.NONE);
		errorLabel = new Label(this, SWT.NONE);
		errorImageLabel.setImage(errorImage);
		errorLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
		setVisible(false);

		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (errorImage != null)
					errorImage.dispose();
			}
		});
	}

	protected void setErrorMessage(String error) {
		if (error == null || error.equals("")) { //$NON-NLS-1$
			setVisible(false);
		} else {
			errorLabel.setText(error);
			setVisible(true);
		}
		errorLabel.pack();
		pack();
		layout();
	}
}