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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.composite.groupedcontent.GroupedContentComposite;
import org.nightlabs.base.ui.composite.groupedcontent.GroupedContentProvider;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.IValidationResultHandler;
import org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.DisplayNamePart;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.exception.DataBlockGroupNotFoundException;
import org.nightlabs.jfire.prop.validation.ValidationResult;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * {@link StructBlock} based implementation of {@link PropertySetEditor}.
 * It shows an {@link GroupedContentComposite} with the {@link StructBlock}-names as table entries
 * and the appropriate {@link DataBlockEditor} from the {@link DataBlockEditorFactoryRegistry} for each StructBlock.
 * <p>
 * The {@link DataBlockEditor} is actually managed a generic {@link DataBlockGroupEditor} that is
 * used as content for every entry (i.e. every StructBlock).
 * </p>
 *
 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractDataBlockEditor
 * @see org.nightlabs.jfire.base.ui.prop.edit.blockbased.EditorStructBlockRegistry
 * @see org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class BlockBasedEditor extends AbstractBlockBasedEditor {

	public static final String EDITORTYPE_BLOCK_BASED = "block-based"; //$NON-NLS-1$

	/**
	 * One {@link ContentProvider} will be instantiated per {@link StructBlock}
	 * in the {@link IStruct} for the edited {@link PropertySet}.
	 *
	 * It shows the {@link DataBlock}s name as title and creates a {@link DataBlockGroupEditor}
	 * as content.
	 */
	private class ContentProvider implements GroupedContentProvider {
		private DataBlockGroupEditor groupEditor;
		private DataBlockGroup blockGroup;
		private IStruct struct;

		public ContentProvider(DataBlockGroup blockGroup, IStruct struct) {
			this.blockGroup = blockGroup;
			this.struct = struct;
		}
		@Override
		public Image getGroupIcon() {
			return null;
		}
		@Override
		public String getGroupTitle() {
			//return blockGroup.getStructBlock(getPropStructure()).getID();
			return blockGroup.getStructBlock(struct).getName().getText();
		}
		@Override
		public Composite createGroupContent(Composite parent) {
			groupEditor = new DataBlockGroupEditor(struct, blockGroup, parent, validationResultHandler);
			if (changeListenerProxy != null)
				groupEditor.addDataBlockEditorChangedListener(changeListenerProxy);
			if (this.blockGroup != null) {
				refresh(this.blockGroup);
			}
			return groupEditor;
		}
		/**
		 * Called to refresh the underlying {@link DataBlockGroupEditor}.
		 *
		 * @param blockGroup The {@link DataBlockGroup} to refresh.
		 */
		public void refresh(DataBlockGroup blockGroup) {
			if (groupEditor != null) {
				groupEditor.refresh(struct, blockGroup);
			}
			this.blockGroup = blockGroup;
		}
		/**
		 * Called to trigger a {@link DataBlockGroupEditor#updatePropertySet()}
		 * of the underlying {@link DataBlockGroupEditor}.
		 */
		public void updateProp() {
			if (groupEditor != null) {
				groupEditor.updatePropertySet();
			}
		}
		
		/**
		 * @return The {@link DataBlockGroupEditor} created by this provider.
		 */
		public DataBlockGroupEditor getGroupEditor() {
			return groupEditor;
		}
	}

	/**
	 * One instance of this class is held per {@link BlockBasedEditor} and will
	 * be added as {@link DataBlockEditorChangedListener} to eachGroup editor created.
	 * It will forward all notifications to the listeners that have been added
	 * to the {@link BlockBasedEditor} by {@link BlockBasedEditor#addChangeListener(DataBlockEditorChangedListener)}.
	 */
	protected class ChangeListenerProxy implements DataBlockEditorChangedListener {
		private ListenerList changeListeners = new ListenerList();

		public void dataBlockEditorChanged(DataBlockEditorChangedEvent changedEvent) {
			if (!refreshing) {
				DataBlockEditor dataBlockEditor = changedEvent.getDataBlockEditor();
				DataFieldEditor<? extends DataField> dataFieldEditor = changedEvent.getDataFieldEditor();
				Collection<DisplayNamePart> parts = dataBlockEditor.getStruct().getDisplayNameParts();
				StructBlock structBlock = dataBlockEditor.getStruct().getStructBlock(dataBlockEditor.getDataBlock().getDataBlockGroup());
				if (structBlock.getDataBlockValidators().size() > 0) {
					// if there are validators for the block we have to update the propertySet
					// i.e. write the data from the editor to the property set
					dataFieldEditor.updatePropertySet();
					updateDisplayName();
					refreshDisplayNameComp();
				} else {
					for (DisplayNamePart part : parts) {
						if (dataFieldEditor.getStructField().equals(part.getStructField())) {
							dataFieldEditor.updatePropertySet();
							updateDisplayName();
							refreshDisplayNameComp();
							break;
						}
					}
				}

				Object[] listeners = changeListeners.getListeners();
				for (Object listener : listeners) {
					if (listener instanceof DataBlockEditorChangedListener) {
						((DataBlockEditorChangedListener) listener).dataBlockEditorChanged(changedEvent);
					}
				}
			}
		}

		public void addChangeListener(DataBlockEditorChangedListener changeListener) {
			this.changeListeners.add(changeListener);
		}
		public void removeChangeListener(DataBlockEditorChangedListener changeListener) {
			this.changeListeners.remove(changeListener);
		}

	}

	private GroupedContentComposite groupedContentComposite;
	private XComposite displayNameComp;
	private Text displayNameText;
	private Button autogenerateNameCheckbox;
	private boolean showHeaderComposite;

	/**
	 * Will be added to the {@link DataBlockEditor}s that have been created
	 * and serves as proxy that notifies the listeners of this editor.
	 */
	private ChangeListenerProxy changeListenerProxy = new ChangeListenerProxy();
	private ListenerList displayNameChangedListeners = new ListenerList()	;
	/**
	 * Stores the {@link ContentProvider} with the DataBlock-key as key.
	 */
	private Map<String, ContentProvider> groupContentProviders = new HashMap<String, ContentProvider>();
	/**
	 * Used to track whether the editor is currently refreshing.
	 */
	private boolean refreshing = false;

	private IValidationResultHandler validationResultHandler;


	private PropertyChangeSupport propertyChangeSupport;
	
	/**
	 * Creates a new {@link BlockBasedEditor}.
	 * @param showHeaderComposite Indicates whether a header composite should be displayed. In this implementation this will then show a composite to edit the display name settings of the managed property set.
	 */
	public BlockBasedEditor(boolean showHeaderComposite) {
		this(null, showHeaderComposite);
	}

	/**
	 * Creates a new {@link BlockBasedEditor}.
	 * @param propSet The {@link PropertySet} to be managed.
	 * @param showHeaderComposite Indicates whether a header composite should be displayed. In this implementation this will then show a composite to edit the display name settings of the managed property set.
	 */
	public BlockBasedEditor(PropertySet propSet, boolean showHeaderComposite) {
		super(propSet);
		this.showHeaderComposite = showHeaderComposite;
		propertyChangeSupport = new PropertyChangeSupport(this);
	}
	/**
	 * Refreshes the UI-Representation.
	 */
	@Override
	public void refreshControl() {
		Display.getDefault().asyncExec(
			new Runnable() {
				public void run() {
					refreshing = true;
					if (groupedContentComposite == null || groupedContentComposite.isDisposed())
						return;

					refreshDisplayNameComp();

					if (!propertySet.isInflated())
						propertySet.inflate(getStructure(new NullProgressMonitor()));

					// get the ordered dataBlocks
					for (StructBlock structBlock : getOrderedStructBlocks()) {
						DataBlockGroup blockGroup = null;
						try {
							blockGroup = propertySet.getDataBlockGroup(structBlock.getStructBlockIDObj());
						} catch (DataBlockGroupNotFoundException e) {
							throw new IllegalStateException("Could not find DataBlockGroup for " + structBlock.getStructBlockIDObj() + " in PropertySet although inflated just before."); //$NON-NLS-1$ //$NON-NLS-2$
						}
						if (shouldDisplayStructBlock(blockGroup)) {
							ContentProvider contentProvider = groupContentProviders.get(blockGroup.getStructBlockKey());
							if (contentProvider == null) {
								// If we have to create the ContentProvider it will do a refresh when constructed.
								contentProvider = new ContentProvider(blockGroup, propertySet.getStructure());
								groupContentProviders.put(blockGroup.getStructBlockKey(), contentProvider);
								groupedContentComposite.addGroupedContentProvider(contentProvider);
							} else {
								// if the provider for this blockGroup was already
								// created we need to refresh its data.
								contentProvider.refresh(blockGroup);
							}
						} // if (shouldDisplayStructBlock(blockGroup)) {
					}
					groupedContentComposite.layout();
					refreshing = false;
				}
			}
		);
	}
	/**
	 * @return The {@link ChangeListenerProxy} of this {@link BlockBasedEditor}.
	 */
	protected ChangeListenerProxy getChangeListenerProxy() {
		return changeListenerProxy;
	}

	/**
	 * Add the given {@link DataBlockEditorChangedListener} to the list of listeners of this Editor.
	 * @param changeListener The changeListener to add.
	 */
	public void addChangeListener(final DataBlockEditorChangedListener changeListener) {
		changeListenerProxy.addChangeListener(changeListener);
	}

	/**
	 * Remove the given {@link DataBlockEditorChangedListener} from the list of listeners of this Editor.
	 * @param changeListener The changeListener to remove.
	 */
	public void removeChangeListener(final DataBlockEditorChangedListener changeListener) {
		changeListenerProxy.removeChangeListener(changeListener);
	}
	
	protected PropertyChangeSupport getPropertyChangeSupport() {
		return propertyChangeSupport;
	}
	
	/**
	 * Add the given {@link PropertyChangeListener} to the list of listeners of this Editor.
	 * This property change listener will be triggered on a change of the additional data
	 * of a PropertySet which might be the display name of a PropertySet or each other
	 * property introduced by a subclass of PropertySet. 
	 * 
	 * @param listener The listener to add.
	 */
	public void addAdditionalDataChangedListener(PropertyChangeListener listener) {
		getPropertyChangeSupport().addPropertyChangeListener(listener);
	}
	
	/**
	 * Remove the given {@link PropertyChangeListener} from the list of listeners of this Editor.
	 * @param listener The listener to remove.
	 */
	public void removeAdditionalDataChangedListener(PropertyChangeListener listener) {
		getPropertyChangeSupport().addPropertyChangeListener(listener);
	}
	
	/**
	 * Add the given {@link PropertyChangeListener} to the list of listeners of this Editor.
	 * This property change listener will be triggered on a change of the given property
	 * of a PropertySet. 
	 *
	 * @param property The additional property of a PropertySet to listen for changes.
	 * @param listener The listener to add.
	 */
	public void addAdditionalDataChangedListener(String property, PropertyChangeListener listener) {
		getPropertyChangeSupport().addPropertyChangeListener(listener);
	}
	
	/**
	 * Removes the given {@link PropertyChangeListener} from the list of listeners of this Editor
	 * that listen to changes of the given property.
	 *
	 * @param property The additional property of a PropertySet the listener listened to.
	 * @param listener The listener to remove.
	 */
	public void removeAdditionalDataChangedListener(String property, PropertyChangeListener listener) {
		getPropertyChangeSupport().addPropertyChangeListener(listener);
	}
	
	protected void fireDataBlockEditorChangedEvent(DataBlockEditor dataBlockEditor, DataFieldEditor<? extends DataField> dataFieldEditor) {
		changeListenerProxy.dataBlockEditorChanged(new DataBlockEditorChangedEvent(dataBlockEditor, dataFieldEditor));
		if (!refreshing)
			refreshControl();
	}

	protected Composite createHeaderComposite(Composite parent)
	{
		displayNameComp = new XComposite(parent, SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		displayNameComp.getGridLayout().numColumns = 2;

		Label label = new Label(displayNameComp, SWT.NONE);
		label.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.BlockBasedEditor.nameLabel")); //$NON-NLS-1$
		label.setLayoutData(new GridData());
		autogenerateNameCheckbox = new Button(displayNameComp, SWT.CHECK);
		autogenerateNameCheckbox.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.blockbased.BlockBasedEditor.autogenerateCheckboxText")); //$NON-NLS-1$

		autogenerateNameCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateDisplayName();
				refreshDisplayNameComp();
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
//		autogenerateNameCheckbox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		autogenerateNameCheckbox.setLayoutData(new GridData());
		
		displayNameText = new Text(displayNameComp, XComposite.getBorderStyle(displayNameComp));
		displayNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (refreshing)
					return;
				String oldDisplayName = propertySet.getDisplayName();
				propertySet.setDisplayName(displayNameText.getText());
				getPropertyChangeSupport().firePropertyChange(PropertySet.PROP_DISPLAY_NAME, oldDisplayName, displayNameText.getText());
			}
		});
		GridData textGD = new GridData(GridData.FILL_HORIZONTAL);
		textGD.horizontalSpan = 2;
		displayNameText.setLayoutData(textGD);

		return displayNameComp;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor#createControl(org.eclipse.swt.widgets.Composite, boolean)
	 */
	@Override
	public Control createControl(Composite parent, boolean refresh) {
		if (groupedContentComposite == null) {
			if (showHeaderComposite) {
				createHeaderComposite(parent);
			}

			groupedContentComposite = new GroupedContentComposite(parent, SWT.NONE, true);
			groupedContentComposite.setGroupTitle("propTail"); //$NON-NLS-1$
		}
		if (refresh)
			refreshControl();
		return groupedContentComposite;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor#disposeControl()
	 */
	@Override
	public void disposeControl() {
		if (groupedContentComposite != null && !groupedContentComposite.isDisposed())
				groupedContentComposite.dispose();

		if (displayNameComp != null && !displayNameComp.isDisposed())
			displayNameComp.dispose();

		groupedContentComposite = null;
		displayNameComp = null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor#updatePropertySet()
	 */
	@Override
	public void updatePropertySet() {
		for (ContentProvider contentProvider : groupContentProviders.values()) {
			contentProvider.updateProp();
		}
		updateDisplayName();
	}

	/**
	 * Called by the {@link ChangeListenerProxy} when it gets notified of a change in the structure
	 * and will update the display name in the {@link PropertySet} that is currently edited.
	 */
	protected void updateDisplayName() {
		if (displayNameComp != null) {
			String displayName = autogenerateNameCheckbox.getSelection() ? null : displayNameText.getText();
			getPropertySet().setAutoGenerateDisplayName(autogenerateNameCheckbox.getSelection());
			getPropertySet().setDisplayName(displayName);
		}
	}

	/**
	 * Called by the {@link ChangeListenerProxy} when it gets notified of a change in the structure
	 * and will update the Composite that shows the display name and its auto-creation setting.
	 */
	protected void refreshDisplayNameComp() {
		// could be called from refreshControl also.
		boolean wasRefreshing = refreshing;
		if (!wasRefreshing)
			refreshing = true;
		try {
			if (displayNameComp != null) {
				String oldDisplayNameText = displayNameText.getText();
				if (propertySet.getDisplayName() != null)
					displayNameText.setText(propertySet.getDisplayName());
				autogenerateNameCheckbox.setSelection(propertySet.isAutoGenerateDisplayName());
				displayNameText.setEnabled(!autogenerateNameCheckbox.getSelection());
				if (!wasRefreshing) {
					if (!displayNameText.getText().equals(oldDisplayNameText)) {
						getPropertyChangeSupport().firePropertyChange(PropertySet.PROP_DISPLAY_NAME, oldDisplayNameText, displayNameText.getText());
					}
				}
			}
		} finally {
			if (!wasRefreshing)
				refreshing = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor#setValidationResultHandler(org.nightlabs.jfire.base.ui.prop.edit.IValidationResultHandler)
	 */
	@Override
	public void setValidationResultHandler(IValidationResultHandler validationResultHandler) {
		this.validationResultHandler = validationResultHandler;
		validate();
	}

	/**
	 * Get the {@link IValidationResultHandler} that is used by this
	 * {@link BlockBasedEditor} to report validation results to the user.
	 *
	 * @return The {@link IValidationResultHandler} of this {@link BlockBasedEditor} or, <code>null</code> if none is set.
	 */
	public IValidationResultHandler getValidationResultManager() {
		return validationResultHandler;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor#validate()
	 */
	@Override
	public List<ValidationResult> validate() {
		if (getPropertySet() != null && groupedContentComposite != null) {
			IStruct structure = getStructure(new NullProgressMonitor());
			ContentProvider provider = (ContentProvider) groupedContentComposite.getSelectedContentProvider();
			List<StructBlock> blocksOfInterest = Collections.singletonList(provider.getGroupEditor().getDataBlockGroup().getStructBlock(structure));
			List<ValidationResult> validationResults = getPropertySet().validate(structure, null, blocksOfInterest, false);
			if (validationResultHandler != null)
				validationResultHandler.handleValidationResults(validationResults);
			return validationResults;
		} else {
			return null;
		}
	}
}
