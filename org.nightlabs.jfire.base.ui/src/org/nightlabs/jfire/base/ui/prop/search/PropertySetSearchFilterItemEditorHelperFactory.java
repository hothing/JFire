package org.nightlabs.jfire.base.ui.prop.search;

public interface PropertySetSearchFilterItemEditorHelperFactory<Helper extends PropertySetSearchFilterItemEditorHelper> {
	/**
	 * Should create a new instance of a PropertySetSearchFilterItemEditorHelper.
	 */
	Helper createHelper();
}

