package org.nightlabs.jfire.geography.admin.ui.templatedata.management;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.District;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.Location;
import org.nightlabs.jfire.geography.admin.ui.GeographyAdminPlugin;
import org.nightlabs.jfire.geography.id.CityID;

public class CityTreeNode extends GeographyTreeNode implements Comparable<GeographyTreeNode>{
	private City city;
	private List<GeographyTreeNode> childNodes = new LinkedList<GeographyTreeNode>();
	private TreeViewer treeViewer = null;
	
	public CityTreeNode(TreeViewer treeViewer, GeographyTreeNode parent, City city)
	{
		this.treeViewer = treeViewer;
		this.city = city;
		setGeographyObject(city);
		setParent(parent);
	}
	
	public void addChild(GeographyTreeNode child)
	{
		childNodes.add(child);
	}
	
	public void removeChild(GeographyTreeNode child)
	{
		childNodes.remove(child);
	}
	
	@Override
	public String getLabel()
	{
		return city.getName().getText();
	}

	@Override
	public GeographyTreeNode[] getChildren()
	{
		if(!childrenLoaded){
			loadChildren();
		}//if
		return childNodes.toArray(new GeographyTreeNode[0]);
	}

	@Override
	public boolean hasChildren()
	{
		return true;
	}
	
	public City getCity()
	{
		return city;
	}

	/**
	 * Compares to StructBlockNodes with respect to their label
	 */
	public int compareTo(GeographyTreeNode c)
	{
		return getLabel().compareTo(c.getLabel());
	}

	@Override
	public void loadChildren() {
		if(isChildrenLoaded())
			return;
		
		childNodes.clear();
		
		if(city != null){
			city = Geography.sharedInstance().getCity(CityID.create(city.getCountryID(), city.getOrganisationID(), city.getCityID()), true);
			Collection<Location> l = city.getLocations();
			for(Location lo : l){
				childNodes.add(new LocationTreeNode(treeViewer, this, lo));
			}//for
			
			Collection<District> d = city.getDistricts();
			for(District di : d){
				childNodes.add(new DistrictTreeNode(treeViewer, this, di));
			}//for
			
			setChildrenLoaded(true);
		}//if
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return GeographyAdminPlugin.getImageDescriptor("icons/city.png"); //$NON-NLS-1$
	}

	@Override
	public I18nText getName() {
		return city.getName();
	}
}
