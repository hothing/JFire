package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.editor.RestorableSectionPart;
import org.nightlabs.base.ui.entity.editor.EntityEditorUtil;
import org.nightlabs.jfire.base.ui.prop.ValidationUtil;
import org.nightlabs.jfire.base.ui.prop.edit.ValidationResultHandler;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class BlockBasedEditorSection extends RestorableSectionPart
{
	/**
	 * The person editor used in this section.
	 */
	private BlockBasedEditor blockBasedEditor;

	/**
	 * The person editor control showed in this section.
	 */
	private Control blockBasedPersonEditorControl;

	/**
	 * Create an instance of UserPropertiesSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public BlockBasedEditorSection(FormPage page, Composite parent, String sectionDescriptionText)
	{
		super(parent, page.getEditor().getToolkit(), ExpandableComposite.EXPANDED | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		createClient(getSection(), page.getEditor().getToolkit(), sectionDescriptionText);
	}

	/**
	 * Create an instance of UserPropertiesSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public BlockBasedEditorSection(FormPage page, Composite parent, int sectionType, String sectionDescriptionText)
	{
		super(parent, page.getEditor().getToolkit(), sectionType);
		createClient(getSection(), page.getEditor().getToolkit(), sectionDescriptionText);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#commit(boolean)
	 */
	@Override
	public void commit(boolean onSave)
	{
		super.commit(onSave);
		blockBasedEditor.updatePropertySet();
	}

	private static String VALIDATION_RESULT_MESSAGE_KEY = "validationResultMessageKey"; //$NON-NLS-1$

	protected BlockBasedEditor createBlockBasedEditor()
	{
		return new BlockBasedEditor(true);
	}

	/**
	 * Create the content for this section.
	 * @param section The section to fill
	 * @param toolkit The toolkit to use
	 */
	protected void createClient(Section section, FormToolkit toolkit, String sectionDescriptionText)
	{
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_BOTH));
		createDescriptionControl(section, toolkit, sectionDescriptionText);
		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 1);

		blockBasedEditor = createBlockBasedEditor();
		blockBasedEditor.setValidationResultHandler(new ValidationResultHandler() {
			/**
			 * Used to cache the validation result because MessageManager
			 * updates UI every time which is quite expensive. Marc
			 */
			private ValidationResult lastValidationResult = null;

			private boolean needUpdate(ValidationResult validationResult)
			{
				if((lastValidationResult == null && validationResult != null) ||
						(lastValidationResult != null && !lastValidationResult.equals(validationResult))) {
					lastValidationResult = validationResult;
					return true;
				}
				return false;
			}

			@Override
			public void handleValidationResult(ValidationResult validationResult) {
				if(!needUpdate(validationResult))
					return;
				IMessageManager messageManager = getManagedForm().getMessageManager();
				if (validationResult == null) {
					messageManager.removeMessage(VALIDATION_RESULT_MESSAGE_KEY);
				} else {
					int type = ValidationUtil.getIMessageProviderType(validationResult.getType());
					messageManager.addMessage(VALIDATION_RESULT_MESSAGE_KEY, validationResult.getMessage(), null, type);
				}
			}
		});
		blockBasedPersonEditorControl = blockBasedEditor.createControl(container, false);
		blockBasedPersonEditorControl.setLayoutData(new GridData(GridData.FILL_BOTH));
		blockBasedEditor.addChangeListener(new DataBlockEditorChangedListener() {
			public void dataBlockEditorChanged(DataBlockEditorChangedEvent changedEvent) {
				markDirty();
			}
		});
		blockBasedEditor.addAdditionalDataChangedListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				markDirty();
			}
		});
	}

	public void setPropertySet(final PropertySet property) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if(property == null)
					return;
				blockBasedEditor.setPropertySet(property, true);
			}
		});
	}

	private void createDescriptionControl(Section section, FormToolkit toolkit, String sectionDescriptionText)
	{
		if (sectionDescriptionText == null || "".equals(sectionDescriptionText)) //$NON-NLS-1$
			return;

		section.setText(sectionDescriptionText);
	}

	public void setAdditionalDataChangeListener(PropertyChangeListener listener) {
		blockBasedEditor.addAdditionalDataChangedListener(listener);
	}
}
