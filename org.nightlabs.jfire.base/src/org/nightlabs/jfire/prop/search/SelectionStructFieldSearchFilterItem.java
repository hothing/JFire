package org.nightlabs.jfire.prop.search;

import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.prop.datafield.SelectionDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class SelectionStructFieldSearchFilterItem extends PropSearchFilterItem
{
	private static final long serialVersionUID = 1L;
	private boolean addedParams = false;
	private String selectedStructFieldValueID;

	/**
	 * @param personStructFieldID
	 * @param matchType
	 * @param needle
	 */
	public SelectionStructFieldSearchFilterItem(
			StructFieldID personStructFieldID, int matchType, String needle, String structFieldValueID)
	{
		super(personStructFieldID, matchType, needle);
		selectedStructFieldValueID = structFieldValueID;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.search.SearchFilterItem#appendSubQuery(int, int, java.util.Set, java.lang.StringBuffer, java.lang.StringBuffer, java.lang.StringBuffer, java.util.Map)
	 */
	@Override
	public void appendSubQuery(int itemIndex, int itemSubIndex,
			Set<Class<?>> imports, StringBuffer vars, StringBuffer filter,
			StringBuffer params, Map<String, Object> paramMap)
	{
		final String paramName = "value"+itemIndex; //$NON-NLS-1$

		if (!addedParams) {
			params.append(", "); //$NON-NLS-1$
			params.append(String.class.getName() + " " + paramName); //$NON-NLS-1$
			paramMap.put(paramName, selectedStructFieldValueID);

			imports.add(JDOHelper.class);
			addedParams = true;
		}

		filter.append(QUERY_DATAFIELD_VARNAME+itemIndex + ".structFieldValueID == " + paramName);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.search.SearchFilterItem#getItemTargetClass()
	 */
	@Override
	public Class<?> getItemTargetClass() {
		return SelectionDataField.class;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.search.SearchFilterItem#isConstraint()
	 */
	@Override
	public boolean isConstraint() {
		return !"".equals(this.needle);
	}

}
