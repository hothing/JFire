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

package org.nightlabs.editor2d.ui.edit.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertySource;
import org.nightlabs.editor2d.DrawComponent;
import org.nightlabs.editor2d.DrawComponentContainer;
import org.nightlabs.editor2d.RootDrawComponent;
import org.nightlabs.editor2d.ui.model.RootDrawComponentPropertySource;
import org.nightlabs.editor2d.ui.outline.filter.FilterManager;


public class RootDrawComponentTreeEditPart
extends DrawComponentContainerTreeEditPart
{
  /**
   * @param model
   */
  public RootDrawComponentTreeEditPart(RootDrawComponent model, FilterManager filterMan) {
    super(model);
    this.filterMan = filterMan;
  }

  protected FilterManager filterMan;
  public FilterManager getFilterMan() {
  	return filterMan;
  }

  public RootDrawComponent getRootDrawComponent() {
  	return (RootDrawComponent) getModel();
  }

//  public Image getImage() {
//    return null;
//  }
  @Override
	public Image getOutlineImage() {
    return null;
  }

//  protected void createEditPolicies()
//  {
//  	super.createEditPolicies();
//  	installEditPolicy(EditPolicy.CONTAINER_ROLE, new DrawComponentContainerEditPolicy());
//  	installEditPolicy(EditPolicy.TREE_CONTAINER_ROLE, new RootDrawComponentTreeEditPolicy());
//  	installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
//  }

  @SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	protected List getModelChildren()
  {
  	if (getFilterMan().isAllFilterSet()) {
  		return getRootDrawComponent().getDrawComponents();
  	}
  	else {
      List filterChildren = new ArrayList();
      filterChildren = getModelChildren(getRootDrawComponent());
      return filterChildren;
  	}
  }

  protected List<DrawComponent> getModelChildren(DrawComponentContainer dcc)
  {
    List<DrawComponent> filterChildren = new ArrayList<DrawComponent>();
  	for (Iterator<Class<? extends DrawComponent>> itFilter = getFilterMan().getFilters().iterator(); itFilter.hasNext(); )
  	{
  		Class<? extends DrawComponent> filter = itFilter.next();
      for (Iterator<DrawComponent> itChildren = dcc.getDrawComponents().iterator(); itChildren.hasNext(); )
      {
        DrawComponent dc = itChildren.next();
  			if (filter.isAssignableFrom(dc.getClass())) {
  				filterChildren.add(dc);
  			}
  			if (dc instanceof DrawComponentContainer) {
  				DrawComponentContainer childDcc = (DrawComponentContainer) dc;
  				filterChildren.addAll(getModelChildren(childDcc));
  			}
      }
  	}
    return filterChildren;
  }

  @Override
	public IPropertySource getPropertySource()
  {
    if (propertySource == null)
    {
      propertySource =
        new RootDrawComponentPropertySource(getRootDrawComponent());
    }
    return propertySource;
  }
}
