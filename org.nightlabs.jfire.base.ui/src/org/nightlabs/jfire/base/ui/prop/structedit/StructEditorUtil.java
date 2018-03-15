/**
 *
 */
package org.nightlabs.jfire.base.ui.prop.structedit;

import java.util.Collection;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.prop.PropertyManagerRemote;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructID;
import org.nightlabs.jfire.prop.id.StructLocalID;

/**
 * @author alex
 *
 */
public class StructEditorUtil {

	public static PropertyManagerRemote getPropertyManager() {
		try {
			return JFireEjb3Factory.getRemoteBean(PropertyManagerRemote.class, Login.getLogin().getInitialContextProperties());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Collection<StructID> getAvailableStructIDs() {
		return getPropertyManager().getAvailableStructIDs();
	}

	public static Collection<StructLocalID> getAvailableStructLocalIDs() {
		return getPropertyManager().getAvailableStructLocalIDs();
	}

	public static long getDataFieldInstanceCount(StructFieldID structFieldID) {
		return getPropertyManager().getDataFieldInstanceCount(structFieldID);
	}
}
