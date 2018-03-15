/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
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
 *     http://opensource.org/licenses/lgpl-license.php                         *
 ******************************************************************************/
package org.nightlabs.jfire.base.admin.ui.editor.prop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.ui.IEditorInput;
import org.nightlabs.base.ui.editor.JDOObjectEditorInput;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.base.ui.entity.tree.ActiveJDOEntityTreeCategory;
import org.nightlabs.jfire.base.ui.prop.structedit.StructEditorUtil;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.StructLocalLifecycleListenerFilter;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Entity tree category for {@link StructLocal}s.
 *
 * @version $Revision: 5032 $ - $Date: 2006-11-20 18:46:17 +0100 (Mo, 20 Nov 2006) $
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class EntityTreeCategoryPropertyStructLocal
extends ActiveJDOEntityTreeCategory<StructLocalID, StructLocal>
{
	protected class LabelProvider extends TableLabelProvider {
			public String getColumnText(Object o, int columnIndex) {
				if (o instanceof String) {
					return (String)o;
				} else if(o instanceof StructLocal) {
					StructLocal struct = (StructLocal) o;
					if (struct.getName() == null)
						return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.prop.EntityTreeCategoryPropertyStructLocal.struct.name.noName"); //$NON-NLS-1$
					else
						return struct.getName().getText();
				} else {
					return ""; //$NON-NLS-1$
				}
			}

		}

	public IEditorInput createEditorInput(Object o)
	{
		StructLocalID structLocalID = (StructLocalID) JDOHelper.getObjectId(o);
		return new JDOObjectEditorInput<StructLocalID>(structLocalID);
	}

	public ITableLabelProvider createLabelProvider() {
		return new LabelProvider();
	}

	@Override
	protected Class<StructLocal> getJDOObjectClass()
	{
		return StructLocal.class;
	}

	/**
	 * We override the default implementation in order to suppress subclasses
	 * of {@link User} (i.e. <code>UserGroup</code> instances) and to
	 * filter for the correct user-type on the server.
	 */
	@Override
	protected IJDOLifecycleListenerFilter createJDOLifecycleListenerFilter()
	{
		return new StructLocalLifecycleListenerFilter(new JDOLifecycleState[] { JDOLifecycleState.NEW });
	}

	public static final String[] FETCH_GROUPS_STRUCT_LOCAL = {
		FetchPlan.DEFAULT,
		IStruct.FETCH_GROUP_ISTRUCT_FULL_DATA,
		IExpression.FETCH_GROUP_IEXPRESSION_FULL_DATA
	};

	@Override
	protected Collection<StructLocal> retrieveJDOObjects(Set<StructLocalID> structLocalIDs, ProgressMonitor monitor)
	{
		List<StructLocal> structLocals = new ArrayList<StructLocal>(structLocalIDs.size());
		for (StructLocalID structLocalID : structLocalIDs) {
			structLocals.add(StructLocalDAO.sharedInstance().getStructLocal(structLocalID, FETCH_GROUPS_STRUCT_LOCAL, monitor));
		}
		return structLocals;
	}

	@Override
	protected Collection<StructLocal> retrieveJDOObjects(ProgressMonitor monitor)
	{
		Collection<StructLocalID> structLocalIDs = StructEditorUtil.getAvailableStructLocalIDs();
		return retrieveJDOObjects(new HashSet<StructLocalID>(structLocalIDs), monitor);
	}

	@Override
	protected void sortJDOObjects(List<StructLocal> structLocals)
	{
		// TODO: Implement sorting.
	}
}
