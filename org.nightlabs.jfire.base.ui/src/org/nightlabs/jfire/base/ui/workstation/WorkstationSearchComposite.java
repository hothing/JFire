package org.nightlabs.jfire.base.ui.workstation;

import java.util.Collection;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.dao.WorkstationDAO;
import org.nightlabs.jfire.workstation.search.WorkstationQuery;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class WorkstationSearchComposite extends XComposite {

	public static final int FLAG_MULTI_SELECTION = 1;
	public static final int FLAG_SEARCH_BUTTON = 32;


	private Text workstationIDText;
	private Text descriptionText;
	private WorkstationTable workstationTable;

	private int flags = 0;

	/**
	 * @param parent
	 * @param style
	 */
	public WorkstationSearchComposite(Composite parent, int style) {
		this(parent, style, 0);
	}

	public WorkstationSearchComposite(Composite parent, int style, int flags) {
		super(parent, style);
		this.flags = flags;
		createComposite();
	}

	protected void createComposite() {
		XComposite searchComp = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		searchComp.getGridLayout().numColumns = isShowSearchButton() ? 3 : 2;
		searchComp.getGridLayout().makeColumnsEqualWidth = false;
		searchComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		workstationIDText = createTextSearchEntry(searchComp, Messages.getString("org.nightlabs.jfire.base.ui.workstation.WorkstationSearchComposite.entry.workstationID")); //$NON-NLS-1$
		descriptionText = createTextSearchEntry(searchComp, Messages.getString("org.nightlabs.jfire.base.ui.workstation.WorkstationSearchComposite.entry.description")); //$NON-NLS-1$
		if (isShowSearchButton()) {
			Button searchButton = new Button(searchComp, SWT.PUSH);
			searchButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
			searchButton.setText(Messages.getString("org.nightlabs.jfire.base.ui.workstation.WorkstationSearchComposite.button.search.text")); //$NON-NLS-1$
			searchButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					searchPressed();
				}
			});
		}

		workstationTable = new WorkstationTable(this, SWT.NONE, true, isMultiSelelect() ? AbstractTableComposite.DEFAULT_STYLE_MULTI_BORDER : AbstractTableComposite.DEFAULT_STYLE_SINGLE_BORDER);
		workstationTable.setLinesVisible(true);
		workstationTable.setHeaderVisible(true);
	}

	protected boolean isMultiSelelect() {
		return (flags & FLAG_MULTI_SELECTION) > 0;
	}
	protected boolean isShowSearchButton() {
		return (flags & FLAG_SEARCH_BUTTON) > 0;
	}

	protected Text createTextSearchEntry(Composite parent, String labelString)
	{
		Composite wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		Label label = new Label(wrapper, SWT.NONE);
		label.setText(labelString);
		Text text = new Text(wrapper, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return text;
	}

	protected WorkstationQuery getWorkstationQuery()
	{
		WorkstationQuery workstationQuery = new WorkstationQuery();

		if (!workstationIDText.getText().trim().equals("")) //$NON-NLS-1$
			workstationQuery.setWorkstationID(workstationIDText.getText());

		if (!descriptionText.getText().trim().equals("")) //$NON-NLS-1$
			workstationQuery.setDescription(descriptionText.getText());

		return workstationQuery;
	}


	public void searchPressed()
	{
		workstationTable.setInput(Messages.getString("org.nightlabs.jfire.base.ui.workstation.WorkstationSearchComposite.label.searching")); //$NON-NLS-1$
		Job job = new Job(Messages.getString("org.nightlabs.jfire.base.ui.workstation.WorkstationSearchComposite.job.searchingWorkstation")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor){
				try {
					final QueryCollection<WorkstationQuery> queries = new QueryCollection<WorkstationQuery>(Workstation.class);
					Display.getDefault().syncExec(new Runnable(){
						public void run() {
							queries.add(getWorkstationQuery());
						}
					});

					final Collection<Workstation> workstations = WorkstationDAO.sharedInstance().getWorkstations(queries,
							new String[] {
								FetchPlan.DEFAULT
							},
							NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor
					);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							workstationTable.setInput(workstations);
						}
					});
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				return Status.OK_STATUS;
			}
		};
//		job.setPriority(Job.SHORT);
		job.schedule();
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		workstationTable.addSelectionChangedListener(listener);
	}

	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		workstationTable.removeSelectionChangedListener(listener);
	}

	public Workstation getFirstSelectedElement() {
		return workstationTable.getFirstSelectedElement();
	}

	public Collection<Workstation> getSelectedElements() {
		return workstationTable.getSelectedElements();
	}
}
