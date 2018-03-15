package org.nightlabs.jfire.querystore.ui;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.I18nTextEditorMultiLine;
import org.nightlabs.base.ui.language.LanguageChooserCombo;
import org.nightlabs.base.ui.language.LanguageChooserCombo.Mode;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.query.store.BaseQueryStore;
import org.nightlabs.jfire.query.store.QueryStore;
import org.nightlabs.jfire.querystore.ui.resource.Messages;

/**
 * Edits the given {@link BaseQueryStore} - its name, description and the public availabiltiy flag.
 *
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class QueryStoreEditDialog extends TitleAreaDialog
{
	private Button publicAvailableButton;
	private boolean publiclyAvailable;

	private I18nTextEditor nameEditor;
	private I18nTextEditorMultiLine descriptionEditor;

	private QueryStore editedStore;

	public QueryStoreEditDialog(Shell parentShell, QueryStore store)
	{
		super(parentShell);
		assert store != null;
		this.editedStore = store;
		this.publiclyAvailable = store.isPubliclyAvailable();
	}

	@Override
	protected int getShellStyle()
	{
		return super.getShellStyle() | SWT.RESIZE;
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText(Messages.getString("org.nightlabs.jfire.querystore.ui.QueryStoreEditDialog.shellTitle")); //$NON-NLS-1$
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		XComposite wrapper = new XComposite(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER, 3);
		LanguageChooserCombo languageChooser = new LanguageChooserCombo(wrapper, Mode.iconAndText);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		languageChooser.setLayoutData(gd);
		publicAvailableButton = new Button(wrapper, SWT.CHECK);
		publicAvailableButton.setText(Messages.getString("org.nightlabs.jfire.querystore.ui.QueryStoreEditDialog.queryPubliclyAvailableButton")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalAlignment = SWT.RIGHT;
		publicAvailableButton.setLayoutData(gd);
		publicAvailableButton.setSelection(publiclyAvailable);
		publicAvailableButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				publiclyAvailable = ((Button) e.getSource()).getSelection();
			}
		});

		Label nameLabel = new Label(wrapper, SWT.NONE);
		nameLabel.setText(Messages.getString("org.nightlabs.jfire.querystore.ui.QueryStoreEditDialog.queryNameLabel")); //$NON-NLS-1$
    nameEditor = new I18nTextEditor(wrapper, languageChooser);
    nameEditor.setI18nText(editedStore.getName());
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    nameEditor.setLayoutData(gd);

		Label descriptionLabel = new Label(wrapper, SWT.NONE);
		descriptionLabel.setText(Messages.getString("org.nightlabs.jfire.querystore.ui.QueryStoreEditDialog.queryDescriptionLabel")); //$NON-NLS-1$
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		descriptionLabel.setLayoutData(gd);

		descriptionEditor = new I18nTextEditorMultiLine(wrapper, languageChooser);
		descriptionEditor.setI18nText(editedStore.getDescription());
    gd = new GridData(GridData.FILL_BOTH);
    gd.horizontalSpan = 2;
		descriptionEditor.setLayoutData(gd);

    applyDialogFont(wrapper);
		setMessage(Messages.getString("org.nightlabs.jfire.querystore.ui.QueryStoreEditDialog.dialogDescription")); //$NON-NLS-1$
		setTitle(Messages.getString("org.nightlabs.jfire.querystore.ui.QueryStoreEditDialog.dialogTitle")); //$NON-NLS-1$

		nameEditor.setFocus();
    return wrapper;
	}

	@Override
	protected void okPressed()
	{
		nameEditor.copyToOriginal();
		descriptionEditor.copyToOriginal();
		if (editedStore != null)
		{
			editedStore.setPubliclyAvailable(publiclyAvailable);
		}
		super.okPressed();
	}

	public I18nText getI18NText()
	{
		return nameEditor.getI18nText();
	}

	public boolean isPubliclyAvailable()
	{
		return publiclyAvailable;
	}
}