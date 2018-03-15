package org.nightlabs.jfire.base.ui.person.search;

import java.lang.reflect.InvocationTargetException;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.nightlabs.base.ui.progress.ProgressMonitorWrapper;
import org.nightlabs.base.ui.wizard.DynamicPathWizard;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.dao.PropertySetDAO;

/**
 * An Wizard for editing the properties of a {@link Person}.
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class PersonEditWizard extends DynamicPathWizard 
{
	private Person person;
	private PersonEditorWizardHop personEditorWizardHop;
	
	/**
	 * Creates a PersonEditWizard.
	 * @param person the Person to show / edit the properties
	 */
	public PersonEditWizard(Person person) 
	{
		if (person == null)
			throw new IllegalArgumentException("Param person must not be null!"); //$NON-NLS-1$
		
		this.person = person;
		personEditorWizardHop = new PersonEditorWizardHop();
		personEditorWizardHop.initialise(person);
		addPage(personEditorWizardHop.getEntryPage());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() 
	{
		personEditorWizardHop.updatePerson();
		try {
			getContainer().run(true, false, new IRunnableWithProgress(){
				@Override
				public void run(IProgressMonitor monitor) 
				throws InvocationTargetException, InterruptedException 
			    {
					try {
						monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.ui.person.search.PersonEditWizard.task.savingPerson"), 100); //$NON-NLS-1$
						person = (Person) PropertySetDAO.sharedInstance().storePropertySet(person, true, 
								new String[] {FetchPlan.DEFAULT, Person.FETCH_GROUP_FULL_DATA}, 
								NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, 
								new ProgressMonitorWrapper(monitor));
						monitor.worked(100);
						monitor.done();
					} catch (Throwable t) {
						throw new RuntimeException(t);
					}
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return true;
	}

}
