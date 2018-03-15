package org.nightlabs.jfire.geography.ui;

import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;

/**
 * These listeners are triggered whenever the Geography's template data changes.
 * Note that the listeners are called on a worker {@link Thread} (within a {@link Job}).
 *
 * @author chairatk
 */
public interface GeographyTemplateDataChangedListener {
	/**
	 * This method is triggered on a worker <code>Thread</code> within a {@link Job} whenever template data is changed.
	 */
	public void geographyTemplateDataChanged(JDOLifecycleEvent event);
}
