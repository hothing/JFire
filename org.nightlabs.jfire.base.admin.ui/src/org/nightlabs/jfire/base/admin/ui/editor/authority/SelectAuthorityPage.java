package org.nightlabs.jfire.base.admin.ui.editor.authority;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.ListComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.I18nTextEditor.EditMode;
import org.nightlabs.base.ui.wizard.WizardHopPage;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.i18n.I18nTextBuffer;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.dao.AuthorityDAO;
import org.nightlabs.jfire.security.dao.AuthorityTypeDAO;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.NLLocale;

public class SelectAuthorityPage extends WizardHopPage
{
	private AuthorityTypeID authorityTypeID;
	private AuthorityType authorityType;
	private InheritedSecuringAuthorityResolver inheritedAuthorityResolver;

	public static enum Action {
		inherit,
		none,
		create,
		select
	}

	private Action action;

	private Button radioButtonInherit;
	private Button radioButtonNone;
	private Button radioButtonCreate;
	private I18nText newAuthorityName = new I18nTextBuffer();
	private I18nTextEditor newAuthorityNameEditor;

	private Button radioButtonSelect;
	private ListComposite<Authority> authorityList;

	private Authority inheritedAuthority = null;
	private AuthorityID selectedAuthorityID;
	private Authority newAuthority = null;

	/**
	 * Create the wizard page for selection/creation of an Authority.
	 *
	 * @param authorityTypeID the ID of the {@link AuthorityType}. This must not be <code>null</code>, because it is required for creation of an authority!
	 * @param inheritedAuthorityResolver Used to find out the inherited {@link Authority}. This can be <code>null</code>, if there is no inheritance in the current use case. If it is <code>null</code>, the "inherit" option will be hidden.
	 */
	public SelectAuthorityPage(AuthorityTypeID authorityTypeID, InheritedSecuringAuthorityResolver inheritedAuthorityResolver) {
		super(SelectAuthorityPage.class.getName(), Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.SelectAuthorityPage.title.selectAuthority")); //$NON-NLS-1$
		if (authorityTypeID == null)
			throw new IllegalArgumentException("authorityTypeID == null"); //$NON-NLS-1$

		this.authorityTypeID = authorityTypeID;
		this.inheritedAuthorityResolver = inheritedAuthorityResolver;
	}

	private void setInheritedAuthorityName(String authorityName)
	{
		radioButtonInherit.setText(String.format(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.SelectAuthorityPage.button.text.inheritAuthority"), authorityName)); //$NON-NLS-1$
	}

	@Override
	public Control createPageContents(Composite parent) {
		final XComposite page = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);

		if (inheritedAuthorityResolver != null) {
			radioButtonInherit = new Button(page, SWT.RADIO);
			radioButtonInherit.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			radioButtonInherit.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setAction(Action.inherit);
				}
			});
			setInheritedAuthorityName(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.SelectAuthorityPage.label.loadingData")); //$NON-NLS-1$
		}

		radioButtonNone = new Button(page, SWT.RADIO);
		radioButtonNone.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		radioButtonNone.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.SelectAuthorityPage.button.text.DoNotAssignAuthority")); //$NON-NLS-1$
		radioButtonNone.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setAction(Action.none);
			}
		});

		radioButtonCreate = new Button(page, SWT.RADIO);
		radioButtonCreate.setEnabled(false); // enabling this when the AuthorityType has been loaded.
		radioButtonCreate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		radioButtonCreate.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.SelectAuthorityPage.button.text.createNewAuthority")); //$NON-NLS-1$
		radioButtonCreate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setAction(Action.create);
			}
		});

		XComposite nameComp = new XComposite(page, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		nameComp.getGridData().grabExcessVerticalSpace = false;
		nameComp.getGridLayout().numColumns = 2;
		Label nameSpacer = new Label(nameComp, SWT.NONE);
		GridData gd = new GridData();
		gd.widthHint = 32;
		gd.heightHint = 1;
		nameSpacer.setLayoutData(gd);

		newAuthorityNameEditor = new I18nTextEditor(nameComp);
		newAuthorityNameEditor.setI18nText(newAuthorityName, EditMode.DIRECT);
		newAuthorityNameEditor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				setAction(Action.create);
			}
		});

		radioButtonSelect = new Button(page, SWT.RADIO);
		radioButtonSelect.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		radioButtonSelect.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.SelectAuthorityPage.button.text.selectAuthority")); //$NON-NLS-1$
		radioButtonSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setAction(Action.select);
			}
		});

		authorityList = new ListComposite<Authority>(page, SWT.NONE, (String)null, new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Authority)element).getName().getText();
			}
		});

		authorityList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				selectedAuthorityID = (AuthorityID) JDOHelper.getObjectId(authorityList.getSelectedElement());
				setAction(Action.select);
			}
		});

		AuthorityType dummyAT = new AuthorityType(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.SelectAuthorityPage.dummy")); //$NON-NLS-1$
		Authority dummy = new Authority(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.SelectAuthorityPage.dummy"), Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.SelectAuthorityPage.dummy"), dummyAT); //$NON-NLS-1$ //$NON-NLS-2$
		dummy.getName().setText(NLLocale.getDefault().getLanguage(), Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.SelectAuthorityPage.loading")); //$NON-NLS-1$
		authorityList.addElement(dummy);

		Job loadJob = new Job(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.SelectAuthorityPage.job.loadingAuthorityTypes")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception
			{
				monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.SelectAuthorityPage.job.loadingAuthorityTypes"), 100); //$NON-NLS-1$

				if (inheritedAuthorityResolver == null) {
					inheritedAuthority = null;
					monitor.worked(20);
				}
				else {
					AuthorityID inheritedAuthorityID = inheritedAuthorityResolver.getInheritedSecuringAuthorityID(new SubProgressMonitor(monitor, 10));
					if (inheritedAuthorityID == null) {
						inheritedAuthority = null;
						monitor.worked(10);
					}
					else
						inheritedAuthority = AuthorityDAO.sharedInstance().getAuthority(
								inheritedAuthorityID,
								new String[] { FetchPlan.DEFAULT, Authority.FETCH_GROUP_NAME},
								NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
								new SubProgressMonitor(monitor, 10));
				}

				authorityType = AuthorityTypeDAO.sharedInstance().getAuthorityType(
						authorityTypeID,
						new String[] { FetchPlan.DEFAULT },
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						new SubProgressMonitor(monitor, 20));

				final List<Authority> authorities = AuthorityDAO.sharedInstance().getAuthorities(
						SecurityReflector.getUserDescriptor().getOrganisationID(),
						authorityTypeID,
						new String[] { FetchPlan.DEFAULT, Authority.FETCH_GROUP_NAME },
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new SubProgressMonitor(monitor, 60));

				Collections.sort(authorities, new Comparator<Authority>() {
						@Override
						public int compare(Authority o1, Authority o2) {
							return o1.getName().getText().compareTo(o2.getName().getText());
						}
				});

				monitor.done();

				if (page.isDisposed())
					return Status.CANCEL_STATUS;

				page.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (authorityList.isDisposed())
							return;

						if (inheritedAuthorityResolver != null)
							setInheritedAuthorityName(inheritedAuthority == null ? Messages.getString("org.nightlabs.jfire.base.admin.ui.editor.authority.SelectAuthorityPage.label.noneAssigned") : inheritedAuthority.getName().getText()); //$NON-NLS-1$

						radioButtonCreate.setEnabled(true); // now we have the authorityType and thus can enable this.

						authorityList.removeAll();
						authorityList.addElements(authorities);
						initializationFinished = true;

						getContainer().updateButtons();
					}
				});

				return Status.OK_STATUS;
			}
		};
		loadJob.setPriority(Job.SHORT);
		loadJob.schedule();

		// setAction accesses the container and if we do that directly here, it causes a NPE. Hence we do it in the next event cycle.
		page.getDisplay().asyncExec(new Runnable() {
			public void run() {
				// if the inheritance option is available, we make it default - if it's not, we use "select" as default
				if (radioButtonInherit != null)
					setAction(Action.inherit);
				else
					setAction(Action.select);
			}
		});

		return page;
	}

	private boolean initializationFinished = false;

	@Override
	public boolean isPageComplete() {
		if (action == null || !initializationFinished) // not yet initialised
			return false;

		switch (action) {
			case create:
				return !newAuthorityName.isEmpty();

			case inherit:
			case none:
				return true;

			case select:
				return selectedAuthorityID != null;

			default:
				throw new IllegalStateException("Unknown action: " + action); //$NON-NLS-1$
		}
	}

	public Action getAction() {
		return action;
	}

	private void setAction(Action action) {
		this.action = action;

		if (radioButtonInherit != null)
			radioButtonInherit.setSelection(Action.inherit == action);

		radioButtonNone.setSelection(Action.none == action);
		radioButtonCreate.setSelection(Action.create == action);
		radioButtonSelect.setSelection(Action.select == action);

		getContainer().updateButtons();
	}

	public AuthorityID getAuthorityID() {
		if (action == null)
			return null;

		switch (action) {
			case inherit:
				return (AuthorityID) JDOHelper.getObjectId(inheritedAuthority);

			case create:
			case none:
				return null;

			case select:
				return selectedAuthorityID;

			default:
				throw new IllegalStateException("Unknown action: " + action); //$NON-NLS-1$
		}
	}

	public Authority getNewAuthority() {
		if (action != Action.create)
			return null;

		if (newAuthority == null) {
			newAuthority = new Authority(
					IDGenerator.getOrganisationID(),
					ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(Authority.class)),
					authorityType);
		}

		newAuthority.getName().copyFrom(newAuthorityName);

		return newAuthority;
	}
}
