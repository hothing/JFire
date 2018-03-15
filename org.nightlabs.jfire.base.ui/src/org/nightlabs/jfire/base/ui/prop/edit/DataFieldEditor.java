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

package org.nightlabs.jfire.base.ui.prop.edit;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;


/**
 * Implementations of this interface are used to edit the {@link DataField}s
 * stored in a {@link PropertySet}. For each type of data field an own
 * DataFieldEditor should be registered as extension to the point <code>org.nightlabs.jfire.base.ui.propDataFieldEditorFactory</code>.
 * <p>
 * Note, that there exists an abstract base class that should be subclassed rather than
 * implementing this interface directly: {@link AbstractDataFieldEditor}.
 * </p>
 * @author  Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public interface DataFieldEditor<F extends DataField> {

	/**
	 * Set the factory this field editor is associated with.
	 * @param factory The factory to set.
	 */
	void setPropDataFieldEditorFactory(DataFieldEditorFactory<F> factory);
	/**
	 * Get the factory this field editor is associated with.
	 * @return The factory this field editor is associated with.
	 */
	DataFieldEditorFactory<F> getPropDataFieldEditorFactory();

	/**
	 * Here a data field editor should add its
	 * control to a parent Composite.
	 * <p>
	 * The Composite returned should be a singleton
	 * and be updated with data changes.
	 * </p>
	 * No data-display will be made here. See {@link #setData(IStruct, DataField)}.
	 *
	 * @param parent The parent to use.
	 * @return A newly create Control.
	 */
	Control createControl(Composite parent);

	/**
	 * Should return the Control created in {@link #createControl(Composite)}.
	 * @return the Control created in {@link #createControl(Composite)}.
	 */
	Control getControl();
	
	/**
	 * {@link DataFieldEditor}s might return a {@link DataFieldEditorLayoutData} here that is used
	 * to layout their control within its container.
	 *  <p>
	 *  The {@link DataFieldEditorLayoutData} is similar to a SWT {@link GridData}
	 *  and can be used like this.
	 *  </p>
	 * @return The layout data to layout the control of this {@link DataFieldEditor} within its container.
	 */
	DataFieldEditorLayoutData getLayoutData();
	
	/**
	 * Editors should refresh their data during this method.
	 * The composite should be refreshed as well.
	 *
	 * @param struct The structure the given field is part of (used to obtain structure data like the field name etc.)
	 * @param data The {@link DataField} this field editor should modify
	 */
	void setData(IStruct struct, F data);

	/**
	 * Editors should return the currently managed {@link DataField} here.
	 * @return the currently managed {@link DataField} here.
	 */
	F getDataField();

	/**
	 * Editors should return the {@link StructField} that corresponds to the currently managed {@link DataField}.
	 * @return the {@link StructField} that corresponds to the currently managed {@link DataField}.
	 */
	StructField<F> getStructField();
	
	/**
	 * Returns the {@link IStruct} this editor operates on (set in {@link #setData(IStruct, DataField)}).
	 * 
	 * @return The {@link IStruct} this editor operates
	 */
	IStruct getStruct();

	/**
	 * Refresh the control with the data currently set.
	 */
	void refresh();

	/**
	 * Updates the PropertySet, sets the data from the UI control
	 * to the {@link DataField} currently set.
	 * <p>
	 * This method should be threadsafe (access GUI on the GUI-Thread).
	 * </p>
	 */
	void updatePropertySet();

	/**
	 * Add a change listener to this field editor. It will be triggered
	 * when the user changes the field value.
	 * @param listener The listener to add.
	 */
	void addDataFieldEditorChangedListener(DataFieldEditorChangedListener listener);
	/**
	 * Remove the given listener from the list of change listeners.
	 * @param listener The listener to remove.
	 */
	void removeDataFieldEditorChangedListener(DataFieldEditorChangedListener listener);
	/**
	 * Set the changed flag of this field editor.
	 * @param changed The new value of the changed flag.
	 */
	void setChanged(boolean changed);
	/**
	 * Check whether the value of this field editor has changed.
	 * @return Whether the value of this field editor has changed.
	 */
	boolean isChanged();

}
