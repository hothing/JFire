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

package org.nightlabs.jfire.base.ui.person.search;

import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.jfire.base.ui.JFireBasePlugin;
import org.nightlabs.jfire.base.ui.person.edit.fieldbased.PersonFieldBasedEditor;
import org.nightlabs.jfire.base.ui.prop.edit.fieldbased.FieldBasedEditorCfModLayoutConfig;
import org.nightlabs.jfire.base.ui.prop.edit.fieldbased.FieldBasedEditorCfModLayoutConfigWizardPage;
import org.nightlabs.jfire.base.ui.resource.Messages;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class PersonEditorWizardPersonalPage
extends FieldBasedEditorCfModLayoutConfigWizardPage {
//extends PersonPersonalDataWizardPage {

//	private boolean pristine = true;
//	
//	public static final StructBlockID[] BLOCKIDS =
//		new StructBlockID[] {PersonStruct.PERSONALDATA};

	public PersonEditorWizardPersonalPage() {
		super(
				PersonEditorWizardPersonalPage.class.getName(),
				Messages.getString("org.nightlabs.jfire.base.ui.person.search.PersonEditorWizardPersonalPage.title"), //$NON-NLS-1$
				false
				);
		setImageDescriptor(
				SharedImages.getSharedImageDescriptor(
						JFireBasePlugin.getDefault(),
						PersonEditorWizardPersonalPage.class, null, SharedImages.ImageDimension._75x70));
	}
	
	@Override
	protected FieldBasedEditorCfModLayoutConfig createFieldBasedEditor() {
		return new PersonFieldBasedEditor(true);
	}
//	@Override
//	public Control createPageContents(Composite parent) {
//		IValidationResultHandler resultManager = new ValidationResultHandler() {
//			@Override
//			public void handleValidationResult(ValidationResult validationResult) {
//				if (pristine)
//					return;
//
//				if (validationResult == null)
//					setMessage(null);
//				else
//					setMessage(validationResult.getMessage(), ValidationUtil.getIMessageProviderType(validationResult.getType()));
//			}
//		};
//		// Register a listener, that sets pristine to false and then immediately deregisteres itself again
//		final DataFieldEditorChangedListener[] listener = new DataFieldEditorChangedListener[1]; 
//		listener[0] = new DataFieldEditorChangedListener() {
//			@Override
//			public void dataFieldEditorChanged(DataFieldEditorChangedEvent dataFieldEditorChangedEvent) {
//				pristine = false;
//				getEditor().removeDataFieldEditorChangedListener(listener[0]);
//			}
//		};
//		getEditor().addDataFieldEditorChangedListener(listener[0]);
//		getEditor().setValidationResultHandler(resultManager);
//		return super.createPageContents(parent);
//	}
//	
//	@Override
//	public boolean isPageComplete() {
//		return !pristine && super.isPageComplete();
//	}
//	
//	
//	/**
//	 * @param title
//	 * @param pageName
//	 * @param titleImage
//	 */
//	public PersonEditorWizardPersonalPage(Person person) {
//		super(
//				PersonEditorWizardPersonalPage.class.getName(),
//				Messages.getString("org.nightlabs.jfire.base.ui.person.search.PersonEditorWizardPersonalPage.title"), //$NON-NLS-1$
//				person);
//		setImageDescriptor(
//				SharedImages.getSharedImageDescriptor(
//						JFireBasePlugin.getDefault(),
//						PersonEditorWizardPersonalPage.class, null, SharedImages.ImageDimension._75x70));
//
//		// TODO: Add editor struct block registry as throw-away object
//
////		EditorStructBlockRegistry.sharedInstance().addEditorStructBlocks(
////			LegalEntityEditorWizard.WIZARD_EDITOR_DOMAIN,
////			this.getClass().getName(),
////			BLOCKIDS
////		);
//	}

}
