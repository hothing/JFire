package org.nightlabs.jfire.base.ui.overview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.nightlabs.jfire.base.ui.login.part.LSDViewPart;

/**
 * Abstract base {@link ViewPart} that will display an {@link OverviewShelf}
 * with the {@link OverviewRegistry} returned in {@link #getOverviewRegistry()}.
 *
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class OverviewView
extends LSDViewPart
{
	public OverviewView() {
		super();
	}

	private OverviewShelf overviewShelf;

	@Override
	public void createPartContents(Composite parent)
	{
		overviewShelf = new OverviewShelf(parent, SWT.NONE)
		{
			@Override
			protected String getScope()
			{
				return OverviewView.this.getScope();
			}
		};
	}

	@Override
	public void setFocus() {
		if (overviewShelf == null || overviewShelf.isDisposed())
			return;

		overviewShelf.setFocus();
	}

	/**
	 * @return The scope of the categories the shelf of this view shall contain.
	 */
	protected abstract String getScope();
}
