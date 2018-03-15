/* *****************************************************************************
 * NightLabs Editor2D - Graphical editor framework                             *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 * Project author: Daniel Mazurek <Daniel.Mazurek [at] nightlabs [dot] org>    *
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

package org.nightlabs.editor2d.ui.tools;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.Cursors;
import org.eclipse.gef.Request;
import org.eclipse.swt.graphics.Cursor;
import org.nightlabs.editor2d.ui.edit.AbstractDrawComponentEditPart;
import org.nightlabs.editor2d.ui.request.EditorRotateCenterRequest;


public class RotateCenterTracker
extends AbstractDragTracker
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(RotateCenterTracker.class);
  
  public RotateCenterTracker(AbstractDrawComponentEditPart owner) {
    super(owner);
  }

  protected AbstractDrawComponentEditPart getAbstractDrawComponentEditPart() {
    return (AbstractDrawComponentEditPart) owner;
  }

  @Override
	protected String getCommandName() {
    return REQ_EDIT_ROTATE_CENTER;
  }

  @Override
	protected Cursor getDefaultCursor() {
    return Cursors.CROSS;
  }
  
  @Override
	protected Request createSourceRequest()
  {
    EditorRotateCenterRequest rotateRequest = new EditorRotateCenterRequest();
    rotateRequest.setType(REQ_EDIT_ROTATE_CENTER);
    rotateRequest.setRotationCenter(getLocation());
    rotateRequest.setEditParts(getCurrentViewer().getSelectedEditParts());
    return rotateRequest;
  }
  
  @Override
	protected void updateSourceRequest()
  {
    getEditorRotateCenterRequest().setRotationCenter(getLocation());
    logger.debug("rotationCenter = "+getLocation()); //$NON-NLS-1$
  }
  
  protected EditorRotateCenterRequest getEditorRotateCenterRequest()
  {
    return (EditorRotateCenterRequest) getSourceRequest();
  }

  // Override to avoid the single selection of the EditPart whiches handle has been selected
	@Override
	protected void performSelection()
	{
	}
}
