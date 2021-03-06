package org.nightlabs.jfire.issuetracking.ui.issue.create;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.DateTimeEdit;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.base.ui.wizard.WizardHopPage;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issuetracking.ui.IssueTrackingPlugin;
import org.nightlabs.l10n.DateFormatter;

/**
 * @author Chairat Kongarayawetchakun - chairat[at]nightlabs[dot]de
 */
public class CreateIssueReminderWizardPage 
extends WizardHopPage
{
	//GUI
	private Label deadlineLabel;
	private DateTimeEdit deadlineDateTimeEdit;
	
	private Issue issue;

	public CreateIssueReminderWizardPage(Issue issue) {
		super(CreateIssueReminderWizardPage.class.getName(), "Reminder Information", SharedImages.getWizardPageImageDescriptor(IssueTrackingPlugin.getDefault(), CreateIssueWizard.class)); //$NON-NLS-1$
		setDescription("Specify the information for reminder");
		this.issue = issue;
	}

	@Override
	public Control createPageContents(Composite parent) {
		XComposite mainComposite = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
		mainComposite.getGridLayout().numColumns = 2;

		deadlineLabel = new Label(mainComposite, SWT.NONE);
		deadlineLabel.setText("Finished Date: ");
		
		deadlineDateTimeEdit = new DateTimeEdit(
				mainComposite,
				DateFormatter.FLAGS_DATE_SHORT_TIME_HMS_WEEKDAY,
				new Date(),
				"",
				false);
		deadlineDateTimeEdit.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		deadlineDateTimeEdit.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				issue.setDeadlineTimestamp(deadlineDateTimeEdit.getDate());
			}
		});
		
		return mainComposite;
	}

	@Override
	public boolean isPageComplete() {
		return getErrorMessage() == null;
	}
}