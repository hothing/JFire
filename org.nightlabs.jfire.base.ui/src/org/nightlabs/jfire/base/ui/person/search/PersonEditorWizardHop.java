/**
 *
 */
package org.nightlabs.jfire.base.ui.person.search;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.wizard.WizardHop;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.config.ConfigUtil;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditConstants;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutConfigModule;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutEntry;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class PersonEditorWizardHop extends WizardHop {

	/**
	 * The personal page
	 */
	private PersonEditorWizardPersonalPage personalPage;
//	private FieldBasedCfModLayoutConfigWizardPage personalPage;
	private PersonEditorWizardOtherPage otherPage;
	private Job loadJob;
	private PropertySetFieldBasedEditLayoutConfigModule configModule;
	private Person person;

	/**
	 *
	 */
	public PersonEditorWizardHop() {
		loadJob = new Job(Messages.getString("org.nightlabs.jfire.base.ui.person.search.PersonEditorWizardHop.job.loadLayout.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				configModule = ConfigUtil.getUserCfMod(
						PropertySetFieldBasedEditLayoutConfigModule.class,
						PropertySetFieldBasedEditConstants.USE_CASE_ID_EDIT_PERSON,
						new String[] {
							FetchPlan.DEFAULT, PropertySetFieldBasedEditLayoutConfigModule.FETCH_GROUP_GRID_LAYOUT,
							PropertySetFieldBasedEditLayoutConfigModule.FETCH_GROUP_EDIT_LAYOUT_ENTRIES,
							PropertySetFieldBasedEditLayoutEntry.FETCH_GROUP_STRUCT_FIELD_ID, PropertySetFieldBasedEditLayoutEntry.FETCH_GROUP_GRID_DATA},
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
				return Status.OK_STATUS;
			}
		};
		loadJob.schedule();
	}


	public void initialise(Person person) {
		this.person = person;
		try {
			loadJob.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

//		if (personalPage == null) {
//			personalPage = new PersonEditorWizardPersonalPage(person);
//			setEntryPage(personalPage);
//		} else
//			personalPage.refresh(person);

		if (personalPage == null) {
			personalPage = new PersonEditorWizardPersonalPage();
			personalPage.setLayoutConfigModule(configModule);

			setEntryPage(personalPage);
			personalPage.getEditor().setPropertySet(person, false);
		} else {
			personalPage.getEditor().setPropertySet(person, true);
		}

		if (otherPage == null) {
			otherPage = new PersonEditorWizardOtherPage(person);
			addHopPage(otherPage);
		}
		else
			otherPage.refresh(person);
	}

	public void updatePerson() {
		if (getWizard().getContainer().getCurrentPage() == personalPage) {
			personalPage.getEditor().updatePropertySet();
//			personalPage.updatePropertySet();
		} else if (getWizard().getContainer().getCurrentPage() == otherPage) {
			otherPage.updatePropertySet();
		}
	}

	public Person getPerson() {
		return person;
	}
}