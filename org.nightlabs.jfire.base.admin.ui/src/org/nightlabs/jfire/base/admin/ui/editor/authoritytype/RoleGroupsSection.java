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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 ******************************************************************************/
package org.nightlabs.jfire.base.admin.ui.editor.authoritytype;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.editor.RestorableSectionPart;
import org.nightlabs.base.ui.entity.editor.EntityEditorUtil;
import org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupSecurityPreferencesModel;
import org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupTableViewer;
import org.nightlabs.jfire.base.admin.ui.editor.user.UserUtil;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.RoleGroup;

/**
 * The section containing the role groups controls.
 * 
 * @version $Revision: 10603 $ - $Date: 2008-05-23 18:14:11 +0000 (Fri, 23 May 2008) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class RoleGroupsSection extends RestorableSectionPart
{
	/**
	 * The editor model.
	 */
	RoleGroupSecurityPreferencesModel model;
	
	RoleGroupTableViewer roleGroupTableViewer;

	/**
	 * Create an instance of RoleGroupsSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public RoleGroupsSection(FormPage page, Composite parent)
	{
		super(parent, page.getEditor().getToolkit(), ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED | ExpandableComposite.TWISTIE);
		createClient(getSection(), page.getEditor().getToolkit());
	}

	/**
	 * Create the content for this section.
	 * @param section The section to fill
	 * @param toolkit The toolkit to use
	 */
	protected void createClient(Section section, FormToolkit toolkit)
	{
		section.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authoritytype.RoleGroupsSection.section.text")); //$NON-NLS-1$
		section.setExpanded(true);
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		createDescriptionControl(section, toolkit);

		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 1);

		ViewerComparator roleGroupComparator = new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((RoleGroup)e1).getName().getText().compareTo(((RoleGroup)e2).getName().getText());
			}
		};
		
//		section.setExpanded(true);
		
		Table fTable = toolkit.createTable(container, SWT.MULTI | SWT.FULL_SELECTION);
		toolkit.paintBordersFor(fTable);
		roleGroupTableViewer = new RoleGroupTableViewer(fTable, UserUtil.getSectionDirtyStateManager(this), false, false);
		roleGroupTableViewer.setComparator(roleGroupComparator);
	}

	private void createDescriptionControl(Section section, FormToolkit toolkit)
	{
		FormText text = toolkit.createFormText(section, true);
		text.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authoritytype.RoleGroupsSection.text.text"), false, false); //$NON-NLS-1$
		section.setDescriptionControl(text);
	}
	
	public void setRoleGroups(java.util.Set<RoleGroup> roleGroups) {
		this.model = new RoleGroupSecurityPreferencesModel();
		this.model.setAllRoleGroupsInAuthority(roleGroups);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				roleGroupTableViewer.setModel(model);
			}
		});
	}
}
