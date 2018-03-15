package org.nightlabs.jfire.base.ui.overview;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * {@link IEditorInput} to be used with {@link OverviewEntryEditor}.
 *
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class OverviewEntryEditorInput
	implements IEditorInput
{

	public OverviewEntryEditorInput(Entry entry) {
		this.entry = entry;
	}

	private Entry entry;
	/**
	 * Returns the entry of this input.
	 * @return The entry of this input.
	 */
	public Entry getEntry() {
		return entry;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Check if the id of the {@link Entry} of two given
	 * {@link OverviewEntryEditorInput}s are equal.
	 * </p>
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;

		if (!(obj instanceof OverviewEntryEditorInput))
			return false;

		OverviewEntryEditorInput input = (OverviewEntryEditorInput) obj;
		if (entry.getEntryFactory().getID().equals(input.getEntry().getEntryFactory().getID()))
			return true;

		return false;
	}

	@Override
	public boolean exists()
	{
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return entry.getEntryFactory().getName();
	}

	@Override
	public IPersistableElement getPersistable()
	{
		return null;
	}

	@Override
	public String getToolTipText()
	{
		return getName();
	}

	@Override
	public Object getAdapter(Class adapter)
	{
		return null;
	}

}
