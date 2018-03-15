/**
 * 
 */
package org.nightlabs.clientui.ui.layout;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.clientui.layout.GridData;
import org.nightlabs.clientui.ui.resource.Messages;

/**
 * Composite used by {@link GridLayoutConfigComposite} to edit the {@link GridData} of a single {@link IGridDataEntry} inside a {@link IGridLayoutConfig}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public class GridDataEntryConfigComposite extends XComposite {

	public static final String PROP_GRID_DATA_ENTY = "gridDataEntry";  //$NON-NLS-1$
	
	private Button fillHorizontalCB;
	private Button fillVerticalCB;
	private Button grabHorizontalCB;
	private Button grabVerticalCB;
	private Text spanHorizontalText;
	private Text spanVerticalText;
	
	private IGridDataEntry gridDataEntry;
	
	private PropertyChangeSupport propertyChangeSupport;
	
	private SelectionListener cbListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			propertyChangeSupport.firePropertyChange(PROP_GRID_DATA_ENTY, gridDataEntry, null);
		}
	};

	private boolean updating = false;
	
	private ModifyListener textListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			if (!updating)
				propertyChangeSupport.firePropertyChange(PROP_GRID_DATA_ENTY, gridDataEntry, null);
		}
	};
	
	/**
	 * Construct a new {@link GridDataEntryConfigComposite}.
	 * @param parent The parent Composite.
	 * @param style The style to apply to the new Composite.
	 */
	public GridDataEntryConfigComposite(Composite parent, int style) {
		super(parent, style);
		init();
	}

	/**
	 * Construct a new {@link GridDataEntryConfigComposite}.
	 * 
	 * @param parent The parent Composite.
	 * @param style The style to apply to the new Composite.
	 * @param layoutDataMode The {@link LayoutDataMode} to apply.
	 */
	public GridDataEntryConfigComposite(Composite parent, int style, LayoutDataMode layoutDataMode) {
		super(parent, style, layoutDataMode);
		init();
	}

	/**
	 * Creates the actual contents.
	 */
	private void init() {
		getGridLayout().numColumns = 2;
		propertyChangeSupport = new PropertyChangeSupport(this);
		
		Label fillLabel = new Label(this, SWT.NONE);
		fillLabel.setLayoutData(createSpanData());
		fillLabel.setText(Messages.getString("org.nightlabs.clientui.ui.layout.GridDataEntryConfigComposite.label.fill.text")); //$NON-NLS-1$
		
		fillHorizontalCB = new Button(this, SWT.CHECK);
		fillHorizontalCB.setLayoutData(createGridData());
		fillHorizontalCB.setText(Messages.getString("org.nightlabs.clientui.ui.layout.GridDataEntryConfigComposite.label.fill-horizontal.text")); //$NON-NLS-1$
		fillHorizontalCB.addSelectionListener(cbListener);
		fillHorizontalCB.addSelectionListener(cbListener);
		
		fillVerticalCB = new Button(this, SWT.CHECK);
		fillVerticalCB.setLayoutData(createGridData());
		fillVerticalCB.setText(Messages.getString("org.nightlabs.clientui.ui.layout.GridDataEntryConfigComposite.label.fill-vertical.text")); //$NON-NLS-1$
		fillVerticalCB.addSelectionListener(cbListener);
		fillVerticalCB.addSelectionListener(cbListener);
		
		Label grabLabel = new Label(this, SWT.NONE);
		grabLabel.setLayoutData(createSpanData());
		grabLabel.setText(Messages.getString("org.nightlabs.clientui.ui.layout.GridDataEntryConfigComposite.label.grab.text")); //$NON-NLS-1$
		
		grabHorizontalCB = new Button(this, SWT.CHECK);
		grabHorizontalCB.setLayoutData(createGridData());
		grabHorizontalCB.setText(Messages.getString("org.nightlabs.clientui.ui.layout.GridDataEntryConfigComposite.label.grab-horizontal.text")); //$NON-NLS-1$
		grabHorizontalCB.addSelectionListener(cbListener);
		grabHorizontalCB.addSelectionListener(cbListener);
		
		grabVerticalCB = new Button(this, SWT.CHECK);
		grabVerticalCB.setLayoutData(createGridData());
		grabVerticalCB.setText(Messages.getString("org.nightlabs.clientui.ui.layout.GridDataEntryConfigComposite.label.grab-vertical.text")); //$NON-NLS-1$
		grabVerticalCB.addSelectionListener(cbListener);
		grabVerticalCB.addSelectionListener(cbListener);
		
		Label spanLabel = new Label(this, SWT.NONE);
		spanLabel.setLayoutData(createSpanData());
		spanLabel.setText(Messages.getString("org.nightlabs.clientui.ui.layout.GridDataEntryConfigComposite.label.span.text")); //$NON-NLS-1$
		
		Label spanHorizontalLabel = new Label(this, SWT.NONE);
		spanHorizontalLabel.setLayoutData(createGridData());
		spanHorizontalLabel.setText(Messages.getString("org.nightlabs.clientui.ui.layout.GridDataEntryConfigComposite.label.span-horizontal.text")); //$NON-NLS-1$
		
		Label spanVerticalLabel = new Label(this, SWT.NONE);
		spanVerticalLabel.setLayoutData(createGridData());
		spanVerticalLabel.setText(Messages.getString("org.nightlabs.clientui.ui.layout.GridDataEntryConfigComposite.label.span-vertical.text")); //$NON-NLS-1$
		
		spanHorizontalText = new Text(this, getBorderStyle());
		spanHorizontalText.setLayoutData(createGridData());
		spanHorizontalText.addModifyListener(textListener);
		spanHorizontalText.addModifyListener(textListener);
		
		spanVerticalText = new Text(this, getBorderStyle());
		spanVerticalText.setLayoutData(createGridData());
		spanVerticalText.addModifyListener(textListener);
		spanVerticalText.addModifyListener(textListener);
	}
	
	private org.eclipse.swt.layout.GridData createSpanData() {
		org.eclipse.swt.layout.GridData gd = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		return gd;
	}
	
	private org.eclipse.swt.layout.GridData createGridData() {
		org.eclipse.swt.layout.GridData gd = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL);
		return gd;
	}

	/**
	 * Set the {@link IGridDataEntry} to edit.
	 * @param gridDataEntry The entry to edit.
	 */
	public void setGridDataEntry(IGridDataEntry gridDataEntry) {
		this.gridDataEntry = gridDataEntry;
		if (!isDisposed()) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!isDisposed()) {
						updating = true;
						try {
							if (GridDataEntryConfigComposite.this.gridDataEntry == null) {
								fillHorizontalCB.setSelection(false);
								fillVerticalCB.setSelection(false);
								grabHorizontalCB.setSelection(false);
								grabVerticalCB.setSelection(false);
								setEnabled(false);
							} else {
								setEnabled(true);
								GridData entry = GridDataEntryConfigComposite.this.gridDataEntry.getGridData();
								fillHorizontalCB.setSelection((entry.getHorizontalAlignment() & GridData.FILL) > 0);
								fillVerticalCB.setSelection((entry.getVerticalAlignment() & GridData.FILL) > 0);
								grabHorizontalCB.setSelection(entry.isGrabExcessHorizontalSpace());
								grabVerticalCB.setSelection(entry.isGrabExcessVerticalSpace());
								spanHorizontalText.setText(String.valueOf(entry.getHorizontalSpan()));
								spanVerticalText.setText(String.valueOf(entry.getVerticalSpan()));
							}
						} finally {
							updating = false;
						}
					}
				}
			});
		}
	}
	
	/**
	 * Reflect all changes made in the ui to the {@link IGridDataEntry}.
	 */
	public void updateGridDataEntry() {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				if (GridDataEntryConfigComposite.this.gridDataEntry == null)
					return;
				GridData gd = GridDataEntryConfigComposite.this.gridDataEntry.getGridData();
				if (fillHorizontalCB.getSelection())
					gd.setHorizontalAlignment(GridData.FILL);
				else
					gd.setHorizontalAlignment(GridData.BEGINNING);
				
				if (fillVerticalCB.getSelection())
					gd.setVerticalAlignment(GridData.FILL);
				else
					gd.setVerticalAlignment(GridData.BEGINNING);
				
				gd.setGrabExcessHorizontalSpace(grabHorizontalCB.getSelection());
				gd.setGrabExcessVerticalSpace(grabVerticalCB.getSelection());
				
				int span = 1;

				try {
					span = Integer.parseInt(spanHorizontalText.getText());					
				} catch (NumberFormatException e) {
					span = 1;
				}
				gd.setHorizontalSpan(span);

				try {
					span = Integer.parseInt(spanVerticalText.getText());					
				} catch (NumberFormatException e) {
					span = 1;
				}
				gd.setVerticalSpan(span);
			}
		});
	}
	/**
	 * @return The {@link IGridDataEntry} that was set for editing.
	 */
	public IGridDataEntry getGridDataEntry() {
		return gridDataEntry;
	}
	/**
	 * Add a {@link PropertyChangeListener} that will be notified when the user
	 * changes the settings of a {@link IGridDataEntry}.
	 * @param listener The listener to add.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}
	/**
	 * Remove the given listener.
	 * @param listener The listener to remove.
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
	
}

