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

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * A interface for common methods of all {@link PropertySet} editors, either
 * block or field based.<br/>
 * Common usage is:<br/>
 * <ul>
 * 	<li>Create a the Editor with parameterless constructor or one specific to the type your using.</li>
 *	<li>Link the editor to a PropertySet by using {@link #setPropertySet(PropertySet)</li>
 *	<li>Create the UI representation by calling {@link #createControl(Composite, boolean)}</li>
 *	<li>If not done with {@link #createControl(Composite, boolean)} set the field values by
 * 		calling {@link #refreshControl()}.
 *	</li>
 *  <li> To update the PropertySet with the values of the editor call {@link #updatePropertySet()}</li>
 * </ul>
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public interface PropertySetEditor {
	
	/**
	 * Link the editor to a {@link PropertySet}.
	 * <p>
	 * Note that it is recommended to pass an already inflated {@link PropertySet}
	 * to the editor.
	 * </p>
	 * @param propertySet The PropertySet this editor should modify
	 */
	public void setPropertySet(PropertySet propertySet);
	
	/**
	 * Link the editor to a Property and refresh the Control
	 * if refresh is true.
	 * <p>
	 * Note that it is recommended to pass an already inflated {@link PropertySet}
	 * to the editor.
	 * </p>
	 * @param propertySet The PropertySet this editor should modify
	 * @param refresh Whether to refresh the control with the values of the new PropertySet
	 */
	public void setPropertySet(PropertySet propSet, boolean refresh);
	
	/**
	 * Create the UI representation of the PropertySetEditor and associate
	 * the passed changeListener with the fields. If refresh is true
	 * refresh the UI representation. This should be done synchronously
	 * on the GUI-Thread to avoid InvalidThreadAccessExceptions.
	 * 
	 * @param parent The parent to use.
	 * @param refresh Whether to refresh the control with the PropertySet previously set (if it was set).
	 * @return A newly created control for this editor.
	 */
	public Control createControl(Composite parent, boolean refresh);
	
	/**
	 * Dispose this editors Control.
	 */
	public void disposeControl();
	
	/**
	 * Refresh the UI representation and perform a validation.
	 * Implementors should refresh on the GUI-Thread to avoid
	 * InvalidThreadAccessExceptions.
	 */
	public void refreshControl();
	
	/**
	 * Set the values from the editor to the PropertySet it
	 * is associated with.
	 */
	public void updatePropertySet();
	
	/**
	 * Set the {@link IValidationResultHandler} that should be invoked
	 * with the validation-results when this editor validates its {@link PropertySet}.
	 * <p>
	 * A {@link PropertySetEditor} should perform a validation and report to the given
	 * handler when it is set and a {@link PropertySet} for editing is present.
	 * </p>
	 * <p>
	 * A {@link PropertySetEditor} should validate the {@link PropertySet} it edits whenever
	 * the user changes the data, so calls to {@link #validate()} are usually not
	 * required.
	 * </p>  
	 * @param validationResultHandler The handler to set.
	 */
	public void setValidationResultHandler(IValidationResultHandler validationResultHandler);

	/**
	 * Performs a validation of the currently edited {@link PropertySet} and
	 * invokes the {@link ValidationResultHandler} currently set.
	 * <p>
	 * Note that calls to this method are usually not necessary as a user change or
	 * a call to {@link #refreshControl()} triggers a validation that will report
	 * to the {@link ValidationResultHandler} set.
	 * </p>
	 * 
	 * @return The validation results, might be <code>null</code> when no validation results were found.
	 */
	public List<ValidationResult> validate();
}
