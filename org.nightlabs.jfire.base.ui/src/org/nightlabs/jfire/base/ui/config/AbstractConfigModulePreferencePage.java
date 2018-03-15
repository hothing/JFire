/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.ui.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.nightlabs.base.ui.composite.FadeableComposite;
import org.nightlabs.base.ui.composite.InheritanceToggleButton;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.notification.IDirtyStateManager;
import org.nightlabs.base.ui.notification.NotificationAdapterJob;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.InheritanceManager;
import org.nightlabs.jfire.base.JFireBaseEAR;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.ui.editlock.EditLockHandle;
import org.nightlabs.jfire.base.ui.editlock.EditLockMan;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.preferences.LSDPreferencePage;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.ConfigManagerRemote;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.dao.ConfigModuleDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigModuleID;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

/**
 * An abstract PreferencePage for ConfigModules.
 * Basicly it takes care of retrieving and storing
 * the config module for you and provides callbacks
 * to present the module to the user.
 * <p>
 * See
 * <ul>
 * <li>{@link #createPreferencePage(Composite)}</li>
 * <li>{@link #updatePreferencePage()}</li>
 * <li>{@link #updateConfigModule()}</li>
 * </ul>
 * for methods you need to implement.
 *
 * <p>
 * Also take a look at
 * {@link #getConfigModuleFetchGroups()}
 * in order to pass appropriate fetch groups to detach
 * your config module.
 *
 * <p>
 * Note that by default the {@link ConfigModule}s are cloned
 * by {@link Utils#cloneSerializable(Object)} before passed
 * to implementors so they might be changed directly without
 * taking care of restorage when the user decides not to
 * store the configuration.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marius Heizmann <marius[AT]nightlabs[DOT]de>
 * @author Daniel Mazurek <daniel[AT]nightlabs[DOT]de>
 */
public abstract class AbstractConfigModulePreferencePage
extends LSDPreferencePage
{
	private static final Logger logger = Logger.getLogger(AbstractConfigModulePreferencePage.class);

	/**
	 * the outmost wrapper used to grey out the page while loading.
	 */
	private FadeableComposite fadableWrapper;

	/**
	 * The container for the header and the body, which is shown when the ConfigModule is loaded.
	 */
	private XComposite loadingDone;
	private XComposite header;
	private FadeableComposite bodyWrapper;
	private XComposite body;

	/**
	 * Shows a simple label telling the user that the information to display is currently being
	 * fetched from the server.
	 */
	private XComposite loading;

	/**
	 * This <code>Button</code> is only instantiated, if we're currently editing a group's ConfigModule.
	 * Otherwise, the {@link #inheritMemberConfigModule} will be created instead.
	 */
	private Button checkBoxAllowOverride;

	/**
	 * This <code>InheritanceToggleButton</code> is only instantiated, if we're currently editing
	 * a member's ConfigModule (i.e. <b>not</b> in group context).
	 *
	 * @see #checkBoxAllowOverride
	 */
	private InheritanceToggleButton inheritMemberConfigModule;

	/**
	 * Whether the <code>currentConfigModule</code> is in a ConfigGroup and therefore underlies the
	 * inheritence constrains.
	 */
	protected boolean currentConfigIsGroupMember = false;

	/**
	 * Whether the <code>currentConfigModule</code> can be edited by the user. This can be set by
	 * {@link #canEdit(ConfigModule)}.
	 */
	protected boolean currentConfigModuleIsEditable = false;

	/**
	 * Whether the <code>currentConfigModule</code> was modified by the user. It is only saved iff
	 * <code>configChanged == true</code>.
	 */
	protected boolean configChanged = false;

	public AbstractConfigModulePreferencePage() {
		super();
	}

	/**
	 * @param title
	 */
	public AbstractConfigModulePreferencePage(String title) {
		super(title);
	}

	/**
	 * @param title
	 * @param image
	 */
	public AbstractConfigModulePreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	private IConfigModuleController configModuleManager = null;
	public IConfigModuleController getConfigModuleController() {
		if (configModuleManager == null) {
			configModuleManager = createConfigModuleController();
		}
		return configModuleManager;
	}

	protected abstract IConfigModuleController createConfigModuleController();

	/**
	 * Checks if the user is allowed to change configuration
	 * for groups or other users.
	 *
	 * @return Weather the user is allowed to change other configurations
	 */
	protected boolean isUserConfigSelectionAllowed() {
		return true;
	}

	/**
	 * Implicit Listener for the cache. This is needed in order to get notified when the
	 * {@link #currentConfigModule} changed in the database and to reflect this change in the GUI.
	 */
	private final NotificationListener changeListener = new NotificationAdapterJob() {

		public void notify(NotificationEvent notificationEvent) {
			Set<DirtyObjectID> dirtyObjectIDs = CollectionUtil.castSet( notificationEvent.getSubjects() );
			ConfigModuleID currentModuleID = (ConfigModuleID) JDOHelper.getObjectId(
					getConfigModuleController().getConfigModule());
//		 there aren't many open ConfigModulePages showing the same kind of ConfigModule, this loop is
// 			therefore not as time consuming as one might think. But if the set of DirtyObjectIDs
//			would be capable of efficiently checking whether a given ConfigID is contained inside itself,
//			then this check would be a lot faster.
			boolean moduleIsUpdated = false;
			for (DirtyObjectID dirtyID : dirtyObjectIDs) {
				if (! dirtyID.getObjectID().equals( currentModuleID ))
					continue;

				moduleIsUpdated = true;
				break;
			}

			if (! moduleIsUpdated)
				return;

			// check if this module has been saved lately and is therefore already up to date.
			// This is a workaround for the recently saved module being notified right after saving.
			// TODO: this should be no prob' anymore once the Cache uses Versioning - I will soon implement this - too busy right now. Marco.
			if (recentlySaved) {
				recentlySaved = false;
				return;
			}

			final ConfigModule updatedModule = getConfigModuleController().retrieveConfigModule(getProgressMonitor());

			// Check if cache sent the same version of the GroupModule after the ChildModule got changed.
			// This might happen, since the cache removes all objects depending on a changed one.
			// Child ConfigModule changes -> GroupModule will be removed from Cache as well.
//			if (JDOHelper.getVersion(currentConfigModule) == JDOHelper.getVersion(updatedModule))
			// --> not applicable, since change in ChildConfigModule results in new Version of GroupConfigModule
//			if (currentConfigModule.isGroupConfigModule() && currentConfigModule.isContentEqual(updatedModule))
			// --> not applicable either, since isContentEqual needs to rely on equals of the the members
			//     of every ConfigModule and equals of JDOObjects is agreed to be true iff the corresponding
			//     JDOObjectIDs are equal.
			// TODO: Hence, we need a new way of checking whether the content of two given ConfigModules is equal! -> see Marcos comment about versioning above
			// Until then we reload the page iff the currentModule hasn't changed, else we ask the user if the page shall be reloaded.

			/**
			 * FIXME: Do i get notified when the membership to a ConfigGroup ends?
			 * 	if so -> change updateGUIwith(module) accordingly.
			 */
			boolean updatedModuleIsGroupMember = getConfigModuleController().checkIfIsGroupMember(updatedModule);
			final boolean updatedModuleIsEditable = getConfigModuleController().canEdit(updatedModule);

			if (updatedModuleIsGroupMember) {
				if (! updatedModuleIsEditable || ! currentConfigModuleIsEditable) {
					// simply update the view
					Display.getDefault().asyncExec( new Runnable() {
						public void run() {
							if (fadableWrapper.isDisposed())
								return;

							getConfigModuleController().updateGuiWith(updatedModule);
						}
					});
				} // (! updatedModuleIsEditable || ! currentConfigModuleIsEditable)
				else {
					if (inheritMemberConfigModule.getSelection()) {
						// we are in a group and the memberConfigModule wants to inherit settings
						// -> synchronous update
						Display.getDefault().asyncExec( new Runnable() {
							public void run() {
								if (fadableWrapper.isDisposed())
									return;

								getConfigModuleController().updateGuiWith(updatedModule);
								setEditable(true);
							}
						});
					} // (inheritMemberConfigModule.getSelection())
					else {
						// the memberConfigModule does not want to inherit settings -> inform user
						Display.getDefault().asyncExec( new Runnable() {
							public void run() {
								if (fadableWrapper.isDisposed())
									return;

								ChangedConfigModulePagesDialog.addChangedConfigModule(AbstractConfigModulePreferencePage.this, updatedModule);
							}
							});
					} // (! inheritMemberConfigModule.getSelection())
				} // (updatedModuleIsEditable && currentConfigModuleIsEditable)
			} // (updatedModuleIsGroupMember)
//			if (! currentConfigIsGroupMember) {
//				// FIXME: How to show the user that the Config is now a group member?
//			}
//			else {
////				if (! currentConfigModule.getConfig().equals(updatedModule.getConfig()))
//				// FIXME: How to show Group changed?
//			}
		else { // (! updatedModuleIsGroupMember)
			if (! currentConfigModuleIsEditable) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						getConfigModuleController().updateGuiWith(updatedModule);
						setEditable(true);
					}
				});
			}	else {
				if (! configChanged) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							if (fadableWrapper.isDisposed())
								return;

							getConfigModuleController().updateGuiWith(updatedModule);
							setEditable(true);
						}
					});
				} else {
					Display.getDefault().asyncExec( new Runnable() {
						public void run() {
							if (fadableWrapper.isDisposed())
								return;

							ChangedConfigModulePagesDialog.addChangedConfigModule(AbstractConfigModulePreferencePage.this, updatedModule);
						}
					});
				}
			} // (! currentModuleIsEditable)
		} // (! updatedModuleIsGroupMember)

		} // notify(NotificationEvent notificationEvent)
	}; // ConfigModuleChangeListener

	/**
	 * Whether the current config has changed since it was last set.
	 *
	 * @return Whether the current config has changed since it was last set.
	 */
	public boolean isConfigChanged() {
		return configChanged;
	}

	/**
	 * Use this to mark the config as changed/not changed in your implementation.
	 * <p>
	 * Configurations will only be stored to the server if {@link #isConfigChanged()}
	 * returns true when the user decides to store.
	 *
	 * @param configChanged The changed flag for the Config.
	 */
	protected void setConfigChanged(boolean configChanged) {
		if (!currentConfigModuleIsEditable)
			return;

		this.configChanged = configChanged;
		if (configChanged) {
			recentlySaved = false;
			notifyConfigChangedListeners();
		}
	}

	/**
	 * doSetControl Whether to call super.setControl() wich is only needed, when inside the Preferences Dialog.
	 */
	protected boolean doSetControl = false;

	private EditLockHandle lockHandle = null;

	@Override
	public void createPartContents(Composite parent)
	{
		fadableWrapper = new FadeableComposite(parent, SWT.NONE, LayoutMode.NONE, LayoutDataMode.NONE);

		// create temporary label for loading time
		StackLayout layout = new StackLayout();
		fadableWrapper.setLayout(layout);
		loading = new XComposite(fadableWrapper, SWT.NONE);
		Label label = new Label(loading, SWT.NONE);
		label.setText(Messages.getString("org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage.loadingLabel")); //$NON-NLS-1$
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		layout.topControl = loading;
		fadableWrapper.setFaded(true);

		loadingDone = new XComposite(fadableWrapper, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		header = new XComposite(loadingDone, SWT.NONE, LayoutMode.ORDINARY_WRAPPER);
		header.getGridData().grabExcessVerticalSpace = false;
		header.setBackgroundMode(SWT.INHERIT_FORCE); // doesn't seem to work
		bodyWrapper = new FadeableComposite(loadingDone, SWT.NONE);
		bodyWrapper.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		bodyWrapper.setLayout(XComposite.getLayout(LayoutMode.TIGHT_WRAPPER));
		body = new XComposite(bodyWrapper, SWT.NONE, LayoutMode.TIGHT_WRAPPER);

		Job fetchJob = new Job(Messages.getString("org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage.fetchJobName")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) {
				monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage.gettingModuleDataTask"), 3); //$NON-NLS-1$
				getConfigModuleController().setConfigModule(getConfigModuleController().retrieveConfigModule(monitor));
				currentConfigIsGroupMember = getConfigModuleController().checkIfIsGroupMember(getConfigModuleController().getConfigModule());
				currentConfigModuleIsEditable = getConfigModuleController().canEdit(getConfigModuleController().getConfigModule());

				monitor.worked(1);

				if (currentConfigModuleIsEditable) {
					if (doSetControl) {
						lockHandle = EditLockMan.sharedInstance().acquireEditLock(JFireBaseEAR.EDIT_LOCK_TYPE_ID_CONFIG_MODULE,
								(ConfigModuleID) JDOHelper.getObjectId(getConfigModuleController().getConfigModule()),
								Messages.getString("org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage.editLockWarning"), //$NON-NLS-1$
								null, null, createSubProgressMonitorWrapper(1));
					}
				}

				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						setUpGui();
						updateConfigHeader();
						updatePreferencePage();
						fadableWrapper.setFaded(false);
						setEditable(currentConfigModuleIsEditable);
					}
				});

				monitor.worked(1);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		fetchJob.setPriority(org.eclipse.core.runtime.jobs.Job.SHORT);
		fetchJob.schedule();

		if (logger.isDebugEnabled())
			logger.debug("createContents: registering changeListener"); //$NON-NLS-1$

		JDOLifecycleManager.sharedInstance().addNotificationListener(getConfigModuleController().getConfigModuleClass(), changeListener);
		fadableWrapper.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (logger.isDebugEnabled())
					logger.debug("widgetDisposed: UNregistering changeListener"); //$NON-NLS-1$

				configChangedListeners.clear();
				JDOLifecycleManager.sharedInstance().removeNotificationListener(getConfigModuleController().getConfigModuleClass(), changeListener);
//				changeListener = null;
			}
		});

		if (doSetControl) {
			setControl(fadableWrapper);
			fadableWrapper.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (lockHandle != null)
						lockHandle.release();
				}
			});
		}
	}

	/**
	 * Initialises the main GUI elements: The header and the body of the preference page.
	 * It will be called by the job fetching getConfigModuleController().getConfigModule() data.
	 */
	protected void setUpGui()
	{
		if (!header.isDisposed()) {
			if (getConfigModuleController().getConfigModule().isGroupConfigModule()) {
				createConfigGroupHeader(header);
			}
			else {
				createConfigMemberHeader(header);
			}
		}
		if (!body.isDisposed()) {
			createPreferencePage(body);
		}
		if (!fadableWrapper.isDisposed()) {
			StackLayout layout = (StackLayout) fadableWrapper.getLayout();
			layout.topControl = loadingDone;
			fadableWrapper.layout(true, true);
		}
	}

	/**
	 * Updates the {@link #header} of the preference page according to the state of controller's
	 * config module.
	 */
	public void updateConfigHeader() {
		if (getConfigModuleController().getConfigModule().isGroupConfigModule()) {
			if (checkBoxAllowOverride == null || checkBoxAllowOverride.isDisposed())
				return;

			checkBoxAllowOverride.setSelection(
					(getConfigModuleController().getConfigModule().getFieldMetaData(ConfigModule.class.getName()).getWritableByChildren()
					& FieldMetaData.WRITABLEBYCHILDREN_YES) != 0);

			checkBoxAllowOverride.setBackground(header.getBackground());
		} else {

			if (inheritMemberConfigModule == null || inheritMemberConfigModule.isDisposed())
				return;

			if (! currentConfigIsGroupMember) {
				inheritMemberConfigModule.setEnabled(false);
				inheritMemberConfigModule.setCaption(Messages.getString("org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage.noGroup")); //$NON-NLS-1$
				return;
			}

			inheritMemberConfigModule.setSelection(
					getConfigModuleController().getConfigModule().getFieldMetaData(ConfigModule.class.getName()).isValueInherited());

			inheritMemberConfigModule.setEnabled(currentConfigModuleIsEditable);

			if (! currentConfigModuleIsEditable)
				inheritMemberConfigModule.setCaption(Messages.getString("org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage.GroupDisallowsOverwrite")); //$NON-NLS-1$
			else
				inheritMemberConfigModule.setCaption(Messages.getString("org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage.inheritFromGroup")); //$NON-NLS-1$

			inheritMemberConfigModule.adaptToToolkit();
			inheritMemberConfigModule.layout(true, true);
		}
	}

	/**
	 * Create the header showing controls for {@link ConfigModule}s of {@link ConfigGroup}s.
	 * <p>
	 * Default implementation adds a checkbox for controlling overwriting for the complete module.
	 *
	 * @param parent The parent to add the header to. It should be empty and have a GridLayout.
	 *
	 * @see #createConfigMemberHeader(Composite)
	 */
	protected void createConfigGroupHeader(Composite parent) {
		checkBoxAllowOverride = new Button(parent, SWT.CHECK);
		checkBoxAllowOverride.setSelection(
				(getConfigModuleController().getConfigModule().getFieldMetaData(ConfigModule.class.getName()).getWritableByChildren()
						& FieldMetaData.WRITABLEBYCHILDREN_YES) != 0
						);
		checkBoxAllowOverride.setText(Messages.getString("org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage.WhetherGroupAllowsConfigOverwrite")); //$NON-NLS-1$

		checkBoxAllowOverride.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (getConfigModuleController().getConfigModule() == null || ! getConfigModuleController().getConfigModule().isGroupConfigModule())
					return;

				getConfigModuleController().getConfigModule().getFieldMetaData(ConfigModule.class.getName()).setWritableByChildren(
						checkBoxAllowOverride.getSelection() == true ? FieldMetaData.WRITABLEBYCHILDREN_YES
								:	FieldMetaData.WRITABLEBYCHILDREN_NO);
				setConfigChanged(true);
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		checkBoxAllowOverride.setLayoutData(gd);
	}

	/**
	 * Creates the header for {@link ConfigModule}s of non-{@link ConfigGroup}s.
	 * <p>
	 * The default implementation creates an {@link InheritanceToggleButton} with a apropriate
	 * caption. <br>
	 * @see #createConfigGroupHeader(Composite)
	 *
	 * @param parent the Composite in which to place the header controls. It should be empty and have
	 * 	a GridLayout.
	 */
	protected void createConfigMemberHeader(Composite parent) {
		inheritMemberConfigModule = new InheritanceToggleButton(parent, ""); //$NON-NLS-1$
		inheritMemberConfigModule.setSelection(getConfigModuleController().getConfigModule().getFieldMetaData(ConfigModule.class.getName()).isValueInherited());

		inheritMemberConfigModule.setEnabled(currentConfigModuleIsEditable);

		if (! currentConfigModuleIsEditable)
			inheritMemberConfigModule.setCaption(Messages.getString("org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage.GroupDisallowsOverwrite")); //$NON-NLS-1$
		else
			inheritMemberConfigModule.setCaption(Messages.getString("org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage.inheritFromGroup")); //$NON-NLS-1$

		if (currentConfigModuleIsEditable)
		{
			inheritMemberConfigModule.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					if (getConfigModuleController().getConfigModule() == null || !getConfigModuleController().getConfigModule().getFieldMetaData(ConfigModule.class.getName()).isWritable())
						return;

					boolean selected = inheritMemberConfigModule.getSelection();
					getConfigModuleController().getConfigModule().getFieldMetaData(
							ConfigModule.FIELD_NAME_FIELDMETADATA_CONFIGMODULE).setValueInherited(selected);

//				FIXME: The first time inheritance is triggered, the valueInherited value is here set to true (look deeper)
					if (selected)
					{
						setBodyContentEditable(false);
						fadableWrapper.setFaded(true);
						inheritMemberConfigModule.setEnabled(false);
						Job fetchJob = new Job(Messages.getString("org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage.fetchJobName")) { //$NON-NLS-1$
							@Override
							protected IStatus run(ProgressMonitor monitor) {
//						FIXME: and is in this job, when read, FALSE!!!! Damn f%&§$=! bug!
//							ConfigID groupID = ConfigSetupRegistry.sharedInstance().getGroupForConfig(
//									ConfigID.create(getConfigModuleController().getConfigModule().getOrganisationID(),
//											getConfigModuleController().getConfigModule().getConfigKey(),
//											getConfigModuleController().getConfigModule().getConfigType()
//									));
//							ConfigModule(groupID,
//									getConfigModuleClass(), configModuleManager.getConfigModuleID(), getConfigModuleFetchGroups().toArray(new String[] {}),
//									getConfigModuleMaxFetchDepth(), monitor);
								ConfigModule groupModule = ConfigModuleDAO.sharedInstance().getGroupsCorrespondingModule(
										configModuleManager.getConfigID(), getConfigModuleController().getConfigModuleClass(),
										configModuleManager.getConfigModuleID(), getConfigModuleController().getConfigModuleFetchGroups().toArray(new String[0]),
										getConfigModuleController().getConfigModuleMaxFetchDepth(), monitor
								);

								InheritanceManager inheritanceManager = new InheritanceManager();
								inheritanceManager.inheritAllFields(groupModule, getConfigModuleController().getConfigModule());

								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										updatePreferencePage();
										fadableWrapper.setFaded(false);
										inheritMemberConfigModule.setEnabled(true);
										inheritMemberConfigModule.setSelection(true);
										setConfigChanged(true);
									}
								});
								return Status.OK_STATUS;
							}
						};
						fetchJob.setPriority(org.eclipse.core.runtime.jobs.Job.SHORT);
						fetchJob.schedule();
					}
					else
					{
						setBodyContentEditable(true);
//						getConfigModuleController().getConfigModule().getFieldMetaData(ConfigModule.FIELD_NAME_FIELDMETADATA_CONFIGMODULE).setValueInherited(false);
//						setConfigChanged(true);
					}
				}
			});
		}
	}

	/**
	 * Default implementation uses the {@link #bodyWrapper} to grey out all content and disable it.
	 * Subclasses may override in order to NOT disable tables, etc. so the user can still scroll.
	 *
	 * <p><b>Note</b>: This method is only triggered when the user decides to inherit the values of
	 * the group's ConfigModule as opposed to {@link #setEditable(boolean)} which en/disables the
	 * whole ConfigModule page, i.e. the header as well as the body is en/disabled and this is done
	 * when the group's ConfigModule does NOT allow to override values.</p>
	 *
	 * @param editable whether or not the
	 */
	protected void setBodyContentEditable(boolean editable)
	{
	 // grey out config module, so user knows that values are inherited.
		bodyWrapper.setFaded(! editable);
	}

	/**
	 * Returns the wrapper of the body into which the config module page is created into.
	 * @return the wrapper of the body into which the config module page is created into.
	 */
	protected FadeableComposite getBodyWrapper()
	{
		return bodyWrapper;
	}

	/**
	 * Called to ask the receiver to create its UI representation.
	 * The parent will be a Composite with a GridLayout.
	 *
	 * @param parent The Composite into which the preference page should be created.
	 */
	protected abstract void createPreferencePage(Composite parent);

	/**
	 * Will be called when the UI has to be updated with values of
	 * a new ConfigModule.
	 *
	 * @param configModule The currently edited ConfigModule
	 */
	protected abstract void updatePreferencePage();

	/**
	 * Should change the GUI to either an editable
	 * or an read-only version of the view of the current ConfigModule.
	 * The default implementation recursively disables/enables
	 * all Buttons of this preference-page.
	 * This is intended to be extended for different behaviour on canEdit() == false.
	 */
	protected void setEditable(boolean editable)
	{
		if (fadableWrapper != null && !fadableWrapper.isDisposed())
			fadableWrapper.setEnabled(editable);

		// Reset the ToggleButton state in case the config is in no group -> it is editable
		// -> the fadableWrapper rekursively sets all elements enabled = true, although
		// the togglebutton should not be enabled!
		if (inheritMemberConfigModule != null && !currentConfigIsGroupMember)
			inheritMemberConfigModule.setEnabled(false);
	}

	/**
	 * This method is called on the UI-thread! <p>
	 * Here you should update the config module with the data from specific UI.
	 */
	public abstract void updateConfigModule();

	public String getSimpleClassName()
	{
		int index = getConfigModuleController().getConfigModuleClass().getName().lastIndexOf("."); //$NON-NLS-1$
		return getConfigModuleController().getConfigModuleClass().getName().substring(index+1, getConfigModuleController().getConfigModuleClass().getName().length()-1);
	}

	/**
	 * Should return the cfModID of the ConfigModule this preference page
	 * does edit. This method is intended to be overridden. The default
	 * implementation returns null.
	 *
	 * @return null
	 */
//	public String getConfigModuleCfModID() {
//		return null;
//	}

	/**
	 * Default implementation does nothing. Subclasses (AbstractUser..., AbstractWorkstation...) have
	 * to set the <code>configID</code> of the their context for this PreferencePage. The
	 * {@link AbstractUserConfigModulePreferencePage}, for example, sets the configID of the Config
	 * attached to the current Userdata.
	 * <p>
	 * This method is called by the PreferencePage-Framework of Eclipse but not by the Config-Framework
	 * of JFire. <br>
	 *
	 * If this page shall be embeded in another Context use {@link #createContents(Composite, ConfigID)}.
	 *
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		super.init(workbench);
	}

	/**
	 * Calls implementors to {@link #updateConfigModule(ConfigModule)} and
	 * stores the updatedConfig module to the server.
	 *
	 * @param doUpdateGUI Whether the {@link #updatePreferencePage()} method should be invoked
	 * 		to cause the page to re-display the recently stored ConfigModule.
	 */
	public void storeConfigModule(boolean doUpdateGUI) {
		if (Thread.currentThread() == Display.getDefault().getThread()) {
			logger.error("This method must not be called on the GUI-thread! Use a job!",  //$NON-NLS-1$
					new Exception("This method must not be called on the GUI-thread! Use a job!")); //$NON-NLS-1$
//			throw new IllegalStateException("This method must not be called on the GUI-thread! Use a job!");
		}

		if (isConfigChanged()) {
			Display.getDefault().syncExec( new Runnable() {
				public void run() {
					if (getControl() != null && !getControl().isDisposed()) {
						updateConfigModule();
					}
				}
			});

			storeModule(doUpdateGUI);
			configChanged = false;
		} // if (isConfigCachanged())
	}

	/**
	 * Is needed to omit a user interaction after the cache notifies this page that a newer {@link ConfigModule}
	 * is available, which is only there since it was just recently saved!
	 *
	 * TODO: When Marco has implemented versioning in the cache, this can be removed.
	 */
	private boolean recentlySaved = false;

	/**
	 * Stores the <code>currentConfigModule</code> to the datastore.
	 * @param doUpdateGUI Whether the {@link #updatePreferencePage()} method should be invoked
	 * 		to cause the page to re-display the recently stored ConfigModule.
	 */
	protected void storeModule(boolean doUpdateGUI) {
		ConfigManagerRemote configManager;
		try {
			configManager = JFireEjb3Factory.getRemoteBean(ConfigManagerRemote.class, Login.getLogin().getInitialContextProperties());
		} catch (LoginException e) {
			throw new RuntimeException(e);
		}
		ConfigModule storedConfigModule = configManager.storeConfigModule(
				getConfigModuleController().getConfigModule(), true, getConfigModuleController().getConfigModuleFetchGroups().toArray(new String[] {}),
				getConfigModuleController().getConfigModuleMaxFetchDepth()
		);

		Cache.sharedInstance().put(null, storedConfigModule, getConfigModuleController().getConfigModuleFetchGroups(),
				getConfigModuleController().getConfigModuleMaxFetchDepth()
		);
		getConfigModuleController().setConfigModule(storedConfigModule);
		if (doUpdateGUI) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (getControl() != null && !getControl().isDisposed())
						updatePreferencePage();
				}
			});
		}
		recentlySaved = true;
	}

	@Override
	public boolean performOk() {
		if (isConfigChanged()) {
			// this has to be done here
			// because in the job it is skipped
			// when the dialog was closed already.
			updateConfigModule();
		}
		Job storeJob = new Job(Messages.getString("org.nightlabs.jfire.base.ui.config.AbstractConfigModulePreferencePage.storeJob.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				storeConfigModule(false);
				return Status.OK_STATUS;
			}
		};

		storeJob.setPriority(org.eclipse.core.runtime.jobs.Job.SHORT);
		storeJob.schedule();

		return true;
	}

	@Override
	protected void updateApplyButton() {
		updateConfigModule();
		storeConfigModule(true);
	}

	/**
	 * A list of listeners that shall be triggered if this module changes.
	 * (see {@link #notifyConfigChangedListeners()})
	 */
	private final List<ConfigPreferenceChangedListener> configChangedListeners =
							new ArrayList<ConfigPreferenceChangedListener>();

	/**
	 * Call this when you modified the entity object.
	 */
	public void notifyConfigChangedListeners()
	{
		Iterator<ConfigPreferenceChangedListener> i = configChangedListeners.iterator();
		while(i.hasNext())
			i.next().configPreferenceChanged(this);
	}

	/**
	 * Listen for modifications of the entity object
	 * @param listener your listener
	 */
	public void addConfigPreferenceChangedListener(ConfigPreferenceChangedListener listener)
	{
		if(!configChangedListeners.contains(listener))
			configChangedListeners.add(listener);
	}

	/**
	 * Remove a listener
	 * @param listener the listener
	 */
	public void removeDataChangedListener(ConfigPreferenceChangedListener listener)
	{
		if(configChangedListeners.contains(listener))
			configChangedListeners.remove(listener);
	}

	/**
	 * Returns an {@link IDirtyStateManager} that may be handed over to embedded controls.
	 * @return An {@link IDirtyStateManager} that may be handed over to embedded controls.
	 */
	public IDirtyStateManager getPageDirtyStateManager() {
		return new IDirtyStateManager() {
			public void markUndirty() {
				setConfigChanged(false);
			}
			public void markDirty() {
				setConfigChanged(true);
			}
			public boolean isDirty() {
				return false;
			}
		};
	}

}
