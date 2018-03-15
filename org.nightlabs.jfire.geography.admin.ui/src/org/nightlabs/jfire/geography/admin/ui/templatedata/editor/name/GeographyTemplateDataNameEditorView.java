package org.nightlabs.jfire.geography.admin.ui.templatedata.editor.name;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.base.ui.login.part.LSDViewPart;

public class GeographyTemplateDataNameEditorView 
//extends ViewPart implements ControllablePart
extends LSDViewPart
{
	public final static String ID_VIEW = "org.nightlabs.jfire.geography.admin.ui.templatedata.editor.name.GeographyTemplateDataNameEditorView"; //$NON-NLS-1$

	private GeographyNameTableComposite geographyTableComposite;
	
	public GeographyTemplateDataNameEditorView() {
		super();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
	}

	public void createPartContents(Composite parent) 
	{
		GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.horizontalSpacing = 1;
    parent.setLayout(gridLayout);
	    
		geographyTableComposite = new GeographyNameTableComposite(parent, SWT.NONE, true);
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		geographyTableComposite.setLayoutData(gd);
	}
	
	public void updateData(Object element){
		geographyTableComposite.updateTable(element);
	}
}