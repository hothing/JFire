/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.edit.fieldbased;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.wizard.WizardHopPage;
import org.nightlabs.jfire.base.ui.prop.ValidationUtil;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedEvent;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedListener;
import org.nightlabs.jfire.base.ui.prop.edit.IValidationResultHandler;
import org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor;
import org.nightlabs.jfire.base.ui.prop.edit.ValidationResultHandler;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutConfigModule;
import org.nightlabs.jfire.prop.validation.ValidationResult;
import org.nightlabs.jfire.prop.validation.ValidationResultType;

/**
 * A Wizard page that creates a {@link FieldBasedEditorCfModLayoutConfig} PropertySet editor.
 * The page handles validation results in its header. It can be configured to block or
 * not block on validation errors.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public class FieldBasedEditorCfModLayoutConfigWizardPage extends WizardHopPage {

	/**
	 * The field-based editor created for this page. 
	 */
	private FieldBasedEditorCfModLayoutConfig editor;
	/**
	 * Tracks whether this page is pristine (no validation error will be shown while pristine)
	 */
	private boolean pristine = true;
	/**
	 * Tracks whether the last validation had errors.
	 */
	private boolean hasValidationError = false;
	/**
	 * Set in the constructor, defines whether a validation error blocks the page (isComplete returns false) 
	 */
	private boolean blockOnValidationErrors = false;

	/**
	 * Used to schedule only one updateButtons
	 * while the user is typing in new data like wild
	 */
	private Runnable scheduledUpdateRunnable = null;
	/**
	 * Performs an updateButtons()
	 */
	private Runnable updateButtonsRunnable = new Runnable() {
		public void run() {
			synchronized (FieldBasedEditorCfModLayoutConfigWizardPage.this) {
				getContainer().updateButtons();
				scheduledUpdateRunnable = null;
			}
		}
	}; 
	
	/**
	 * Constructs a new {@link FieldBasedEditorCfModLayoutConfigWizardPage}.
	 * @param pageName The name (id) of the new page.
	 * @param blockOnValidationErrors Defines whether the new page should block on validation errors, i.e. return <code>false</code> on isPageComplete.
	 */
	public FieldBasedEditorCfModLayoutConfigWizardPage(String pageName, boolean blockOnValidationErrors) {
		super(pageName);
		init(blockOnValidationErrors);
	}

	/**
	 * Constructs a new {@link FieldBasedEditorCfModLayoutConfigWizardPage} with the given title.
	 * @param pageName The name (id) of the new page.
	 * @param title The title of the new page.
	 * @param blockOnValidationErrors Defines whether the new page should block on validation errors, i.e. return <code>false</code> on isPageComplete.
	 */
	public FieldBasedEditorCfModLayoutConfigWizardPage(String pageName, String title, boolean blockOnValidationErrors) {
		super(pageName, title);
		init(blockOnValidationErrors);
	}

	/**
	 * Constructs a new {@link FieldBasedEditorCfModLayoutConfigWizardPage} with the given title and title image.
	 * @param pageName The name (id) of the new page.
	 * @param title The title of the new page.
	 * @param titleImage The title image of the new page.
	 * @param blockOnValidationErrors Defines whether the new page should block on validation errors, i.e. return <code>false</code> on isPageComplete.
	 */
	public FieldBasedEditorCfModLayoutConfigWizardPage(String pageName, String title, ImageDescriptor titleImage, boolean blockOnValidationErrors) {
		super(pageName, title, titleImage);
		init(blockOnValidationErrors);
	}
	
	/**
	 * Used internally, creates the editor.
	 * @param blockOnValidationErrors Defines whether the new page should block on validation errors, i.e. return <code>false</code> on isPageComplete. 
	 */
	private void init(boolean blockOnValidationErrors) {
		editor = createFieldBasedEditor();
		this.blockOnValidationErrors = blockOnValidationErrors;
	}

	/**
	 * Creates the {@link FieldBasedEditorCfModLayoutConfig} this page uses.
	 * @return A new instance of {@link FieldBasedEditorCfModLayoutConfig} that this page uses.
	 */
	protected FieldBasedEditorCfModLayoutConfig createFieldBasedEditor() {
		return new FieldBasedEditorCfModLayoutConfig(true);
	}
	
	/**
	 * Set the {@link PropertySetFieldBasedEditLayoutConfigModule} for the editor of this page,
	 * where the editor can read its ui layout and the fields to display from.
	 * Note, that this has to be set before {@link #createPageContents(Composite)} is invoked.
	 * 
	 * @param configModule The config module to set.
	 */
	public void setLayoutConfigModule(PropertySetFieldBasedEditLayoutConfigModule configModule) {
		editor.setConfigModule(configModule);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.wizard.DynamicPathWizardPage#createPageContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createPageContents(Composite parent) {
		Control c = editor.createControl(parent, true);
		editor.addDataFieldEditorChangedListener(new DataFieldEditorChangedListener() {
			@Override
			public void dataFieldEditorChanged(DataFieldEditorChangedEvent dataFieldEditorChangedEvent) {
				editor.updatePropertySet();
			}
		});
		
		IValidationResultHandler resultManager = new ValidationResultHandler() {
			@Override
			public void handleValidationResult(ValidationResult validationResult) {
				if (validationResult == null) {
					setMessage(null);
					hasValidationError = false;
				} else {
					if (!pristine)
						setMessage(validationResult.getMessage(), ValidationUtil.getIMessageProviderType(validationResult.getType()));
					hasValidationError = validationResult.getType() == ValidationResultType.ERROR;
				}
				scheduleUpdateButtons();
			}
		};
		// Register a listener, that sets pristine to false and then immediately deregisteres itself again
		final DataFieldEditorChangedListener[] listener = new DataFieldEditorChangedListener[1]; 
		listener[0] = new DataFieldEditorChangedListener() {
			@Override
			public void dataFieldEditorChanged(DataFieldEditorChangedEvent dataFieldEditorChangedEvent) {
				pristine = false;
				getEditor().removeDataFieldEditorChangedListener(listener[0]);
			}
		};
		editor.addDataFieldEditorChangedListener(listener[0]);
		editor.setValidationResultHandler(resultManager);
		
		setControl(c);
		return c;
	}

	private void scheduleUpdateButtons() {
		synchronized (this) {
			if (scheduledUpdateRunnable == null) {
				scheduledUpdateRunnable = updateButtonsRunnable;
				getControl().getDisplay().asyncExec(updateButtonsRunnable);
			}
		}
	}
	
	/**
	 * The {@link PropertySetEditor} created for this page.
	 * This is accessible after the constructor.
	 * 
	 * @return The {@link PropertySetEditor} created for this page. 
	 */
	public FieldBasedEditorCfModLayoutConfig getEditor() {
		return editor;
	}
	
	@Override
	public void onShow() {
		editor.refreshControl();
		editor.validate();
		super.onShow();
	}
	
	@Override
	public void onHide() {
		editor.updatePropertySet();
		super.onHide();
	}
	
	@Override
	public boolean isPageComplete() {
		if (blockOnValidationErrors)
			return !hasValidationError();
		return true;
	}
	
	private boolean hasValidationError() {
		if (editor.getPropertySet() != null) {
			return editor.getPropertySet().hasValidationError(editor.getPropertySet().getStructure());
		}
		return false;
	}
}

