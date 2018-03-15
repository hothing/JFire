package org.nightlabs.jfire.geography.admin.ui.templatedata.management;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.geography.Location;
import org.nightlabs.jfire.geography.admin.ui.GeographyAdminPlugin;
import org.nightlabs.util.NLLocale;

public class LocationTreeNode extends GeographyTreeNode implements Comparable<LocationTreeNode>{
	private Location location;
//	private TreeViewer treeViewer;

	public LocationTreeNode(TreeViewer treeViewer, GeographyTreeNode parent, Location location)
	{
//		this.treeViewer = treeViewer;
		this.location = location;
		setGeographyObject(location);
		setParent(parent);
	}

	@Override
	public String getLabel()
	{
		return location.getName().getText(NLLocale.getDefault().getLanguage());
	}

	@Override
	public GeographyTreeNode[] getChildren()
	{
		return null;
	}

	@Override
	public boolean hasChildren()
	{
		return false;
	}

	public Location getLocation()
	{
		return location;
	}

	/**
	 * Compares to StructBlockNodes with respect to their label
	 */
	public int compareTo(LocationTreeNode c)
	{
		return getLabel().compareTo(c.getLabel());
	}

	@Override
	public void loadChildren() {

	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return GeographyAdminPlugin.getImageDescriptor("icons/location.png"); //$NON-NLS-1$
	}

	@Override
	public I18nText getName() {
		return location.getName();
	}
}
