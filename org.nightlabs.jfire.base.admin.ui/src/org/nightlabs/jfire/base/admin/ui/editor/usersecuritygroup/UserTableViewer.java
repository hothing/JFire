package org.nightlabs.jfire.base.admin.ui.editor.usersecuritygroup;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.notification.IDirtyStateManager;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.EmulatedNativeCheckBoxTableLabelProvider;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.editor.user.CheckboxEditingSupport;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.User;

public class UserTableViewer extends TableViewer
{
	/**
	 * Label provider for users.
	 */
	private class UserLabelProvider extends EmulatedNativeCheckBoxTableLabelProvider
	{
		public UserLabelProvider(TableViewer viewer) {
			super(viewer);
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex)
		{
			switch(columnIndex) {
//			case 0: return CheckboxCellEditorHelper.getCellEditorImage(model.getIncludedUsers().contains(element), false);
			case 0: return getCheckBoxImage(model.getIncludedUsers().contains(element));
			case 1:return SharedImages.getSharedImage(BaseAdminPlugin.getDefault(), UserLabelProvider.class);
			default: return null;
			}
		}

		public String getColumnText(Object element, int columnIndex)
		{
			if(!(element instanceof User))
				throw new RuntimeException("Invalid object type, expected User"); //$NON-NLS-1$
			User u = (User)element;
			switch(columnIndex) {
				case 1: return u.getName();
				case 2: return u.getDescription();
			}
			return ""; // must never return null - otherwise it causes an error //$NON-NLS-1$
		}

		// this method is used by the ViewerComparator (see below).
		@Override
		public String getText(Object element) {
			return ((User)element).getName();
		}
	}

	private GroupSecurityPreferencesModel model;
	private IDirtyStateManager dirtyStateManager;

	public UserTableViewer(Table table, IDirtyStateManager dirtyStateManager) {
		super(table);

		this.dirtyStateManager = dirtyStateManager;

		// no need for a special implementation - the default implementation works fine with ILabelProvider.getText(...) - see above.
		setComparator(new ViewerComparator());

		// Layout stuff
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		getTable().setLayoutData(gd);

		TableViewerColumn col1 = new TableViewerColumn(this, SWT.CENTER);
		col1.getColumn().setResizable(false);
		col1.getColumn().setText(""); //$NON-NLS-1$
		col1.setEditingSupport(new CheckboxEditingSupport<User>(this) {
			@Override
			protected boolean doGetValue(User element) {
				return model.getIncludedUsers().contains(element);
			}

			@Override
			protected void doSetValue(User element, boolean value) {
				if (value)
					model.addUser(element);
				else
					model.removeUser(element);

				UserTableViewer.this.dirtyStateManager.markDirty();
			}
		});

		TableColumn col2 = new TableColumn(getTable(), SWT.NULL);
		col2.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.usersecuritygroup.UsersSection.user")); //$NON-NLS-1$

		TableLayout tlayout = new WeightedTableLayout(new int[] { -1, 100 }, new int[] { 22, -1 });
		getTable().setLayout(tlayout);
		getTable().setHeaderVisible(true);

		setContentProvider(new ArrayContentProvider());
		setLabelProvider(new UserLabelProvider(this));
	}

	public void setModel(GroupSecurityPreferencesModel model) {
		this.model = model;
		setInput(model.getAvailableUsers());
	}
}
