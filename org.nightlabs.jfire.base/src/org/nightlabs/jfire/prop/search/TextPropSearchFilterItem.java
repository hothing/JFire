/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.prop.search;

import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class TextPropSearchFilterItem extends PropSearchFilterItem
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 20080811L;

	public TextPropSearchFilterItem(StructFieldID structFieldID, int matchType, String needle) {
		super(structFieldID, matchType, needle);
	}

	public TextPropSearchFilterItem(StructFieldID[] structFieldIDs, int matchType, String needle) {
		super(structFieldIDs, matchType, needle);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.search.SearchFilterItem#isConstraint()
	 */
	@Override
	public boolean isConstraint() {
		return !"".equals(this.needle);
	}

	private boolean addedParams = false;

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.search.SearchFilterItem#appendSubQuery(int, int, java.util.Set, java.lang.StringBuffer, java.lang.StringBuffer, java.lang.StringBuffer, java.util.Map)
	 */
	@Override
	public void appendSubQuery(int itemIndex, int itemSubIndex, Set<Class<?>> imports, StringBuffer vars,
			StringBuffer filter, StringBuffer params, Map<String, Object> paramMap) {

		String needleLowerCase = "needle"+itemIndex+".toLowerCase()";
		if (!addedParams) {
			params.append(", ");
			params.append(String.class.getName()+" needle"+itemIndex);
			paramMap.put("needle"+itemIndex, getNeedle());
			addedParams = true;
		}

		filter.append(QUERY_DATAFIELD_VARNAME+itemSubIndex+".text.toLowerCase()");
		switch (matchType) {
			case MATCHTYPE_BEGINSWITH:
				filter.append(".startsWith("+needleLowerCase+")");
				break;
			case MATCHTYPE_ENDSWITH:
				filter.append(".endsWith("+needleLowerCase+")");
				break;
			case MATCHTYPE_CONTAINS:
				filter.append(".indexOf("+needleLowerCase+") >= 0");
				break;
			case MATCHTYPE_NOTCONTAINS:
				filter.append(".indexOf("+needleLowerCase+") < 0");
				break;
			case MATCHTYPE_EQUALS:
				filter.append(" == "+needleLowerCase);
				break;
			case MATCHTYPE_NOTEQUALS:
				filter.append(" != "+needleLowerCase);
				break;
			case MATCHTYPE_MATCHES:
				params.append(", ");
				params.append(String.class.getName()+" regex"+itemIndex);
				paramMap.put("regex"+itemIndex, getNeedle());
				filter.append(".toLowerCase().matches(regex"+itemIndex+".toLowerCase())");
				break;
			default:
				filter.append(" == "+needleLowerCase);
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.search.SearchFilterItem#getItemTargetClass()
	 */
	@Override
	public Class<TextDataField> getItemTargetClass() {
		return TextDataField.class;
	}
}
