/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.I18nTextEditor.EditMode;
import org.nightlabs.base.ui.layout.WeightedTableLayout;
import org.nightlabs.base.ui.message.IErrorMessageDisplayer;
import org.nightlabs.base.ui.tree.TreeContentProvider;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.i18n.I18nTextBuffer;
import org.nightlabs.jfire.base.expression.AndCondition;
import org.nightlabs.jfire.base.expression.Composition;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.base.expression.Negation;
import org.nightlabs.jfire.base.expression.OrCondition;
import org.nightlabs.jfire.base.ui.prop.structedit.ValidationResultTypeCombo;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.validation.DataFieldExpression;
import org.nightlabs.jfire.prop.validation.GenericDataFieldNotEmptyExpression;
import org.nightlabs.jfire.prop.validation.ValidationResultType;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class ExpressionValidatorComposite 
extends XComposite 
implements IExpressionValidatorEditor
{
	class ContentProvider extends TreeContentProvider {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		@Override
		public Object[] getElements(Object inputElement) 
		{
			if (inputElement instanceof Collection<?>) {
				Collection<?> collection = (Collection<?>) inputElement;
				return collection.toArray();
			}
			else if (inputElement instanceof Composition) {
				Composition composition = (Composition) inputElement;
				return composition.getExpressions().toArray();
			}
			return new Object[] {};
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.tree.TreeContentProvider#getChildren(java.lang.Object)
		 */
		@Override
		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.tree.TreeContentProvider#getParent(java.lang.Object)
		 */
		@Override
		public Object getParent(Object element) {
			return super.getParent(element);
		}

		/* (non-Javadoc)
		 * @see org.nightlabs.base.ui.tree.TreeContentProvider#hasChildren(java.lang.Object)
		 */
		@Override
		public boolean hasChildren(Object element) 
		{
			if (element instanceof Composition) {
				Composition composition = (Composition) element;
				return !composition.getExpressions().isEmpty();
			}
			return false;
		}
		
	}
	
	class LabelProvider extends org.eclipse.jface.viewers.LabelProvider {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element) 
		{
			if (element instanceof Composition) {
				return Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.composition"); //$NON-NLS-1$
			}
			else if (element instanceof IExpression) {
				return Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.expression"); //$NON-NLS-1$
			}
			else {
				return super.getText(element);	
			}
		}
	}

	public static final String NEGATION = Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.negation"); //$NON-NLS-1$
	public static final String NOT_EMPTY = Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.notEmpty"); //$NON-NLS-1$
	
	public enum Mode 
	{
		STRUCT_BLOCK,
		STRUCT_FIELD,
		STRUCT
	}
	
	private TreeViewer treeViewer;
	private IExpression expression;
	private I18nTextEditor i18nTextEditor;
	private I18nText message = new I18nTextBuffer();
	private ValidationResultTypeCombo validationResultTypeCombo;
	private ValidationResultType validationResultType;
	private IExpressionValidatorHandler addHandler;
	private IExpression selectedExpression;
	private CompositionCombo conditionOperatorCombo;
	private Button removeExpression;
	private Button addExpression;
	private Button addComposition;
	private IStruct struct;
	private Text expressionText;
	private Composite expressionDetailComposite;
	private Mode mode = Mode.STRUCT_BLOCK;
	private Composite buttonComp;
	private IErrorMessageDisplayer messageDisplayer;
	private SashForm sash;
	private CompositionOperatorLabelProvider compositionOperatorLabelProvider;
	
	/**
	 * @param parent
	 * @param style
	 */
	public ExpressionValidatorComposite(Composite parent, int style, IExpression expression,
			IStruct struct, IExpressionValidatorHandler handler, Mode mode, IErrorMessageDisplayer messageDisplayer) 
	{	
		super(parent, style);
		assert struct != null;
		assert handler != null;
		assert mode != null;
		assert messageDisplayer != null;
		
		this.expression = expression;
		this.struct = struct;
		this.mode = mode;
		this.addHandler = handler;
		this.messageDisplayer = messageDisplayer;
		addHandler.setExpressionValidatorEditor(this);
		compositionOperatorLabelProvider = new CompositionOperatorLabelProvider();
		createComposite(this);
	}

	private ISelectionChangedListener treeListener = new ISelectionChangedListener(){
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = treeViewer.getSelection();
			if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				Object firstElement = sel.getFirstElement();
				if (firstElement instanceof IExpression) 
				{
					IExpression expression = (IExpression) firstElement;
					selectedExpression = expression;
					showExpression(selectedExpression);
					removeExpression.setEnabled(true);
					addExpression.setEnabled(expression instanceof Composition);
					addComposition.setEnabled(expression instanceof Composition);
				}
			}
			else {
				removeExpression.setEnabled(false);
			}
		}
	};
	
	protected void showExpression(IExpression expression) 
	{
		expressionText.setText(getText(expression));		
		createExpressionDetail(expression, sash);
		createButtonComposite(expression, this);
		sash.setWeights(new int[] {1,1});
		layout(true, true);
		validateOK();
	}
		
	protected void createComposite(Composite parent) 
	{
		Composite wrapper = new Composite(parent, SWT.NONE);
		wrapper.setLayout(new GridLayout(2, false));
		wrapper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label messageLabel = new Label(wrapper, SWT.NONE);
		messageLabel.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.label.message.text")); //$NON-NLS-1$
		i18nTextEditor = new I18nTextEditor(wrapper);
		i18nTextEditor.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				validateOK();
			}
		});
		
		Label validationTypeLabel = new Label(wrapper, SWT.NONE);
		validationTypeLabel.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.label.validationType.text")); //$NON-NLS-1$
		validationResultTypeCombo = new ValidationResultTypeCombo(wrapper, SWT.READ_ONLY | SWT.BORDER);
		validationResultTypeCombo.selectElement(ValidationResultType.ERROR);
		if (validationResultType != null) {
			validationResultTypeCombo.selectElement(validationResultType);
		}
		validationResultType = validationResultTypeCombo.getSelectedElement();
		validationResultTypeCombo.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				validationResultType = validationResultTypeCombo.getSelectedElement();
			}
		});		
  
		sash = new SashForm(parent, SWT.VERTICAL);
		sash.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite treeAndTextComposite = new XComposite(sash, SWT.NONE);
		treeAndTextComposite.setLayout(new GridLayout(2, true));
		createTreeViewer(treeAndTextComposite);
		createExpressionText(expression, treeAndTextComposite);
		
		setExpression(expression);
	}

	protected void createExpressionDetail(IExpression expression, Composite parent) 
	{
		if (expressionDetailComposite != null && !expressionDetailComposite.isDisposed()) {
			expressionDetailComposite.dispose();
		}
		expressionDetailComposite = new XComposite(parent, SWT.BORDER);
		createExpressionComposite(expression, expressionDetailComposite);
	}

	protected void createExpressionComposite(IExpression expression, Composite parent) 
	{
		// TODO should come from extension-point for extensibility
		
		if (expression instanceof Composition) {
			createCompositionComposite((Composition)expression, parent);
		}
		else if (expression instanceof DataFieldExpression<?>) {
			createDataFieldExpressionComposite((DataFieldExpression<?>)expression, parent, mode);
		}
		else if (expression instanceof Negation) {
			createNegationCompoiste((Negation)expression, parent);
		}
	}
	
	protected void createDataFieldExpressionComposite(DataFieldExpression<?> expression, Composite parent, Mode mode) 
	{
		DataFieldExpressionComposite comp = new DataFieldExpressionComposite(parent, SWT.NONE, 
				LayoutMode.TIGHT_WRAPPER, expression, mode, struct, true, this);
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	protected void createCompositionComposite(final Composition composition, Composite parent) 
	{
		Composite comp = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final CompositionCombo compositionKindCombo = new CompositionCombo(comp, SWT.READ_ONLY | SWT.BORDER);
		compositionKindCombo.setLayoutData(new GridData());
		compositionKindCombo.addElements(Arrays.asList(new String[] {AndCondition.OPERATOR_TEXT, OrCondition.OPERATOR_TEXT}));
		compositionKindCombo.selectElement(composition.getOperatorText());
		compositionKindCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String operator = compositionKindCombo.getSelectedElement();
				if (!operator.equals(composition.getOperatorText())) 
				{
					Composition newComposition = null;
					if (operator.equals(AndCondition.OPERATOR_TEXT)) {
						newComposition = new AndCondition(composition.getExpressions().toArray(
								new IExpression[composition.getExpressions().size()]));
					}
					else if (operator.equals(OrCondition.OPERATOR_TEXT)) {
						newComposition = new OrCondition(composition.getExpressions().toArray(
								new IExpression[composition.getExpressions().size()]));
					}
					Composition parent = getParentForExpression(expression, composition);
					if (parent != null) {
						parent.replaceExpression(composition, newComposition);
					}
					// composition is the root expression
					else if (composition.equals(expression)) {
						expression = newComposition;
					}
					refresh();
				}
			}
		});
		Label amountLabel = new Label(comp, SWT.NONE);
		amountLabel.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.label.amount.text")); //$NON-NLS-1$
		Group expressionsComp = new Group(parent, SWT.NONE);
		expressionsComp.setLayout(new GridLayout());
		expressionsComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		for (IExpression expr : composition.getExpressions()) 
		{
			if (expr instanceof Composition) {
				createNestedCompositionComposite((Composition) expr, expressionsComp);
			}
			else {
				createExpressionComposite(expr, expressionsComp);	
			}
		}
	}
	
	protected void createNestedCompositionComposite(Composition composition, Composite parent) 
	{
		ExpandableComposite expandableComposite = new ExpandableComposite(parent, SWT.NONE);
		expandableComposite.setLayout(new GridLayout());
		expandableComposite.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.composition")); //$NON-NLS-1$
		expandableComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Group comp = new Group(expandableComposite, SWT.NONE);
		comp.setLayout(new GridLayout());
		for (IExpression expr : composition.getExpressions()) {
			createExpressionComposite(expr, comp);
		}
		expandableComposite.setClient(comp);
		expandableComposite.addExpansionListener(new IExpansionListener(){
			@Override
			public void expansionStateChanging(ExpansionEvent e) {
				
			}
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				layout(true, true);
			}
		});
	}
	
	protected void createNegationCompoiste(final Negation negation, Composite parent) 
	{
		Composite comp = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final Combo negationCombo = new Combo(comp, SWT.READ_ONLY | SWT.BORDER);
		negationCombo.setItems(new String[] {"", ExpressionValidatorComposite.NEGATION}); //$NON-NLS-1$
		negationCombo.select(1);
		negationCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String negationString = negationCombo.getText();
				if (!negationString.equals(ExpressionValidatorComposite.NEGATION)) {
					Composition parent = getParentForExpression(expression, negation);
					if (parent != null) {
						IExpression expression = negation.getExpression();
						parent.replaceExpression(negation, expression);
						refresh();
					}
				}
			}
		});
		createExpressionComposite(negation.getExpression(), comp);
	}
	
	protected void createTreeViewer(Composite parent) 
	{
		treeViewer = new TreeViewer(parent, getBorderStyle());
		treeViewer.setContentProvider(new ContentProvider());
		treeViewer.setLabelProvider(new LabelProvider());
		Tree tree = treeViewer.getTree();
		TreeColumn column = new TreeColumn(tree, SWT.NONE);
		column.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.table.column.name")); //$NON-NLS-1$
		WeightedTableLayout layout = new WeightedTableLayout(new int[] {1});
		tree.setLayout(layout);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		treeViewer.addSelectionChangedListener(treeListener);		
	}
	
	protected void createButtonComposite(IExpression expression, Composite parent) 
	{
		if (buttonComp != null && !buttonComp.isDisposed()) {
			buttonComp.dispose();
		}
		buttonComp = new XComposite(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER, 4);
		buttonComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addExpression = new Button(buttonComp, SWT.NONE);
		addExpression.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.button.addExpression.text")); //$NON-NLS-1$
		addExpression.addSelectionListener(new SelectionAdapter(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				addExpressionPressed();
			}
		});
		removeExpression = new Button(buttonComp, SWT.NONE);
		removeExpression.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.button.removeExpression.text")); //$NON-NLS-1$
		removeExpression.setEnabled(false);
		removeExpression.addSelectionListener(new SelectionAdapter(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeExpressionPressed();
			}
		});
		addComposition = new Button(buttonComp, SWT.NONE);
		addComposition.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.button.addComposition.text")); //$NON-NLS-1$
		addComposition.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				addCompositionPressed();
			}
		});		
		conditionOperatorCombo = new CompositionCombo(buttonComp, SWT.READ_ONLY | SWT.BORDER);
		conditionOperatorCombo.addElements(Arrays.asList(new String[] {AndCondition.OPERATOR_TEXT, OrCondition.OPERATOR_TEXT}));
		conditionOperatorCombo.selectElement(OrCondition.OPERATOR_TEXT);
		
		if (mode == Mode.STRUCT_FIELD) {
			addComposition.setEnabled(false);
			conditionOperatorCombo.setEnabled(false);
		}
		
	}
	
	protected void createExpressionText(IExpression expression, Composite parent) 
	{
		expressionText = new Text(parent, SWT.READ_ONLY | SWT.WRAP | SWT.BORDER);
		expressionText.setLayoutData(new GridData(GridData.FILL_BOTH));
		if (expression != null) {
			expressionText.setText(getText(expression));
		}
	}
	
	protected String getText(IExpression expression) 
	{
		// TODO should come from extension-point
		if (expression != null) 
		{
			if (expression instanceof Composition) 
			{
				Composition composition = (Composition) expression;
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("("); //$NON-NLS-1$
				for (int i=0; i<composition.getExpressions().size(); i++) {
					IExpression expr = composition.getExpressions().get(i);
					stringBuilder.append(getText(expr));
					if (i < composition.getExpressions().size()-1) {
						stringBuilder.append(" "); //$NON-NLS-1$
						stringBuilder.append(compositionOperatorLabelProvider.getText(composition.getOperatorText()));
						stringBuilder.append(" ");						 //$NON-NLS-1$
					}
				}
				stringBuilder.append(")"); //$NON-NLS-1$
				return stringBuilder.toString();
			}
			else if (expression instanceof DataFieldExpression<?> && struct != null) {
				DataFieldExpression<?> dataFieldExpression = (DataFieldExpression<?>) expression;
				StructFieldID structFieldID = dataFieldExpression.getStructFieldID();			
				StructField<?> structField;
				StringBuilder stringBuilder = new StringBuilder();
				if (expression != null) {
					try {
						structField = struct.getStructField(structFieldID);
						String name = structField.getName().getText();
						stringBuilder.append(name);
						if (expression instanceof GenericDataFieldNotEmptyExpression) {
//							stringBuilder.insert(0, NOT_EMPTY + " "); //$NON-NLS-1$
							stringBuilder.append(" "); //$NON-NLS-1$
							stringBuilder.append(NOT_EMPTY); //$NON-NLS-1$
						}
						return stringBuilder.toString();
					} catch (Exception e) {
						return expression.toString();	
					}				
				}
			}
			else if (expression instanceof Negation) 
			{
				Negation negation = (Negation) expression;
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(NEGATION);
				stringBuilder.append(" "); //$NON-NLS-1$
				stringBuilder.append(getText(negation.getExpression()));
				return stringBuilder.toString();
			}
			else {
				return expression.toString();	
			}			
		}
		return ""; //$NON-NLS-1$
	}
	
	public void setExpression(IExpression expression) 
	{
		this.expression = expression;		
		refresh();
	}

	public void refresh() 
	{
		ISelection oldSelection = treeViewer.getSelection();
		showExpression(expression);
		if (expression != null) {
			treeViewer.setInput(Collections.singleton(this.expression));	
		}
		else {
			treeViewer.setInput(Collections.emptyList());	
		}
		treeViewer.expandAll();
		if (oldSelection != null) {
			treeViewer.setSelection(oldSelection, true);
		}
	}
	
	public IExpression getExpression() {
		return expression;
	}
	
	public IExpression getSelectedExpression() {
		return selectedExpression;
	}
	
	public void setMessage(I18nText message) {
		this.message.copyFrom(message);
		if (i18nTextEditor != null && !i18nTextEditor.isDisposed()) {
			i18nTextEditor.setI18nText(this.message, EditMode.DIRECT);
		}
	}

	public I18nText getMessage() {
		return message;
	}
	
	public void setValidationResultType(ValidationResultType type) {
		this.validationResultType = type;
		if (validationResultTypeCombo != null && !validationResultTypeCombo.isDisposed()) {
			validationResultTypeCombo.selectElement(type);
		}
	}
	
	public ValidationResultType getValidationResultType() {
		return validationResultType;
	}
	
	protected void addExpressionPressed() 
	{
		addHandler.addExpressionPressed();
		validateOK();
	}
	
	protected void addCompositionPressed() 
	{
		String selection = conditionOperatorCombo.getSelectedElement();
		Composition newComposition = null; 
		if (selection.equals(AndCondition.OPERATOR_TEXT)) {
			newComposition = new AndCondition();
		}
		else if (selection.equals(OrCondition.OPERATOR_TEXT)) {
			newComposition = new OrCondition();
		}
		
		if (expression instanceof Composition) {
			Composition root = (Composition) expression;
			root.addExpression(newComposition);
			refresh();
		}
		else if (expression == null) {
			expression = newComposition;
			setExpression(expression);
		}
	}
	
	protected void removeExpressionPressed() {
		if (selectedExpression != null) {
			Composition parent = getParentForExpression(expression, selectedExpression);
			if (parent != null) {
				parent.removeExpression(selectedExpression);
			}
			// the selectedExpression is the root
			else if (selectedExpression.equals(expression)) {
				expression = null;
			}
			selectedExpression = null;
			setExpression(expression);
		}
	}
	
	protected Composition getParentForExpression(IExpression root, IExpression selectedExpression) {
		if (root instanceof Composition) {
			Composition composition = (Composition) root;
			for (IExpression expr : composition.getExpressions()) {
				if (expr.equals(selectedExpression)) {
					return composition;
				}
				Composition parent = getParentForExpression(expr, selectedExpression);
				if (parent != null) {
					return parent;
				}
			}
		}
		return null;
	}
	
	protected void validateOK() 
	{
		String message = null;
		
		if (expression == null)
			message = Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.errorMessage.noExpression"); //$NON-NLS-1$
		
		if (!checkExpression(expression))
			message = Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.errorMessage.compositionTooSmall"); //$NON-NLS-1$
		
		if (i18nTextEditor.getEditText().isEmpty())
			message = Messages.getString("org.nightlabs.jfire.base.ui.prop.validation.ExpressionValidatorComposite.errorMessage.messageEmpty"); //$NON-NLS-1$
		
		messageDisplayer.setMessage(message, IMessageProvider.INFORMATION);
	}
	
	protected boolean checkExpression(IExpression expression) 
	{
		if (expression instanceof Composition) {
			Composition composition = (Composition) expression;
			if (composition.getExpressions().size() < 2)
				return false;
			
			for (IExpression expr : composition.getExpressions()) {
				boolean check = checkExpression(expr);
				if (!check)
					return check;
			}
		}
		return true;
	}
}
