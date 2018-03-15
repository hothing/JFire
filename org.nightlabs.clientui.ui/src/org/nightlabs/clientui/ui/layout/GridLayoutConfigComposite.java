/**
 * 
 */
package org.nightlabs.clientui.ui.layout;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.clientui.layout.GridLayout;
import org.nightlabs.clientui.ui.resource.Messages;

/**
 * Composite that serves for editing an instance of {@link IGridLayoutConfig}.
 * <p>
 * It lets the user configure the properties of the {@link GridLayout} in the {@link IGridLayoutConfig} 
 * (numColumns, makeColumnsEqualWidth) and the list of its entries ({@link IGridDataEntry}).
 * </p>
 * <p>
 * Instantiate a {@link GridLayoutConfigComposite} and set the {@link IGridLayoutConfig} to edit by {@link #setGridLayoutConfig(IGridLayoutConfig)}. 
 * </p>
 * <p>
 * Instantiate a {@link GridLayoutConfigComposite} and set the {@link IGridLayoutConfig} to edit by {@link #setGridLayoutConfig(IGridLayoutConfig)}.
 * Note, that this Composites directly writes changes to the {@link IGridLayoutConfig} it edits, however, before using the {@link IGridLayoutConfig} again,
 * call {@link #updateGridLayoutConfig()} to be sure that all changes made by the user are reflected in the config. 
 * </p>
 * <p>
 * You can add {@link PropertyChangeListener} to react on changes made to the layout. Notifications are made for the following
 * properties {@link #PROP_GRID_LAYOUT_CONFIG}, {@link #PROP_GRID_LAYOUT}, {@link #PROP_GRID_DATA_ENTRY}. 
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public class GridLayoutConfigComposite extends XComposite {

	/**
	 * Property name for notifications made when the {@link GridLayout} of the edited {@link IGridLayoutConfig} changed.
	 */
	public static final String PROP_GRID_LAYOUT = "gridLayout"; //$NON-NLS-1$
	/**
	 * Property name for notifications made when the layout configuration changed in general,
	 * i.e. when the list or the order of its child entries was changed.
	 */
	public static final String PROP_GRID_LAYOUT_CONFIG = "gridLayoutConfig"; //$NON-NLS-1$
	/**
	 * Property name for notifications made when one of the entries inside the edited {@link IGridLayoutConfig} chagned. 
	 * 
	 */
	public static final String PROP_GRID_DATA_ENTRY = "gridDataEntry"; //$NON-NLS-1$
	/** The currently edited layout config */
	private IGridLayoutConfig gridLayoutConfig;
	
	private Text numColText;
	private Button colsEqualWidthCB;
	
	private GridDataEntryTable gridDataEntryTable;
	private Button addEntryButton;
	private Button removeEntryButton;
	private Button moveEntryUpButton;
	private Button moveEntryDownButton;
	
	private GridDataEntryConfigComposite gridDataEntryConfigComposite;
	
	private PropertyChangeSupport propertyChangeSupport;
	
	private PropertyChangeListener entryListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			propertyChangeSupport.firePropertyChange(PROP_GRID_DATA_ENTRY, evt.getOldValue(), evt.getNewValue());
		}
	};
	
	private SelectionListener cbListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (!updating) {
				updateGridLayout();
				fireGridLayoutChanged();
			}
		}
	};

	private boolean updating = false;
	
	private ModifyListener textListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			if (!updating) {
				updateGridLayout();
				fireGridLayoutChanged();
			}
		}
	};
	

	private ISelectionChangedListener selectionChangeListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			updateGridDataEntryConfigComposite();
		}
	};
	
	/**
	 * Construct a new {@link GridLayoutConfigComposite}.
	 * @param parent The parent Composite.
	 * @param style The style to apply to the new Composite.
	 */
	public GridLayoutConfigComposite(Composite parent, int style) {
		super(parent, style);
		propertyChangeSupport = new PropertyChangeSupport(this);
		XComposite layoutWrapper = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		layoutWrapper.getGridLayout().numColumns = 2;
		layoutWrapper.getGridLayout().makeColumnsEqualWidth = false;
		Label numColLabel = new Label(layoutWrapper, SWT.NONE);
		numColLabel.setText(Messages.getString("org.nightlabs.clientui.ui.layout.GridLayoutConfigComposite.label.numberOfColumns.text")); //$NON-NLS-1$
		
		Button previewButton = new Button(layoutWrapper, SWT.PUSH);
		GridData previewGD = new GridData();
		previewGD.verticalSpan = 3;
		previewGD.verticalAlignment = SWT.END;
		previewButton.setLayoutData(previewGD);
		previewButton.setText(Messages.getString("org.nightlabs.clientui.ui.layout.GridLayoutConfigComposite.button.preview.text")); //$NON-NLS-1$
		previewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (gridLayoutConfig != null) {
					gridDataEntryConfigComposite.updateGridDataEntry();
					updateGridLayout();
					GridLayoutConfigPreviewDialog dlg = new GridLayoutConfigPreviewDialog(getShell(), null, gridLayoutConfig);
					dlg.open();
				}
			}
		});
		
		numColText = new Text(layoutWrapper, getBorderStyle());
		GridData gd = new GridData();
		gd.widthHint = 80;
		numColText.setLayoutData(gd);
		numColText.addModifyListener(textListener);
		
		colsEqualWidthCB = new Button(layoutWrapper, SWT.CHECK);
		colsEqualWidthCB.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		colsEqualWidthCB.setText(Messages.getString("org.nightlabs.clientui.ui.layout.GridLayoutConfigComposite.button.columnsEqualWidth.text")); //$NON-NLS-1$
		colsEqualWidthCB.addSelectionListener(cbListener);
		
		XComposite bottomWrapper = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		bottomWrapper.getGridLayout().numColumns = 2;
		bottomWrapper.getGridLayout().makeColumnsEqualWidth = false;
		
		gridDataEntryTable = new GridDataEntryTable(bottomWrapper, SWT.NONE);
		gridDataEntryTable.addSelectionChangedListener(selectionChangeListener);
		
		XComposite buttonWrapper = new XComposite(bottomWrapper, SWT.NONE);
		
		addEntryButton = new Button(buttonWrapper, SWT.PUSH);
		addEntryButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addEntryButton.setText(Messages.getString("org.nightlabs.clientui.ui.layout.GridLayoutConfigComposite.button.add.text")); //$NON-NLS-1$
		addEntryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (gridLayoutConfig != null) {
					IGridDataEntry entry = gridLayoutConfig.addGridDataEntry();
					if (entry != null) {
						refreshEntryTable();
						gridDataEntryTable.setSelection(new StructuredSelection(entry), true);
						fireGridLayoutConfigChanged();
					}
				}					
			}
		});
		
		removeEntryButton = new Button(buttonWrapper, SWT.PUSH);
		removeEntryButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeEntryButton.setText(Messages.getString("org.nightlabs.clientui.ui.layout.GridLayoutConfigComposite.button.remove.text")); //$NON-NLS-1$
		removeEntryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (gridLayoutConfig != null) {
					Collection<IGridDataEntry> entries = gridDataEntryTable.getSelectedElements();
					for (IGridDataEntry entry : entries) {
						gridLayoutConfig.removeGridDataEntry(entry);
					}
					refreshEntryTable();
					fireGridLayoutConfigChanged();
				}
			}
		});
		
		
		moveEntryUpButton = new Button(buttonWrapper, SWT.PUSH);
		moveEntryUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		moveEntryUpButton.setText(Messages.getString("org.nightlabs.clientui.ui.layout.GridLayoutConfigComposite.button.up.text")); //$NON-NLS-1$
		moveEntryUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (gridLayoutConfig != null) {
					Collection<IGridDataEntry> entries = gridDataEntryTable.getSelectedElements();
					for (IGridDataEntry entry : entries) {
						if (!gridLayoutConfig.moveEntryUp(entry)) {
							break;
						}
					}
					refreshEntryTable();
					fireGridLayoutConfigChanged();
				}
			}
		});
		
		moveEntryDownButton  = new Button(buttonWrapper, SWT.PUSH);
		moveEntryDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		moveEntryDownButton.setText(Messages.getString("org.nightlabs.clientui.ui.layout.GridLayoutConfigComposite.button.down.text")); //$NON-NLS-1$
		moveEntryDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (gridLayoutConfig != null) {
					List<IGridDataEntry> entries = new ArrayList<IGridDataEntry>(gridDataEntryTable.getSelectedElements());
					Collections.reverse(entries);
					for (IGridDataEntry entry : entries) {
						if (!gridLayoutConfig.moveEntryDown(entry)) {
							break;
						}
					}
					refreshEntryTable();
					fireGridLayoutConfigChanged();
				}
			}
		});
		
		gridDataEntryConfigComposite = new GridDataEntryConfigComposite(buttonWrapper, SWT.NONE, LayoutDataMode.NONE);
		GridData gdEntry = new GridData(GridData.FILL_VERTICAL);
		gdEntry.horizontalAlignment = SWT.FILL;
		gdEntry.widthHint = 50;
		gridDataEntryConfigComposite.setLayoutData(gdEntry);
		gridDataEntryConfigComposite.addPropertyChangeListener(entryListener);
	}
	
	/**
	 * Set the {@link IGridLayoutConfig} that should be edited by this {@link GridLayoutConfigComposite}.
	 * After the user finished editing make sure you call {@link #updateGridLayoutConfig()}.
	 * 
	 * @param gridLayoutConfig The {@link IGridLayoutConfig} to edit.
	 */
	public void setGridLayoutConfig(IGridLayoutConfig gridLayoutConfig) {
		this.gridLayoutConfig = gridLayoutConfig;
		if (isDisposed())
			return;
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				updating = true;
				try {
					IGridLayoutConfig cfg = GridLayoutConfigComposite.this.gridLayoutConfig;
					if (cfg == null) {
						setEnabled(false);
					} else {
						setEnabled(true);
						gridDataEntryTable.setInput(cfg.getGridDataEntries());
						if (gridDataEntryTable.getFirstSelectedElement() == null) {
							gridDataEntryTable.select(0);
							updateGridDataEntryConfigComposite();
						}
						GridLayout gridLayout = cfg.getGridLayout();
						if (gridLayout != null) {
							numColText.setText(String.valueOf(gridLayout.getNumColumns()));
							numColText.setText(String.valueOf(gridLayout.getNumColumns()));
							colsEqualWidthCB.setSelection(gridLayout.isMakeColumnsEqualWidth());
						} else {
							numColText.setText("1"); //$NON-NLS-1$
							colsEqualWidthCB.setSelection(true);
						}
					}
				} finally {
					updating = false;
				}
			}
		});
	}
	
	/**
	 * Copies all changes made to the {@link IGridLayoutConfig} currently edited.
	 * Note, that some changes might already have been made to the layout while it 
	 * was edited. 
	 */
	protected void updateGridLayout() {
		if (isDisposed())
			return;
		getDisplay().syncExec(new Runnable() {
			public void run() {
				gridDataEntryConfigComposite.updateGridDataEntry();
				IGridLayoutConfig cfg = GridLayoutConfigComposite.this.gridLayoutConfig;
				if (cfg == null)
					return;
				GridLayout gridLayout = cfg.getGridLayout();
				if (gridLayout != null) {
					gridLayout.setMakeColumnsEqualWidth(colsEqualWidthCB.getSelection());
					int numCols = 1;
					try {
						numCols = Integer.parseInt(numColText.getText());
					} catch (NumberFormatException e) {
						numCols = 1;
						updating = true;
						try {
							numColText.setText(String.valueOf(numCols));
						} finally {
							updating = true;
						}
					}
				}
			}
		});
	}
	
	public void updateGridLayoutConfig() {
		updateGridDataEntryConfigComposite();
		updateGridLayout();
	}
	
	protected void updateGridDataEntryConfigComposite() {
		gridDataEntryConfigComposite.updateGridDataEntry();
		if (gridDataEntryTable.getFirstSelectedElement() != null) {
			gridDataEntryConfigComposite.setGridDataEntry(gridDataEntryTable.getFirstSelectedElement());
		}		
	}
	
	protected void refreshEntryTable() {
		if (gridLayoutConfig != null) {
			gridDataEntryTable.setInput(gridLayoutConfig.getGridDataEntries());
			gridDataEntryTable.refresh();
		}
	}
	
	private void fireGridLayoutConfigChanged() {
		propertyChangeSupport.firePropertyChange(PROP_GRID_LAYOUT_CONFIG, null, gridLayoutConfig);
	}
	private void fireGridLayoutChanged() {
		propertyChangeSupport.firePropertyChange(PROP_GRID_LAYOUT, null, gridLayoutConfig);
	}
	
	public IGridLayoutConfig getGridLayoutConfig() {
		return gridLayoutConfig;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
