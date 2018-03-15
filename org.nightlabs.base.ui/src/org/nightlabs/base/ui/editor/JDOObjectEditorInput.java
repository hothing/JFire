/* *****************************************************************************
 * org.nightlabs.base.ui - NightLabs Eclipse utilities                            *
 * Copyright (C) 2004-2006 NightLabs - http://NightLabs.org                    *
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
 ******************************************************************************/
package org.nightlabs.base.ui.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.nightlabs.jdo.ObjectID;

/**
 * A generic base class for editor inputs using a
 * JDO Object ID.
 *
 * @version $Revision: 14483 $ - $Date: 2009-05-08 15:22:52 +0200 (Fr, 08 Mai 2009) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JDOObjectEditorInput<ID extends ObjectID> implements IEditorInput
{
	/**
	 * The JDO Object ID.
	 */
	ID jdoObjectID;

	long secondaryId;

	String name;

	String toolTipText;

	/**
	 * Create an instance of this editor input for a
	 * JDO Object ID.
	 * @param jdoObjectID The JDO Object ID
	 */
	public JDOObjectEditorInput(ID jdoObjectID)
	{
		this(jdoObjectID, false);
	}

	/**
	 * Create an instance of this editor input for a
	 * JDO Object ID.
	 * @param jdoObjectID The JDO Object ID
	 */
	public JDOObjectEditorInput(ID jdoObjectID, boolean createUniqueInput)
	{
		assert jdoObjectID != null;
		this.jdoObjectID = jdoObjectID;
		if (createUniqueInput)
			secondaryId = System.currentTimeMillis();
		else
			secondaryId = 0L;
	}

	/**
	 * Get the JDO Object ID
	 * @return The JDO Object ID
	 */
	public ID getJDOObjectID()
	{
		return jdoObjectID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName()
	{
		return name == null ? "" : name; //$NON-NLS-1$
	}

	/**
	 * Set the name.
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText()
	{
		return toolTipText == null ? getName() : toolTipText;
	}

	/**
	 * Set the tooltipText.
	 * @param tooltipText the tooltipText to set
	 */
	public void setTooltipText(String tooltipText)
	{
		this.toolTipText = tooltipText;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class arg0)
	{
		return null;
	}

	/**
	 * Check for equality using the JDO Object ID
	 * equals method.
	 * @param other The object to check.
	 */
	@Override
	public boolean equals(Object other)
	{
		return
				other != null &&
				other instanceof JDOObjectEditorInput &&
				((JDOObjectEditorInput)other).jdoObjectID.equals(this.jdoObjectID) &&
				((JDOObjectEditorInput)other).secondaryId == this.secondaryId;
	}
}
