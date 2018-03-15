package org.nightlabs.jfire.base.ui.security;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.eclipse.ui.dialog.ResizableTrayDialog;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.security.User;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class UserSearchDialog
extends ResizableTrayDialog
{
	private int flags;
	/**
	 * @param parentShell
	 * @param searchText
	 */
	public UserSearchDialog(Shell parentShell, String searchText, int flags) {
		super(parentShell, Messages.RESOURCE_BUNDLE);
		this.searchText = searchText;
		this.flags = flags;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	/**
	 * @param parentShell
	 * @param searchText
	 */
	public UserSearchDialog(Shell parentShell, String searchText) {
		super(parentShell, Messages.RESOURCE_BUNDLE);
		this.searchText = searchText;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/**
	 * @param parentShell
	 * @param searchText
	 */
	public UserSearchDialog(IShellProvider parentShell, String searchText) {
		super(parentShell, null);
		this.searchText = searchText;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("org.nightlabs.jfire.base.ui.security.UserSearchDialog.SearchUser")); //$NON-NLS-1$
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		if (flags != 0)
			userSearchComposite = new UserSearchComposite(parent, SWT.NONE, flags);
		else
			userSearchComposite = new UserSearchComposite(parent, SWT.NONE);
		if (searchText != null && !searchText.trim().equals("")) { //$NON-NLS-1$
			userSearchComposite.getUserIDText().setText(searchText);
		}
		userSearchComposite.getUserTable().addDoubleClickListener(userDoubleClickListener);
		return userSearchComposite;
	}
	
	private UserSearchComposite userSearchComposite = null;
	private String searchText = ""; //$NON-NLS-1$
	private User selectedUser = null;
	public User getSelectedUser() {
		return selectedUser;
	}

	public static final int SEARCH_ID = IDialogConstants.CLIENT_ID + 1;
	
	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		super.createButtonsForButtonBar(parent);
		Button searchButton = createButton(parent, SEARCH_ID, Messages.getString("org.nightlabs.jfire.base.ui.security.UserSearchDialog.Search"), true); //$NON-NLS-1$
		searchButton.addSelectionListener(searchButtonListener);
	}
	
	private SelectionListener searchButtonListener = new SelectionListener(){
		public void widgetSelected(SelectionEvent e) {
			userSearchComposite.searchPressed();
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
	};
	
	@Override
	protected void okPressed()
	{
		selectedUser = userSearchComposite.getSelectedUser();
		super.okPressed();
	}
	
	@Override
	protected void cancelPressed()
	{
		selectedUser = null;
		super.cancelPressed();
	}
	
	private IDoubleClickListener userDoubleClickListener = new IDoubleClickListener(){
		public void doubleClick(DoubleClickEvent event) {
			if (!event.getSelection().isEmpty() && event.getSelection() instanceof StructuredSelection) {
				StructuredSelection sel = (StructuredSelection) event.getSelection();
				if (sel.getFirstElement() instanceof User) {
					selectedUser = (User) sel.getFirstElement();
					close();
				}
			}
		}
	};
	
}
