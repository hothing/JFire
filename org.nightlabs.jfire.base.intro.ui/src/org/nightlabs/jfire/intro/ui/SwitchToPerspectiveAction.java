package org.nightlabs.jfire.intro.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.intro.IIntroConstants;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public abstract class SwitchToPerspectiveAction extends Action
{
	private String perspectiveId;
	
	protected SwitchToPerspectiveAction(String perspectiveId)
	{
		this.perspectiveId = perspectiveId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run()
	{
		switchToPerspective(perspectiveId);

		IViewPart introView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().
			findView(IIntroConstants.INTRO_VIEW_ID);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(introView);
	}
	
	private static void switchToPerspective(String perspectiveId)
	{
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
		IPerspectiveDescriptor perspective = workbench.getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
		activePage.setPerspective(perspective);
	}
}
