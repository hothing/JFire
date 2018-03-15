package org.nightlabs.jfire.base.admin.ui.editor.user;

import org.eclipse.core.runtime.ListenerList;
import org.nightlabs.jfire.base.admin.ui.editor.ModelChangeEvent;
import org.nightlabs.jfire.base.admin.ui.editor.ModelChangeListener;

public class BaseModel
{
	private ListenerList changeListeners = new ListenerList();
	
	public void addModelChangeListener(ModelChangeListener listener) {
		changeListeners.add(listener);
	}
	
	public void removeModelChangeListener(ModelChangeListener listener) {
		changeListeners.remove(listener);
	}

	private boolean hasDeferredModelChangeEvents = false;
	
	protected void modelChanged() {
		if (deferModelChangedEvents > 0) {
			hasDeferredModelChangeEvents = true;
			return;
		}

		for (Object listener : changeListeners.getListeners())
			((ModelChangeListener)listener).modelChanged(new ModelChangeEvent(this));
	}

	private int deferModelChangedEvents = 0;

	public void beginDeferModelChangedEvents() {
		++this.deferModelChangedEvents;
	}

	public void endDeferModelChangedEvents() {
		if (deferModelChangedEvents == 0)
			throw new IllegalStateException("endDeferModelChangedEvents called without begin!"); //$NON-NLS-1$

		if (--this.deferModelChangedEvents == 0) {
			if (hasDeferredModelChangeEvents)
				modelChanged();

			hasDeferredModelChangeEvents = false;
		}
	}
}
