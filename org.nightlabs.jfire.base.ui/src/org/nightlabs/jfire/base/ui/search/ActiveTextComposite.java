package org.nightlabs.jfire.base.ui.search;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.base.ui.resource.Messages;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ActiveTextComposite
extends XComposite
implements ActiveStateManager
{
	private Group group;
	private Button activeButton;
	private Text text;
	private Button browseButton;

	public ActiveTextComposite(Composite parent, String groupTitle,
			SelectionListener activeSelectionListener, SelectionListener browseSelectionListener)
	{
		super(parent, SWT.NONE);
		createComposite(this, groupTitle, activeSelectionListener, browseSelectionListener);
	}

	protected void createComposite(Composite parent, String groupTitle,
			SelectionListener activeSelectionListener, SelectionListener browseSelectionListener)
	{
		setLayout(new GridLayout());
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group = new Group(parent, SWT.NONE);
		group.setText(groupTitle);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		activeButton = new Button(group, SWT.CHECK);
		GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
		buttonData.horizontalSpan = 2;
		activeButton.setLayoutData(buttonData);
		activeButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.search.ActiveTextComposite.button.active")); //$NON-NLS-1$
		activeButton.addSelectionListener(activeSelectionListener);
		text = new Text(group, SWT.BORDER);
		text.setEnabled(false);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.addSelectionListener(browseSelectionListener);
		browseButton = new Button(group, SWT.NONE);
		browseButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.search.ActiveTextComposite.button.browse")); //$NON-NLS-1$
		browseButton.addSelectionListener(browseSelectionListener);
		browseButton.setEnabled(false);

		activeButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				text.setEnabled(((Button)e.getSource()).getSelection());
				browseButton.setEnabled(((Button)e.getSource()).getSelection());
			}
		});
	}

	public boolean isActive() {
		return activeButton.getSelection();
	}

	public void setActive(boolean active) {
		text.setEnabled(active);
		browseButton.setEnabled(active);
		activeButton.setSelection(active);
	}

	public void clear() {
		text.setText(""); //$NON-NLS-1$
	}

	public Button getActiveButton() {
		return activeButton;
	}

	public void setText(String text) {
		this.text.setText(text);
	}
}
