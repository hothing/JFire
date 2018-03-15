package org.nightlabs.jfire.base.admin.ui.editor;

import java.util.EventObject;

import org.nightlabs.jfire.base.admin.ui.editor.user.BaseModel;

public class ModelChangeEvent
extends EventObject
{
	private static final long serialVersionUID = 1L;

	public ModelChangeEvent(BaseModel model) {
		super(model);
	}

	@Override
	public BaseModel getSource() {
		return (BaseModel) super.getSource();
	}
}
