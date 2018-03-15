/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.edit.fieldbased;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.clientui.ui.layout.GridLayoutUtil;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedEvent;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorChangedListener;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorFactoryRegistry;
import org.nightlabs.jfire.base.ui.prop.edit.DataFieldEditorNotFoundException;
import org.nightlabs.jfire.base.ui.prop.edit.DisplayNameEditComposite;
import org.nightlabs.jfire.base.ui.prop.edit.IValidationResultHandler;
import org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutConfigModule;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutEntry;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.exception.DataNotFoundException;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.validation.ValidationResult;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.progress.ProgressMonitor;

/**
 * A field-based {@link PropertySetEditor} that configures the fields it displays and 
 * the layout to present them from an {@link PropertySetFieldBasedEditLayoutConfigModule}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public class FieldBasedEditorCfModLayoutConfig implements PropertySetEditor {

	public static final Logger LOGGER = Logger.getLogger(FieldBasedEditorCfModLayoutConfig.class);

	private boolean showDisplayNameEdit;
	private DisplayNameEditComposite displayNameEditComposite;
	
	private PropertySetFieldBasedEditLayoutConfigModule configModule;
	private PropertySet propertySet;
	private Map<StructFieldID, DataFieldEditor<DataField>> fieldEditors = new HashMap<StructFieldID, DataFieldEditor<DataField>>();
	private Composite wrapper;
	private Composite editorWrapper;
	private String editorType = FieldBasedEditor.EDITORTYPE_FIELD_BASED;
	
	private ListenerList fieldEditorChangedListeners = new ListenerList(); 
	
	private IValidationResultHandler validationResultHandler;
	
	private List<StructField<?>> displayedStructFieldIDs = new LinkedList<StructField<?>>();
	
	private PropertyChangeSupport propertyChangeSupport;
	
	/**
	 * Constructs a new {@link FieldBasedEditorCfModLayoutConfig} that reads its configuration
	 * from the given config module.
	 * @param configModule The config module this editor can read its layout from
	 * @param showDisplayNameEdit Whether this editor should show a control where the user can edit the display-name of the property-set 
	 */
	public FieldBasedEditorCfModLayoutConfig(PropertySetFieldBasedEditLayoutConfigModule configModule, boolean showDisplayNameEdit) {
		this.showDisplayNameEdit = showDisplayNameEdit;
		setConfigModule(configModule);
		addDataFieldEditorChangedListener(new DataFieldEditorChangedListener() {

			@Override
			public void dataFieldEditorChanged(DataFieldEditorChangedEvent event) {
				event.getDataFieldEditor().getDataField().validate(event.getDataFieldEditor().getStruct());
			}
			
		});
		propertyChangeSupport = new PropertyChangeSupport(this);
	}
	
	/**
	 * Constructs a new {@link FieldBasedEditorCfModLayoutConfig}.
	 * Note, that in order to use that editor you have to set the config module it can
	 * read is configuration from (see {@link #setConfigModule(PropertySetFieldBasedEditLayoutConfigModule)}).   
	 */
	public FieldBasedEditorCfModLayoutConfig(boolean showDisplayNameEdit) {
		this.showDisplayNameEdit = showDisplayNameEdit;
		propertyChangeSupport = new PropertyChangeSupport(this);
	}
	
	protected PropertyChangeSupport getPropertyChangeSupport() {
		return propertyChangeSupport;
	}
	
	/**
	 * Sets the {@link PropertySetFieldBasedEditLayoutConfigModule} this editor reads its configuration from.
	 * Note, that this has to be set before the editor is used, i.e. its ui is created.
	 * 
	 * @param configModule The config module to set.
	 */
	public void setConfigModule(PropertySetFieldBasedEditLayoutConfigModule configModule) {
		if (configModule == null)
			throw new IllegalArgumentException("Parameter configModule must not be null."); //$NON-NLS-1$
		this.configModule = configModule;
	}
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor#createControl(org.eclipse.swt.widgets.Composite, boolean)
	 */
	@Override
	public Control createControl(Composite parent, boolean refresh) {
		if (editorWrapper == null) {
			wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.NONE);
			
			if (showDisplayNameEdit) {
				createHeaderComposite(wrapper);
			}
			
			editorWrapper = new XComposite(wrapper, SWT.NONE);
			
			GridLayout gridLayout = GridLayoutUtil.createGridLayout(configModule.getGridLayout());
			if (gridLayout == null)
				throw new IllegalStateException("Could not obtain GridLayout from config module."); //$NON-NLS-1$
			
			editorWrapper.setLayout(gridLayout);
		}
		if (refresh)
			refreshControl();
		
		return wrapper;
	}
	
	protected Composite createHeaderComposite(Composite parent) {
		displayNameEditComposite = new DisplayNameEditComposite(parent, SWT.NONE, getPropertyChangeSupport());
		displayNameEditComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// add the listener that will update the display name control
		addDataFieldEditorChangedListener(new DataFieldEditorChangedListener() {
			@Override
			public void dataFieldEditorChanged(
					DataFieldEditorChangedEvent dataFieldEditorChangedEvent) {
				dataFieldEditorChangedEvent.getDataFieldEditor().updatePropertySet();
				displayNameEditComposite.refresh();
				
				validate();
			}
		});
		return displayNameEditComposite;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor#disposeControl()
	 */
	@Override
	public void disposeControl() {
		if (editorWrapper != null) {
			editorWrapper.dispose();
			editorWrapper = null;
		}
	}

	/**
	 * Get the editorType.
	 * @return The editorType.
	 */
	public String getEditorType() {
		return editorType;
	}
	/**
	 * Set the editorType.
	 * Use the static finals.
	 * @param editorType The editorType to set.
	 */
	public void setEditorType(String editorType) {
		this.editorType = editorType;
	}

	private DataField getDataField(StructFieldID structFieldID) {
		try {
			return propertySet.getDataField(structFieldID);
		} catch (DataNotFoundException e) {
			LOGGER.error("Could not find PropDataField for "+structFieldID,e); //$NON-NLS-1$
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor#refreshControl()
	 */
	@Override
	public void refreshControl() {
		Display.getDefault().syncExec(
				new Runnable() {
					public void run() {
						if (propertySet == null)
							return;
						if (configModule == null)
							throw new IllegalStateException("This instance of " + FieldBasedEditorCfModLayoutConfig.this.getClass().getName() + " does not have a config module assigned yet. Use setConfigModule() before creating the UI for this editor."); //$NON-NLS-1$ //$NON-NLS-2$
						
						if (!propertySet.isInflated())
							propertySet.inflate(getPropStructure(new NullProgressMonitor()));
						
						if (fieldEditors.isEmpty()) {
							// the field editors have not been initialized yet, we create them with their ui.
							
							// iterate all struct-fields configured in the config-module.
							for (PropertySetFieldBasedEditLayoutEntry editLayoutEntry : configModule.getEditLayoutEntries()) {

								StructFieldID structFieldID = editLayoutEntry.getStructFieldID();
								if (structFieldID == null) {
									// this is a separator
									Label separator = new Label(editorWrapper, SWT.SEPARATOR | SWT.HORIZONTAL);
									GridData gd = GridLayoutUtil.createGridData(editLayoutEntry.getGridData());
									gd.heightHint = 20;
									separator.setLayoutData(gd);
								} else {

									DataField field = getDataField(structFieldID);

									DataFieldEditor<DataField> editor = null;
									if (!fieldEditors.containsKey(structFieldID)) {
										try {
											editor = DataFieldEditorFactoryRegistry.sharedInstance().getNewEditorInstance(
													propertySet.getStructure(), getEditorType(), null,
													field
											);
										} catch (DataFieldEditorNotFoundException e) {
											LOGGER.error("Could not find DataFieldEditor for "+field.getClass().getName(),e); //$NON-NLS-1$
											continue;
										}
										Control editorControl = editor.createControl(editorWrapper);
										editor.setData(propertySet.getStructure(), field);
										GridData editorGD = GridLayoutUtil.createGridData(editLayoutEntry.getGridData());
										if (editorGD != null)
											editorControl.setLayoutData(editorGD);
										fieldEditors.put(structFieldID, editor);
										addListenersToEditor(editor); // add all listeners already registered to the new editor
										try {
											StructField<? extends DataField> structField = propertySet.getStructure().getStructField(structFieldID);
											displayedStructFieldIDs.add(structField);
										} catch (Exception e) {
										}
									} else {
										editor = fieldEditors.get(structFieldID);
									}
									editor.setData(propertySet.getStructure(), field);
								}
//								editor.refresh();
							}
						} else {
							// the field-editors have been initialized already, only re-set their data 
							for (PropertySetFieldBasedEditLayoutEntry editLayoutEntry : configModule.getEditLayoutEntries()) {
								StructFieldID structFieldID = editLayoutEntry.getStructFieldID();
								if (structFieldID != null) {
									// this is a separator
									DataFieldEditor<DataField> editor = fieldEditors.get(structFieldID);
									editor.setData(propertySet.getStructure(), getDataField(structFieldID));
								}
							}
						}
						
						if (displayNameEditComposite != null) {
							displayNameEditComposite.setPropertySet(getPropertySet(), true);
						}
					}
				}
			);
		}
		
		protected IStruct getPropStructure(ProgressMonitor monitor) {
			if (propertySet.isInflated())
				return propertySet.getStructure();
			monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.ui.prop.edit.fieldbased.FieldBasedEditor.getPropStructure.monitor.taskName"), 1); //$NON-NLS-1$
			IStruct structure = StructLocalDAO.sharedInstance().getStructLocal(propertySet.getStructLocalObjectID(), monitor);
			monitor.worked(1);
			return structure;
		}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor#setPropertySet(org.nightlabs.jfire.prop.PropertySet)
	 */
	@Override
	public void setPropertySet(PropertySet propertySet) {
		setPropertySet(propertySet, false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor#setPropertySet(org.nightlabs.jfire.prop.PropertySet, boolean)
	 */
	@Override
	public void setPropertySet(PropertySet propSet, boolean refresh) {
		this.propertySet = propSet;
		if (!refresh && displayNameEditComposite != null) {
			displayNameEditComposite.setPropertySet(propertySet, false);
		}
		if (refresh) {
			refreshControl();
		}
	}

	/**
	 * Get the {@link PropertySet} currently edited by this editor.
	 * @return The {@link PropertySet} currently edited by this editor.
	 */
	public PropertySet getPropertySet() {
		return propertySet;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.prop.edit.PropertySetEditor#updatePropertySet()
	 */
	@Override
	public void updatePropertySet() {
		for (DataFieldEditor<DataField> fieldEditor : fieldEditors.values()) {
			fieldEditor.updatePropertySet();
		}
		if (displayNameEditComposite != null) {
			displayNameEditComposite.updatePropertySet();
		}
	}

	private void addListenersToEditor(DataFieldEditor<DataField> editor) {
		Object[] listeners = fieldEditorChangedListeners.getListeners();
		for (Object listener : listeners) {
			if (listener instanceof DataFieldEditorChangedListener) {
				editor.addDataFieldEditorChangedListener((DataFieldEditorChangedListener) listener);
			}
		}
	}
	
	// TODO: These need to be in the interface, implementation should also add the listener to new editors
	
	public void addDataFieldEditorChangedListener(DataFieldEditorChangedListener listener) {
		for (DataFieldEditor<DataField> editor : fieldEditors.values()) {
			editor.addDataFieldEditorChangedListener(listener);
		}
		fieldEditorChangedListeners.add(listener);
	}
	
	public void removeDataFieldEditorChangedListener(DataFieldEditorChangedListener listener) {
		for (DataFieldEditor<DataField> editor : fieldEditors.values()) {
			editor.removeDataFieldEditorChangedListener(listener);
		}
		fieldEditorChangedListeners.remove(listener);
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

	@Override
	public void setValidationResultHandler(IValidationResultHandler validationResultHandler) {
		this.validationResultHandler = validationResultHandler;
	}
	
	@Override
	public List<ValidationResult> validate() {
		if (getPropertySet() != null) {
			List<ValidationResult> validationResults = getPropertySet().validate(
					getPropStructure(new NullProgressMonitor()),
					displayedStructFieldIDs, null, false
			);
			if (validationResultHandler != null) {
				validationResultHandler.handleValidationResults(validationResults);
			}
			return validationResults;
		} else {
			return null;
		}
	}
}
