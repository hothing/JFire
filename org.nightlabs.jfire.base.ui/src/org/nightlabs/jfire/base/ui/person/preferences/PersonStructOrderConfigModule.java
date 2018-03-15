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

package org.nightlabs.jfire.base.ui.person.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;
import org.nightlabs.jfire.person.PersonStruct;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
@SuppressWarnings("unchecked")
public class PersonStructOrderConfigModule extends ConfigModule {
	private static final long serialVersionUID = 1L;

	/**
	 * this will be serialized
	 */
	public static class PersonStructOrderMapEntry {
		public PersonStructOrderMapEntry() {
		}

		private String key;
		private int priority;

		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public int getPriority() {
			return priority;
		}
		public void setPriority(int priority) {
			this.priority = priority;
		}
	}

	/**
	 *
	 */
	public PersonStructOrderConfigModule() {
		super();
	}

	@Override
	public void init() throws InitException {
		super.init();
	}

	private List structBlockDisplayOrderList;

	/**
	 * Not to be called, just for serialization.
	 * Will always recreate the structBlockDisplayOrderList;
	 * @return
	 */
	public List getStructBlockDisplayOrderList() {
		if (structBlockDisplayOrder != null) {
			for (Iterator iter = structBlockDisplayOrder().entrySet().iterator(); iter.hasNext();) {
				if (structBlockDisplayOrderList == null)
					structBlockDisplayOrderList = new ArrayList();
				Map.Entry entry = (Map.Entry) iter.next();
				PersonStructOrderMapEntry listEntry = new PersonStructOrderMapEntry();
				listEntry.setKey((String)entry.getKey());
				listEntry.setPriority(((Integer)entry.getValue()).intValue());
				structBlockDisplayOrderList.add(listEntry);
			}
		}
		if (structBlockDisplayOrderList == null)
			structBlockDisplayOrderList = new ArrayList();

		return structBlockDisplayOrderList;
	}
	/**
	 * Not to be called, just for serialization.
	 * @param structFieldDisplayOrderList
	 */
	public void setStructBlockDisplayOrderList(List structBlockDisplayOrderList) {
		this.structBlockDisplayOrderList = structBlockDisplayOrderList;
		setChanged();
	}

	private List structFieldDisplayOrderList;

	/**
	 * Not to be called, just for serialization.
	 * Will always recreate the structFieldDisplayOrderList;
	 * @return
	 */
	public List getStructFieldDisplayOrderList() {
		if (structFieldDisplayOrder != null) {
			for (Iterator iter = structFieldDisplayOrder().entrySet().iterator(); iter.hasNext();) {
				if (structFieldDisplayOrderList == null)
					structFieldDisplayOrderList = new ArrayList();
				Map.Entry entry = (Map.Entry) iter.next();
				PersonStructOrderMapEntry listEntry = new PersonStructOrderMapEntry();
				listEntry.setKey((String)entry.getKey());
				listEntry.setPriority(((Integer)entry.getValue()).intValue());
				structFieldDisplayOrderList.add(listEntry);
			}
		}
		if (structFieldDisplayOrderList == null)
			structFieldDisplayOrderList = new ArrayList();

		return structFieldDisplayOrderList;
	}
	/**
	 * Not to be called, just for serialization.
	 * @param structFieldDisplayOrderList
	 */
	public void setStructFieldDisplayOrderList(List structFieldDisplayOrderList) {
		this.structFieldDisplayOrderList = structFieldDisplayOrderList;
		setChanged();
	}

	/**
	 * key: String PersonStructBlock.getPrimaryKey<br/>
	 * value: Integer priority
	 */
	private Map structBlockDisplayOrder;

	/**
	 * Returns a Map of (String,Integer) of
	 * priorities mapped to PersonStructBlockKeys
	 *
	 * @return
	 */
	public Map structBlockDisplayOrder() {
		if (structBlockDisplayOrder == null) {
			if (structBlockDisplayOrderList != null) {
				structBlockDisplayOrder = new HashMap();
				for (Iterator iter = structBlockDisplayOrderList.iterator(); iter.hasNext();) {
					PersonStructOrderMapEntry entry = (PersonStructOrderMapEntry) iter.next();
					structBlockDisplayOrder.put(entry.getKey(), Integer.valueOf(entry.getPriority()));
				}
			}
			else
				structBlockDisplayOrder = getDefaultStructBlockDisplayOrder();
			setChanged();
		}
		return structBlockDisplayOrder;
	}

	public void setStructBlockDisplayOrder(Map displayOrder) {
		this.structBlockDisplayOrder = displayOrder;
	}


	/**
	 * key: String StructField.getPrimaryKey<br/>
	 * value: Integer priority
	 */
	private Map structFieldDisplayOrder;

	/**
	/**
	 * Returns a Map of (String,Integer) of
	 * priorities mapped to PersonStructFieldKeys
	 *
	 * @return
	 */
	public Map structFieldDisplayOrder() {
		if (structFieldDisplayOrder == null) {
			if (structFieldDisplayOrderList != null) {
				structFieldDisplayOrder = new HashMap();
				for (Iterator iter = structFieldDisplayOrderList.iterator(); iter.hasNext();) {
					PersonStructOrderMapEntry entry = (PersonStructOrderMapEntry) iter.next();
					structFieldDisplayOrder.put(entry.getKey(), Integer.valueOf(entry.getPriority()));
				}
			}
			else
				structFieldDisplayOrder = getDefaultStructFieldDisplayOrder();
		}
		return structFieldDisplayOrder;
	}

	public void setStructFieldDisplayOrder(Map displayOrder) {
		this.structFieldDisplayOrder = displayOrder;
		setChanged();
	}

	private Map getDefaultStructBlockDisplayOrder() {
		Map result = new HashMap();
		result.put(PersonStruct.PERSONALDATA.getPrimaryKey(), Integer.valueOf(0));
		result.put(PersonStruct.POSTADDRESS.getPrimaryKey(), Integer.valueOf(1));
		result.put(PersonStruct.PHONE.getPrimaryKey(), Integer.valueOf(2));
		result.put(PersonStruct.INTERNET.getPrimaryKey(), Integer.valueOf(3));
		result.put(PersonStruct.FAX.getPrimaryKey(), Integer.valueOf(4));
		result.put(PersonStruct.CREDITCARD.getPrimaryKey(), Integer.valueOf(5));
		result.put(PersonStruct.COMMENT.getPrimaryKey(), Integer.valueOf(6));
		return result;
	}

	private Map getDefaultStructFieldDisplayOrder() {
		Map result = new HashMap();
		return result;
	}

	private static PersonStructOrderConfigModule sharedInstance;

	/**
	 * Catches ConfigException and throws an IllegalStateException
	 * instead Config
	 * @return shared instance of PersonStructOrderConfigModule
	 */
	public static PersonStructOrderConfigModule sharedInstance() {
		if (sharedInstance == null) {
			try {
				sharedInstance = (Config.sharedInstance().createConfigModule(PersonStructOrderConfigModule.class));
			} catch (ConfigException e) {
				IllegalStateException ill = new IllegalStateException("Error creating ConfigModule"); //$NON-NLS-1$
				ill.initCause(e);
				throw ill;
			}
		}
		return sharedInstance;
	}

}
