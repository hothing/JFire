/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.validation;

import javax.jdo.JDOHelper;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.base.ui.prop.structedit.StructBlockCombo;
import org.nightlabs.jfire.base.ui.prop.structedit.StructFieldCombo;
import org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.Mode;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.validation.DataFieldExpression;
import org.nightlabs.jfire.prop.validation.GenericDataFieldNotEmptyExpression;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class DataFieldExpressionComposite extends XComposite {

	private IStruct struct;
	private StructBlockCombo structBlockCombo;
	private StructFieldCombo structFieldCombo;
	private Combo operationCombo;
	private boolean liveUpdate;
	private DataFieldExpression<?> expression;
	private IExpressionValidatorEditor expressionValidatorEditor; 
	private ISelectionChangedListener structBlockComboListener = new ISelectionChangedListener(){
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			StructBlock structBlock = structBlockCombo.getSelectedElement();
			structFieldCombo.addElements(structBlock.getStructFields());
			structFieldCombo.selectElementByIndex(0);
		}
	}; 
	
	/**
	 * @param parent
	 * @param style
	 */
	public DataFieldExpressionComposite(Composite parent, int style, LayoutMode layoutMode, DataFieldExpression<?> expression, 
			Mode mode, IStruct struct, boolean liveUpdate, IExpressionValidatorEditor expressionValidatorEditor) 
	{
		super(parent, style, layoutMode);
		if (struct == null) {
			throw new IllegalArgumentException("Param struct must not be null!"); //$NON-NLS-1$
		}
		if (expression == null) {
			throw new IllegalArgumentException("Param expression mut not be null!"); //$NON-NLS-1$
		}
		this.struct = struct;
		this.liveUpdate = liveUpdate;
		this.expression = expression;
		this.expressionValidatorEditor = expressionValidatorEditor;
		createExpressionComposite(expression, this, mode);
	}	
	
//	protected void createExpressionComposite(final DataFieldExpression<?> expression, Composite parent, Mode mode) 
//	{
//		int columns = 1;
//		switch (mode) {
//			case STRUCT:
//				columns = 3;
//				break;
//			case STRUCT_BLOCK:
//				columns = 2;
//				break;
//			case STRUCT_FIELD:
//				columns = 1;
//				break;								
//		}
//		Composite wrapper = new XComposite(parent, SWT.NONE);
//		wrapper.setLayout(new GridLayout(columns, false));
//		wrapper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		
//		int comboStyle = SWT.READ_ONLY | SWT.BORDER;
//		
//		operationCombo = new Combo(wrapper, comboStyle);
//		operationCombo.setItems(new String[] {ExpressionValidatorComposite.NOT_EMPTY});
//		operationCombo.select(0);
//
//		StructBlock structBlock = null;
//		StructField<?> structField = null;
//		try {
//			structField = struct.getStructField(expression.getStructFieldID());
//			structBlock = structField.getStructBlock();
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//
//		if (mode == Mode.STRUCT) {
//			structBlockCombo = new StructBlockCombo(wrapper, comboStyle);
//			structBlockCombo.addSelectionChangedListener(structBlockComboListener);
//			structBlockCombo.addElements(struct.getStructBlocks());
//			if (structBlock != null) {
//				structBlockCombo.selectElement(structBlock);
//			}
//			structBlockCombo.addSelectionChangedListener(new ISelectionChangedListener(){
//				@Override
//				public void selectionChanged(SelectionChangedEvent event) {
//					StructBlock structBlock = structBlockCombo.getSelectedElement();
//					structFieldCombo.removeAll();
//					structFieldCombo.addElements(structBlock.getStructFields());
//					if (!structFieldCombo.getElements().isEmpty())
//						structFieldCombo.selectElementByIndex(0);
//				}
//			});
//		}
//
//		if (mode == Mode.STRUCT_BLOCK) {
//			structFieldCombo = new StructFieldCombo(wrapper, comboStyle);
//			if (structBlock != null) {
//				structFieldCombo.addElements(structBlock.getStructFields());
//				if (structField != null) {
//					structFieldCombo.selectElement(structField);
//				}
//			}
//			if (liveUpdate) {
//				structFieldCombo.addSelectionChangedListener(new ISelectionChangedListener(){
//					@Override
//					public void selectionChanged(SelectionChangedEvent event) {
//						StructField<?> sf = structFieldCombo.getSelectedElement();
//						expression.setStructFieldID(sf.getStructFieldIDObj());
//						expressionValidatorEditor.refresh();
//					}
//				});
//			}
//		}
//	}
	
	protected void createExpressionComposite(final DataFieldExpression<?> expression, Composite parent, Mode mode) 
	{
		int columns = 1;
		switch (mode) {
			case STRUCT:
				columns = 3;
				break;
			case STRUCT_BLOCK:
				columns = 2;
				break;
			case STRUCT_FIELD:
				columns = 1;
				break;								
		}
		Composite wrapper = new XComposite(parent, SWT.NONE);
		wrapper.setLayout(new GridLayout(columns, false));
		wrapper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		int comboStyle = SWT.READ_ONLY | SWT.BORDER;
		
		StructBlock structBlock = null;
		StructField<?> structField = null;
		try {
			structField = struct.getStructField(expression.getStructFieldID());
			structBlock = structField.getStructBlock();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (mode == Mode.STRUCT) {
			structBlockCombo = new StructBlockCombo(wrapper, comboStyle);
			structBlockCombo.addSelectionChangedListener(structBlockComboListener);
			structBlockCombo.addElements(struct.getStructBlocks());
			if (structBlock != null) {
				structBlockCombo.selectElement(structBlock);
			}
			structBlockCombo.addSelectionChangedListener(new ISelectionChangedListener(){
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					StructBlock structBlock = structBlockCombo.getSelectedElement();
					structFieldCombo.removeAll();
					structFieldCombo.addElements(structBlock.getStructFields());
					if (!structFieldCombo.getElements().isEmpty())
						structFieldCombo.selectElementByIndex(0);
				}
			});
		}

		if (mode == Mode.STRUCT_BLOCK) {
			structFieldCombo = new StructFieldCombo(wrapper, comboStyle);
			if (structBlock != null) {
				structFieldCombo.addElements(structBlock.getStructFields());
				if (structField != null) {
					structFieldCombo.selectElement(structField);
				}
			}
			if (liveUpdate) {
				structFieldCombo.addSelectionChangedListener(new ISelectionChangedListener(){
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						StructField<?> sf = structFieldCombo.getSelectedElement();
						expression.setStructFieldID(sf.getStructFieldIDObj());
						expressionValidatorEditor.refresh();
					}
				});
			}
		}
		
		operationCombo = new Combo(wrapper, comboStyle);
		operationCombo.setItems(new String[] {ExpressionValidatorComposite.NOT_EMPTY});
		operationCombo.select(0);		
	}
	
	public IExpression getExpression() 
	{
		if (liveUpdate) {
			return expression;
		}
		else {			
			StructField<?> structField = structFieldCombo.getSelectedElement();
			String operation = operationCombo.getItem(operationCombo.getSelectionIndex());
			if (ExpressionValidatorComposite.NOT_EMPTY.equals(operation)) {
				return new GenericDataFieldNotEmptyExpression((StructFieldID)JDOHelper.getObjectId(structField));
			}			
		}
		return expression;
	}
}
