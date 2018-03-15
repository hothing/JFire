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

package org.nightlabs.base.ui.wizard;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.ui.config.DialogCf;
import org.nightlabs.base.ui.config.DialogCfMod;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.config.Config;


/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class DynamicPathWizardDialog extends WizardDialog
{
//	private DynamicPathWizard dynamicWizard;
	private IWizard dynamicWizard;

	private ListenerList buttonListeners;

	/**
	 * Create a new DynamicPathWizardDialog.
	 * @param wizard The wizard to show
	 * @deprecated It is dangerous to use this, since {@link RCPUtil#getActiveShell()} is used by this method and sometimes
	 * returns a window of a Job or another short-living shell that's not supposed to be used by this Dialog. Better
	 * use {@link #DynamicPathWizardDialog(Shell, IWizard)} and pass your shell from the UI element that triggered this
	 * dialog to be created.
	 */
	@Deprecated
	public DynamicPathWizardDialog(IWizard wizard)
	{
		this(RCPUtil.getActiveShell(), wizard);
	}

	/**
	 * Create a new DynamicPathWizardDialog.
	 * @param shell The parent shell
	 * @param wizard The wizard to show
	 */
	public DynamicPathWizardDialog(Shell shell, IWizard wizard)
	{
		super(shell, wizard);
		dynamicWizard = wizard;
		if (dynamicWizard instanceof DynamicPathWizard) {
			((DynamicPathWizard)dynamicWizard).setDynamicWizardDialog(this);
		}
	}

	/**
	 * Overrides and makes it public so the wizard can
	 * trigger the update of the dialog buttons.
	 *
	 * @see org.eclipse.jface.wizard.WizardDialog#update()
	 */
	@Override
	public void update() {
		super.update();
	}

	/**
	 * Overrides and makes it public.
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#getButton(int)
	 */
	@Override
	public Button getButton(int id) {
		return super.getButton(id);
	}

	@Override
	protected void backPressed() {
		buttonBar.setFocus(); // to trigger all GUI-element-to-backend-object-store-methods

		if (getCurrentPage() instanceof IDynamicPathWizardPage) {
			((IDynamicPathWizardPage)getCurrentPage()).onPrevious();
		}

		super.backPressed();
	}

	@Override
	protected void nextPressed() {
		buttonBar.setFocus(); // to trigger all GUI-element-to-backend-object-store-methods

		if (getCurrentPage() instanceof IDynamicPathWizardPage) {
			((IDynamicPathWizardPage)getCurrentPage()).onNext();
		}

//		if (getCurrentPage() == dynamicWizard.getWizardEntryPage()) {
//			if (dynamicWizard.getPopulator() != null)
//				dynamicWizard.getPopulator().addDynamicWizardPages(dynamicWizard);
//		}
		super.nextPressed();
	}

	/**
	 * @see org.eclipse.jface.wizard.WizardDialog#finishPressed()
	 */
	@Override
	protected void finishPressed()
	{
		buttonBar.setFocus(); // to trigger all GUI-element-to-backend-object-store-methods
		storeDialogSize();
		super.finishPressed();
	}

	/**
	 * @see org.eclipse.jface.wizard.WizardDialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed()
	{
		buttonBar.setFocus(); // to trigger all GUI-element-to-backend-object-store-methods
		super.cancelPressed();
	}


	@Override
	protected Control createContents(Composite parent) {
		Control result = super.createContents(parent);
		if (dynamicWizard instanceof DynamicPathWizard)
		{
			DynamicPathWizard dynamicPathWizard = (DynamicPathWizard) dynamicWizard;
			if (dynamicPathWizard.getFirstPage() instanceof IDynamicPathWizardPage)
				((IDynamicPathWizardPage)dynamicPathWizard.getFirstPage()).onShow();
			for (Object o : getDynamicPathWizardListeners().getListeners()) {
				IDynamicPathWizardListener l = (IDynamicPathWizardListener) o;
				l.pageChanged(dynamicPathWizard.getFirstPage());
			}
		}
		return result;
	}

	@Override
	protected void buttonPressed(int buttonId)
	{
		if (buttonId == IDialogConstants.FINISH_ID) {
			IWizardPage currPage = getCurrentPage();
			if (currPage instanceof IDynamicPathWizardPage)
				((IDynamicPathWizardPage)currPage).onHide();
		}
		super.buttonPressed(buttonId);
		if (getReturnCode() != Window.OK) {
			IWizardPage currPage = getCurrentPage();
			if (currPage instanceof IDynamicPathWizardPage)
				((IDynamicPathWizardPage)currPage).onShow();
		}

		for (Object o : getDynamicPathWizardListeners().getListeners()) {
			IDynamicPathWizardListener l = (IDynamicPathWizardListener) o;
			l.buttonPressed(buttonId);
		}
	}

	/**
	 * @see org.eclipse.jface.wizard.WizardDialog#showPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	@Override
	public void showPage(IWizardPage page)
	{
		IWizardPage currPage = getCurrentPage();

		if (currPage == page)
			return;

		if (currPage instanceof IDynamicPathWizardPage)
			((IDynamicPathWizardPage)currPage).onHide();

		super.showPage(page);

		if (page instanceof IDynamicPathWizardPage)
			((IDynamicPathWizardPage)page).onShow();

		for (Object o : getDynamicPathWizardListeners().getListeners()) {
			IDynamicPathWizardListener l = (IDynamicPathWizardListener) o;
			l.pageChanged(getCurrentPage());
		}
	}

	@Override
	public void create()
	{
		super.create();

		DialogCf cf = getDialogCfMod().getDialogCf(getWizardIdentifier(getWizard()));
		if (cf == null) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Point shellSize = getShell().getSize();
			int diffWidth = screenSize.width - shellSize.x;
			int diffHeight = screenSize.height - shellSize.y;
			getShell().setLocation(diffWidth/2, diffHeight/2);
		}
		else {
			getShell().setLocation(cf.getX(), cf.getY());
			getShell().setSize(cf.getWidth(), cf.getHeight());
		}
	}

	private static DialogCfMod getDialogCfMod()
	{
		return Config.sharedInstance().createConfigModule(DialogCfMod.class);
	}

	private static String getWizardIdentifier(IWizard wizard)
	{
		String wizardIdentifier = wizard instanceof IDynamicPathWizard ? ((IDynamicPathWizard)wizard).getIdentifier() : wizard.getClass().getName();
		if (wizardIdentifier == null)
			throw new IllegalStateException("identifier is null! Check the class " + wizard.getClass().getName()); //$NON-NLS-1$

		return wizardIdentifier;
	}

	@Override
	public boolean close()
	{
		storeDialogSize();
		return super.close();
	}

	protected void storeDialogSize() {
		getDialogCfMod().createDialogCf(
				getWizardIdentifier(getWizard()),
				getShell().getLocation().x,
				getShell().getLocation().y,
				getShell().getSize().x,
				getShell().getSize().y);
	}

	protected ListenerList getDynamicPathWizardListeners() {
		if (buttonListeners == null) {
			buttonListeners = new ListenerList();
		}
		return buttonListeners;
	}

	public void addListener(IDynamicPathWizardListener listener)
	{
		getDynamicPathWizardListeners().add(listener);
	}

	public void removeListener(IDynamicPathWizardListener listener)
	{
		getDynamicPathWizardListeners().remove(listener);
	}

	@Override
	public IProgressMonitor getProgressMonitor() {
		return super.getProgressMonitor();
	}
}
