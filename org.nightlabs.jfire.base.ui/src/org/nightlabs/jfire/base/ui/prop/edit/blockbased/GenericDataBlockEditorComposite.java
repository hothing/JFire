/**
 *
 */
package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorFactoryRegistry;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorLayoutData;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorNotFoundException;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.jfire.prop.id.StructFieldID;

public class GenericDataBlockEditorComposite extends AbstractDataBlockEditorComposite {

	private static final Logger logger = Logger.getLogger(GenericDataBlockEditorComposite.class);

	/**
	 * Assumes to have a parent with GridLayout.
	 * Adds its controls to the parent.
	 *
	 * @param parent Should be a ExpandableDataBlockGroupEditor
	 * @param style SWT-style for the container-GenericDataBlockEditorComposite
	 * @param columnHint A hint for the column count the Editor should use
	 */
	public GenericDataBlockEditorComposite(
			DataBlockEditor editor,
			Composite parent,
			int style,
			int columnHint
	) {
		super(editor, parent, style);
		// set grid data for this
		GridData thisData = new GridData(GridData.FILL_BOTH);
//		thisData.grabExcessHorizontalSpace = true;
		this.setLayoutData(thisData);

		GridLayout thisLayout = new GridLayout();
		thisLayout.numColumns = columnHint;
		thisLayout.makeColumnsEqualWidth = true;
		thisLayout.marginWidth = 0;
		thisLayout.marginHeight = 0;
		setLayout(thisLayout);
		createFieldEditors();
	}


	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractDataBlockEditorComposite#createFieldEditors()
	 */
	public void createFieldEditors() {
		for (StructField<?> structField : getOrderedStructFields()) {
			if (!hasFieldEditorFor(structField.getStructFieldIDObj())) {
				DataFieldEditor<DataField> fieldEditor;
				try {
					fieldEditor = DataFieldEditorFactoryRegistry.sharedInstance().getNewEditorInstance(
							getStruct(), ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE,
							null, getDataBlock().getDataField((StructFieldID) JDOHelper.getObjectId(structField))
					);
				} catch (DataFieldEditorNotFoundException e) {
					// could not find editor for class log the error
					logger.error("Editor not found for one field, continuing", e); //$NON-NLS-1$
					continue;
				} catch (DataFieldNotFoundException e) {
					// could not find data for the given struct field
					logger.error("Could not find DataField for StructField " + JDOHelper.getObjectId(structField), e); //$NON-NLS-1$
					continue;
				}
				addFieldEditor(structField.getStructFieldIDObj(), fieldEditor,true);
				// add the field editor
				fieldEditor.createControl(this);
				fieldEditor.refresh();
				DataFieldEditorLayoutData layoutData = fieldEditor.getLayoutData();
				if (layoutData != null) {
					fieldEditor.getControl().setLayoutData(DataFieldEditorLayoutData.toGridData(layoutData));
				} else {
					fieldEditor.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				}
			}
		}
	}
}