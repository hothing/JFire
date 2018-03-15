package org.nightlabs.jfire.base.admin.ui.editor.authority;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.nightlabs.base.ui.action.InheritanceAction;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.editor.ToolBarSectionPart;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.I18nTextEditorMultiLine;
import org.nightlabs.base.ui.language.I18nTextEditor.EditMode;
import org.nightlabs.base.ui.wizard.DynamicPathWizardDialog;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.SecuredObject;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.util.Util;

/**
 * Section to edit an {@link Authority}s name and description.
 * It also serves as the control to assign a new {@link Authority} to the current {@link SecuredObject}
 * as well as managing the inheritance of the {@link Authority} from the parent {@link SecuredObject}.
 * This section uses an {@link AuthorityPageControllerHelper} that should be set by {@link #setAuthorityPageControllerHelper(AuthorityPageControllerHelper)}.
 *
 * @author marco schulze - marco at nightlabs dot de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class AuthoritySection
extends ToolBarSectionPart
{
	private I18nTextEditor name;
	private I18nTextEditor description;
	private Label nameLabel;
	private Label descriptionLabel;

	public AuthoritySection(IFormPage page, Composite parent) {
		super(page, parent, ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE, Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.AbstractAuthoritySection.title.authority")); //$NON-NLS-1$
		((GridData)getSection().getLayoutData()).grabExcessVerticalSpace = false;

		Composite wrapper = new XComposite(getContainer(), SWT.NONE);
		wrapper.setLayout(new GridLayout(2, false));

		nameLabel = new Label(wrapper, SWT.NONE);
		nameLabel.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.AuthoritySection.label.name")); //$NON-NLS-1$
		name = new I18nTextEditor(wrapper);
		name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		name.addModifyListener(markDirtyModifyListener);

		descriptionLabel = new Label(wrapper, SWT.NONE);
		descriptionLabel.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.AuthoritySection.label.description")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.verticalAlignment = GridData.BEGINNING;
		descriptionLabel.setLayoutData(gd);
		description = new I18nTextEditorMultiLine(wrapper, name.getLanguageChooser());
		description.setLayoutData(new GridData(GridData.FILL_BOTH));
		description.addModifyListener(markDirtyModifyListener);

		assignAuthorityAction.setEnabled(false);
		getToolBarManager().add(assignAuthorityAction);
		inheritAction.setEnabled(false);
		getToolBarManager().add(inheritAction);
		updateToolBarManager();

		name.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				setAuthorityPageControllerHelper(null);
			}
		});

		setEnabled(false);
	}

	private ModifyListener markDirtyModifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent arg0) {
			markDirty();
		}
	};

	private AuthorityPageControllerHelper authorityPageControllerHelper;

	/**
	 * Get the object that has been set by {@link #setAuthorityPageControllerHelper(AuthorityPageControllerHelper)} before or <code>null</code>.
	 *
	 * @return an instance of <code>AuthorityPageControllerHelper</code> or <code>null</code>.
	 */
	protected AuthorityPageControllerHelper getAuthorityPageControllerHelper() {
		return authorityPageControllerHelper;
	}

	private Action assignAuthorityAction = new Action() {
		{
			setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.AbstractAuthoritySection.action.text.assignAuthority")); //$NON-NLS-1$
		}

		@Override
		public void run() {
			if (authorityPageControllerHelper == null)
				return;

			final AssignAuthorityWizard assignAuthorityWizard = new AssignAuthorityWizard(
					authorityPageControllerHelper.getAuthorityTypeID(),
					authorityPageControllerHelper.getInheritedSecuringAuthorityResolver()
			);
			DynamicPathWizardDialog dialog = new DynamicPathWizardDialog(getSection().getShell(), assignAuthorityWizard);
			if (dialog.open() == Dialog.OK) {
				Job job = new Job(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.AbstractAuthoritySection.job.loadingAuthority")) { //$NON-NLS-1$
					@Override
					protected org.eclipse.core.runtime.IStatus run(org.nightlabs.progress.ProgressMonitor monitor) throws Exception {
						authorityPageControllerHelper.load(
								assignAuthorityWizard.getAuthorityTypeID(),
								assignAuthorityWizard.getAuthorityID(),
								assignAuthorityWizard.getNewAuthority(),
								monitor);

						authorityPageControllerHelper.setAssignSecuringAuthority(
								assignAuthorityWizard.getAuthorityID(),
								assignAuthorityWizard.isAuthorityIDInherited()
						);

						getSection().getDisplay().asyncExec(new Runnable() {
							public void run() {
								inheritAction.setChecked(assignAuthorityWizard.isAuthorityIDInherited());
								authorityChanged();
								markDirty();
							}
						});

						return Status.OK_STATUS;
					}
				};
				job.setPriority(Job.SHORT);
				job.schedule();
			}
		}
	};

	private InheritanceAction inheritAction = new InheritanceAction() {
		@Override
		public void run() {
			final boolean oldEnabled = inheritAction.isEnabled();
			inheritAction.setEnabled(false);
			Job job = new Job(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.AbstractAuthoritySection.job.loadingAuthority")) { //$NON-NLS-1$
				@Override
				protected org.eclipse.core.runtime.IStatus run(org.nightlabs.progress.ProgressMonitor monitor) throws Exception {
					try {
						if (!authorityPageControllerHelper.isManageInheritance())
							return Status.OK_STATUS;
						boolean setInherited = isChecked();
						AuthorityID parentAuthorityID = authorityPageControllerHelper.isManageInheritance()
							? authorityPageControllerHelper.getInheritedSecuringAuthorityResolver().getInheritedSecuringAuthorityID(monitor)
							: null;
						authorityPageControllerHelper.setAssignSecuringAuthority(parentAuthorityID, setInherited);
						AuthorityID newAuthorityID = setInherited ? parentAuthorityID : authorityPageControllerHelper.getAuthorityID();
						if (!Util.equals(newAuthorityID, authorityPageControllerHelper.getAuthorityID())) {
							authorityPageControllerHelper.load(
									authorityPageControllerHelper.getAuthorityTypeID(), // The type should not change when re-assigning
									newAuthorityID,
									null, monitor);
						}

						getSection().getDisplay().asyncExec(new Runnable() {
							public void run() {
								authorityChanged();
								markDirty();
							}
						});

						return Status.OK_STATUS;
					} finally {
						getSection().getDisplay().asyncExec(new Runnable() {
							public void run() {
								inheritAction.setEnabled(oldEnabled);
							}
						});
					}
				}
			};
			job.setPriority(Job.SHORT);
			job.schedule();
		}
	};

	/**
	 * Set the {@link AuthorityPageControllerHelper} that is used for the current editor page. It is possible to
	 * pass <code>null</code> in order to indicate that there is nothing to be managed right now (and thus to clear
	 * the UI).
	 *
	 * @param authorityPageControllerHelper an instance of <code>AuthorityPageControllerHelper</code> or <code>null</code>.
	 */
	protected void setAuthorityPageControllerHelper(final AuthorityPageControllerHelper authorityPageControllerHelper) {
		this.authorityPageControllerHelper = authorityPageControllerHelper;
		getSection().getDisplay().asyncExec(new Runnable() {
			public void run() {
				assignAuthorityAction.setEnabled(authorityPageControllerHelper != null);
				authorityChanged();
				inheritAction.setEnabled(authorityPageControllerHelper != null);
				inheritAction.setChecked(false);
				if (authorityPageControllerHelper != null && authorityPageControllerHelper.isManageInheritance()) {
					inheritAction.setEnabled(true);
					inheritAction.setChecked(authorityPageControllerHelper.isAuthorityInitiallyInherited());
				}
			}
		});
	}

//	/**
//	 * This method should take your implementation of {@link IEntityEditorPageController} and must call
//	 * {@link #setAuthorityPageControllerHelper(AuthorityPageControllerHelper)}.
//	 *
//	 * @param pageController your use-case-specific implementation of {@link IEntityEditorPageController}.
//	 */
//	public abstract void setPageController(IEntityEditorPageController pageController);

	private void authorityChanged() {
		if (name.isDisposed())
			return;

		if (authorityPageControllerHelper == null || authorityPageControllerHelper.getAuthority() == null) {
			name.setI18nText(null, EditMode.DIRECT);
			description.setI18nText(null, EditMode.DIRECT);

			if (authorityPageControllerHelper == null)
				setMessage(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.AbstractAuthoritySection.message.noSecuredObjectSelected")); //$NON-NLS-1$
			else
				setMessage(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.AbstractAuthoritySection.message.noAuthorityAssigned")); //$NON-NLS-1$

			setEnabled(false);
		}
		else {
			name.setI18nText(authorityPageControllerHelper.getAuthority().getName(), EditMode.DIRECT);
			description.setI18nText(authorityPageControllerHelper.getAuthority().getDescription(), EditMode.DIRECT);

			setMessage(null);
			setEnabled(true);
		}
	}

	private void setEnabled(boolean enabled) {
		name.setEnabled(enabled);
		description.setEnabled(enabled);
		nameLabel.setEnabled(enabled);
		descriptionLabel.setEnabled(enabled);
	}
}
