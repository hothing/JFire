/* ********************************************************************
 * NightLabs PDF Viewer - http://www.nightlabs.org/projects/pdfviewer *
 * Copyright (C) 2004-2008 NightLabs GmbH - http://NightLabs.org      *
 *                                                                    *
 * This library is free software; you can redistribute it and/or      *
 * modify it under the terms of the GNU Lesser General Public         *
 * License as published by the Free Software Foundation; either       *
 * version 2.1 of the License, or (at your option) any later version. *
 *                                                                    *
 * This library is distributed in the hope that it will be useful,    *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of     *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  *
 * Lesser General Public License for more details.                    *
 *                                                                    *
 * You should have received a copy of the GNU Lesser General Public   *
 * License along with this library; if not, write to the              *
 *     Free Software Foundation, Inc.,                                *
 *     51 Franklin St, Fifth Floor,                                   *
 *     Boston, MA  02110-1301  USA                                    *
 *                                                                    *
 * Or get it online:                                                  *
 *     http://www.gnu.org/copyleft/lesser.html                        *
 **********************************************************************/
package org.nightlabs.eclipse.ui.pdfviewer.internal;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.eclipse.ui.pdfviewer.PdfDocument;
import org.nightlabs.eclipse.ui.pdfviewer.PdfSimpleNavigator;
import org.nightlabs.eclipse.ui.pdfviewer.PdfViewer;
import org.nightlabs.eclipse.ui.pdfviewer.PdfViewerPlugin;

import com.sun.pdfview.PDFFile;

/**
 * @version $Revision: 343 $ - $Date: 2008-10-10 00:42:14 +0200 (Fr, 10 Okt 2008) $
 * @author frederik loeser - frederik at nightlabs dot de
 * @author marco schulze - marco at nightlabs dot de
 */
public class PdfSimpleNavigatorComposite extends Composite {

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	private PdfSimpleNavigator pdfSimpleNavigator;
	private Button gotoFirstPageButton;
	private Button gotoPreviousPageButton;
	private Button gotoNextPageButton;
	private Button gotoLastPageButton;
	private Text currentPageNumberText;
	private Label label;
	private PDFFile pdfFile;
	private PdfDocument pdfDocument;
	private int numberOfPages;
//	private ModifyListenerImpl modifyListenerImpl;

	public enum ImageKey {
		gotoFirstPageButton_enabled,
		gotoPreviousPageButton_enabled,
		gotoNextPageButton_enabled,
		gotoLastPageButton_enabled
	}

	public PdfSimpleNavigatorComposite(Composite parent, int style, PdfSimpleNavigator pdfSimpleNavigator) {
		super(parent, style);
		this.pdfSimpleNavigator = pdfSimpleNavigator;

		GridLayout gridLayout = new GridLayout(6, false);
		this.setLayout(gridLayout);

		ImageRegistry imageRegistry = PdfViewerPlugin.getDefault().getImageRegistry();
		Image gotoFirstPageButtonImage = imageRegistry.get(ImageKey.gotoFirstPageButton_enabled.name());
		Image gotoPreviousPageButtonImage = imageRegistry.get(ImageKey.gotoPreviousPageButton_enabled.name());
		Image gotoNextPageButtonImage = imageRegistry.get(ImageKey.gotoNextPageButton_enabled.name());
		Image gotoLastPageButtonImage = imageRegistry.get(ImageKey.gotoLastPageButton_enabled.name());

		gotoFirstPageButton = new Button(this, SWT.NONE);
		gotoFirstPageButton.setImage(gotoFirstPageButtonImage);
		gotoPreviousPageButton = new Button(this, SWT.NONE);
		gotoPreviousPageButton.setImage(gotoPreviousPageButtonImage);
		currentPageNumberText = new Text(this, SWT.BORDER);
		GridData gd1 = new GridData();
		currentPageNumberText.setLayoutData(gd1);
		currentPageNumberText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
//			    if (e.keyCode == 16777296) {
				if (e.character == '\r' || e.character == '\n') { // in Linux, it should be \n, but I just got \r => we better play safe and check for both.
			    	int pageNumber = Integer.parseInt(currentPageNumberText.getText());
			    	gotoPage(pageNumber);
			    }
			}
		});
		label = new Label(this, SWT.NONE);
		gotoNextPageButton = new Button(this, SWT.NONE);
		gotoNextPageButton.setImage(gotoNextPageButtonImage);
		gotoLastPageButton = new Button(this, SWT.NONE);
		gotoLastPageButton.setImage(gotoLastPageButtonImage);

//		modifyListenerImpl = new ModifyListenerImpl();
//		currentPageNumberText.addModifyListener(modifyListenerImpl);


		gotoFirstPageButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
//				int oldPageNumber = new Integer(currentPageNumberText.getText()).intValue();
				gotoPage(1);
			}
		});

		gotoPreviousPageButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				int oldPageNumber = new Integer(currentPageNumberText.getText()).intValue();
				gotoPage(oldPageNumber - 1);
			}
		});

		gotoNextPageButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				int oldPageNumber = new Integer(currentPageNumberText.getText()).intValue();
				gotoPage(oldPageNumber + 1);
			}
		});

		gotoLastPageButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
//				int oldPageNumber = new Integer(currentPageNumberText.getText()).intValue();
				gotoPage(numberOfPages);
			}
		});

		currentPageNumberText.addVerifyListener(new VerifyListener() {
			@Override
            public void verifyText(VerifyEvent event) {
				if (event.text != null) {
					event.doit = true;
					for (int idx = 0; idx < event.text.length(); ++idx) {
						if (!validChars.contains(event.text.charAt(idx))) {
							event.doit = false;
							break;
						}
                    }
				}
				else {
					event.doit = validChars.contains(event.character) || event.keyCode == SWT.DEL;

//					event.doit = (
//							(c >= '0' && c <= '9') // accept digits
//							|| c == 8 // accept backspace
//							|| event.keyCode == SWT.DEL // accept 'delete' key
//					);
				}
            }
		});

		currentPageNumberText.addKeyListener(new KeyListener() {
			@Override
            public void keyPressed(KeyEvent e) {
            }
			@Override
            public void keyReleased(KeyEvent e) {
            }
		});

		setPdfDocument(null);
	}

	protected PdfSimpleNavigator getPdfSimpleNavigator() {
	    return pdfSimpleNavigator;
    }

	private static final Set<Character> validChars;
	static {
		Set<Character> chars = new HashSet<Character>();
		for (char c = '0'; c <= '9'; ++c)
			chars.add(c);
		chars.add((char)8);

		validChars = Collections.unmodifiableSet(chars);
	}

	public void setControlEnabledStatus(int currentPageNumber)
	{
		gotoFirstPageButton.setEnabled(currentPageNumber > 1);
		gotoPreviousPageButton.setEnabled(currentPageNumber > 1);

		gotoNextPageButton.setEnabled(currentPageNumber < numberOfPages);
		gotoLastPageButton.setEnabled(currentPageNumber < numberOfPages);
	}

	private void gotoPage(int pageNumber) {
//		currentPageNumberText.removeModifyListener(modifyListenerImpl);

		if (pageNumber < 1)
			pageNumber = 1;

		if (pageNumber > numberOfPages)
			pageNumber = numberOfPages;

		currentPageNumberText.setText(String.valueOf(pageNumber));
		setControlEnabledStatus(pageNumber);

		propertyChangeSupport.firePropertyChange(	PdfViewer.PROPERTY_CURRENT_PAGE,
													0,
													pageNumber
												);

//		if (chosenPageNumber >= 1 && chosenPageNumber <= numberOfPages) {
//			currentPageNumberText.setText(String.valueOf(chosenPageNumber));
//
//			if (chosenPageNumber == 1) {
//				gotoPreviousPageButton.setEnabled(false);
//				if (numberOfPages > 1 && !gotoNextPageButton.isEnabled()){
//					gotoNextPageButton.setEnabled(true);
//				}
//			}
//			if (chosenPageNumber == numberOfPages) {
//				gotoNextPageButton.setEnabled(false);
//				if (numberOfPages > 1 && !gotoPreviousPageButton.isEnabled()) {
//					gotoPreviousPageButton.setEnabled(true);
//				}
//			}
//			if (chosenPageNumber > 1 && chosenPageNumber < numberOfPages) {
//				if (!gotoPreviousPageButton.isEnabled()) {
//					gotoPreviousPageButton.setEnabled(true);
//				}
//				if (!gotoNextPageButton.isEnabled()) {
//					gotoNextPageButton.setEnabled(true);
//				}
//			}
//
//			gotoFirstPageButton.setEnabled(chosenPageNumber > 1);
//			gotoNextPageButton.setEnabled(numberOfPages > 1 && chosenPageNumber < numberOfPages);
//
//			scrollToPage(chosenPageNumber);
//		}
//		else {
//			currentPageNumberText.setText(String.valueOf(pdfSimpleNavigator.getPdfViewer().getCurrentPage()));
//			scrollToPage(numberOfPages);
//		}

//		currentPageNumberText.addModifyListener(modifyListenerImpl);
	}

	public PdfDocument getPdfDocument() {
		return pdfDocument;
	}

	public void setPdfDocument(PdfDocument pdfDocument) {
		this.pdfDocument = pdfDocument;
		this.pdfFile = pdfDocument != null ? pdfDocument.getPdfFile() : null;

		if (pdfFile == null)
			numberOfPages = 0;
		else
			numberOfPages = pdfFile.getNumPages();

		if (numberOfPages < 1) {
			setControlEnabledStatus(1);
			currentPageNumberText.setText(String.valueOf(1));
			label.setText(" / 0"); //$NON-NLS-1$
		}
		else {
			currentPageNumberText.setText(String.valueOf(1));
			currentPageNumberText.setTextLimit((Integer.toString(numberOfPages)).length());
			label.setText(" / " + numberOfPages); //$NON-NLS-1$
			boolean morePages = numberOfPages > 1 ? true : false;
			gotoNextPageButton.setEnabled(morePages);

			// the width of currentPageNumberText is dependent on the number of pages of the given document
			((GridData)currentPageNumberText.getLayoutData()).widthHint = label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			getParent().layout(true, true);
		}
		setControlEnabledStatus(1);
	}

	public PDFFile getPdfFile() {
		return pdfFile;
	}

//	private class ModifyListenerImpl implements ModifyListener {
//		@Override
//        public void modifyText(ModifyEvent event) {
//			Text text = (Text) event.widget;
//			// TODO activate verify listener for checking validity of input in general
//			if (!(text.getText().equals(""))) {
//				int chosenPageNumber = new Integer(text.getText()).intValue();
//				// TODO do not use highest page number as fix old value but send real old value instead
//				gotoPage(numberOfPages, chosenPageNumber);
//			}
//        }
//	}

	public Text getCurrentPageNumberText() {
    	return currentPageNumberText;
    }

	public void setCurrentPageNumberText(Text currentPageNumberText) {
    	this.currentPageNumberText = currentPageNumberText;
    }

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}


}
