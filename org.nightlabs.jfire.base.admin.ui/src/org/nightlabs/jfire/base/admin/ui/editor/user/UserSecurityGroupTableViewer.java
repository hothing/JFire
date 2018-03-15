package org.nightlabs.jfire.base.admin.ui.editor.user;

import java.util.Collection;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.notification.IDirtyStateManager;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.EmulatedNativeCheckBoxTableLabelProvider;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.UserSecurityGroup;

public class UserSecurityGroupTableViewer extends TableViewer {

	/**
	 * Content provider for user groups.
	 */
	private final class UserSecurityGroupContentProvider extends ArrayContentProvider
	{
		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.table.TableContentProvider#getElements(java.lang.Object)
		 */
		@SuppressWarnings("unchecked") //$NON-NLS-1$
		@Override
		public Object[] getElements(Object inputElement)
		{
			Collection<UserSecurityGroup> userSecurityGroups = (Collection<UserSecurityGroup>)inputElement;
			return userSecurityGroups.toArray();
		}
	}

	/**
	 * Label provider for user groups.
	 */
	private class UserSecurityGroupsLabelProvider extends EmulatedNativeCheckBoxTableLabelProvider
	{
		public UserSecurityGroupsLabelProvider(TableViewer viewer) {
			super(viewer);
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.table.TableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		@Override
		public Image getColumnImage(Object element, int columnIndex)
		{
			if (columnIndex == 0)
				return getCheckBoxImage(model.getUserSecurityGroups().contains(element));
//				return CheckboxCellEditorHelper.getCellEditorImage(model.getUserSecurityGroups().contains(element), false);

			if(columnIndex == 1)
				return SharedImages.getSharedImage(BaseAdminPlugin.getDefault(), UserSecurityGroupsLabelProvider.class);
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		@Override
		public String getColumnText(Object element, int columnIndex)
		{
			UserSecurityGroup g = (UserSecurityGroup)element;
			switch(columnIndex) {
				case 1: return g.getName();
				case 2: return g.getDescription();
			}
			return null;
		}
	}

	private UserSecurityPreferencesModel model;
	private IDirtyStateManager dirtyStateManager;

	public UserSecurityGroupTableViewer(Table table, IDirtyStateManager dirtyStateManager) {
		super(table);

		this.dirtyStateManager = dirtyStateManager;

		ViewerComparator userSecurityGroupComparator = new ViewerComparator() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				UserSecurityGroup u1 = (UserSecurityGroup) e1;
				UserSecurityGroup u2 = (UserSecurityGroup) e2;
				return u1.getName().compareTo(u2.getName());
			}
		};

		setComparator(userSecurityGroupComparator);

		TableViewerColumn col1 = new TableViewerColumn(this, SWT.CENTER);
		col1.getColumn().setText(""); //$NON-NLS-1$
		col1.setEditingSupport(new CheckboxEditingSupport<UserSecurityGroup>(this) {
			/* (non-Javadoc)
			 * @see org.nightlabs.jfire.base.admin.ui.editor.user.CheckboxEditingSupport#doGetValue(java.lang.Object)
			 */
			@Override
			protected boolean doGetValue(UserSecurityGroup element) {
				return model.getUserSecurityGroups().contains(element);
			}

			/* (non-Javadoc)
			 * @see org.nightlabs.jfire.base.admin.ui.editor.user.CheckboxEditingSupport#doSetValue(java.lang.Object, boolean)
			 */
			@Override
			protected void doSetValue(UserSecurityGroup element, boolean value) {
				if (value)
					model.addUserSecurityGroup(element);
				else
					model.removeUserSecurityGroup(element);

				UserSecurityGroupTableViewer.this.dirtyStateManager.markDirty();
			}
		});

		new TableColumn(table, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserSecurityGroupTableViewer.userSecurityGroup")); //$NON-NLS-1$
		new TableColumn(table, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserSecurityGroupTableViewer.description")); //$NON-NLS-1$

		table.setLayout(new WeightedTableLayout(new int[] {-1, 30, 70}, new int[] {22, -1, -1}));

		setContentProvider(new UserSecurityGroupContentProvider());
		setLabelProvider(new UserSecurityGroupsLabelProvider(this));
	}

	public void setModel(UserSecurityPreferencesModel model) {
		this.model = model;
		setInput(model.getAvailableUserSecurityGroups());
	}
}
