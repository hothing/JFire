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
package org.nightlabs.jfire.base.admin.ui.editor.usersecuritygroup;

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
import org.nightlabs.jfire.base.admin.ui.editor.user.UserUtil;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;

/**
 * The section containing the users controls.
 *
 * @version $Revision$ - $Date$
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Niklas Schiffler <nick@nightlabs.de>
 */

public class UsersSection extends RestorableSectionPart
{
	UserTableViewer userTableViewer;

//	/**
//	 * The model for the usergroup
//	 */
//	private GroupSecurityPreferencesModel model;

	/**
	 * Create an instance of RoleGroupsSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public UsersSection(FormPage page, Composite parent)
	{
		super(parent, page.getEditor().getToolkit(), ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		createClient(getSection(), page.getEditor().getToolkit());
	}

	/**
	 * Create the content for this section.
	 * @param section The section to fill
	 * @param toolkit The toolkit to use
	 */
	protected void createClient(Section section, FormToolkit toolkit)
	{
		section.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usersecuritygroup.UsersSection.sectionTitle")); //$NON-NLS-1$
		section.setExpanded(true);
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		createDescriptionControl(section, toolkit);

		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 3);

		Table fTable = toolkit.createTable(container, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		toolkit.paintBordersFor(fTable);
		userTableViewer = new UserTableViewer(fTable, UserUtil.getSectionDirtyStateManager(this));
	}

	private void createDescriptionControl(Section section, FormToolkit toolkit)
	{
		FormText text = toolkit.createFormText(section, true);
		text.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usersecuritygroup.UsersSection.description"), false, false); //$NON-NLS-1$
//		text.addHyperlinkListener(new HyperlinkAdapter() {
//			/* (non-Javadoc)
//			 * @see org.eclipse.ui.forms.events.HyperlinkAdapter#linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent)
//			 */
//			@Override
//			public void linkActivated(HyperlinkEvent e)
//			{
//				System.err.println("HYPERLINK EVENT! "+e); //$NON-NLS-1$
//			}
//		});
		section.setDescriptionControl(text);
	}

	public void setModel(final GroupSecurityPreferencesModel model) {
//		this.model = model;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				userTableViewer.setModel(model);
			}
		});
	}
}
