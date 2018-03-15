/**
 *
 */
package org.nightlabs.jfire.base.ui.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.nightlabs.base.ui.notification.SelectionManager;
import org.nightlabs.base.ui.search.AbstractSelectionZoneActionHandler;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.notification.NotificationEvent;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *
 */
public abstract class AbstractJDOSelectionZoneActionHandler
extends AbstractSelectionZoneActionHandler
{
	@Override
	public void run() {
		Collection<?> selectedObjects = getSearchResultProvider().getSelectedObjects();
		if (selectedObjects != null) {
			Collection<ObjectID> selectedObjectIDs = new ArrayList<ObjectID>();
			// before use NLJDOHelper.getObjectIDSet check if maybe already ObjectIDs are contained
			for (Iterator<?> it = selectedObjects.iterator(); it.hasNext(); ) {
				Object o = it.next();
				if (o instanceof ObjectID) {
					selectedObjectIDs.add((ObjectID)o);
				}
			}
			if (selectedObjectIDs.isEmpty()) {
				selectedObjectIDs = NLJDOHelper.getObjectIDSet(selectedObjects);
			}
			Collection<Class<?>> subjectClassesToClear = new ArrayList<Class<?>>();
			subjectClassesToClear.add(getSearchResultProvider().getFactory().getResultTypeClass());
			if (selectedObjects != null) {
				SelectionManager.sharedInstance().notify(new NotificationEvent(
						AbstractJDOSelectionZoneActionHandler.this,
						getSelectionZone(),
						selectedObjectIDs,
						subjectClassesToClear));
			}
		}
	}

}
