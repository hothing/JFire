package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.LanguageChooser;
import org.nightlabs.base.ui.language.LanguageChooserCombo;
import org.nightlabs.base.ui.language.I18nTextEditor.EditMode;
import org.nightlabs.base.ui.language.LanguageChooserCombo.Mode;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.IStruct;

public class StructEditorComposite extends XComposite {
	private StructTree structTree;
	private StructEditor structEditor;

	private I18nTextEditor structNameEditor;
	private Composite partEditorComposite;
	private LanguageChooserCombo languageChooser;
	
	public StructEditorComposite(
			Composite parent, int style,
			final StructEditor structEditor, StructTree structTree
		) {
		super(parent, style);
		this.setLayout(new GridLayout(2, false));
		this.structEditor = structEditor;

		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.CENTER;

		XComposite topLine = new XComposite(this, SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		topLine.getGridLayout().numColumns = 2;
		topLine.getGridData().horizontalSpan = 2;
		XComposite nameWrapper = new XComposite(topLine, SWT.NONE, LayoutMode.TOTAL_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);

		gd = new GridData();
		gd.horizontalAlignment = SWT.RIGHT;

		languageChooser = new LanguageChooserCombo(topLine, Mode.iconAndText);
		languageChooser.setLayoutData(gd);

		structNameEditor = new I18nTextEditor(nameWrapper, languageChooser);
		structNameEditor.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				structEditor.setChanged(true);
			}
		});


		this.structTree = structTree;
		structTree.createComposite(this, style, languageChooser);
		gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = 200;
		structTree.getComposite().setLayoutData(gd);

		partEditorComposite = new XComposite(this, SWT.NONE);
	}

	public void setLoadingText() {
		structTree.setInput(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructEditorComposite.structTree.input_loadingStructure")); //$NON-NLS-1$
	}

	public void setStruct(IStruct struct) {
		structTree.setInput(struct);
		structTree.triggerSelectionChangeEvent();
		structNameEditor.setI18nText(struct.getName(), EditMode.DIRECT);
	}


	public void setPartEditor(StructPartEditor<?> structPartEditor) {
		if (partEditorComposite != null) {
			partEditorComposite.dispose();
		}

		this.partEditorComposite = structPartEditor.createComposite(this, this.getStyle(), structEditor, languageChooser);
		((GridData)this.partEditorComposite.getLayoutData()).verticalAlignment = SWT.TOP;
		this.partEditorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.layout(true, true);
	}

	public LanguageChooser getLanguageChooser() {
		return languageChooser;
	}
}
