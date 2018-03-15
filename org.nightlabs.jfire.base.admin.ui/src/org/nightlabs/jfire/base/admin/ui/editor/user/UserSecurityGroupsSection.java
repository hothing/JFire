package org.nightlabs.jfire.base.admin.ui.editor.user;

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
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.editor.RestorableSectionPart;
import org.nightlabs.base.ui.entity.editor.EntityEditorUtil;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;

/**
 * The section containing the user groups controls for the {@link PersonPreferencesPage}.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class UserSecurityGroupsSection extends RestorableSectionPart {

	/**
	 * The editor model.
	 */
	UserSecurityPreferencesModel model;

	UserSecurityGroupTableViewer userSecurityGroupTableViewer;

	/**
	 * Create an instance of UserSecurityGroupsSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public UserSecurityGroupsSection(FormPage page, Composite parent)
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
		section.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserSecurityGroupsSection.sectionTitle")); //$NON-NLS-1$
		section.setExpanded(true);
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		createDescriptionControl(section, toolkit);

		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 3);

		userSecurityGroupTableViewer = new UserSecurityGroupTableViewer(createUserSecurityGroupsTable(toolkit, container), UserUtil.getSectionDirtyStateManager(this));
	}

	public void setModel(final UserSecurityPreferencesModel model) {
		this.model = model;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (userSecurityGroupTableViewer != null && !userSecurityGroupTableViewer.getTable().isDisposed())
					userSecurityGroupTableViewer.setModel(model);
			}
		});
	}

	private void createDescriptionControl(Section section, FormToolkit toolkit)
	{
		FormText text = toolkit.createFormText(section, true);
		text.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserSecurityGroupsSection.descriptionText"), true, false); //$NON-NLS-1$
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

	private Table createUserSecurityGroupsTable(FormToolkit toolkit, Composite container)
	{
		Table fTable = toolkit.createTable(container, SWT.MULTI | SWT.FULL_SELECTION | XComposite.getBorderStyle(container));
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		fTable.setLayoutData(gd);
//		TableColumn col1 = new TableColumn(fTable, SWT.NULL);
//		col1.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserSecurityGroupsSection.col0")); //$NON-NLS-1$
//		TableColumn col2 = new TableColumn(fTable, SWT.NULL);
//		col2.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserSecurityGroupsSection.col1")); //$NON-NLS-1$
//		TableLayout tlayout = new TableLayout();
//		tlayout.addColumnData(new ColumnWeightData(30, 30));
//		tlayout.addColumnData(new ColumnWeightData(70, 70));
//		fTable.setLayout(tlayout);
		fTable.setLayout(new WeightedTableLayout(new int[] {-1, 30, 70}, new int[] {20, -1, -1}));
		fTable.setHeaderVisible(true);
		toolkit.paintBordersFor(fTable);
		//createContextMenu(fTable);
		return fTable;
	}
}