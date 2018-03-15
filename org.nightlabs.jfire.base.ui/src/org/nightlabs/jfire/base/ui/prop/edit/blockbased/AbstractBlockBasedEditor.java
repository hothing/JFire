/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
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
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import java.util.ArrayList;
import java.util.List;

import org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Base class for the block based implementation of {@link PropertySetEditor}s.
 * It manages (holds) the {@link PropertySet} to edit and the StructBlocks
 * that should be visible when editing the {@link PropertySet}.
 *
 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractDataBlockEditor
 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.EditorStructBlockRegistry
 * @see org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractBlockBasedEditor implements PropertySetEditor { // extends ScrolledComposite {

	protected PropertySet propertySet;
	/**
	 * The registry that manages/registers {@link StructBlock}s edited in
	 * a domain. This will either be assigned in the constructor when a
	 * {@link PropertySet} is passed. Or in {@link #setEditorDomain(String, EditorStructBlockRegistry)}.
	 */
	protected EditorStructBlockRegistry structBlockRegistry;
	/**
	 * The struct-blocks to edit in the domain of this editor.
	 * If this member is set, no {@link StructBlock}s that
	 * are not registered for the editor domain will be shown.
	 */
	private List<StructBlockID> domainPropStructBlocks;
	/**
	 * The name of the editor under which StructBlocks will be registered
	 * in the {@link EditorStructBlockRegistry}. This will be set
	 * in {@link #setEditorDomain(String, EditorStructBlockRegistry)}.
	 */
	private String editorName;

	/**
	 * Create a new {@link AbstractBlockBasedEditor}.
	 */
	public AbstractBlockBasedEditor() {
		this(null);
	}

	/**
	 * Create a new {@link AbstractBlockBasedEditor} for the given
	 * {@link PropertySet}. Note that a <code>null</code> value can
	 * be passed here and that the {@link PropertySet} can be reset
	 * by calling {@link #setPropertySet(PropertySet)}
	 *
	 * @param propertySet The propertySet to edit.
	 */
	public AbstractBlockBasedEditor(PropertySet propertySet) {
		this.propertySet = propertySet;
//		if (this.propertySet != null && this.propertySet.isInflated()) {
//			IStruct struct = this.propertySet.getStructure();
//			String localScope = StructLocal.DEFAULT_SCOPE;
//			if (struct instanceof StructLocal)
//				localScope = ((StructLocal) struct).getStructLocalScope();
//			structBlockRegistry = new EditorStructBlockRegistry(struct.getOrganisationID(), struct.getLinkClass(), struct.getStructScope(), localScope);
//		}
		if (propertySet != null)
			structBlockRegistry = new EditorStructBlockRegistry(propertySet.getStructLocalObjectID());
	}

	/**
	 * Sets the current propertySet of this editor.
	 * If refresh is true {@link #refreshForm(DataBlockEditorChangedListener)}
	 * is called.
	 *
	 * @param propertySet The {@link PropertySet} to edit.
	 * @param refresh Whether to refresh the editor.
	 */
	public void setPropertySet(PropertySet propSet, boolean refresh) {
		this.propertySet = propSet;
		if (refresh)
			refreshControl();
	}

	/**
	 * Will only set the propertySet, no changes to the UI will be made.
	 *
	 * @param propertySet The {@link PropertySet} to edit.
	 */
	@Override
	public void setPropertySet(PropertySet propSet) {
		setPropertySet(propSet, false);
	}
	/**
	 * Returns the {@link PropertySet} associated to this editor.
	 * @return The {@link PropertySet} associated to this editor.
	 */
	public PropertySet getPropertySet() {
		return propertySet;
	}

	/**
	 * Returns the {@link IStruct} the current {@link PropertySet}
	 * was built with. If the current {@link PropertySet} was
	 * already inflated, its structure will be returned otherwise
	 * the {@link StructLocal} the current {@link PropertySet} references
	 * will be queried using the {@link StructLocalDAO}.
	 *
	 * @return The {@link IStruct} the current {@link PropertySet} was built with.
	 */
	protected IStruct getStructure(ProgressMonitor monitor) {
		if (propertySet.isInflated())
			return propertySet.getStructure();
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractBlockBasedEditor.getPropStructure.monitor.taskName"), 1); //$NON-NLS-1$
		IStruct structure = StructLocalDAO.sharedInstance().getStructLocal(
				propertySet.getStructLocalObjectID(), monitor
		);
		monitor.worked(1);
		return structure;
	}


	/**
	 * Refreshes the UI-Representation of the given {@link PropertySet}.
	 */
	public abstract void refreshControl();


	/**
	 * Sets the editor domain for this editor and registers the structBlocks that should be
	 * displayed in this editor according to the given registry.
	 *
	 * @param editorName The name of the editor used to find StructBlockIDs int the given registry.
	 * @param structBlockRegistry The registry to find the StructBlockIDs of the blocks that should be displayed.
	 */
	public void setEditorDomain(String editorName, EditorStructBlockRegistry structBlockRegistry) {
		this.editorName = editorName;
		this.structBlockRegistry = structBlockRegistry;
		// TODO shouldn't we check for compatibility with the propertySet (the Struct[Local] might be different)?!
		buildDomainDataBlockGroups();
	}

	/**
	 * Check whether the given {@link DataBlockGroup} should be displayed according
	 * to the current editor domain or the {@link StructBlockID}s set with {@link #setEditorStructBlockList(List)}.
	 *
	 * @param blockGroup The {@link DataBlockGroup} to check.
	 * @return Whether the given {@link DataBlockGroup} should be displayed.
	 */
	protected boolean shouldDisplayStructBlock(DataBlockGroup blockGroup) {
		// default is all PropStructBlocks
		if (domainPropStructBlocks == null)
			return true;
		else
			return domainPropStructBlocks.contains(
					StructBlockID.create(blockGroup.getStructBlockOrganisationID(), blockGroup.getStructBlockID()));
	}

	protected void buildDomainDataBlockGroups() {
		if (domainPropStructBlocks == null) {
			if (editorName != null && structBlockRegistry != null) {
				List<StructBlockID> structBlockList = structBlockRegistry.getEditorStructBlocks(editorName);
				if (!structBlockList.isEmpty())
					domainPropStructBlocks = structBlockList;
			}
		}
	}

	/**
	 * Shortcut to set the list of PropStructBlocks this editor should display.
	 * After this was set to a non null value this editor
	 * will not care about registrations in {@link EditorStructBlockRegistry}.
	 *
	 * @param structBlockList The list of {@link StructBlockID}s of the blocks that should be displayed.
	 */
	public void setEditorStructBlockList(List<StructBlockID> structBlockIDs) {
		if (structBlockIDs != null && structBlockIDs.size() > 0)
		{
			domainPropStructBlocks = structBlockIDs;
		}
		else
		{
			domainPropStructBlocks = null;
		}
	}

	/**
	 * Returns all {@link StructBlock}s the {@link IStruct} of the current {@link PropertySet}
	 * as ordered list. Use this method to build and {@link #shouldDisplayStructBlock(DataBlockGroup)}
	 * to check for the struct blocks of the current {@link PropertySet}.
	 *
	 * @return A list of all {@link StructBlock}s for the current {@link PropertySet}.
	 */
	protected List<StructBlock> getOrderedStructBlocks() {
		//return AbstractPropStructOrderConfigModule.sharedInstance().structBlockDisplayOrder();
		buildDomainDataBlockGroups();
		if (propertySet == null)
			throw new IllegalStateException("Do not call this method prior to setPropertySet()"); //$NON-NLS-1$
		if (!propertySet.isInflated())
			throw new IllegalStateException("The current PropertySet was not inflated yet, make sure it is inflated before you call this method.!"); //$NON-NLS-1$
		IStruct struct = propertySet.getStructure();
		return new ArrayList<StructBlock>(struct.getStructBlocks());
	}
}
