/* *****************************************************************************
 * org.nightlabs.base.ui - NightLabs Eclipse utilities                            *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.base.ui.print.pref;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.composite.LabeledText;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.resource.Messages;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.print.DocumentPrinterDelegateConfig;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class EditDocumentPrinterTypeRegsComposite extends XComposite {

//	private List fileExtList;
	private ListViewer fileExtListViewer;
	private XComposite buttonWrapper;
	private Button addDelegation;
	private Button removeDelegation;

	private Map<String, DocumentPrinterDelegateConfig> delegateConfigs = new HashMap<String, DocumentPrinterDelegateConfig>();

	/**
	 * @param parent
	 * @param style
	 */
	public EditDocumentPrinterTypeRegsComposite(Composite parent, int style) {
		super(parent, style);
		initGUI();
	}

	/**
	 * @param parent
	 * @param style
	 * @param layoutMode
	 */
	public EditDocumentPrinterTypeRegsComposite(Composite parent, int style,
			LayoutMode layoutMode) {
		super(parent, style, layoutMode);
		initGUI();
	}

	/**
	 * @param parent
	 * @param style
	 * @param layoutDataMode
	 */
	public EditDocumentPrinterTypeRegsComposite(Composite parent, int style,
			LayoutDataMode layoutDataMode) {
		super(parent, style, layoutDataMode);
		initGUI();
	}

	/**
	 * @param parent
	 * @param style
	 * @param layoutMode
	 * @param layoutDataMode
	 */
	public EditDocumentPrinterTypeRegsComposite(Composite parent, int style,
			LayoutMode layoutMode, LayoutDataMode layoutDataMode) {
		super(parent, style, layoutMode, layoutDataMode);
		initGUI();
	}

	/**
	 * @param parent
	 * @param style
	 * @param layoutMode
	 * @param layoutDataMode
	 * @param cols
	 */
	public EditDocumentPrinterTypeRegsComposite(Composite parent, int style,
			LayoutMode layoutMode, LayoutDataMode layoutDataMode, int cols) {
		super(parent, style, layoutMode, layoutDataMode, cols);
		initGUI();
	}

	class ListViewerContentProvider implements IStructuredContentProvider {

		@Override
        public Object[] getElements(Object inputElement) {

			if (inputElement instanceof String){
				return new Object[] {inputElement};
			}
			return new Object[] {};

        }

		@Override
        public void dispose() {
        }

		@Override
        public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
        }

	}

	class ListViewerLabelProvider extends LabelProvider {
		@Override
        public org.eclipse.swt.graphics.Image getImage(Object element) {
			return null;
	    }

	    @Override
        public String getText(Object element) {
	    	return element.toString();


//	    	return null;
	    }
	}

	protected void initGUI() {
		getGridLayout().numColumns = 2;
//		fileExtList = new List(this, SWT.BORDER);
//		fileExtList.setLayoutData(new GridData(GridData.FILL_BOTH));

		fileExtListViewer = new ListViewer(this, SWT.BORDER | SWT.V_SCROLL);
		fileExtListViewer.setContentProvider(new ListViewerContentProvider());
		fileExtListViewer.setLabelProvider(new ListViewerLabelProvider());
		fileExtListViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

		buttonWrapper = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.NONE);
		buttonWrapper.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		addDelegation = new Button(buttonWrapper, SWT.PUSH);
		addDelegation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addDelegation.setText(Messages.getString("org.nightlabs.base.ui.print.pref.EditDocumentPrinterTypeRegsComposite.addDelegation.text")); //$NON-NLS-1$
		addDelegation.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			public void widgetSelected(SelectionEvent arg0) {
				String fileExt = openFileExtDlg();
				if (fileExt != null && !"".equals(fileExt)) { //$NON-NLS-1$
					addTypeReg(fileExt);
					// TODO: Removed, as the SelectionListener does not get notified. Why not?
//					fileExtList.setSelection(new String[]{fileExt});
				}
			}
		});

		removeDelegation = new Button(buttonWrapper, SWT.PUSH);
		removeDelegation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeDelegation.setText(Messages.getString("org.nightlabs.base.ui.print.pref.EditDocumentPrinterTypeRegsComposite.removeDelegation.text")); //$NON-NLS-1$
		removeDelegation.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			public void widgetSelected(SelectionEvent arg0) {
				removeTypeReg(getSelectedFileExt());
			}
		});
	}

	public String getSelectedFileExt() {
//		String[] selection = fileExtList.getSelection();
//		if (selection.length > 0)
//			return selection[0];
//		else
//			return null;

		if (fileExtListViewer.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) fileExtListViewer.getSelection();
			if (structuredSelection.getFirstElement() instanceof String) {
				String selection = (String)structuredSelection.getFirstElement();
				System.out.println("selected entry: " + selection); //$NON-NLS-1$
				return selection;
			}
		}
		return null;
	}

	/**
	 * @param arg0
	 * @see org.eclipse.swt.widgets.Button#addSelectionListener(org.eclipse.swt.events.SelectionListener)
	 */
//	public void addSelectionListener(SelectionListener listener) {
//		fileExtList.addSelectionListener(listener);
//	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fileExtListViewer.addSelectionChangedListener(listener);
	}


	public void setDelegateConfigs(Map<String, DocumentPrinterDelegateConfig> _delegateConfigs) {
		delegateConfigs.clear();
		if (_delegateConfigs != null) {
			for (Entry<String, DocumentPrinterDelegateConfig> entry : _delegateConfigs.entrySet()) {
				delegateConfigs.put(entry.getKey(), entry.getValue());
			}
		}
		updateList();
	}

	public DocumentPrinterDelegateConfig getDelegateConfig(String fileExt) {
		return delegateConfigs.get(fileExt);
	}

	public boolean hasRegistration(String fileExt) {
		return delegateConfigs.containsKey(fileExt);
	}

	public Map<String, DocumentPrinterDelegateConfig> getDelegateConfigs() {
		return delegateConfigs;
	}

	public void setDelegateConfig(String fileExt, DocumentPrinterDelegateConfig delegateConfig) {
		delegateConfigs.put(fileExt, delegateConfig);
		updateList();
	}

	public void updateList() {

//		String[] selection = fileExtList.getSelection();
//		fileExtList.removeAll();
//		SortedSet<String> keys = new TreeSet<String>();
//		keys.addAll(delegateConfigs.keySet());
//		for (String fileExt : keys) {
//			fileExtList.add(fileExt);
//		}
//		fileExtList.setSelection(selection);


		// does only remember the first selection out of a (possible larger) set of selections
/*		String selection = "";
		if (fileExtListViewer.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) fileExtListViewer.getSelection();
			if (structuredSelection.getFirstElement() instanceof String) {
				selection = (String)structuredSelection.getFirstElement();
			}
		}*/

		String[] selections = null; // = new String[];

		if (fileExtListViewer.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) fileExtListViewer.getSelection();
			int amountOfChosenEntries = structuredSelection.toList().size();
			selections = new String[amountOfChosenEntries];
			for (int j = 0; j < amountOfChosenEntries; j++) {
				selections[j] = (String) structuredSelection.toList().get(j);
			}
		}

		fileExtListViewer.setInput(null);
		SortedSet<String> keys = new TreeSet<String>();
		keys.addAll(delegateConfigs.keySet());
		for (String fileExt : keys) {
			fileExtListViewer.add(fileExt);
		}

		if (selections != null) {
			fileExtListViewer.getList().setSelection(selections);
		}

	}

	public void addTypeReg(String fileExt) {
		delegateConfigs.put(fileExt, null);
		updateList();
		fileExtListViewer.setSelection(new StructuredSelection(fileExt));
	}

	public void removeTypeReg(String fileExt) {
		delegateConfigs.remove(fileExt);
		updateList();
	}

	public static class FileExtDialog extends Dialog {
		public LabeledText fileExtText;
		private String fileExt;
		public FileExtDialog(Shell parentShell) {
			super(parentShell);
		}
		@Override
		protected Control createDialogArea(Composite parent) {
			XComposite wrapper = new XComposite(parent, SWT.NONE);
			fileExtText = new LabeledText(wrapper, Messages.getString("org.nightlabs.base.ui.print.pref.EditDocumentPrinterTypeRegsComposite.fileExtText.caption")); //$NON-NLS-1$
			fileExtText.getTextControl().addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent evt) {
					fileExt = fileExtText.getTextControl().getText();
				}
			});
			return wrapper;
		}
		public String getFileExt() {
			return fileExt;
		}
	}

	public static String openFileExtDlg() {
		FileExtDialog dlg = new FileExtDialog(RCPUtil.getActiveShell());
		if (dlg.open() == Window.OK)
			return dlg.getFileExt();
		else
			return null;
	}

}
