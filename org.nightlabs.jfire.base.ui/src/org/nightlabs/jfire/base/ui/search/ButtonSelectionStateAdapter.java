package org.nightlabs.jfire.base.ui.search;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class ButtonSelectionStateAdapter
	extends SelectionAdapter
{
	/**
	 * The ActiveStateManager to which the state change will be propagated.
	 */
	protected ActiveStateManager activeStateManager;
	
	/**
	 * @param activeStateManager
	 */
	public ButtonSelectionStateAdapter(ActiveStateManager activeStateManager)
	{
		this.activeStateManager = activeStateManager;
	}

	/**
	 * This implementation delegates to {@link #widgetSelected(SelectionEvent)}.
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e)
	{
		widgetSelected(e);
	}
	
	/**
	 * Delegates the state change to the given {@link #activeStateManager} and calls
	 * {@link #handleSelection(boolean)}.
	 */
	@Override
	public void widgetSelected(SelectionEvent e)
	{
		final boolean active = ((Button) e.getSource()).getSelection();
		if (activeStateManager != null)
		{
			activeStateManager.setActive(active);
		}
		handleSelection(active);
	}
	
	/**
	 * Implementors should update the UI here.
	 * @param active the new active state.
	 */
	protected abstract void handleSelection(boolean active);
}
