package org.nightlabs.jfire.base.admin.ui.editor.user;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.editor.RestorableSectionPart;
import org.nightlabs.base.ui.entity.editor.EntityEditorUtil;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.eclipse.ui.dialog.ChangePasswordDialog;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;

/**
 * The form section for basic user data like Id, Name, Description
 * and password.
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author unascribed
 */
public class UserDataSection extends RestorableSectionPart
{
	private Text userIdText;
	private Text userNameText;
	private Text userDescriptionText;
	private Button passwordButton;
	private Button autogenerateNameCheckBox;
	private String newPassword;

	/**
	 * The user object this section is connected to.
	 */
	private User user;

	/**
	 * Set to <code>true</code> while automatic refreshing of UI elements
	 * happens. Some listeners are enabled at this time.
	 */
	private boolean refreshing = false;

	ModifyListener dirtyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			if (!refreshing)
				markDirty();
		}
	};
	private PersonPreferencesPage personPreferencesPage;

	/**
	 * Create an instance of UserPropertiesSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public UserDataSection(FormPage page, Composite parent, String sectionDescriptionText) {
		super(parent, page.getEditor().getToolkit(), ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		personPreferencesPage = (PersonPreferencesPage) page;
		createClient(getSection(), page.getEditor().getToolkit(), sectionDescriptionText);
	}

	private void createClient(Section section, FormToolkit toolkit, String sectionDescriptionText) {
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createDescriptionControl(section, toolkit, sectionDescriptionText);
		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 1);
		GridLayout layout = (GridLayout) container.getLayout();
		layout.horizontalSpacing = 10;
		layout.numColumns = 2;

		createLabel(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserDataSection.userIDLabel"), 2); //$NON-NLS-1$
		userIdText = new Text(container, XComposite.getBorderStyle(container));
		userIdText.setEditable(false);
		userIdText.setLayoutData(getGridData(2));

		createLabel(container,	Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserDataSection.userNameLabel"), 2); //$NON-NLS-1$
		userNameText = new Text(container, XComposite.getBorderStyle(container));
		userNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		userNameText.addModifyListener(dirtyListener);

		autogenerateNameCheckBox = new Button(container, SWT.CHECK);
		autogenerateNameCheckBox.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserDataSection.AutogenerateLabel")); //$NON-NLS-1$
		autogenerateNameCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				userNameText.setEnabled(!autogenerateNameCheckBox.getSelection());
				String oldText = userNameText.getText();
				refreshing = true;
				try {
					updateDisplayName();
				} finally {
					refreshing = false;
				}
				if (!oldText.equals(userNameText.getText()))
					markDirty();
			}
		});

		createLabel(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserDataSection.DescriptionLabel"), 2); //$NON-NLS-1$
		userDescriptionText = new Text(container, XComposite.getBorderStyle(container));
		userDescriptionText.setLayoutData(getGridData(3));
		userDescriptionText.addModifyListener(dirtyListener);

		createLabel(container, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserDataSection.PasswordLabel"), 2); //$NON-NLS-1$
		passwordButton = new Button(container, SWT.PUSH);
		passwordButton.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserDataSection.SetPasswordButtonText")); //$NON-NLS-1$
		passwordButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				passwordButtonPressed();
			}
		});
	}

	/**
	 * Called when the "change password" button is pressed.
	 */
	private void passwordButtonPressed()
	{
		IInputValidator newPasswordValidator = new IInputValidator() {
			@Override
			public String isValid(String password) {
				if (password.length() < UserLocal.MIN_PASSWORD_LENGTH)
					return String.format(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserDataSection.minPasswordLengthText"), UserLocal.MIN_PASSWORD_LENGTH); //$NON-NLS-1$
				if (password.matches("\\*+")) //$NON-NLS-1$
					return Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.user.UserDataSection.invalidPasswordError"); //$NON-NLS-1$
				return null;
			}
		};

		ChangePasswordDialog dialog = new ChangePasswordDialog(RCPUtil.getActiveShell(), newPasswordValidator, null);
		if (dialog.open() == Window.OK) {
			newPassword = dialog.getConfirmedPassword();
			markDirty();
		}
	}

	private void createLabel(Composite container, String text, int span) {
		Label label = new Label(container, SWT.NONE);
		label.setText(text);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = span;
		label.setLayoutData(gd);
	}

	private GridData getGridData(int span) {
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = span;
		return gd;
	}

	private void createDescriptionControl(Section section, FormToolkit toolkit, String sectionDescriptionText) {
		if (sectionDescriptionText == null || "".equals(sectionDescriptionText)) //$NON-NLS-1$
			return;

		section.setText(sectionDescriptionText);
	}

	public void setUser(User _user) {
		this.user = _user;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (userIdText == null || userIdText.isDisposed())
					return;

				refreshing = true;
				try {
					userIdText.setText(user.getUserID());
					if (user.getName() != null)
						userNameText.setText(user.getName());
					if (user.getDescription() != null)
						userDescriptionText.setText(user.getDescription());

					passwordButton.setEnabled(!user.getUserID().startsWith("_"));

					autogenerateNameCheckBox.setSelection(user.isAutogenerateName());
					userNameText.setEnabled(!autogenerateNameCheckBox.getSelection());

					personPreferencesPage.getUserPropertiesSection().setAdditionalDataChangeListener(new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							refreshing = true;
							updateDisplayName();
							refreshing = false;
						}
					});
				} finally {
					refreshing = false;
				}
			}
		});
	}

	@Override
	public void commit(boolean onSave) {
		super.commit(onSave);
		user.setDescription(userDescriptionText.getText());
		user.setName(userNameText.getText());
		user.setAutogenerateName(autogenerateNameCheckBox.getSelection());
		if (newPassword != null)
			user.getUserLocal().setNewPassword(newPassword);
	}

	void updateDisplayName() {
		user.setAutogenerateName(autogenerateNameCheckBox.getSelection());

		if (autogenerateNameCheckBox.getSelection()) {
			// FIXME: why changing the user object here? Does it make any sense? Marc
			user.setNameAuto();
			userNameText.setText(user.getName());
		}
	}
}
