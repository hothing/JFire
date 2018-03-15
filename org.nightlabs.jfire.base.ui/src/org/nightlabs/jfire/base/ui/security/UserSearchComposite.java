package org.nightlabs.jfire.base.ui.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.search.UserQuery;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class UserSearchComposite
extends XComposite
{
	public static final int FLAG_MULTI_SELECTION = 1;
	public static final int FLAG_TYPES_ALL = 2;
	public static final int FLAG_TYPE_USER = 4;
	public static final int FLAG_TYPE_USER_GROUP = 8;
	public static final int FLAG_TYPE_ORGANISATION = 16;
	public static final int FLAG_SEARCH_BUTTON = 32;


	private int flags = 0;

	public UserSearchComposite(Composite parent, int style) {
		this(parent, style, FLAG_TYPES_ALL);
	}

	public UserSearchComposite(Composite parent, int style, int flags) {
		super(parent, style);
		this.flags = flags;
		createComposite(this);
	}

	protected boolean _useAllTypes() {
		return (flags & FLAG_TYPES_ALL) > 0;
	}
	protected boolean useAllTypes() {
		return
			 _useAllTypes()
			||
			(
				!useTypeUser() && !useTypeUserGroup() && !useTypeOrganisation()
			);
	}
	protected boolean useTypeUser() {
		return (flags & FLAG_TYPE_USER) > 0 || _useAllTypes();
	}
	protected boolean useTypeUserGroup() {
		return (flags & FLAG_TYPE_USER_GROUP) > 0 || _useAllTypes();
	}
	protected boolean useTypeOrganisation() {
		return (flags & FLAG_TYPE_ORGANISATION) > 0 || _useAllTypes();
	}
	protected boolean isMultiSelelect() {
		return (flags & FLAG_MULTI_SELECTION) > 0;
	}
	protected boolean isShowSearchButton() {
		return (flags & FLAG_SEARCH_BUTTON) > 0;
	}



 	private Text userIDText = null;
	public Text getUserIDText() {
		return userIDText;
	}

	private Text nameText = null;
	public Text getNameText() {
		return nameText;
	}

//	private Text userTypeText = null;
//	public Text getUserTypeText() {
//		return userTypeText;
//	}

	private Combo userTypeCombo = null;
	public Combo getUserTypeCombo() {
		return userTypeCombo;
	}

	private UserTable userTable = null;
	public UserTable getUserTable() {
		return userTable;
	}

	private User selectedUser = null;
	public User getSelectedUser() {
		return selectedUser;
	}

	public Collection<User> getSelectedUsers() {
		return userTable.getSelectedElements();
	}

	protected void createComposite(Composite parent)
	{
		XComposite searchComp = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		searchComp.getGridLayout().numColumns = isShowSearchButton() ? 4 : 3;
		searchComp.getGridLayout().makeColumnsEqualWidth = false;
		searchComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		userIDText = createTextSearchEntry(searchComp, Messages.getString("org.nightlabs.jfire.base.ui.security.UserSearchComposite.userID")); //$NON-NLS-1$
		nameText = createTextSearchEntry(searchComp, Messages.getString("org.nightlabs.jfire.base.ui.security.UserSearchComposite.name")); //$NON-NLS-1$
//		userTypeText = createTextSearchEntry(searchComp, Messages.getString("security.UserSearchComposite.userType")); //$NON-NLS-1$
		Composite wrapper = new XComposite(searchComp, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		Label label = new Label(wrapper, SWT.NONE);
		label.setText(Messages.getString("org.nightlabs.jfire.base.ui.security.UserSearchComposite.userType")); //$NON-NLS-1$
		userTypeCombo = new Combo(wrapper, SWT.BORDER);
		userTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (useAllTypes()) {
			userTypeCombo.setItems(new String[] {"User", "UserGroup", "Organisation"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			List<String> types = new ArrayList<String>();
			if (useTypeUser())
				types.add("User"); //$NON-NLS-1$
			if (useTypeUserGroup())
				types.add("UserGroup"); //$NON-NLS-1$
			if (useTypeOrganisation())
				types.add("Organisation"); //$NON-NLS-1$
			userTypeCombo.setItems(types.toArray(new String[types.size()]));
			if (types.size() == 1) {
				userTypeCombo.select(0);
			}
		}

		if (isShowSearchButton()) {
			Button searchButton = new Button(searchComp, SWT.PUSH);
			searchButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
			searchButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.security.UserSearchComposite.button.search.text")); //$NON-NLS-1$
			searchButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					searchPressed();
				}
			});
		}

		userTable = new UserTable(parent, SWT.NONE, true, isMultiSelelect() ? AbstractTableComposite.DEFAULT_STYLE_MULTI_BORDER : AbstractTableComposite.DEFAULT_STYLE_SINGLE_BORDER);
		userTable.setLinesVisible(true);
		userTable.setHeaderVisible(true);
		userTable.addSelectionChangedListener(userTableSelectionListener);
	}

	protected Text createTextSearchEntry(Composite parent, String labelString)
	{
		Composite wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		Label label = new Label(wrapper, SWT.NONE);
		label.setText(labelString);
		Text text = new Text(wrapper, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return text;
	}

	protected UserQuery getUserQuery()
	{
		UserQuery userQuery = new UserQuery();

		if (!nameText.getText().trim().equals("")) //$NON-NLS-1$
			userQuery.setName(nameText.getText());

		if (!userIDText.getText().trim().equals("")) //$NON-NLS-1$
			userQuery.setUserID(userIDText.getText());

//		if (!userTypeText.getText().trim().equals("")) //$NON-NLS-1$
//			userQuery.setUserType(userTypeText.getText());

		if (userTypeCombo.getSelectionIndex() != -1 && !userTypeCombo.getText().trim().equals("")) //$NON-NLS-1$
			userQuery.setUserTypes(Collections.singleton(userTypeCombo.getText()));
		else if (!useAllTypes()) {
			Collection<String> types = new HashSet<String>();
			for (String type : userTypeCombo.getItems()) {
				types.add(type);
			}
			userQuery.setUserTypes(types);
		}

		return userQuery;
	}

	public void searchPressed()
	{
		userTable.setInput(Messages.getString("org.nightlabs.jfire.base.ui.security.UserSearchComposite.input_loading")); //$NON-NLS-1$
		Job job = new Job(Messages.getString("org.nightlabs.jfire.base.ui.security.UserSearchComposite.loadJob.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor){
				try {
					JFireSecurityManagerRemote um = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, Login.getLogin().getInitialContextProperties());
					final QueryCollection<UserQuery> queries =
						new QueryCollection<UserQuery>(User.class);
					Display.getDefault().syncExec(new Runnable(){
						public void run() {
							queries.add(getUserQuery());
						}
					});
					Set<UserID> userIDs = um.getUserIDs(queries);
					if (userIDs != null && !userIDs.isEmpty()) {
						String[] USER_FETCH_GROUPS = new String[] {FetchPlan.DEFAULT};
						final List<User> users = UserDAO.sharedInstance().getUsers(userIDs, USER_FETCH_GROUPS,
								NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								userTable.setInput(users);
							}
						});
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				return Status.OK_STATUS;
			}
		};
//		job.setPriority(Job.SHORT);
		job.schedule();
	}

	private ISelectionChangedListener userTableSelectionListener = new ISelectionChangedListener(){
		public void selectionChanged(SelectionChangedEvent event) {
			if (!event.getSelection().isEmpty() && event.getSelection() instanceof StructuredSelection) {
				StructuredSelection sel = (StructuredSelection) event.getSelection();
				if (sel.getFirstElement() instanceof User) {
					selectedUser = (User) sel.getFirstElement();
				}
			}
		}
	};

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		userTable.addSelectionChangedListener(listener);
	}

	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		userTable.removeSelectionChangedListener(listener);
	}

}
