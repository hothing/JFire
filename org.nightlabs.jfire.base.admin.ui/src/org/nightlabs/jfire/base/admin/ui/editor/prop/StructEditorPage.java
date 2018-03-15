package org.nightlabs.jfire.base.admin.ui.editor.prop;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageControllerModifyEvent;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageWithProgress;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageController;
import org.nightlabs.base.ui.entity.editor.IEntityEditorPageFactory;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.prop.IStruct;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class StructEditorPage
extends EntityEditorPageWithProgress
{
//	private static Logger logger = Logger.getLogger(StructEditorPage.class);
	/**
	 * The Factory is registered to the extension-point and creates
	 * new instances of {@link StructEditorPage}.
	 */
	public static class Factory implements IEntityEditorPageFactory {
		public IFormPage createPage(FormEditor formEditor) {
			return new StructEditorPage(formEditor);
		}

		public IEntityEditorPageController createPageController(EntityEditor editor) {
			return new StructLocalEditorPageController(editor);
		}
	}

	public StructEditorPage(FormEditor editor) {
		super(editor, StructEditorPage.class.getName(), Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.prop.StructEditorPage.pageName")); //$NON-NLS-1$
	}

	private StructEditorSection structEditorSection;
	public StructEditorSection getStructEditorSection() {
		return structEditorSection;
	}

	@Override
	protected void addSections(Composite parent) {
		structEditorSection = new StructEditorSection(this, getStructEditorPageController(), parent);
		getManagedForm().addPart(structEditorSection);
	}

	protected AbstractStructEditorPageController<? extends IStruct> getStructEditorPageController() {
		return (AbstractStructEditorPageController<IStruct>) getPageController();
	}

	@Override
	protected void handleControllerObjectModified(final EntityEditorPageControllerModifyEvent modifyEvent) {
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				IStruct struct = (IStruct) modifyEvent.getNewObject();
				structEditorSection.getStructEditor().setStruct(struct);
				switchToContent();
			}
		});
	}

	@Override
	protected String getPageFormTitle() {
		return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.prop.StructEditorPage.pageTitle"); //$NON-NLS-1$
	}

}
