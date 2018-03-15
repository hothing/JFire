/**
 * 
 */
package org.nightlabs.jfire.base.admin.ui.editor.prop;

import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.jfire.base.ui.entity.editor.ActiveEntityEditorPageController;
import org.nightlabs.jfire.prop.IStruct;

/**
 * @author alex
 *
 */
public abstract class AbstractStructEditorPageController<EntityType extends IStruct> extends
		ActiveEntityEditorPageController<EntityType> {

	/**
	 * @param editor
	 */
	public AbstractStructEditorPageController(EntityEditor editor) {
		super(editor);
	}

	/**
	 * @param editor
	 * @param startBackgroundLoading
	 */
	public AbstractStructEditorPageController(EntityEditor editor,
			boolean startBackgroundLoading) {
		super(editor, startBackgroundLoading);
	}

	public void setStruct(IStruct struct) {
		setControllerObject((EntityType) struct);
	}
}
