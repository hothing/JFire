package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.action.SelectionAction;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.LanguageChooser;
import org.nightlabs.base.ui.language.I18nTextEditor.EditMode;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.base.ui.prop.validation.DataBlockValidatorTable;
import org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorDialog;
import org.nightlabs.jfire.base.ui.prop.validation.IDataChangeListener;
import org.nightlabs.jfire.base.ui.prop.validation.ScriptValidatorDialog;
import org.nightlabs.jfire.base.ui.prop.validation.StructBlockExpressionValidatorHandler;
import org.nightlabs.jfire.base.ui.prop.validation.StructBlockScriptValidatorHandler;
import org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.Mode;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.validation.ExpressionDataBlockValidator;
import org.nightlabs.jfire.prop.validation.IDataBlockValidator;
import org.nightlabs.jfire.prop.validation.IExpressionValidator;
import org.nightlabs.jfire.prop.validation.IScriptValidator;
import org.nightlabs.jfire.prop.validation.ScriptDataBlockValidator;
import org.nightlabs.jfire.prop.validation.ValidationResultType;

public class StructBlockEditorComposite extends XComposite
{
	class AddScriptValidatorAction extends SelectionAction 
	{
		public AddScriptValidatorAction() {
			super();
			setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructBlockEditorComposite.action.addScriptValidator.text")); //$NON-NLS-1$
			setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructBlockEditorComposite.action.addScriptValidator.tooltip")); //$NON-NLS-1$
			setId(AddScriptValidatorAction.class.getName());
			setImageDescriptor(SharedImages.ADD_16x16);
		}
		
		@Override
		public void run() {
			ScriptDataBlockValidator newValidator = new ScriptDataBlockValidator(
					ScriptDataBlockValidator.SCRIPT_ENGINE_NAME, "", block); //$NON-NLS-1$
			ScriptValidatorDialog dialog = new ScriptValidatorDialog(getShell(), null, 
					newValidator, new StructBlockScriptValidatorHandler(block));
			int returnCode = dialog.open();
			if (returnCode == Window.OK) {
				IScriptValidator<?, ?> scriptValidator = dialog.getScriptValidator();
				ScriptDataBlockValidator validator = new ScriptDataBlockValidator(
						ScriptDataBlockValidator.SCRIPT_ENGINE_NAME, scriptValidator.getScript(), block);
				for (String key : scriptValidator.getValidationResultKeys()) {
					validator.addValidationResult(key, scriptValidator.getValidationResult(key));
				}
				block.addDataBlockValidator(validator);
				validatorTable.setInput(block.getDataBlockValidators());
				markDirty();
			}
		}
		
		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateEnabled()
		 */
		@Override
		public boolean calculateEnabled() 
		{
			if (block != null) {
				return !block.getStructFields().isEmpty();	
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateVisible()
		 */
		@Override
		public boolean calculateVisible() {
			return true;
		}		
	}
	
	class AddExpressionValidatorAction extends SelectionAction 
	{
		public AddExpressionValidatorAction() {
			super();
			setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructBlockEditorComposite.action.addExpressionValidator.text")); //$NON-NLS-1$
			setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructBlockEditorComposite.action.addExpressionValidator.tooltip")); //$NON-NLS-1$
			setId(AddExpressionValidatorAction.class.getName());
			setImageDescriptor(SharedImages.ADD_16x16);
		}
		
		@Override
		public void run() {
			ExpressionValidatorDialog dialog = new ExpressionValidatorDialog(getShell(), null, null, 
					block.getStruct(), new StructBlockExpressionValidatorHandler(block), Mode.STRUCT_BLOCK) ;
			int returnCode = dialog.open();
			if (returnCode == Window.OK) {
				IExpression expression = dialog.getExpressionValidatorComposite().getExpression();
				I18nText message = dialog.getExpressionValidatorComposite().getMessage();
				ValidationResultType validationResultType = dialog.getExpressionValidatorComposite().getValidationResultType();
				ExpressionDataBlockValidator validator = new ExpressionDataBlockValidator(
						expression, message.getText(), validationResultType, block);
				validator.getValidationResult().getI18nValidationResultMessage().copyFrom(message);
				block.addDataBlockValidator(validator);
				validatorTable.setInput(block.getDataBlockValidators());
				markDirty();
			}
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateEnabled()
		 */
		@Override
		public boolean calculateEnabled() 
		{
			if (block != null) {
				return !block.getStructFields().isEmpty();	
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateVisible()
		 */
		@Override
		public boolean calculateVisible() {
			return true;
		}
	}
	
	class DeleteValidatorAction extends SelectionAction 
	{
		public DeleteValidatorAction() {
			super();
			setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructBlockEditorComposite.action.removeValidator.text")); //$NON-NLS-1$
			setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructBlockEditorComposite.action.removeValidator.tooltip")); //$NON-NLS-1$
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
			IDataBlockValidator validator = (IDataBlockValidator) getSelectedObjects().get(0);
			block.removeDataBlockValidator(validator);
			validatorTable.refresh();
			markDirty();
		}
	}
	
	class EditValidatorAction extends SelectionAction 
	{
		public EditValidatorAction() {
			super();
			setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructBlockEditorComposite.action.editValidator.text")); //$NON-NLS-1$
			setToolTipText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructBlockEditorComposite.action.editValidator.tooltip")); //$NON-NLS-1$
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
			IDataBlockValidator validator = (IDataBlockValidator) getSelectedObjects().get(0);
			if (validator instanceof IScriptValidator) {
				ScriptDataBlockValidator scriptValidator = (ScriptDataBlockValidator) validator;
				ScriptValidatorDialog dialog = new ScriptValidatorDialog(getShell(), null, scriptValidator,
						new StructBlockScriptValidatorHandler(block));
				int returnCode = dialog.open();
				if (returnCode == Window.OK) {
//					scriptValidator.setScript(dialog.getScript());
					validatorTable.refresh();
					markDirty();
				}
			}
			if (validator instanceof IExpressionValidator) {
				IExpressionValidator expressionValidator = (IExpressionValidator) validator;
				ExpressionValidatorDialog dialog = new ExpressionValidatorDialog(getShell(), null, expressionValidator.getExpression(), 
						block.getStruct(), new StructBlockExpressionValidatorHandler(block), Mode.STRUCT_BLOCK) ;
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
					markDirty();					
				}
			}
		}
	}

	private I18nTextEditor blockNameEditor;
	private Button uniqueButton;
	private StructBlock block;
	private DataBlockValidatorTable validatorTable;
	private ListenerList listeners;
	private ToolBarSectionPart sectionPart;
	
	public StructBlockEditorComposite(Composite parent, int style, LanguageChooser languageChooser) {
		super(parent, style, LayoutMode.TOP_BOTTOM_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);

		this.setVisible(false);

		blockNameEditor = new I18nTextEditor(this, languageChooser, Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructBlockEditorComposite.blockNameEditor.caption")); //$NON-NLS-1$
		
		new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		uniqueButton = new Button(this, SWT.CHECK);
		uniqueButton.setLayoutData((new GridData()));
		uniqueButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructBlockEditorComposite.unique.text")); //$NON-NLS-1$
		
		sectionPart = new ToolBarSectionPart(new FormToolkit(getDisplay()), this, Section.TITLE_BAR, 
				Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructBlockEditorComposite.section.validators.title")); //$NON-NLS-1$
		validatorTable = new DataBlockValidatorTable(sectionPart.getSection(), SWT.NONE, true, AbstractTableComposite.DEFAULT_STYLE_SINGLE);
		validatorTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		sectionPart.getSection().setClient(validatorTable);
		
		listeners = new ListenerList();
		
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
		
		uniqueButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				markDirty();
			}			
		});
		
		blockNameEditor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				markDirty();
			}
		});		
	}

	public void setStructBlock(StructBlock psb) {
		block = psb;
		blockNameEditor.setI18nText(psb.getName(), EditMode.DIRECT);
		uniqueButton.setSelection(block.isUnique());
		validatorTable.setInput(block.getDataBlockValidators());
		this.setVisible(true);
		sectionPart.updateToolBarManager();
	}

	public I18nTextEditor getBlockNameEditor() {
		return blockNameEditor;
	}
		
	protected void markDirty() {
		notifyDataChangeListeners();
	}
	
	public void addDataChangeListener(IDataChangeListener listener) {
		listeners.add(listener);
	}

	public void removeDataChangeListener(IDataChangeListener listener) {
		listeners.remove(listener);
	}
	
	protected void notifyDataChangeListeners() {
		for (Object listener : listeners.getListeners()) {
			((IDataChangeListener)listener).dataChanged();
		}
	}
	
	public void setEnabled(boolean enabled, boolean validatorEnablement) {
		super.setEnabled(enabled);
		sectionPart.getSection().setEnabled(validatorEnablement);
	}
}
