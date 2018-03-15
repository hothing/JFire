package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.language.LanguageChooser;
import org.nightlabs.jfire.prop.ModifyListener;

public interface StructPartEditor<P> {
	/**
	 * Here a part editor should add its control to a parent composite.<br/>
	 * The Composite returned should be a singleton and be updated with data changes.
	 * No data-display will be made here. See {@link #setData(Object)}.
	 *
	 * @param parent the parent of the composite
	 * @param style SWT style flag
	 * @param structEditor the structEditor
	 * @return a composite containing the struct part editor
	 */
	public Composite createComposite(Composite parent, int style, StructEditor structEditor, LanguageChooser languageChooser);
	
	/**
	 * Returns the composite
	 * @return the composite
	 */
	public Composite getComposite();
	
	/**
	 * Editors should refresh their data during this method. The composite should be refreshed as well.
	 * You have to call {@link #createComposite(Composite, int, StructEditor, LanguageChooser)} prior
	 * to calling this method.
	 * 
	 * @param data The data to be set.
	 */
	public void setData(P data);
	
	public void setFocus();

	public void setEnabled(boolean enabled);
	
	public void addModifyListener(ModifyListener modifyListener);
	
	public void removeModifyListener(ModifyListener modifyListener);
}
