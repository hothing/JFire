/**
 * 
 */
package org.nightlabs.jfire.geography.admin.ui.templatedata.management.country;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.wizard.WizardHopPage;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.admin.ui.templatedata.editor.name.GeographyNameTableComposite;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.NLLocale;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class CountryNewPage 
extends WizardHopPage 
{
	private GeographyNameTableComposite geographyNameTableComposite;	
	private Text countryIsoCodeText;
	private String countryIsoCode;
	
	/**
	 * @param pageName
	 */
	public CountryNewPage() {
		super(CountryNewPage.class.getName());
		setTitle(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.country.CountryNewPage.title")); //$NON-NLS-1$
		setDescription(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.country.CountryNewPage.description")); //$NON-NLS-1$
		setPageComplete(false);
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.wizard.DynamicPathWizardPage#createPageContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createPageContents(Composite parent) {
		XComposite wizardPageComposite = new XComposite(parent, SWT.NONE);
//		wizardPageComposite.getGridLayout().numColumns = 2;
	
		XComposite textComp = new XComposite(wizardPageComposite, SWT.NONE);		
		textComp.getGridLayout().numColumns = 2;
		textComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label isoCodeLabel = new Label(textComp, SWT.NONE);
		isoCodeLabel.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.country.CountryNewPage.label.isoCountryCode")); //$NON-NLS-1$
		countryIsoCodeText = new Text(textComp, SWT.BORDER);
		countryIsoCodeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		countryIsoCodeText.addModifyListener(modifyListener);
		
		geographyNameTableComposite = new GeographyNameTableComposite(wizardPageComposite, SWT.NONE, false);
		GridData gd = new GridData(GridData.FILL_BOTH);
		geographyNameTableComposite.setLayoutData(gd);
		
		return wizardPageComposite;
	}

	private ModifyListener modifyListener = new ModifyListener(){
		@Override
		public void modifyText(ModifyEvent arg0) {
			final String text = countryIsoCodeText.getText();
			countryIsoCode = text;
			int textLength = text.length();
			if (textLength == 2) 
			{
				if (text.toUpperCase().equals(text)) {
					final CountryID countryID = CountryID.create(text);
					Job loadCountryJob = new Job(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.country.CountryNewPage.job.checkCountry.name")) { //$NON-NLS-1$
						@Override
						protected IStatus run(ProgressMonitor monitor) throws Exception 
						{
							Country country = Geography.sharedInstance().getCountry(countryID, false);
							String message = null;
							if (country != null) {							
								message = String.format(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.country.CountryNewPage.errorMessage.countryAlreadyExisting"), country.getName().getText(NLLocale.getDefault()), text); //$NON-NLS-1$
							}
							final String errorMessage = message; 
							Display.getDefault().syncExec(new Runnable(){
								@Override
								public void run() {
									updateStatus(errorMessage);
								}
							});
							return Status.OK_STATUS;
						}
					};
					loadCountryJob.schedule();					
				} else {
					updateStatus(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.country.CountryNewPage.errorMessage.uppercase")); //$NON-NLS-1$
				}
			} 
			else {
				updateStatus(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.country.CountryNewPage.errorMessage.2letters")); //$NON-NLS-1$
			}
		}
	};
		
	protected GeographyNameTableComposite getGeographyNameTableComposite(){
		return geographyNameTableComposite;
	}
	
	/**
	 * Returns the two letter ISO 3166 country code of the new country.
	 * @return the two letter ISO 3166 country code
	 */
	public String getCountryIsoCode() {
		return countryIsoCode;
	}
	
}
