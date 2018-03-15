/**
 * 
 */
package org.nightlabs.jfire.base.admin.ui.timer;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.editor.RestorableSectionPart;
import org.nightlabs.base.ui.timepattern.TimePatternSetComposite;
import org.nightlabs.base.ui.timepattern.builder.TimePatternSetBuilderWizard;
import org.nightlabs.base.ui.toolkit.IToolkit;
import org.nightlabs.jfire.base.admin.ui.resource.Messages;
import org.nightlabs.jfire.timer.Task;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class TaskDetailSection
extends RestorableSectionPart
{
	private static final Logger logger = Logger.getLogger(TaskDetailSection.class);

// TODO @fleque: This ModifyListener was not used at all - I commented it out. Marco :-)
//	private ModifyListener modifyDirtyListener = new ModifyListener() {
//		public void modifyText(ModifyEvent e) {
//			markDirty();
//		}
//	};

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public TaskDetailSection(FormPage page, Composite parent, String title, String formText) {
		super(parent, page.getEditor().getToolkit(), ExpandableComposite.TITLE_BAR);
		this.title = title;
		createClient(getSection(), (IToolkit) page.getEditor().getToolkit());
	}
	
	private String title;
	private String formText;
	private Button enabled;
	private TimePatternSetComposite timePatternSetComposite;
	private Button buildTimePattern;

	/**
	 * Create the content for this section.
	 * @param section The section to fill
	 * @param toolkit The toolkit to use
	 */
	protected void createClient(Section section, IToolkit toolkit)
	{
		section.setText(title);
		section.setExpanded(true);
		GridLayout gl = new GridLayout();
		section.setLayout(gl);
//		section.setLayoutData(new ColumnLayoutData());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		section.setLayoutData(gd);
//		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		createDescriptionControl(section, toolkit);
		
//		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 2);
		
		XComposite container = new XComposite(section, SWT.NONE, XComposite.LayoutMode.TIGHT_WRAPPER);
//		container.getGridLayout().makeColumnsEqualWidth = true;
		section.setClient(container);
		container.setToolkit(toolkit);
		container.adaptToToolkit();

// TODO @fleque: This SelectionListener was not used at all. I commented it out. Marco :-)
//		SelectionListener dirtyMarker = new SelectionListener() {
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//			public void widgetSelected(SelectionEvent e) {
//				markDirty();
//			}
//		};
		XComposite above = new XComposite(container, SWT.NONE, XComposite.LayoutMode.TIGHT_WRAPPER);
		above.getGridData().grabExcessHorizontalSpace = true;
		above.getGridData().grabExcessVerticalSpace = false;
		above.getGridLayout().numColumns = 2;
		enabled = new Button(above, SWT.CHECK);
		enabled.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskDetailSection.enabled.text")); //$NON-NLS-1$
		enabled.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				TaskDetailSection.this.markDirty();
			}
		});
		buildTimePattern = new Button(above, SWT.PUSH);
		buildTimePattern.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL));
		buildTimePattern.setText(Messages.getString("org.nightlabs.jfire.base.admin.ui.timer.TaskDetailSection.buildTimePattern.text"));		 //$NON-NLS-1$
		buildTimePattern.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if (TimePatternSetBuilderWizard.open(task.getTimePatternSet())) {
					timePatternSetComposite.refresh(true);
					TaskDetailSection.this.markDirty();
				}
			}
		});
		timePatternSetComposite = new TimePatternSetComposite(container, SWT.NONE);
	}

	
	private void createDescriptionControl(Section section, IToolkit toolkit)
	{
		if (formText == null || "".equals(formText)) //$NON-NLS-1$
			return;
		FormText text = toolkit.createFormText(section, true);
		try {
			text.setText(formText, true, false);
		} catch (Exception e) {
			logger.warn("Wrong section text: "+formText, e); //$NON-NLS-1$
		}
		section.setDescriptionControl(text);
	}
	
	@Override
	public void commit(boolean onSave) {
		if (onSave)
			super.commit(onSave);
		if (task != null) {
			System.err.println("********************* commit Task names *******************"); //$NON-NLS-1$
//			task.getName().copyFrom(nameEditor.getI18nText());
//			task.getDescription().copyFrom(descriptionEditor.getI18nText());
			task.setEnabled(enabled.getSelection());
		}
	}
	
	private Task task;
	
	public void setTask(final Task task) {
		if (timePatternSetComposite == null || timePatternSetComposite.isDisposed())
			return;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
//				detailComposite.setTask(task);
				TaskDetailSection.this.task = task;
				enabled.setSelection(task.isEnabled());
				timePatternSetComposite.setTimePatternSet(task.getTimePatternSet());
			}
		});
	}
	
}
