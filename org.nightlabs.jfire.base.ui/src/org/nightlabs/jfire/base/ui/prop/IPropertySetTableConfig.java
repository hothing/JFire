package org.nightlabs.jfire.base.ui.prop;

import java.util.List;

import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.id.StructFieldID;

/*
* @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
*/
public interface IPropertySetTableConfig {
	public IStruct getIStruct();
	public StructFieldID[] getStructFieldIDs();
	public List<StructFieldID[]> getStructFieldIDsList();
}
