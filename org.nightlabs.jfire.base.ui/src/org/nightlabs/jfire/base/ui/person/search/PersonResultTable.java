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

package org.nightlabs.jfire.base.ui.person.search;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.base.ui.prop.DefaultPropertySetTableConfig;
import org.nightlabs.jfire.base.ui.prop.IPropertySetTableConfig;
import org.nightlabs.jfire.base.ui.prop.PropertySetTable;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * Table Composite that displays {@link StructField} values
 * for a {@link Person}.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class PersonResultTable extends PropertySetTable<Person> {

	public PersonResultTable(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Creates a new instance of the PersonResultTable with an option for viewerStyle.
	 */
	public PersonResultTable(Composite parent, int style, int viewerStyle) {
		super(parent, style, viewerStyle);
	}

	@Override
	protected IPropertySetTableConfig getPropertySetTableConfig() {
		return new PersonResultTableConfig();
	}

	class PersonResultTableConfig extends DefaultPropertySetTableConfig {
		@Override
		public IStruct getIStruct() {
			return StructLocalDAO.sharedInstance().getStructLocal(
					StructLocalID.create(
							Organisation.DEV_ORGANISATION_ID,
							Person.class, Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE
					),
					new NullProgressMonitor()
			);
		}
		@Override
		public StructFieldID[] getStructFieldIDs() {
			return new StructFieldID[] {
					PersonStruct.PERSONALDATA_COMPANY, PersonStruct.PERSONALDATA_NAME, PersonStruct.PERSONALDATA_FIRSTNAME,
					PersonStruct.POSTADDRESS_CITY, PersonStruct.POSTADDRESS_ADDRESS, PersonStruct.POSTADDRESS_POSTCODE, PersonStruct.PHONE_PRIMARY, PersonStruct.INTERNET_EMAIL
				};
		}
		@Override
		public List<StructFieldID[]> getStructFieldIDsList() {
			List<StructFieldID[]> l = new ArrayList<StructFieldID[]>();
			l.add(new StructFieldID[]{PersonStruct.PERSONALDATA_COMPANY});
			l.add(new StructFieldID[]{PersonStruct.PERSONALDATA_NAME, PersonStruct.PERSONALDATA_FIRSTNAME});
			l.add(new StructFieldID[]{PersonStruct.POSTADDRESS_CITY});
			l.add(new StructFieldID[]{PersonStruct.POSTADDRESS_ADDRESS, PersonStruct.POSTADDRESS_POSTCODE});
			l.add(new StructFieldID[]{PersonStruct.PHONE_PRIMARY});
			l.add(new StructFieldID[]{PersonStruct.INTERNET_EMAIL});
			return l;
		}
	}
	
	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		super.setTableProvider(tableViewer);
		tableViewer.setComparator(new ViewerComparator() {
			private Collator collator = Collator.getInstance();

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				String s1 = ((PropertySet)e1).getDisplayName();
				String s2 = ((PropertySet)e2).getDisplayName();
				return collator.compare(s1, s2);
			}
		});				
	}
}
