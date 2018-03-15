package org.nightlabs.jfire.base.ui.prop.structedit;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.nightlabs.base.ui.composite.AbstractListComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.language.LanguageChangeEvent;
import org.nightlabs.base.ui.language.LanguageChangeListener;
import org.nightlabs.base.ui.language.LanguageChooser;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.ModifyListener;
import org.nightlabs.jfire.prop.IStruct.OrderMoveDirection;
import org.nightlabs.jfire.prop.i18n.MultiSelectionStructFieldValueName;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructField;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructFieldValue;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.language.LanguageCf;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class MultiSelectionStructFieldEditor extends AbstractStructFieldEditor<MultiSelectionStructField> 
{
	public static class MultiSelectionStructFieldEditorFactory extends AbstractStructFieldEditorFactory 
	{
		public String getStructFieldEditorClass() {
			return MultiSelectionStructFieldEditor.class.getName();
		}

		@Override
		@SuppressWarnings("unchecked")
		public StructFieldEditor createStructFieldEditor() {
			return new MultiSelectionStructFieldEditor();
		}
	}

	private MultiSelectionStructField selectionField;
	private MultiSelectionStructFieldEditComposite comp;

	ModifyListener modifyListener = new ModifyListener() {
		@Override
		public void modifyData() {
			setChanged();
		}
	};

	@Override
	protected Composite createSpecialComposite(Composite parent, int style) {
		comp = new MultiSelectionStructFieldEditComposite(parent, style, this, getLanguageChooser());
		return comp;
	}

	@Override
	public void setSpecialData(MultiSelectionStructField field) {
		selectionField = field;
		comp.setField(selectionField);
	}
}

class MultiSelectionStructFieldEditComposite extends XComposite implements LanguageChangeListener 
{
	private MultiSelectionStructFieldEditor editor;
	private MultiSelectionStructFieldValueTable structFieldValueTable;
	private MultiSelectionStructField selectionField;
	private LanguageCf currLanguage;
	private Button addValueButton;
	private Button remValueButton;
	private Button moveUpButton;
	private Button moveDownButton;
	private Button setDefaultButton;
	private Button removeDefaultButton;
	private Button emptySelectionCheckbox;

	public MultiSelectionStructFieldEditComposite(Composite parent, int style, final MultiSelectionStructFieldEditor editor, LanguageChooser langChooser) 
	{
		super(parent, style, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA, 2);
		getGridLayout().horizontalSpacing = 2;

		currLanguage = langChooser.getLanguage();
		langChooser.addLanguageChangeListener(this);

		XComposite wrapper = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
		new Label(wrapper, SWT.NONE).setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.MultiSelectionStructFieldEditor.possibleValuesLabel.text")); //$NON-NLS-1$

		structFieldValueTable = new MultiSelectionStructFieldValueTable(wrapper, AbstractListComposite.getDefaultWidgetStyle(this), editor.modifyListener);
		structFieldValueTable.setCurrentLanguage(currLanguage);
		structFieldValueTable.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		this.editor = editor;

		wrapper.layout(true, true);

		wrapper = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		wrapper.getGridData().verticalAlignment = SWT.TOP;

		// Phantom label for proper spacing
		new Label(wrapper, SWT.NONE);

		addValueButton = new Button(wrapper, SWT.NONE);
		addValueButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.MultiSelectionStructFieldEditor.addValueButton.text")); //$NON-NLS-1$
		addValueButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addNewValue();
			}
		});

		remValueButton = new Button(wrapper, SWT.NONE);
		remValueButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.MultiSelectionStructFieldEditor.removeValueButton.text")); //$NON-NLS-1$
		remValueButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeValue();
			}
		});

		moveUpButton = new Button(wrapper, SWT.NONE);
		moveUpButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.MultiSelectionStructFieldEditor.button.moveUp.text"));  //$NON-NLS-1$
		moveUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveValue(OrderMoveDirection.up);
			}
		});
		
		moveDownButton = new Button(wrapper, SWT.NONE);
		moveDownButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.MultiSelectionStructFieldEditor.button.moveDown.text"));  //$NON-NLS-1$
		moveDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveValue(OrderMoveDirection.down);
			}
		});
		
		setDefaultButton = new Button(wrapper, SWT.NONE);
		setDefaultButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.MultiSelectionStructFieldEditor.button.default.text")); //$NON-NLS-1$
		setDefaultButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (structFieldValueTable.getSelectionCount() < 1)
					return;

				Collection<MultiSelectionStructFieldValue> newDefault = structFieldValueTable.getSelectedElements();
				selectionField.setDefaultValues(newDefault);
				structFieldValueTable.getTableViewer().refresh(true);
				editor.setChanged();
			}
		});

		removeDefaultButton = new Button(wrapper, SWT.NONE);
		removeDefaultButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.MultiSelectionStructFieldEditor.button.remove.text")); //$NON-NLS-1$
		removeDefaultButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (structFieldValueTable.getSelectionCount() < 1)
					return;

				Collection<MultiSelectionStructFieldValue> toRemove = structFieldValueTable.getSelectedElements();
				Set<MultiSelectionStructFieldValue> newDefaultValues = new HashSet<MultiSelectionStructFieldValue>(selectionField.getDefaultValues());
				newDefaultValues.removeAll(toRemove);
				selectionField.setDefaultValues(newDefaultValues);
				structFieldValueTable.getTableViewer().refresh(true);
				editor.setChanged();
			}
		});

		wrapper.layout();

		// Make button widths equal
		Button[] buttons = new Button[] { addValueButton, remValueButton, moveUpButton, moveDownButton, setDefaultButton, removeDefaultButton };
		int maxWidth = 0;
		for (Button button : buttons)
			maxWidth = Math.max(maxWidth, button.getSize().x);

		GridData gd;
		for (Button button : buttons) {
			gd = new GridData();
			gd.widthHint = maxWidth;
			button.setLayoutData(gd);
		}
		
		emptySelectionCheckbox = new Button(wrapper, SWT.CHECK);
		emptySelectionCheckbox.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.MultiSelectionStructFieldEditor.checkbox.allowEmptySelection.text")); //$NON-NLS-1$
		emptySelectionCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO handle minimum selection count correctly
				selectionField.setMinimumSelectionCount(emptySelectionCheckbox.getSelection() ? 0 : 1);
				editor.setChanged();
			}
		});
		
		structFieldValueTable.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) 
			{
				updateButtonEnablement();
			}
		});
		updateButtonEnablement();
	}

	private void updateButtonEnablement()
	{
		int selectionCount = structFieldValueTable.getSelectionCount();
		addValueButton.setEnabled(true);
		remValueButton.setEnabled(selectionCount > 0);
		moveUpButton.setEnabled(selectionCount == 1);
		moveDownButton.setEnabled(selectionCount == 1);
		setDefaultButton.setEnabled(selectionCount > 0);
		removeDefaultButton.setEnabled(selectionCount > 0);
	}
	
	/**
	 * Adds a new value to the list and also to the structure.
	 */
	private void addNewValue() {
		MultiSelectionStructFieldValue value = selectionField.newStructFieldValue();
		MultiSelectionStructFieldValueName valueName = value.getValueName();
		valueName.setText(currLanguage.getLanguageID(), Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.MultiSelectionStructFieldEditor.newValue.text")); //$NON-NLS-1$

		structFieldValueTable.refresh();
		structFieldValueTable.setSelectedElements(Collections.singletonList(value));
		structFieldValueTable.activateCellEditor(value);
		editor.setChanged();
	}

	/**
	 * Removes the currently selected value from the list and also from the
	 * structure.
	 */
	private void removeValue() {
		if (structFieldValueTable.getSelectionCount() < 1)
			return;
		Collection<MultiSelectionStructFieldValue> selectedElements = structFieldValueTable.getSelectedElements();
		selectionField.removeStructFieldValues(selectedElements);
		structFieldValueTable.refresh();
		editor.setChanged();
	}

	/**
	 * Moves the currently selected value.
	 */
	private void moveValue(OrderMoveDirection moveDirection) {
		if (structFieldValueTable.getSelectionCount() != 1)
			return;
		MultiSelectionStructFieldValue toMove = structFieldValueTable.getFirstSelectedElement();
		selectionField.moveStructFieldValue(toMove, moveDirection);
		structFieldValueTable.refresh();
		editor.setChanged();
	}
	
	/**
	 * Sets the currently display field.
	 *
	 * @param field
	 *          The {@link SelectionStructField} to be displayed.
	 */
	public void setField(MultiSelectionStructField field) {
		selectionField = field;

		if (selectionField == null)
			return;

		structFieldValueTable.setSelectionField(selectionField);
		// TODO handle minimum selection count correctly
		emptySelectionCheckbox.setSelection(selectionField.getMinimumSelectionCount() == 0);
	}

	/**
	 * @see LanguageChangeListener#languageChanged(LanguageChangeEvent)
	 */
	public void languageChanged(LanguageChangeEvent event) {
		currLanguage = event.getNewLanguage();
		structFieldValueTable.setCurrentLanguage(currLanguage);
	}
}

class MultiSelectionStructFieldValueTable extends AbstractTableComposite<MultiSelectionStructFieldValue>
{
	private LanguageCf currentLanguage;
	private MultiSelectionStructField structField;
	private ModifyListener modifyListener;

	public MultiSelectionStructFieldValueTable(Composite parent, int style, ModifyListener modifyListener) 
	{
		super(parent, style);

		setHeaderVisible(false);

		this.modifyListener = modifyListener;
	}

	@Override
	protected void createTableColumns(TableViewer tableViewer, final Table table) {
		TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		EditingSupport editingSupport = new EditingSupport(tableViewer) {
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(table);
			}
			@Override
			protected Object getValue(Object element) {
				return ((MultiSelectionStructFieldValue) element).getValueName().getText(currentLanguage.getLanguageID());
			}
			@Override
			protected void setValue(Object element, Object value) {
				((MultiSelectionStructFieldValue) element).getValueName().setText(currentLanguage.getLanguageID(), (String) value);
				getTableViewer().refresh();
				modifyListener.modifyData();
			}
		};
		viewerColumn.setEditingSupport(editingSupport);

		table.setLayout(new WeightedTableLayout(new int[] { 1 }));
//		tableViewer.setComparator(new ViewerComparator());
	}

	public void setSelectionField(MultiSelectionStructField structField) {
		this.structField = structField;
		setInput(structField.getStructFieldValues());
	}
	
	@Override
	public void refresh() {
		if (structField != null)
			setInput(structField.getStructFieldValues());
		else
			super.refresh();
	}

	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				Set<MultiSelectionStructFieldValue> defValue = structField.getDefaultValues();
				String text = ((MultiSelectionStructFieldValue) element).getValueName().getText(currentLanguage.getLanguageID());
				if(defValue != null && defValue.contains(element))
					return String.format(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.MultiSelectionStructFieldEditor.[Standard]"), text); //$NON-NLS-1$
				else
					return text;
			}
		});
	}

	public void setCurrentLanguage(LanguageCf currentLanguage) {
		this.currentLanguage = currentLanguage;
		if (!getTable().isDisposed())
			refresh();
	}

	void activateCellEditor(MultiSelectionStructFieldValue value) {
		getTableViewer().editElement(value, 0);
	}
}