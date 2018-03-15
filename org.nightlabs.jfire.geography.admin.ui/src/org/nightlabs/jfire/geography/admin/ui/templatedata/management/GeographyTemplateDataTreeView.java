package org.nightlabs.jfire.geography.admin.ui.templatedata.management;

import java.util.Iterator;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.DrillDownAdapter;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;
import org.nightlabs.jfire.base.ui.JFireBasePlugin;
import org.nightlabs.jfire.base.ui.config.ConfigLinkSelectionNotificationProxy;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.login.part.LSDViewPart;
import org.nightlabs.jfire.geography.CSV;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.GeographyManagerRemote;
import org.nightlabs.jfire.geography.admin.ui.GeographyAdminPlugin;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;
import org.nightlabs.jfire.geography.admin.ui.templatedata.editor.name.GeographyTemplateDataNameEditorView;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.city.CityNewWizard;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.country.CountryNewWizard;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.district.DistrictNewWizard;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.location.LocationNewWizard;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.region.RegionNewWizard;
import org.nightlabs.jfire.geography.id.CSVID;
import org.nightlabs.jfire.geography.ui.GeographyImplClient;
import org.nightlabs.jfire.geography.ui.GeographyTemplateDataChangedListener;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.progress.ProgressMonitor;

public class GeographyTemplateDataTreeView
extends LSDViewPart
{
	public final static String ID_VIEW = "org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView"; //$NON-NLS-1$

	private TreeViewer treeViewer;

//	private Action addZipCode;
	private Action addDistrictAction;
	private Action addLocationAction;
	private Action addCityAction;
	private Action addRegionAction;
	private Action addCountryAction;

//	private Action editAction;

	private DrillDownAdapter drillDownAdapter;

	private CountryTreeNode selectedCountryTreeNode = null;
	private RegionTreeNode selectedRegionTreeNode = null;
	private CityTreeNode selectedCityTreeNode = null;
	private DistrictTreeNode selectedDistrictTreeNode = null;
	private LocationTreeNode selectedLocationTreeNode = null;
	private EarthNode earthNode = null;

	private GeographyTreeNode currentSelectedElement = null;

	//the listener we register with the selection service
	private ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {

			if(event.getSelection() == null || event.getSelection().isEmpty())
				currentSelectedElement = null;
			else
				currentSelectedElement = (GeographyTreeNode)(((TreeSelection)event.getSelection()).getFirstElement());
			adaptActionsToSelection();
			adaptViewsToSelection();
		}
	};

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				GeographyTemplateDataTreeView.this.fillContextMenu(manager);
			}
		});

		Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeViewer);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(addCountryAction);
		manager.add(addRegionAction);
		manager.add(addCityAction);
		manager.add(addLocationAction);
		manager.add(addDistrictAction);

		manager.add(new Separator());
//		manager.add(editAction);
//		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void makeActions() {
		//Add New Location Menu Item
		addDistrictAction = new Action() {
			@Override
			public void run() {
				DistrictNewWizard districtWizard = new DistrictNewWizard(GeographyTemplateDataTreeView.this);
				districtWizard.init(getSite().getWorkbenchWindow().getWorkbench(),
						(IStructuredSelection)treeViewer.getSelection());
				//Instantiates the wizard container with the wizard and opens it
				WizardDialog dialog = new WizardDialog(getSite().getShell(), districtWizard);
				dialog.open();
			}
		};
		addDistrictAction.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.addNewDistrictActionText")); //$NON-NLS-1$
		addDistrictAction.setToolTipText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.addNewDistrictActionTooltip")); //$NON-NLS-1$
		addDistrictAction.setImageDescriptor(GeographyAdminPlugin.getImageDescriptor("icons/target.png")); //$NON-NLS-1$
		addDistrictAction.setEnabled(false);

		//Add New Location Menu Item
		addLocationAction = new Action() {
			@Override
			public void run() {
				LocationNewWizard locationWizard = new LocationNewWizard();
				locationWizard.init(getSite().getWorkbenchWindow().getWorkbench(),
						(IStructuredSelection)treeViewer.getSelection());
				//Instantiates the wizard container with the wizard and opens it
				WizardDialog dialog = new WizardDialog(getSite().getShell(), locationWizard);
				dialog.open();
			}
		};
		addLocationAction.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.addNewLocationActionText")); //$NON-NLS-1$
		addLocationAction.setToolTipText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.addNewLocationActionTooltip")); //$NON-NLS-1$
		addLocationAction.setImageDescriptor(GeographyAdminPlugin.getImageDescriptor("icons/location.png")); //$NON-NLS-1$
		addLocationAction.setEnabled(false);

		//Add New City Menu Item
		addCityAction = new Action() {
			@Override
			public void run() {
				CityNewWizard cityWizard = new CityNewWizard();
				cityWizard.init(getSite().getWorkbenchWindow().getWorkbench(),
						(IStructuredSelection)treeViewer.getSelection());
				//Instantiates the wizard container with the wizard and opens it
				WizardDialog dialog = new WizardDialog(getSite().getShell(), cityWizard);
				dialog.open();
			}
		};
		addCityAction.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.addNewCityActionText")); //$NON-NLS-1$
		addCityAction.setToolTipText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.addNewCityActionTooltip")); //$NON-NLS-1$
		addCityAction.setImageDescriptor(GeographyAdminPlugin.getImageDescriptor("icons/city.png")); //$NON-NLS-1$
		addCityAction.setEnabled(false);

		//Region
		addRegionAction = new Action() {
			@Override
			public void run() {
				RegionNewWizard regionWizard = new RegionNewWizard();
				regionWizard.init(getSite().getWorkbenchWindow().getWorkbench(),
						(IStructuredSelection)treeViewer.getSelection());
				//Instantiates the wizard container with the wizard and opens it
				WizardDialog dialog = new WizardDialog(getSite().getShell(), regionWizard);
				dialog.open();
			}
		};
		addRegionAction.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.addNewRegionActionText")); //$NON-NLS-1$
		addRegionAction.setToolTipText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.addNewRegionActionTooltip")); //$NON-NLS-1$
		addRegionAction.setImageDescriptor(GeographyAdminPlugin.getImageDescriptor("icons/globe2.png")); //$NON-NLS-1$
		addRegionAction.setEnabled(false);

		//Country
		addCountryAction = new Action() {
			@Override
			public void run() {
				CountryNewWizard countryWizard = new CountryNewWizard();
				countryWizard.init(getSite().getWorkbenchWindow().getWorkbench(),
						(IStructuredSelection)treeViewer.getSelection());
				//Instantiates the wizard container with the wizard and opens it
				WizardDialog dialog = new WizardDialog(getSite().getShell(), countryWizard);
				dialog.open();
			}
		};
		addCountryAction.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.addCountryAction.text")); //$NON-NLS-1$
		addCountryAction.setToolTipText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.addCountryAction.tooltip")); //$NON-NLS-1$
		addCountryAction.setImageDescriptor(GeographyAdminPlugin.getImageDescriptor("icons/globe_red.png")); //$NON-NLS-1$
		addCountryAction.setEnabled(true);

//		//Edit Menu Item
//		editAction = new Action() {
//			@Override
//			public void run() {
//				GeographyTemplateDataEditorDialog dialog =
//					new GeographyTemplateDataEditorDialog(
//							GeographyTemplateDataTreeView.this.getSite().getShell(), currentSelectedElement.getGeographyObject());
//				dialog.open();
//			}
//		};
//		editAction.setToolTipText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.editActionTooltip")); //$NON-NLS-1$
//		editAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
//				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
//		editAction.setEnabled(false);
	}

	public TreeViewer getTreeViewer(){
		return treeViewer;
	}

	public CityTreeNode getSelectedCityTreeNode() {
		return selectedCityTreeNode;
	}

	/*
	 * Getters and Setters of All Geography Data Type
	 */
	public void setSelectedCityTreeNode(CityTreeNode selectedCityTreeNode) {
		this.selectedCityTreeNode = selectedCityTreeNode;
	}

	public CountryTreeNode getSelectedCountryTreeNode() {
		return selectedCountryTreeNode;
	}

	public void setSelectedCountryTreeNode(CountryTreeNode selectedCountryTreeNode) {
		this.selectedCountryTreeNode = selectedCountryTreeNode;
	}

	public DistrictTreeNode getSelectedDistrictTreeNode() {
		return selectedDistrictTreeNode;
	}

	public void setSelectedDistrictTreeNode(DistrictTreeNode selectedDistrictTreeNode) {
		this.selectedDistrictTreeNode = selectedDistrictTreeNode;
	}

	public RegionTreeNode getSelectedRegionTreeNode() {
		return selectedRegionTreeNode;
	}

	public void setSelectedRegionTreeNode(RegionTreeNode selectedRegionTreeNode) {
		this.selectedRegionTreeNode = selectedRegionTreeNode;
	}

	public LocationTreeNode getSelectedLocationTreeNode() {
		return selectedLocationTreeNode;
	}

	public void setSelectedLocationTreeNode(LocationTreeNode selectedLocationTreeNode) {
		this.selectedLocationTreeNode = selectedLocationTreeNode;
	}

	@Override
	public boolean canDisplayPart() {
		return Login.isLoggedIn();
	}

	public void createPartContents(Composite parent)
	{
		GeographyImplClient gc = (GeographyImplClient)Geography.sharedInstance();
		gc.addGeographyTemplateDataChangedListener(geographyDataChangedListener);
		Geography.sharedInstance().getCountries();

		treeViewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		treeViewer.setContentProvider(new GeographyTemplateDataTreeContentProvider(this));
		treeViewer.setLabelProvider(new GeographyTemplateDataLabelProvider());
		earthNode = new EarthNode(treeViewer);
		treeViewer.setInput(earthNode);
		treeViewer.setSorter(new ViewerSorter());

		drillDownAdapter = new DrillDownAdapter(treeViewer);
		makeActions();
		hookContextMenu();

		getSite().setSelectionProvider(treeViewer);

		treeViewer.addSelectionChangedListener(selectionListener);
	}

	public ISelectionChangedListener createListChangeListener() {
		return new ConfigLinkSelectionNotificationProxy(this, JFireBasePlugin.ZONE_ADMIN, true, false);
	}

	private void adaptActionsToSelection()
	{
		if(currentSelectedElement == null) {
			addDistrictAction.setEnabled(false);
			addLocationAction.setEnabled(false);
			addCityAction.setEnabled(false);
			addRegionAction.setEnabled(false);
//			editAction.setEnabled(false);
		}
		else if(currentSelectedElement instanceof CountryTreeNode) {
			CountryTreeNode countryNode = (CountryTreeNode)currentSelectedElement;
			setSelectedCountryTreeNode(countryNode);
			setSelectedRegionTreeNode(null);
			setSelectedCityTreeNode(null);
			setSelectedLocationTreeNode(null);
			setSelectedDistrictTreeNode(null);
//			editAction.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.editCountryActionText")); //$NON-NLS-1$
//			editAction.setEnabled(true);
			addCountryAction.setEnabled(true);
			addRegionAction.setEnabled(true);
			addDistrictAction.setEnabled(false);
			addLocationAction.setEnabled(false);
			addCityAction.setEnabled(false);
		}//if
		else if(currentSelectedElement instanceof RegionTreeNode) {
			RegionTreeNode regionNode = (RegionTreeNode)currentSelectedElement;
			setSelectedCountryTreeNode((CountryTreeNode)regionNode.getParent());
			setSelectedRegionTreeNode(regionNode);
			setSelectedCityTreeNode(null);
			setSelectedLocationTreeNode(null);
			setSelectedDistrictTreeNode(null);
//			editAction.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.editRegionActionText")); //$NON-NLS-1$
//			editAction.setEnabled(true);
			addCountryAction.setEnabled(true);
			addRegionAction.setEnabled(true);
			addCityAction.setEnabled(true);
			addDistrictAction.setEnabled(false);
			addLocationAction.setEnabled(false);
		}//else if
		else if(currentSelectedElement instanceof CityTreeNode) {
			CityTreeNode cityNode = (CityTreeNode)currentSelectedElement;
			RegionTreeNode regionTreeNode = (RegionTreeNode)cityNode.getParent();
			CountryTreeNode countryTreeNode = (CountryTreeNode)regionTreeNode.getParent();
			setSelectedCountryTreeNode(countryTreeNode);
			setSelectedRegionTreeNode(regionTreeNode);
			setSelectedCityTreeNode(cityNode);
			setSelectedLocationTreeNode(null);
			setSelectedDistrictTreeNode(null);
//			editAction.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.editCityActionText")); //$NON-NLS-1$
//			editAction.setEnabled(true);
			addCountryAction.setEnabled(true);
			addRegionAction.setEnabled(true);
			addCityAction.setEnabled(true);
			addDistrictAction.setEnabled(true);
			addLocationAction.setEnabled(true);
		}//else if
		else if(currentSelectedElement instanceof DistrictTreeNode) {
			DistrictTreeNode districtNode = (DistrictTreeNode)currentSelectedElement;
			CityTreeNode cityNode = (CityTreeNode)districtNode.getParent();
			RegionTreeNode regionTreeNode = (RegionTreeNode)cityNode.getParent();
			CountryTreeNode countryTreeNode = (CountryTreeNode)regionTreeNode.getParent();
			setSelectedCountryTreeNode(countryTreeNode);
			setSelectedRegionTreeNode(regionTreeNode);
			setSelectedCityTreeNode(cityNode);
			setSelectedDistrictTreeNode(districtNode);
//			editAction.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.editDistrictActionText")); //$NON-NLS-1$
//			editAction.setEnabled(true);
			addCountryAction.setEnabled(true);
			addRegionAction.setEnabled(true);
			addCityAction.setEnabled(true);
			addDistrictAction.setEnabled(true);
			addLocationAction.setEnabled(true);
		}//else if
		else if(currentSelectedElement instanceof LocationTreeNode) {
			LocationTreeNode locationNode = (LocationTreeNode)currentSelectedElement;
			GeographyTreeNode parentNode = locationNode.getParent();
			CityTreeNode cityNode = null;
			if (parentNode instanceof CityTreeNode) {
				cityNode = (CityTreeNode) parentNode;
				setSelectedCityTreeNode(cityNode);
			}//if
			else if (parentNode instanceof DistrictTreeNode) {
				DistrictTreeNode districtNode = (DistrictTreeNode) parentNode;
				cityNode = (CityTreeNode)districtNode.getParent();
				setSelectedDistrictTreeNode(districtNode);
			}//else if
			RegionTreeNode regionTreeNode = (RegionTreeNode)cityNode.getParent();
			CountryTreeNode countryTreeNode = (CountryTreeNode)regionTreeNode.getParent();
			setSelectedCountryTreeNode(countryTreeNode);
			setSelectedRegionTreeNode(regionTreeNode);
			setSelectedLocationTreeNode(locationNode);
//			editAction.setText(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.editLocationActionText")); //$NON-NLS-1$
//			editAction.setEnabled(true);
			addCountryAction.setEnabled(true);
			addRegionAction.setEnabled(true);
			addCityAction.setEnabled(true);
			addDistrictAction.setEnabled(true);
			addLocationAction.setEnabled(true);
		}//else if
	}

	private void adaptViewsToSelection()
	{
		if(currentSelectedElement != null) {
			GeographyTemplateDataInformationView infoView = (GeographyTemplateDataInformationView)RCPUtil.findView(GeographyTemplateDataInformationView.ID_VIEW);
			if(infoView != null)
				infoView.updateGeographyInfomation(currentSelectedElement.getGeographyObject());

			GeographyTemplateDataNameEditorView editorView = (GeographyTemplateDataNameEditorView)RCPUtil.findView(GeographyTemplateDataNameEditorView.ID_VIEW);
			if(editorView != null)
				editorView.updateData(currentSelectedElement);
		}
	}

	private static final String[] FETCH_GROUPS_CSV = { FetchPlan.DEFAULT, CSV.FETCH_GROUP_DATA };

	private String reloadedGeographyType = CSV.CSV_TYPE_COUNTRY;
//	private GeographyTemplateDataChangedListener geographyDataChangedListener = new GeographyTemplateDataChangedListener() {
//		public void geographyTemplateDataChanged(final JDOLifecycleEvent event) {
//			Job job = new Job(Messages.getString("org.nightlabs.jfire.trade.ui.account.editor.AccountEditor.loadingAccountJob.name")) //$NON-NLS-1$
//			{
//				@Override
//				protected IStatus run(ProgressMonitor monitor)
//				throws Exception
//				{
//					Set<DirtyObjectID> objIDSet = event.getDirtyObjectIDs();
//					GeographyManager gm = GeographyManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
//					if(objIDSet.size() != 0 && selectedCountryTreeNode != null){
//						for (Iterator<DirtyObjectID> it = objIDSet.iterator(); it.hasNext();) {
//							DirtyObjectID dID = it.next();
//							CSV csv = (CSV)gm.getGeographyObject((CSVID)dID.getObjectID(), FETCH_GROUPS_CSV, 1);
//							if (csv.getCsvType().equals(CSV.CSV_TYPE_COUNTRY)) {
//								reloadedGeographyType = CSV.CSV_TYPE_COUNTRY;
//							}//if
//							else if(csv.getCsvType().equals(CSV.CSV_TYPE_CITY)){
//								reloadedGeographyType = CSV.CSV_TYPE_REGION;
//							}//else
//							Geography.sharedInstance().clearCache();
//						}
//					}//if
//					Display.getDefault().asyncExec(new Runnable() {
//						public void run() {
//							if (!treeViewer.getTree().isDisposed()){
//								if(reloadedGeographyType.equals(CSV.CSV_TYPE_REGION)) {
//									if (selectedRegionTreeNode != null) {
//										selectedRegionTreeNode.setChildrenLoaded(false);
//										treeViewer.refresh(selectedRegionTreeNode, true);
//									} else {
//										treeViewer.refresh(true);
//									}
//								}//if
//								else{
//									if (selectedCountryTreeNode != null) {
//										selectedCountryTreeNode.setChildrenLoaded(false);
//										treeViewer.refresh(selectedCountryTreeNode, true);
//									} else {
//										treeViewer.refresh(true);
//									}
//								}
//							}//if
//						}
//					});//ui thread ends
//					return Status.OK_STATUS;
//				}
//			};//job thread ends
//			job.setPriority(org.eclipse.core.runtime.jobs.Job.SHORT);
//			job.schedule();
//		}
//	};
	private GeographyTemplateDataChangedListener geographyDataChangedListener = new GeographyTemplateDataChangedListener() {
		public void geographyTemplateDataChanged(final JDOLifecycleEvent event) {
			Job job = new Job(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView.job.loadingChangedData.name")) //$NON-NLS-1$
			{
				@Override
				protected IStatus run(ProgressMonitor monitor)
				throws Exception
				{
					Set<DirtyObjectID> objIDSet = event.getDirtyObjectIDs();
					GeographyManagerRemote gm = JFireEjb3Factory.getRemoteBean(GeographyManagerRemote.class, Login.getLogin().getInitialContextProperties());
					if (objIDSet.size() != 0) {
						for (Iterator<DirtyObjectID> it = objIDSet.iterator(); it.hasNext();) {
							DirtyObjectID dID = it.next();
							CSV csv = (CSV)gm.getGeographyObject((CSVID)dID.getObjectID(), FETCH_GROUPS_CSV, 1);
							if (csv.getCsvType().equals(CSV.CSV_TYPE_COUNTRY)) {
								reloadedGeographyType = CSV.CSV_TYPE_COUNTRY;
							}//if
							else if(csv.getCsvType().equals(CSV.CSV_TYPE_REGION)){
								reloadedGeographyType = CSV.CSV_TYPE_REGION;
							}//else
							else if(csv.getCsvType().equals(CSV.CSV_TYPE_CITY)){
								reloadedGeographyType = CSV.CSV_TYPE_CITY;
							}//else
							else if(csv.getCsvType().equals(CSV.CSV_TYPE_DISTRICT)){
								reloadedGeographyType = CSV.CSV_TYPE_DISTRICT;
							}//else
							else if(csv.getCsvType().equals(CSV.CSV_TYPE_LOCATION)){
								reloadedGeographyType = CSV.CSV_TYPE_LOCATION;
							}//else
							Geography.sharedInstance().clearCache();
						}
					}//if
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							if (!treeViewer.getTree().isDisposed()) {
								if (reloadedGeographyType.equals(CSV.CSV_TYPE_COUNTRY))
								{
									if (earthNode != null) {
										earthNode.setChildrenLoaded(false);
									}
									if (selectedCountryTreeNode != null) {
										selectedCountryTreeNode.setChildrenLoaded(false);
									}
								}
								else if (reloadedGeographyType.equals(CSV.CSV_TYPE_REGION))
								{
									if (selectedCountryTreeNode != null) {
										selectedCountryTreeNode.setChildrenLoaded(false);
									}
									if (selectedRegionTreeNode != null) {
										selectedRegionTreeNode.setChildrenLoaded(false);
									}
								}
								else if (reloadedGeographyType.equals(CSV.CSV_TYPE_CITY))
								{
									if (selectedRegionTreeNode != null) {
										selectedRegionTreeNode.setChildrenLoaded(false);
									}
									if (selectedCityTreeNode != null) {
										selectedCityTreeNode.setChildrenLoaded(false);
									}
								}
								else if (reloadedGeographyType.equals(CSV.CSV_TYPE_DISTRICT)) {
									if (selectedCityTreeNode != null) {
										selectedCityTreeNode.setChildrenLoaded(false);
									}
									if (selectedDistrictTreeNode != null) {
										selectedDistrictTreeNode.setChildrenLoaded(false);
									}
								}
								else if (reloadedGeographyType.equals(CSV.CSV_TYPE_LOCATION)) {
									if (selectedCityTreeNode != null) {
										selectedCityTreeNode.setChildrenLoaded(false);
									}
									if (selectedLocationTreeNode != null) {
										selectedLocationTreeNode.setChildrenLoaded(false);
									}
								}
//								if (currentSelectedElement != null)
//									treeViewer.setSelection(new StructuredSelection(currentSelectedElement), true);
								refreshViewer();
							}//if
						}
					});//ui thread ends
					return Status.OK_STATUS;
				}
			};//job thread ends
			job.setPriority(org.eclipse.core.runtime.jobs.Job.SHORT);
			job.schedule();
		}
	};

	private void refreshViewer()
	{
		treeViewer.setInput(earthNode);
		treeViewer.refresh(true);
	}
}