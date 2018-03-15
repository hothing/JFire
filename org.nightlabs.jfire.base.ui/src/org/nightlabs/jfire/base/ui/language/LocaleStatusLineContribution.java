/**
 *
 */
package org.nightlabs.jfire.base.ui.language;

import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.action.AbstractContributionItem;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.language.LanguageManager;
import org.nightlabs.util.NLLocale;

/**
 * StatusLine Contribution that displays the current Locale to the user.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class LocaleStatusLineContribution
extends AbstractContributionItem
{
	private XComposite wrapper;
	private Button flagButton;
	private Label text;

	public LocaleStatusLineContribution(String name, boolean fillToolBar, boolean fillCoolBar, boolean fillMenuBar, boolean fillComposite) {
		super(LocaleStatusLineContribution.class.getName(), name, fillToolBar, fillCoolBar, fillMenuBar, fillComposite);
		init();
	}

	public LocaleStatusLineContribution(String name) {
		super(LocaleStatusLineContribution.class.getName(), name);
		init();
	}

	private void init() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.action.AbstractContributionItem#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createControl(Composite parent) {
		wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		StatusLineLayoutData layoutData = new StatusLineLayoutData();
		layoutData.widthHint = 100;
		wrapper.setLayoutData(layoutData);
		wrapper.getGridLayout().numColumns = 2;
		wrapper.getGridLayout().makeColumnsEqualWidth = false;
		flagButton = new Button(wrapper, SWT.FLAT);
		flagButton.setImage(LanguageManager.sharedInstance().getFlag16x16Image(NLLocale.getDefault().getLanguage()));
		flagButton.setLayoutData(new GridData());
		flagButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new SwitchLanguageDialog(wrapper.getShell()).open();
			}
		});
		text = new Label(wrapper, SWT.NONE);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setText(NLLocale.getDefault().getDisplayLanguage());
		return wrapper;
	}

}
