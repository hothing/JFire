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

package org.nightlabs.jfire.base.admin.ui.organisation.registration;

import java.util.Collection;

import javax.jdo.FetchPlan;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.organisation.OrganisationManagerRemote;
import org.nightlabs.jfire.organisation.RegistrationStatus;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class RegistrationTableContentProvider
	implements IStructuredContentProvider
{
	private Object[] registrationStatus;

	public RegistrationTableContentProvider()
	{
		loadData(); // TODO should be done asynchronously!
	}

	public void loadData()
	{
		try {
			OrganisationManagerRemote organisationManager = JFireEjb3Factory.getRemoteBean(OrganisationManagerRemote.class, Login.getLogin().getInitialContextProperties());
			Collection<RegistrationStatus> registrationStatusCollection = organisationManager.getPendingRegistrations(
					new String[]{ FetchPlan.DEFAULT, RegistrationStatus.FETCH_GROUP_USERS },
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
			);

			registrationStatus = registrationStatusCollection.toArray();
		} catch (RuntimeException x) {
			ExceptionHandlerRegistry.asyncHandleException(x);
			throw x;
		} catch (Exception x) {
			ExceptionHandlerRegistry.asyncHandleException(x);
			throw new RuntimeException(x);
		}
	}

	@Override
	public Object[] getElements(Object inputElement)
	{
		if (inputElement != this || registrationStatus == null)
			return new Object[0];

		return registrationStatus;
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}

}
