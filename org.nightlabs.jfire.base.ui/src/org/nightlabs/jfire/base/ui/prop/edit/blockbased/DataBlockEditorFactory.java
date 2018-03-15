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

import org.eclipse.core.runtime.IExecutableExtension;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * {@link DataBlockEditorFactory}s are used to register specific types
 * of {@link DataBlockEditor}s to edit their corresponding {@link DataField}.
 * The factory is responsible for creating the field editors and should be
 * registered as extension to the point <code>org.nightlabs.jfire.base.ui.propDataFieldEditorFactory</code>
 *  
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public interface DataBlockEditorFactory extends IExecutableExtension {
	public StructBlockID getProviderStructBlockID();
	public DataBlockEditor createDataBlockEditor(IStruct struct, DataBlock dataBlock);
}
