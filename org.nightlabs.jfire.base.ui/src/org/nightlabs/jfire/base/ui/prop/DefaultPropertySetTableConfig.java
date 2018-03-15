package org.nightlabs.jfire.base.ui.prop;

import java.util.ArrayList;
import java.util.List;

import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.id.StructFieldID;


/*
* @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
*/
public class DefaultPropertySetTableConfig implements IPropertySetTableConfig 
{
	/**
	 * Default constructor.
	 */
	public DefaultPropertySetTableConfig() {
		super();
	}

	public IStruct getIStruct() {
		return null;
	}
	
	public StructFieldID[] getStructFieldIDs() {
		return new StructFieldID[0];
	}
	public List<StructFieldID[]> getStructFieldIDsList() {
		return new ArrayList<StructFieldID[]>();
	}
}
