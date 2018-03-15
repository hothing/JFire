package org.nightlabs.jfire.base.admin.ui.editor.prop;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.ui.prop.structedit.StructEditor;
import org.nightlabs.jfire.base.ui.prop.structedit.StructureChangedListener;
import org.nightlabs.jfire.base.ui.prop.structedit.action.AddStructBlockAction;
import org.nightlabs.jfire.base.ui.prop.structedit.action.AddStructFieldAction;
import org.nightlabs.jfire.base.ui.prop.structedit.action.MoveStructElementAction;
import org.nightlabs.jfire.base.ui.prop.structedit.action.RemoveStructElementAction;
import org.nightlabs.jfire.base.ui.prop.structedit.action.TestStructAction;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.IStruct.OrderMoveDirection;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Marius Heinzmann marius[at]NightLabs[dot]de
 */
public class StructEditorSection
extends ToolBarSectionPart
implements StructureChangedListener
{
	private AbstractStructEditorPageController<? extends IStruct> pageController;
	public StructEditorSection(IFormPage page, AbstractStructEditorPageController<? extends IStruct> pageController, Composite parent)
	{
		super(page, parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE,
					Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.prop.StructEditorSection.title")); //$NON-NLS-1$

		structEditor = new StructEditor();

		structEditor.createComposite(getContainer(), SWT.NONE);
		structEditor.addStructureChangedListener(this);
		structEditor.getStructTree().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateActionStatus();
			}
		});

		List<ContributionItem> actionList = new LinkedList<ContributionItem>();
		actionList.add( new ActionContributionItem(new MoveStructElementAction(structEditor, OrderMoveDirection.up, this)) );
		actionList.add( new ActionContributionItem(new MoveStructElementAction(structEditor, OrderMoveDirection.down, this)) );
		actionList.add( new Separator() );
		actionList.add( new ActionContributionItem(new AddStructBlockAction(structEditor)) );
		addStructFieldActionItem = new ActionContributionItem(new AddStructFieldAction(structEditor));
		actionList.add( addStructFieldActionItem );
		removeStructElementAction = new ActionContributionItem(new RemoveStructElementAction(structEditor));
		actionList.add( removeStructElementAction );
		actionList.add( new ActionContributionItem(new TestStructAction(structEditor)) );

		ToolBarManager toolBarManager = getToolBarManager();
		final MenuManager menuManager = new MenuManager("Actions"); //$NON-NLS-1$
		addActionsToContributionManager(toolBarManager, actionList);

		TreeViewer structTreeViewer = structEditor.getStructTree().getTreeViewer();
		menuManager.createContextMenu(structTreeViewer.getControl());
		addActionsToContributionManager(menuManager, actionList);

		updateToolBarManager();
		menuManager.update(true);

		Menu popupMenu = menuManager.getMenu();
		structTreeViewer.getTree().setMenu(popupMenu);
		// TODO if this identifier for registerContextMenu is really meaningful, it should be a constant! And it should be documented! Marco.
		// This is necessary if we want other plugins to be able to extend this menu, but then we need more than only this registration.
		//  -> remove it for now. (Marius)
//		page.getEditorSite().registerContextMenu("StructEditorPage.PropertyActions", menuManager, structTreeViewer); //$NON-NLS-1$
//		((EntityEditorPageWithProgress)page).setMenu(popupMenu);
		this.pageController = pageController;
	}

	private void updateActionStatus() {
		addStructFieldActionItem.getAction().setEnabled(structEditor.canAddStructField());
		removeStructElementAction.getAction().setEnabled(structEditor.canRemoveCurrentElement());
	}

	private void addActionsToContributionManager(IContributionManager contributionManager, List<ContributionItem> actionList)
	{
		if (actionList == null)
			return;

		for (ContributionItem item : actionList) {
			if (item instanceof ActionContributionItem)
//			 add only action, so that the manager wraps it in a new ActionContributionItem.
//			 otherwise this item is used in the toolbar creating Toolbaritem, which is not usable in the MenuManager
				contributionManager.add( ((ActionContributionItem) item).getAction() );
			else
				contributionManager.add( item );
		}
	}

	private StructEditor structEditor;
	private ActionContributionItem removeStructElementAction;
	private ActionContributionItem addStructFieldActionItem;

	public StructEditor getStructEditor() {
		return structEditor;
	}

	public void structureChanged() {
		markDirty();
	}
	
	@Override
	public void commit(boolean onSave) {
		super.commit(onSave);		
		pageController.setStruct(structEditor.getStruct());
	}

}
