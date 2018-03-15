package org.nightlabs.jfire.base.admin.ui.editor.user;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;

public abstract class CheckboxEditingSupport<ElementType> extends EditingSupport {

	private CheckboxCellEditor checkboxCellEditor;

	public CheckboxEditingSupport(TableViewer viewer) {
		super(viewer);
		checkboxCellEditor = new CheckboxCellEditor();
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return checkboxCellEditor;
	}

	@Override
	protected Object getValue(Object element) {
		return doGetValue((ElementType) element);
	}
	
	protected abstract boolean doGetValue(ElementType element);

	@Override
	protected void setValue(Object element, Object value) {
		boolean checkState = (Boolean) value;
		doSetValue((ElementType) element, checkState);
		getViewer().update(element, null);
	}
	
	protected abstract void doSetValue(ElementType element, boolean value);
}
