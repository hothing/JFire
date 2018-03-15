package org.nightlabs.jfire.base.admin.ui.editor.user;

import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.notification.IDirtyStateManager;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.table.EmulatedNativeCheckBoxTableLabelProvider;
import org.nightlabs.jfire.base.admin.ui.BaseAdminPlugin;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.util.NLLocale;
public class RoleGroupTableViewer extends TableViewer
{
	private static final Logger logger = Logger.getLogger(RoleGroupTableViewer.class);

	/**
	 * Content provider for role groups.
	 */
	private final class RoleGroupsContentProvider extends ArrayContentProvider
	{
		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.table.TableContentProvider#getElements(java.lang.Object)
		 */
		@SuppressWarnings("unchecked") //$NON-NLS-1$
		@Override
		public Object[] getElements(Object inputElement) {
			Collection<RoleGroup> roleGroups = (Collection<RoleGroup>) inputElement;
			return roleGroups.toArray();
		}
	}

	/**
	 * Label provider for role groups.
	 */
	private class RoleGroupsLabelProvider extends EmulatedNativeCheckBoxTableLabelProvider
	{
		public RoleGroupsLabelProvider(TableViewer viewer) {
			super(viewer);
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.table.TableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			switch (columnIndex) {
			case 1: return showCheckBoxes ? getCheckBoxImage(model.isInAuthority() && model.getRoleGroupsAssignedDirectly().contains(element)) : null;
			case 2:	return SharedImages.getSharedImage(BaseAdminPlugin.getDefault(), RoleGroupsLabelProvider.class);
			default: return null;
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (!(element instanceof RoleGroup))
				throw new RuntimeException("Invalid object type, expected RoleGroup"); //$NON-NLS-1$
			RoleGroup g = (RoleGroup) element;
			switch (columnIndex) {
			case 0:
				if (model.isControlledByOtherUser() && model.getRoleGroupsAssignedToOtherUser().contains(element))
					return "O"; //$NON-NLS-1$
				if (model.isInAuthority() && model.getRoleGroupsAssignedDirectly().contains(element) && model.getRoleGroupsAssignedToUserGroups().contains(element))
					return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupTableViewer.roleGroupSourceDirectAndGroup"); //$NON-NLS-1$
				else if (model.isInAuthority() && model.getRoleGroupsAssignedDirectly().contains(element))
					return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupTableViewer.roleGroupSourceDirect"); //$NON-NLS-1$
				else if (model.getRoleGroupsAssignedToUserGroups().contains(element))
					return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupTableViewer.roleGroupSourceGroup"); //$NON-NLS-1$
				return ""; //$NON-NLS-1$
			case 2:	return g.getName().getText(NLLocale.getDefault().getLanguage());
			case 3:	return g.getDescription().getText(NLLocale.getDefault().getLanguage());
			}
			return null;
		}
	}

	private RoleGroupSecurityPreferencesModel model;
	private IDirtyStateManager dirtyStateManager;

	private boolean showCheckBoxes;

	public RoleGroupTableViewer(Table table, IDirtyStateManager dirtyStateManager, boolean showAssignmentSourceColum, boolean showCheckBoxes)
	{
		super(table);

		this.dirtyStateManager = dirtyStateManager;
		this.showCheckBoxes = showCheckBoxes;

		ViewerComparator roleGroupComparator = new ViewerComparator() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				RoleGroup r1 = (RoleGroup) e1;
				RoleGroup r2 = (RoleGroup) e2;
				return r1.getName().getText().compareTo(r2.getName().getText());
			}
		};

		setComparator(roleGroupComparator);

		// Layout stuff
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		getTable().setLayoutData(gd);

		// Table columns
		new TableColumn(getTable(), SWT.LEFT).setResizable(false); // total availability

		TableViewerColumn col2 = new TableViewerColumn(this, SWT.CENTER);
		col2.getColumn().setResizable(false);
		col2.getColumn().setText(""); //$NON-NLS-1$
		col2.setEditingSupport(checkboxEditingSupport);

		TableColumn col3 = new TableColumn(getTable(), SWT.NULL);
		col3.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupTableViewer.roleGroup")); //$NON-NLS-1$

		TableColumn col4 = new TableColumn(getTable(), SWT.NULL);
		col4.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.RoleGroupTableViewer.description")); //$NON-NLS-1$

		int column1Width = showAssignmentSourceColum ? 30 : 0;
		int column2Witdh = showCheckBoxes ? 22 : 0;

		TableLayout tlayout = new WeightedTableLayout(new int[] { -1, -1, 30, 70 }, new int[] { column1Width, column2Witdh, -1, -1 });
		getTable().setLayout(tlayout);
		getTable().setHeaderVisible(true);

		getTable().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				disposeToolTip();
			}
		});

		getTable().addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseEnter(MouseEvent event) {
//				if (logger.isDebugEnabled())
//					logger.debug("table.MouseTrackListener.mouseEnter");
			}
			@Override
			public void mouseExit(MouseEvent event) {
				if (logger.isDebugEnabled())
					logger.debug("table.MouseTrackListener.mouseExit: clearing tool tip"); //$NON-NLS-1$

				disposeToolTip();
			}
			@Override
			public void mouseHover(MouseEvent event) {
				if (logger.isDebugEnabled())
					logger.debug("table.MouseTrackListener.mouseHover: creating tool tip"); //$NON-NLS-1$

				createToolTip(new Point(event.x, event.y));
			}
		});

		getTable().addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent event) {
				if (logger.isDebugEnabled())
					logger.debug("table.MouseMoveListener.mouseMove: clearing tool tip"); //$NON-NLS-1$

				disposeToolTip();
			}
		});

		setContentProvider(new RoleGroupsContentProvider());
		setLabelProvider(new RoleGroupsLabelProvider(this));
//		toolTipManagerThread.start();
	}

//	private ToolTip toolTip;
//
//	private Thread toolTipManagerThread = new Thread() {
//		{
//			setDaemon(true);
//		}
//
//		@Override
//		public void run() {
//			while (true) {
//				try {
//					Thread.sleep(5000);
//				} catch (InterruptedException e) {
//					// ignore
//				}
//
//				if (getTable().isDisposed()) {
//					getTable().getDisplay().asyncExec(new Runnable() {
//						public void run() {
//							if (toolTip != null) {
//								toolTip.setMessage(" ");
//								toolTip.dispose();
//								toolTip = null;
//							}
//						}
//					});
//
//					return;
//				}
//
//				getTable().getDisplay().asyncExec(new Runnable() {
//					public void run() {
//						if (lastToolTipRequest == null)
//							return;
//
//						if (toolTip != null) {
//							toolTip.setMessage(" ");
//							toolTip.dispose();
//							toolTip = null;
//							return; // wait for the next loop
//						}
//
//						if (lastToolTipRequest.getPosition() != null && lastToolTipRequest.getText() != null) {
//							if (logger.isDebugEnabled())
//								logger.debug("toolTipManagerThread: creating tool tip: " + lastToolTipRequest.getText());
//
//							toolTip = new ToolTip(getTable().getShell(), SWT.ICON_INFORMATION); // SWT.BALLOON);
//							toolTip.addDisposeListener(new DisposeListener() {
//								@Override
//								public void widgetDisposed(DisposeEvent event) {
//									if (logger.isDebugEnabled())
//										logger.debug("toolTip.DisposeListener: tool tip widget disposed");
//								}
//							});
//
//							toolTip.setAutoHide(false);
//							toolTip.setMessage(lastToolTipRequest.getText());
//							toolTip.setLocation(lastToolTipRequest.getPosition());
//							toolTip.setVisible(true);
//						}
//
//						lastToolTipRequest = null;
//					}
//				});
//			}
//		}
//	};

// This fucking ToolTip doesn't work in a clean way in GTK+ (Linux)! Even this workaround with the thread didn't help. It seems, there
// is only one shared tool-tip-GTK-widget (heavy weight) that's somehow shared and accessed by the table itself and by my manual code
// (i.e. my ToolTip instance). It works pretty well with SWT.BALLOON. Shall we use this instead? How does it look on Windows? Marco.

	private void disposeToolTip() {
		lastToolTipRequest = new ToolTipRequest(null, null);
		getTable().setToolTipText(""); //$NON-NLS-1$
//		if (roleGroupDescriptionToolTip != null) {
//			roleGroupDescriptionToolTip.setMessage("");
//			roleGroupDescriptionToolTip.setVisible(false);
//			roleGroupDescriptionToolTip.dispose();
//			roleGroupDescriptionToolTip = null;
//
//			// SWT seems to use a persistent object in the background that's not disposed - thus I try this workaround, but it doesn't work 100% :-(
//			ToolTip workaround = new ToolTip(getTable().getShell(), SWT.ICON_INFORMATION);
//			workaround.setMessage("");
//			workaround.setVisible(true);
//			workaround.setVisible(false);
//			workaround.dispose();
//		}
	}

	private volatile ToolTipRequest lastToolTipRequest = null;

	private static class ToolTipRequest {
		private Point position;
		private String text;

		public ToolTipRequest(Point position, String text)
		{
			this.position = position;
			this.text = text;
		}
		public Point getPosition() {
			return position;
		}
		public String getText() {
			return text;
		}
	}

	private void createToolTip(final Point mousePositionRelativeToTable) {
		TableItem tableItem = getTable().getItem(mousePositionRelativeToTable);
		Object o = tableItem == null ? null :tableItem.getData();
		if (!(o instanceof RoleGroup)) {
			if (logger.isDebugEnabled())
				logger.debug("showToolTip: no role-group => disposing tool tip"); //$NON-NLS-1$

			disposeToolTip();
			return;
		}

		final RoleGroup roleGroup = (RoleGroup) o;
		final String toolTipText = roleGroup.getDescription().getText();

//		if (logger.isDebugEnabled())
//			logger.debug("createToolTip: disposing tool tip before setting it to: " + toolTipText);

		// Since we cannot relocate it (that only works with the ToolTip if it has another style - SWT.BALOON - which has other problems),
		// we destroy it and recreate it.
		disposeToolTip();

		if (logger.isDebugEnabled())
			logger.debug("createToolTip: setting tool tip to: " + toolTipText); //$NON-NLS-1$

		lastToolTipRequest = new ToolTipRequest(getTable().toDisplay(mousePositionRelativeToTable), toolTipText);

		getTable().setToolTipText(toolTipText);
//		roleGroupDescriptionToolTip = new ToolTip(getTable().getShell(), SWT.ICON_INFORMATION); // SWT.BALLOON);
//		roleGroupDescriptionToolTip.addDisposeListener(new DisposeListener() {
//			@Override
//			public void widgetDisposed(DisposeEvent event) {
//				if (logger.isDebugEnabled())
//					logger.debug("roleGroupDescriptionToolTip.DisposeListener: tool tip widget disposed");
//			}
//		});
//
//		roleGroupDescriptionToolTip.setAutoHide(false);
//		roleGroupDescriptionToolTip.setMessage(toolTipText);
//		roleGroupDescriptionToolTip.setLocation(getTable().toDisplay(mousePositionRelativeToTable));
//		roleGroupDescriptionToolTip.setVisible(true);
	}

	private CheckboxEditingSupport<RoleGroup> checkboxEditingSupport = new CheckboxEditingSupport<RoleGroup>(this) {
		@Override
		protected boolean canEdit(Object element) {
			boolean result = model != null && model.isInAuthority();
			return result;
		}

		@Override
		protected boolean doGetValue(RoleGroup element) {
			boolean result = model.isInAuthority() && model.getRoleGroupsAssignedDirectly().contains(element);
			return result;
		}

		@Override
		protected void doSetValue(RoleGroup element, boolean value) {
			if (value)
				model.addRoleGroup(element);
			else
				model.removeRoleGroup(element);

			RoleGroupTableViewer.this.dirtyStateManager.markDirty();
		}
	};

	public void setModel(RoleGroupSecurityPreferencesModel model) {
		this.model = model;

		if (model == null)
			setInput(Collections.emptySet());
		else
			setInput(model.getAllRoleGroupsInAuthority());
	}
}
