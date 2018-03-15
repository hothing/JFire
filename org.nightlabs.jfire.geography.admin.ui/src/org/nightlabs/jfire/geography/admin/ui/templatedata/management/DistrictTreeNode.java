package org.nightlabs.jfire.geography.admin.ui.templatedata.management;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.geography.District;
import org.nightlabs.jfire.geography.admin.ui.GeographyAdminPlugin;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;

public class DistrictTreeNode extends GeographyTreeNode implements Comparable<DistrictTreeNode>{
	private District district;
//	private TreeViewer treeViewer;

	public DistrictTreeNode(TreeViewer treeViewer, GeographyTreeNode parent, District district)
	{
//		this.treeViewer = treeViewer;
		this.district = district;
		setGeographyObject(district);
		setParent(parent);
	}

	@Override
	public String getLabel()
	{
		return String.format(Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.DistrictTreeNode.coordinatesTemplate"), district.getLatitude(), district.getLongitude()); //$NON-NLS-1$
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

	public District getDistrict()
	{
		return district;
	}

	/**
	 * Compares to StructBlockNodes with respect to their label
	 */
	public int compareTo(DistrictTreeNode c)
	{
		return getLabel().compareTo(c.getLabel());
	}

	@Override
	public void loadChildren() {

	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return GeographyAdminPlugin.getImageDescriptor("icons/target.png"); //$NON-NLS-1$
	}

	@Override
	public I18nText getName() {
		return null;
	}

}
