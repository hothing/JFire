/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.core.runtime.ListenerList;
import org.nightlabs.jfire.prop.ModifyListener;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public abstract class AbstractStructPartEditor<P> implements StructPartEditor<P> {

	/**
	 * 
	 */
	public AbstractStructPartEditor() {
	}

	private ListenerList modifyListeners = new ListenerList();
	
	
	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		modifyListeners.add(modifyListener);
	}
	
	@Override
	public void removeModifyListener(ModifyListener modifyListener) {
		modifyListeners.remove(modifyListener);
	}
	
	protected void notifyModifyListeners() {
		Object[] listeners = modifyListeners.getListeners();
		for (Object l : listeners) {
			((ModifyListener) l).modifyData();
		}
	}
	
}
