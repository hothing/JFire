package org.nightlabs.jfire.geography.admin.ui.templatedata.management;

import org.eclipse.jface.resource.ImageDescriptor;
import org.nightlabs.i18n.I18nText;

public abstract class GeographyTreeNode {
	private Object geographyObject;
	private GeographyTreeNode parent;
	protected boolean childrenLoaded = false;
	
	public abstract String getLabel();
	public abstract boolean hasChildren();
	public abstract GeographyTreeNode[] getChildren();
	public abstract void loadChildren();
	public abstract ImageDescriptor getImageDescriptor();
	public abstract I18nText getName();
	
	protected void setGeographyObject(Object geographyObj){
		this.geographyObject = geographyObj;
	}
	
	public Object getGeographyObject(){
		return geographyObject;
	}
	
	public void setParent(GeographyTreeNode parent){
		this.parent = parent;
	}
	
	public GeographyTreeNode getParent(){
		return parent;
	}
	
	public boolean isChildrenLoaded() {
		return childrenLoaded;
	}
	
	public void setChildrenLoaded(boolean isChildrenLoaded){
		this.childrenLoaded = isChildrenLoaded;
	}
	
	public GeographyTreeNode findNode(GeographyTreeNode node){
		if(getGeographyObject().equals(node)){
			return this;
		}
		else{
			GeographyTreeNode[] nodes = getChildren();
			if(nodes != null){
				for(GeographyTreeNode iNode : nodes){
					if(iNode.equals(node)){
						return iNode;
					}//if
				}//for
			}//if
		}//else
		
		return null;
	}
}
