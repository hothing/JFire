package org.nightlabs.jfire.base.ui.search;

import org.apache.log4j.Logger;

/**
 * Default active state manager that keeps track of how many times the active state has been
 * set to <code>true</code> and returns <code>true</code> as long as this count is not 0.
 *
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class DefaultActiveStateManager
	implements ActiveStateManager
{
	/**
	 * The logger used in this class.
	 */
	static final private Logger logger = Logger.getLogger(DefaultActiveStateManager.class);

	/**
	 * The count of parts that can be active and if more than one element is active ->
	 * this manager is active as well.
	 */
	private int activePartsCounter;

	/**
	 * Returns <code>true</code> as long as active state has been set at least one time more to
	 * <code>true</code> than <code>false</code>.
	 */
	@Override
	public boolean isActive()
	{
		return activePartsCounter > 0;
	}

	@Override
	public void setActive(boolean active)
	{
		if (active)
		{
			activePartsCounter++;
		}
		else
		{
			activePartsCounter--;
		}

		if (activePartsCounter < 0)
		{
			logger.trace("There might be an incorrect usage of this ActiveStateManager, since " + //$NON-NLS-1$
					"setActive(false) is called at least one time too ofter (counter is getting negative)!", //$NON-NLS-1$
					new Exception());

			activePartsCounter = 0;
		}
	}

}
