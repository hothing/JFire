package org.nightlabs.jfire.base.ui.person.edit.blockbased;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormPage;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.BlockBasedEditor;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.BlockBasedEditorSection;

public class PersonBlockBasedEditorSection extends BlockBasedEditorSection {

	public PersonBlockBasedEditorSection(FormPage page, Composite parent, String sectionDescriptionText) {
		super(page, parent, sectionDescriptionText);
	}

	public PersonBlockBasedEditorSection(FormPage page, Composite parent, int sectionType, String sectionDescriptionText) {
		super(page, parent, sectionType, sectionDescriptionText);
	}

	private PersonBlockBasedEditor personBlockBasedEditor;

	@Override
	protected BlockBasedEditor createBlockBasedEditor() {
		personBlockBasedEditor = new PersonBlockBasedEditor();
		return personBlockBasedEditor;
	}

}
