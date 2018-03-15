package org.nightlabs.jfire.geography.admin.ui.templatedata.management;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.nightlabs.jfire.geography.District;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;

class GeographyTemplateDataTreeContentProvider
implements ITreeContentProvider
{
	private TreeViewer treeViewer;

//	private static final String LOADING_CHILDREN = Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeContentProvider.loadingChildrenListEntry"); //$NON-NLS-1$

	public GeographyTemplateDataTreeContentProvider(GeographyTemplateDataTreeView viewPart) {
		this.treeViewer = viewPart.getTreeViewer();
	}

	public Object[] getChildren(final Object parentElement) 
	{
		if (parentElement instanceof GeographyTreeNode) {
			final GeographyTreeNode node = (GeographyTreeNode)parentElement;
			if (!node.isChildrenLoaded()) {
				treeViewer.getTree().setEnabled(false);
				node.loadChildren();
				Object[] elements = node.getChildren();
				treeViewer.getTree().setEnabled(true);
				return elements;
//				new Job(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeContentProvider.loadingGeographyDataJob")) { //$NON-NLS-1$
//					@Override
//					protected IStatus run(IProgressMonitor monitor) {
//						// may be slow
//						node.loadChildren();
//						Display.getDefault().asyncExec(new Runnable() {
//							public void run() {
//								if(!treeViewer.getTree().isDisposed()){
//									treeViewer.refresh(node);
//								}
//								treeViewer.getTree().setEnabled(true);
//							}
//						});
//						return Status.OK_STATUS;
//					}
//				}.schedule();
//
//				return new String[] { LOADING_CHILDREN };
			} else {
				return node.getChildren();
			}
		}
		return new Object[] {};
	}

	public Object getParent(Object childElement) {
		if(childElement instanceof RegionTreeNode){
			RegionTreeNode region = (RegionTreeNode)childElement;
			return region.getRegion().getCountry();
		}//if
		else if(childElement instanceof CityTreeNode){
			CityTreeNode city = (CityTreeNode)childElement;
			return city.getCity().getRegion();
		}//else if
		else if(childElement instanceof LocationTreeNode || childElement instanceof District){
			LocationTreeNode district = (LocationTreeNode)childElement;
			return district.getLocation().getCity();
		}//else if

		return null;
	}

	public boolean hasChildren(Object element) {
		boolean result = false;
		if(element instanceof GeographyTreeNode){
			GeographyTreeNode node = (GeographyTreeNode)element;
			result = node.hasChildren();
		}//if
		return result;
	}


//	private Collection<Country> countries = null;
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof EarthNode) 
		{
			EarthNode earthNode = (EarthNode) inputElement;
			treeViewer.getTree().setEnabled(false);
			earthNode.loadChildren();
			final GeographyTreeNode[] nodes = earthNode.getChildren();
			treeViewer.getTree().setEnabled(true);
			return nodes;
//			new Job(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeContentProvider.loadingGeographyDataJob")) { //$NON-NLS-1$
//				@Override
//				protected IStatus run(IProgressMonitor monitor) {
//					//very slow :)
//					EarthNode earthNode = (EarthNode)inputElement;
//					earthNode.loadChildren();
//					final GeographyTreeNode[] nodes = earthNode.getChildren();
//					Display.getDefault().syncExec(new Runnable() {
//						public void run() {
//							if (treeViewer != null && !treeViewer.getTree().isDisposed() && nodes != null)
//								treeViewer.setInput(nodes);
//						}
//					});
//					return Status.OK_STATUS;
//				}
//			}.schedule();
		}//if
		else if (inputElement instanceof Object[]) {
			return (Object[])inputElement;
		}

		return new String[] {Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeContentProvider.loadingCountriesListEntry")}; //$NON-NLS-1$
	}

	public void dispose() {

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}