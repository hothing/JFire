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
package org.nightlabs.jfire.base.admin.ui.editor.workstation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.ui.IEditorInput;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.entity.tree.ActiveJDOEntityTreeCategory;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.dao.WorkstationDAO;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Entity tree category for {@link Workstation}s.
 *
 * @version $Revision: 4838 $ - $Date: 2006-10-31 13:18:46 +0000 (Tue, 31 Oct 2006) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author marco schulze - marco at nightlabs dot de
 */
public class EntityTreeCategoryWorkstation
extends ActiveJDOEntityTreeCategory<WorkstationID, Workstation>
{
	protected class LabelProvider extends TableLabelProvider {
		public String getColumnText(Object o, int columnIndex) {
			// check for string first, so we don't need to be logged in when dsplaying a simple string
			if (o instanceof String) {
				return (String)o;
			} else if (o instanceof Workstation) {
//				return ((Workstation)o).getOrganisationID() + '/' + ((Workstation)o).getWorkstationID();
				return ((Workstation)o).getWorkstationID();
			} else {
				return super.getText(o);
			}
		}
	}

	public IEditorInput createEditorInput(Object o)
	{
		Workstation workstation = (Workstation)o;
		WorkstationID workstationID = WorkstationID.create(workstation.getOrganisationID(), workstation.getWorkstationID());
		return new WorkstationEditorInput(workstationID);
	}

	public ITableLabelProvider createLabelProvider() {
		return new LabelProvider();
	}

	@Override
	protected Class<Workstation> getJDOObjectClass()
	{
		return Workstation.class;
	}

	public static final String[] FETCH_GROUPS_WORKSTATION = {
		FetchPlan.DEFAULT, Workstation.FETCH_GROUP_THIS_WORKSTATION
		};

	@Override
	protected Collection<Workstation> retrieveJDOObjects(Set<WorkstationID> workstationIDs, ProgressMonitor monitor)
	{
		return WorkstationDAO.sharedInstance().getWorkstations(workstationIDs, FETCH_GROUPS_WORKSTATION, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	@Override
	protected Collection<Workstation> retrieveJDOObjects(ProgressMonitor monitor)
	{
		return WorkstationDAO.sharedInstance().getWorkstations(FETCH_GROUPS_WORKSTATION, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	@Override
	protected void sortJDOObjects(List<Workstation> workstations)
	{
		Collections.sort(workstations, new Comparator<Workstation>() {
			public int compare(Workstation o1, Workstation o2)
			{
				int res = o1.getOrganisationID().compareTo(o2.getOrganisationID());
				if (res != 0)
					return res;

				return o1.getWorkstationID().compareTo(o2.getWorkstationID());
			}
		});
	}
}
