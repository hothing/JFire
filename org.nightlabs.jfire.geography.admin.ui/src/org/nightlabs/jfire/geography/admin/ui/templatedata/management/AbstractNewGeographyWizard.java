/**
 * 
 */
package org.nightlabs.jfire.geography.admin.ui.templatedata.management;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.admin.ui.templatedata.GeographyTemplateDataAdminPerspective;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public abstract class AbstractNewGeographyWizard 
extends Wizard 
implements INewWizard 
{	
	private boolean openGeoPerspective = false;
	
	public AbstractNewGeographyWizard() {
		this(true);
	}

	public AbstractNewGeographyWizard(boolean openGeoPerspectiveWhenFinished) {
		super();
		this.openGeoPerspective = openGeoPerspectiveWhenFinished;
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		if (getGeographyTemplateDataTreeView() != null) {
			Geography.sharedInstance().getCountries();
		}
	}

	protected GeographyTemplateDataTreeView getGeographyTemplateDataTreeView() 
	{
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchPart part = workbench.getActiveWorkbenchWindow().getActivePage().getActivePart();
		if (part instanceof GeographyTemplateDataTreeView){
			return (GeographyTemplateDataTreeView) part;
		}
		return null;
	}
	
	protected void openGeoPerspective() 
	{
		if (openGeoPerspective) {
			String perspectiveId = RCPUtil.getActivePerspectiveID();
			if (!GeographyTemplateDataAdminPerspective.ID_PERSPECTIVE.equals(perspectiveId)) {
				getShell().getDisplay().asyncExec(new Runnable(){
					@Override
					public void run() {
						try {
							PlatformUI.getWorkbench().showPerspective(GeographyTemplateDataAdminPerspective.ID_PERSPECTIVE, 
									PlatformUI.getWorkbench().getActiveWorkbenchWindow());
						} catch (WorkbenchException e) {
							throw new RuntimeException(e);
						}				
					}
				});	
			}
		}
	}
}
