package org.nightlabs.jfire.geography.admin.ui.templatedata.management;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.GeographyAdminPlugin;
import org.nightlabs.jfire.geography.id.RegionID;

public class RegionTreeNode extends GeographyTreeNode implements Comparable<RegionTreeNode>{
	private Region region;
	private List<CityTreeNode> cities = new LinkedList<CityTreeNode>();
	private TreeViewer treeViewer = null;
	
	public RegionTreeNode(TreeViewer treeViewer, GeographyTreeNode parent, Region region)
	{
		this.treeViewer = treeViewer;
		this.region = region;
		setGeographyObject(region);
		setParent(parent);
	}
	
	public void addCity(CityTreeNode city)
	{
		cities.add(city);
	}
	
	public void removeCity(CityTreeNode city)
	{
		cities.remove(city);
	}
	
	@Override
	public String getLabel()
	{
		return region.getName().getText();
	}

	@Override
	public CityTreeNode[] getChildren()
	{
		return cities.toArray(new CityTreeNode[0]);
	}

	@Override
	public boolean hasChildren()
	{
		return true;
	}
	
	public Region getRegion()
	{
		return region;
	}

	/**
	 * Compares to StructBlockNodes with respect to their label
	 */
	public int compareTo(RegionTreeNode c)
	{
		return getLabel().compareTo(c.getLabel());
	}
	
	@Override
	public void loadChildren() {
		if (isChildrenLoaded())
			return;
		
		cities.clear();
		
		region = (Region)getGeographyObject();
		region = Geography.sharedInstance().getRegion(RegionID.create(region.getCountryID(), region.getOrganisationID(), region.getRegionID()), true);
		if(region != null){
			Collection<City> c = region.getCities();
			for(City ci : c){
				cities.add(new CityTreeNode(treeViewer, this, ci));
			}//for
		}//if
		
		setChildrenLoaded(true);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return GeographyAdminPlugin.getImageDescriptor("icons/globe2.png"); //$NON-NLS-1$
	}

	@Override
	public I18nText getName() {
		return region.getName();
	}

}
