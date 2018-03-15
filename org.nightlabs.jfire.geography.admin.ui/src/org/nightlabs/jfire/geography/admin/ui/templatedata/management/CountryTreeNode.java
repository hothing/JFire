package org.nightlabs.jfire.geography.admin.ui.templatedata.management;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.GeographyAdminPlugin;
import org.nightlabs.jfire.geography.id.CountryID;

public class CountryTreeNode extends GeographyTreeNode implements Comparable<CountryTreeNode>{
	private List<RegionTreeNode> regions = new LinkedList<RegionTreeNode>();
	private TreeViewer treeViewer;
	
	public CountryTreeNode(TreeViewer treeViewer, GeographyTreeNode parent, Country country)
	{
		if (country == null)
			throw new NullPointerException("country must be non-null."); //$NON-NLS-1$

		this.treeViewer = treeViewer;
		setGeographyObject(country);
		setParent(parent);
	}

	public void addRegion(RegionTreeNode region)
	{
		regions.add(region);
	}

	public void removeRegion(RegionTreeNode region)
	{
		regions.remove(region);
	}

	@Override
	public String getLabel()
	{
		return ((Country)getGeographyObject()).getName().getText();
	}

	@Override
	public RegionTreeNode[] getChildren()
	{
		return regions.toArray(new RegionTreeNode[regions.size()]);
	}

	@Override
	public boolean hasChildren()
	{
		return true;
	}

	public Country getCountry()
	{
		return (Country)getGeographyObject();
	}

	/**
	 * Compares to Country with respect to their label
	 */
	public int compareTo(CountryTreeNode c)
	{
		return getLabel().compareTo(c.getLabel());
	}

	@Override
	public void loadChildren() {
		if(isChildrenLoaded())
			return;
		
		Country country = (Country)getGeographyObject();
		country = Geography.sharedInstance().getCountry(CountryID.create(country.getCountryID()), true);
		Collection<Region> r = country.getRegions();
		regions.clear();
		for(Region ri : r){
			regions.add(new RegionTreeNode(treeViewer, this, ri));
		}//for
		
		setChildrenLoaded(true);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return GeographyAdminPlugin.getImageDescriptor("icons/globe_red.png"); //$NON-NLS-1$
	}

	@Override
	public I18nText getName() {
		return ((Country)getGeographyObject()).getName();
	}
}
