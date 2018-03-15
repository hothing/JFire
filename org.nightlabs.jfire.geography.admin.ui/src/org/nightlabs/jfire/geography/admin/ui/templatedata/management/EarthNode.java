package org.nightlabs.jfire.geography.admin.ui.templatedata.management;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.admin.ui.GeographyAdminPlugin;
import org.nightlabs.jfire.geography.admin.ui.resource.Messages;

public class EarthNode extends GeographyTreeNode implements Comparable<EarthNode>{
	private List<CountryTreeNode> countryTreeNodes = new LinkedList<CountryTreeNode>();
	private TreeViewer treeViewer;
	
	public EarthNode(TreeViewer treeViewer){
		this.treeViewer = treeViewer;
	}
	
	@Override
	public GeographyTreeNode[] getChildren() {
		return countryTreeNodes.toArray(new CountryTreeNode[countryTreeNodes.size()]);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return GeographyAdminPlugin.getImageDescriptor("icons/earth.png"); //$NON-NLS-1$
	}

	@Override
	public String getLabel() {
		return Messages.getString("org.nightlabs.jfire.geography.admin.ui.templatedata.management.EarthNode.earthNodeName"); //$NON-NLS-1$
	}

	@Override
	public I18nText getName() {
		return null;
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	@Override
	public void loadChildren() {
		if (isChildrenLoaded())
			return;
		
		Collection<Country> countries = Geography.sharedInstance().getCountries();
		countryTreeNodes.clear();
		for (Country c : countries)
			countryTreeNodes.add(new CountryTreeNode(treeViewer, this, c));
		
		setChildrenLoaded(true);
	}
	
	/**
	 * Compares to Nodes with respect to their label
	 */
	public int compareTo(EarthNode e)
	{
		return getLabel().compareTo(e.getLabel());
	}
}
