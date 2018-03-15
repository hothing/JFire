/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
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
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.ui.prop.edit.blockbased;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.wizard.WizardHopPage;
import org.nightlabs.jfire.base.ui.prop.ValidationUtil;
import org.nightlabs.jfire.base.ui.prop.edit.IValidationResultHandler;
import org.nightlabs.jfire.base.ui.prop.edit.ValidationResultHandler;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.validation.ValidationResult;

/**
 * A {@link WizardPage} that will create a block-based editor showing
 * all blocks that were not covered by a given {@link EditorStructBlockRegistry}.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class FullDataBlockCoverageWizardPage extends WizardHopPage {

	/**
	 * The property-set editor composite created for this page.
	 */
	protected FullDataBlockCoverageComposite fullDataBlockCoverageComposite;
	/**
	 * The currently edited property set
	 */
	protected PropertySet prop;
	/**
	 * The place where this page can see which blocks have been covered somewhere else.
	 */
	protected EditorStructBlockRegistry editorStructBlockRegistry;

	/**
	 * This variable is used to retain a possible validation error message until the first input by the user was made.
	 */
	protected boolean pristine = true;
	
	/**
	 * Set in the constructor, defines whether a validation error blocks the page (isComplete returns false) 
	 */
	protected boolean blockOnValidationErrors;

	/**
	 * Used to schedule only one updateButtons
	 * while the user is typing in new data like wild
	 */
	private Runnable scheduledUpdateRunnable = null;
	/**
	 * Performs an updateButtons()
	 */
	private Runnable updateButtonsRunnable = new Runnable() {
		public void run() {
			synchronized (FullDataBlockCoverageWizardPage.this) {
				getContainer().updateButtons();
				scheduledUpdateRunnable = null;
			}
		}
	}; 
	
	/**
	 * Constructs a new {@link FullDataBlockCoverageWizardPage}
	 * 
	 * @param pageName The name (id) of the new page.
	 * @param title The title of the new page
	 * @param propSet The {@link PropertySet} to edit.
	 * @param blockOnValiationErrors Whether this page should block on validation errors, i.e. return <code>false</code> in isPageComplete.
	 * @param editorStructBlockRegistry The registry where the blocks already covered can be obtained from. Might be <code>null</code>.
	 */
	public FullDataBlockCoverageWizardPage(
			String pageName, String title, PropertySet propSet,
			boolean blockOnValiationErrors,
			EditorStructBlockRegistry editorStructBlockRegistry) 
	{
		super(pageName, title);
		this.prop = propSet;
		this.editorStructBlockRegistry = editorStructBlockRegistry;
		this.blockOnValidationErrors = blockOnValiationErrors;
	}

	/**
	 * @see org.nightlabs.base.ui.wizard.DynamicPathWizardPage#createPageContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createPageContents(Composite parent) {
		IValidationResultHandler resultManager = new ValidationResultHandler() {
			@Override
			public void handleValidationResult(ValidationResult validationResult) {
				if (validationResult == null) {
					setMessage(null);
				}
				else {
					setMessage(validationResult.getMessage(), ValidationUtil.getIMessageProviderType(validationResult.getType()));
				}
				scheduleUpdateButtons();
			}
		};
		fullDataBlockCoverageComposite = new FullDataBlockCoverageComposite(parent, SWT.NONE, prop, editorStructBlockRegistry, resultManager) {
			@Override
			protected BlockBasedEditor createBlockBasedEditor() {
				return FullDataBlockCoverageWizardPage.this.createBlockBasedEditor();
			}
		};

		// Register a listener, that sets pristine to false and then immediately deregisteres itself again
		final DataBlockEditorChangedListener[] listener = new DataBlockEditorChangedListener[1];
		listener[0] = new DataBlockEditorChangedListener() {
			@Override
			public void dataBlockEditorChanged(DataBlockEditorChangedEvent dataBlockEditorChangedEvent) {
				pristine = false;
				fullDataBlockCoverageComposite.removeChangeListener(listener[0]);
			}
		};
		fullDataBlockCoverageComposite.addChangeListener(listener[0]);

		return fullDataBlockCoverageComposite;
	}

	/**
	 * This method is delegated to in order to create the {@link BlockBasedEditor}
	 * that will show all {@link StructBlock}s to fulfill full coverage.
	 * @return A new {@link BlockBasedEditor}.
	 */
	protected BlockBasedEditor createBlockBasedEditor() {
		return new BlockBasedEditor(true);
	}

	private void scheduleUpdateButtons() {
		synchronized (this) {
			if (scheduledUpdateRunnable == null) {
				scheduledUpdateRunnable = updateButtonsRunnable;
				getControl().getDisplay().asyncExec(updateButtonsRunnable);
			}
		}
	}
	
	@Override
	public boolean isPageComplete() {
		if (blockOnValidationErrors)
			return !hasValidationError();
		return true;
	}

	private boolean hasValidationError() {
		if (prop != null) {
			return prop.hasValidationError(prop.getStructure());
		}
		return false;
	}
	
	/**
	 * See {@link FullDataBlockCoverageComposite#updatePropertySet()}
	 */
	public void updatePropertySet() {
		if (fullDataBlockCoverageComposite != null)
			fullDataBlockCoverageComposite.updatePropertySet();
	}

	/**
	 * See {@link FullDataBlockCoverageComposite#refresh(PropertySet)}
	 */
	public void refresh(PropertySet propertySet) {
		this.prop = propertySet;
		if (fullDataBlockCoverageComposite != null)
			fullDataBlockCoverageComposite.refresh(propertySet);
	}

	@Override
	public void onShow() {
		super.onShow();
		refresh(prop);
		if (fullDataBlockCoverageComposite != null && !fullDataBlockCoverageComposite.isDisposed()) {
			fullDataBlockCoverageComposite.getDisplay().asyncExec(new Runnable() {
				public void run() {
					fullDataBlockCoverageComposite.validate();
				}
			});
		}
	}

	@Override
	public void onHide() {
		updatePropertySet();
		super.onHide();
	}

	public void markPristine() {
		pristine = true;
	}

	public PropertySet getPropertySet() {
		return prop;
	}
}
