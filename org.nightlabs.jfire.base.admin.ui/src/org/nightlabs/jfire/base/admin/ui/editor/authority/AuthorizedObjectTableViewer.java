package org.nightlabs.jfire.base.admin.ui.editor.authority;

import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.notification.IDirtyStateManager;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.EmulatedNativeCheckBoxTableLabelProvider;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.editor.user.CheckboxEditingSupport;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.AuthorizedObject;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.UserSecurityGroup;

public class AuthorizedObjectTableViewer extends TableViewer
{
	/**
	 * Label provider for authorizedObjects.
	 */
	private class AuthorizedObjectLabelProvider extends EmulatedNativeCheckBoxTableLabelProvider
	{
		public AuthorizedObjectLabelProvider(TableViewer viewer) {
			super(viewer);
		}

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		@Override
		public Image getColumnImage(Object element, int columnIndex)
		{
			Map.Entry<AuthorizedObject, Boolean> me = (Map.Entry<AuthorizedObject, Boolean>)element;
			switch(columnIndex) {
				case 0: return getCheckBoxImage(me.getValue());
				case 1:
					AuthorizedObject authorizedObject = me.getKey();
					if (authorizedObject instanceof UserSecurityGroup) {
						return SharedImages.getSharedImage(BaseAdminPlugin.getDefault(),
								AuthorizedObjectLabelProvider.class, "UserSecurityGroup");	//$NON-NLS-1$
					}
					else if (authorizedObject instanceof UserLocal) {
						return SharedImages.getSharedImage(BaseAdminPlugin.getDefault(),
								AuthorizedObjectLabelProvider.class);	//$NON-NLS-1$
					}
				default: return null;
			}
		}

		@SuppressWarnings("unchecked") //$NON-NLS-1$
		public String getColumnText(Object element, int columnIndex)
		{
			Map.Entry<AuthorizedObject, Boolean> me = (Map.Entry<AuthorizedObject, Boolean>)element;
			switch(columnIndex) {
				case 1: return me.getKey().getName();
				case 2: return me.getKey().getDescription();
			}
			return ""; //$NON-NLS-1$
		}

		// This method is used by the ViewerComparator (see below).
		@SuppressWarnings("unchecked") //$NON-NLS-1$
		@Override
		public String getText(Object element) {
			return ((Map.Entry<AuthorizedObject, Boolean>)element).getKey().getName();
		}
	}

	private IDirtyStateManager dirtyStateManager;

	public AuthorizedObjectTableViewer(Composite parent, IDirtyStateManager dirtyStateManager, int tableStyle) {
		super(parent, tableStyle);

		this.dirtyStateManager = dirtyStateManager;

		// There is no need for a specific implementation of ViewerComparator, because the default implementation
		// uses the ILabelProvider.getText() method (see above in our LabelProvider).
		setComparator(new ViewerComparator());

		// Layout stuff
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		getTable().setLayoutData(gd);

		TableViewerColumn col1 = new TableViewerColumn(this, SWT.CENTER);
		col1.getColumn().setResizable(false);
		col1.getColumn().setText(""); //$NON-NLS-1$
		col1.setEditingSupport(new CheckboxEditingSupport<Map.Entry<AuthorizedObject, Boolean>>(this) {
			@Override
			protected boolean doGetValue(Map.Entry<AuthorizedObject, Boolean> element) {
				return element.getValue().booleanValue();
			}

			@Override
			protected void doSetValue(Map.Entry<AuthorizedObject, Boolean> element, boolean value) {
				element.setValue(value);
				AuthorizedObjectTableViewer.this.dirtyStateManager.markDirty();
			}
		});

		TableColumn col2 = new TableColumn(getTable(), SWT.NULL);
		col2.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.AuthorizedObjectTableViewer.column.authorizedObject")); //$NON-NLS-1$

		TableLayout tlayout = new WeightedTableLayout(new int[] { -1, 100 }, new int[] { 22, -1 });
		getTable().setLayout(tlayout);
		getTable().setHeaderVisible(true);

		setContentProvider(new ArrayContentProvider());
		setLabelProvider(new AuthorizedObjectLabelProvider(this));
	}
}
