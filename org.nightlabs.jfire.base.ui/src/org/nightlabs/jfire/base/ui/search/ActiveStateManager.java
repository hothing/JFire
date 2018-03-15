package org.nightlabs.jfire.base.ui.search;

/**
 * A simple interface for a state manager that manages a state of the assigned object and can be
 * either active or inactive.
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public interface ActiveStateManager
{
	/**
	 * Sets the active state of the corresponding object to <code>active</code>.
	 * 
	 * @param active
	 *          the new active state of the managed object.
	 */
	void setActive(boolean active);

	/**
	 * Returns the active state of the corresponding object.
	 * 
	 * @return the active state of the corresponding object.
	 */
	boolean isActive();
}
