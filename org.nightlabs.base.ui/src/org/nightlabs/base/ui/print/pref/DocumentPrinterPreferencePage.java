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

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.print.DelegatingDocumentPrinterCfMod;
import org.nightlabs.print.DocumentPrinterDelegateConfig;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class DocumentPrinterPreferencePage
extends PreferencePage
implements IWorkbenchPreferencePage {

	private XComposite wrapper;

	private String currentFileExt = null;
	private EditDocumentPrinterTypeRegsComposite typeRegsComposite;
	private EditDocumentPrinterConfigComposite printerConfigComposite;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		wrapper = new XComposite(parent, SWT.NONE);
		typeRegsComposite = new EditDocumentPrinterTypeRegsComposite(wrapper, SWT.NONE);
//		typeRegsComposite.addSelectionListener(new SelectionListener() {
		typeRegsComposite.addSelectionChangedListener(new ISelectionChangedListener() {

/*			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			public void widgetSelected(SelectionEvent arg0) {
				if (currentFileExt != null) {
					if (typeRegsComposite.hasRegistration(currentFileExt))
						typeRegsComposite.setDelegateConfig(currentFileExt, printerConfigComposite.readDelegateConfig());
				}
				currentFileExt = typeRegsComposite.getSelectedFileExt();
				printerConfigComposite.setDelegateConfig(typeRegsComposite.getDelegateConfig(currentFileExt));
			}*/

			@Override
            public void selectionChanged(SelectionChangedEvent arg0) {
				if (currentFileExt != null) {
					if (typeRegsComposite.hasRegistration(currentFileExt))
						typeRegsComposite.setDelegateConfig(currentFileExt, printerConfigComposite.readDelegateConfig());
				}
				currentFileExt = typeRegsComposite.getSelectedFileExt();
				printerConfigComposite.setDelegateConfig(typeRegsComposite.getDelegateConfig(currentFileExt));

            }
		});
		printerConfigComposite = new EditDocumentPrinterConfigComposite(wrapper, SWT.NONE);

		typeRegsComposite.setDelegateConfigs(DelegatingDocumentPrinterCfMod.sharedInstance().getPrintConfigs());
		return wrapper;
	}

	@Override
	public boolean performOk() {
		if (typeRegsComposite.getSelectedFileExt() != null)
			typeRegsComposite.setDelegateConfig(typeRegsComposite.getSelectedFileExt(), printerConfigComposite.readDelegateConfig());
		Map<String, DocumentPrinterDelegateConfig> configs = typeRegsComposite.getDelegateConfigs();
		DelegatingDocumentPrinterCfMod cfMod = DelegatingDocumentPrinterCfMod.sharedInstance();
		cfMod.getPrintConfigs().clear();
		for (Entry<String, DocumentPrinterDelegateConfig> entry : configs.entrySet()) {
			cfMod.getPrintConfigs().put(entry.getKey(), entry.getValue());
			cfMod.addKnownExtension(entry.getKey());
		}
		cfMod.setPrintConfigs(cfMod.getPrintConfigs());
		try {
			Config.sharedInstance().save();
		} catch (ConfigException e) {
			ExceptionHandlerRegistry.asyncHandleException(e);
//			MessageDialog dlg = new MessageDialog(RCPUtil.getActiveWorkbenchShell(), "Saving DocumentPrinter config failed", null, e.getMessage(), 0, new String[]{"OK"}, 0);
//			dlg.open();
		}
		return super.performOk();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
