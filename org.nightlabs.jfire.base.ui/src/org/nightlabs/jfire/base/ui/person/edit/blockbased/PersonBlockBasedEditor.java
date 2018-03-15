/**
 * 
 */
package org.nightlabs.jfire.base.ui.person.edit.blockbased;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.base.ui.language.LanguageManager;
import org.nightlabs.jfire.base.ui.prop.edit.blockbased.BlockBasedEditor;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.language.LanguageCf;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.NLLocale;

/**
 * A {@link BlockBasedEditor} that adds a control to edit a persons locale to the editors header.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class PersonBlockBasedEditor extends BlockBasedEditor {
	
	private List<Locale> locales;
	private XComboComposite<Locale> localeCombo;

	public PersonBlockBasedEditor() {
		super(true);
		locales = CollectionUtil.array2ArrayList(Locale.getAvailableLocales());

		Set<String> languageIDs = new HashSet<String>();
		for (LanguageCf languageCf : LanguageManager.sharedInstance().getLanguages())
			languageIDs.add(languageCf.getLanguageID());

		for (Iterator<Locale> it = locales.iterator(); it.hasNext();) {
			Locale locale = it.next();
			if (!languageIDs.contains(locale.getLanguage()))
				it.remove();
		}

		Collections.sort(locales, new Comparator<Locale>() {
			@Override
			public int compare(Locale o1, Locale o2) {
				return o1.getDisplayName(NLLocale.getDefault()).compareTo(o2.getDisplayName(NLLocale.getDefault()));
			}
		});
	}


	@Override
	protected Composite createHeaderComposite(Composite parent) {
		XComposite carrier = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		carrier.getGridData().grabExcessVerticalSpace = false;
		carrier.getGridLayout().numColumns = 3;
		carrier.getGridLayout().makeColumnsEqualWidth = true;

		Composite displayNameComposite = super.createHeaderComposite(carrier);
		GridData gd = (GridData) displayNameComposite.getLayoutData();
		gd.horizontalSpan = 2;

		XComposite carrier2 = new XComposite(carrier, SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER);
		Label localeLabel = new Label(carrier2, SWT.NONE);
		localeLabel.setText(Messages.getString("org.nightlabs.jfire.base.ui.person.edit.blockbased.PersonBlockBasedEditorSection.label.language")); //$NON-NLS-1$
		localeCombo = new XComboComposite<Locale>(
				carrier2, SWT.READ_ONLY,
				new LabelProvider() {
					@Override
					public String getText(Object element) {
						Locale locale = (Locale) element;
						return locale.getDisplayName(NLLocale.getDefault());
					}
				}
		);
		localeCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Person person = ((Person) getPropertySet());
				Locale oldLocale = person.getLocale();
				person.setLocale(localeCombo.getSelectedElement());
				getPropertyChangeSupport().firePropertyChange(Person.PROP_LOCALE, oldLocale, person.getLocale());
			}
		});
		
		localeCombo.getDisplay().asyncExec(new Runnable() {
			public void run() {
				// done async, so layout does not care about content...
				localeCombo.addElements(locales);
			}
		});

		return carrier;
	}

	@Override
	public void refreshControl() {
		super.refreshControl();
		Locale personLocale = ((Person)getPropertySet()).getLocale();
		localeCombo.selectElement(personLocale);
		if (localeCombo.getSelectedElement() == null) {
			localeCombo.addElement(personLocale);
			localeCombo.selectElement(personLocale);
		}
	}
}