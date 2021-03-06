/* *****************************************************************************
 * org.nightlabs.base.ui - NightLabs Eclipse utilities                            *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/
package org.nightlabs.base.ui.editor;

import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.action.IUpdateActionOrContributionItem;
import org.nightlabs.base.ui.action.SelectionAction;

/**
 * A {@link SectionPart} which has a {@link ToolBarManager} and shows its {@link ToolBar} 
 * as text client for the {@link Section}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ToolBarSectionPart
extends MessageSectionPart
{
	private ToolBar toolBar;
	private ToolBarManager toolBarManager;
	private LinkedList<IAction> registeredActions = null;
	
	public ToolBarSectionPart(IFormPage page, Composite parent, int style, String title) {
		this(page.getEditor().getToolkit(), parent, style, title);
	}
	
	public ToolBarSectionPart(FormToolkit toolkit, Composite parent, int style, String title) {
		super(toolkit, parent, style, title);
	}

	/**
	 * Returns the {@link ToolBarManager} where all registered {@link IAction}s
	 * are contained.
	 * <p>
	 * Note, that this method will lazily create a {@link ToolBar} and
	 * a {@link ToolBarManager} for this section if not already done.
	 * </p>
	 * @return the {@link ToolBarManager}.
	 */
	public ToolBarManager getToolBarManager() {
		if (toolBarManager == null) {
			toolBar = new ToolBar(getSection(), SWT.FLAT | SWT.HORIZONTAL);
			toolBarManager = new ToolBarManager(toolBar);
			toolBar.setBackground(getSection().getBackground());
			toolBar.setBackgroundImage(getSection().getBackgroundImage());
			toolBar.setBackgroundMode(SWT.INHERIT_FORCE);
			getSection().setTextClient(toolBar);
		}
		return toolBarManager;
	}
	
	/**
	 * Registers the given action and adds it to the toolbarManager.
	 * <p>
	 * {@link #updateToolBarManager()} will iterate all registered actions
	 * and check whether they implement {@link IUpdateActionOrContributionItem} and
	 * set their enablement state according to their {@link IUpdateActionOrContributionItem#calculateEnabled()} method.
	 * </p>
	 * @param action The action to register.
	 */
	public void registerAction(IAction action) {
		registerAction(action, true);
	}

	/**
	 * Registers the given action.
	 * <p>
	 * {@link #updateToolBarManager()} will iterate all registered actions
	 * and check whether they implement {@link IUpdateActionOrContributionItem} and
	 * set their enablement state according to their {@link IUpdateActionOrContributionItem#calculateEnabled()} method.
	 * </p>
	 * @param action The action to register.
	 * @param addToToolbarManager Whether to add the action to the toolbarManager
	 */
	public synchronized void registerAction(IAction action, boolean addToToolbarManager) {
		if (registeredActions == null)
			registeredActions = new LinkedList<IAction>();
		registeredActions.add(action);
		if (addToToolbarManager)
			getToolBarManager().add(action);
	}

	/**
	 * This should be called after contributing to the ToolBarManager ({@link #getToolBarManager()})
	 * and everytime you want the actions to be updated.
	 * <p>
	 * This iterates all {@link IAction}s registered by {@link #registerAction(IAction, boolean)}
	 * and sets their enablement state according to their {@link IUpdateActionOrContributionItem#calculateEnabled()} method.
	 * </p>
	 */
	public void updateToolBarManager()
	{
		if (registeredActions != null) {
			for (IAction action : new ArrayList<IAction> (registeredActions)) {
				if (action instanceof IUpdateActionOrContributionItem) {
					action.setEnabled(((IUpdateActionOrContributionItem) action).calculateEnabled());
				}
			}
		}
		getToolBarManager().update(true);
	}
	
	private ISelectionProvider selectionProvider;

	/**
	 * @return the selectionProvider
	 */
	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}

	/**
	 * Setting the selection provider triggers an update of the registered {@link SelectionAction}s whenever the selection is changed.
	 * 
	 * @param selectionProvider the selectionProvider to set
	 */
	public void setSelectionProvider(final ISelectionProvider selectionProvider) {
		this.selectionProvider = selectionProvider;
		selectionProvider.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				for (IAction action : new ArrayList<IAction> (registeredActions)) {
					if (action instanceof SelectionAction)
						((SelectionAction) action).setSelection(selectionProvider.getSelection());
				}
				updateToolBarManager();
			}
		});
	}
}
