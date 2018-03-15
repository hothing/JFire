package org.nightlabs.jfire.geography.admin.ui.templatedata.editor.name;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.language.I18nTextEditorTable;
import org.nightlabs.base.ui.language.LanguageManager;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.i18n.I18nTextBuffer;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.Location;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.admin.ui.templatedata.GeographyTemplateDataAdmin;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.CityTreeNode;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.CountryTreeNode;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTreeNode;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.LocationTreeNode;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.RegionTreeNode;
import org.nightlabs.language.LanguageCf;

public class GeographyNameTableComposite 
extends Composite 
implements ModifyListener
{
	private I18nTextEditorTable i18nTable;

	private Object element = null;

	private I18nText i18nText = new I18nTextBuffer();

	private LanguageCf[] supportLanguages = LanguageManager.sharedInstance().getLanguages().toArray(new LanguageCf[0]);

//	private boolean isUpdate;

	public GeographyNameTableComposite(Composite parent, int style, boolean isUpdate)
	{
		super(parent, style);
//		this.isUpdate = isUpdate;

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.horizontalSpacing = 1;
		this.setLayout(gridLayout);

		i18nTable = new I18nTextEditorTable(this);
		for(int i = 0; i < supportLanguages.length; i++){
			i18nText.setText(supportLanguages[i].getLanguageID(), ""); //$NON-NLS-1$
		}//if
		i18nTable.setI18nText(i18nText);
		i18nTable.addModifyListener(this);		
	}

	public void updateTable(Object element){
		this.element = element;

		if(element instanceof GeographyTreeNode){
			GeographyTreeNode treeNode = (GeographyTreeNode)element;
			i18nTable.setI18nText(treeNode.getName());
		}//if
	}

	public void modifyText(ModifyEvent arg0) {
		try {
			GeographyTemplateDataAdmin geoAdmin = new GeographyTemplateDataAdmin();
			I18nText i18nText = i18nTable.getI18nText();
//			String rootOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID(); //TODO Change to root orgID

			GeographyTreeNode treeNode = (GeographyTreeNode) element;

			if(treeNode != null){
				for(String languageID : i18nText.getLanguageIDs()){
					if (treeNode.getName() != null)
						treeNode.getName().setText(languageID, i18nText.getText(languageID));
				}//for

				if (treeNode instanceof CountryTreeNode){
					geoAdmin.storeGeographyTemplateCountryData((Country)treeNode.getGeographyObject());
				}//if
				else if (treeNode instanceof RegionTreeNode){
					geoAdmin.storeGeographyTemplateRegionData((Region)treeNode.getGeographyObject());
				}//if
				else if (treeNode instanceof CityTreeNode){
					geoAdmin.storeGeographyTemplateCityData((City)treeNode.getGeographyObject());
				}//if
				else if (treeNode instanceof LocationTreeNode){
					geoAdmin.storeGeographyTemplateLocationData((Location)treeNode.getGeographyObject());
				}//if
				
				GeographyTemplateDataTreeView dataTreeView = (GeographyTemplateDataTreeView)RCPUtil.findView(GeographyTemplateDataTreeView.ID_VIEW);
				if(dataTreeView != null)
					dataTreeView.getTreeViewer().refresh(treeNode, true);
			}//if

		}//try
		finally {
		}//finally
	}

	public I18nText getI18nText(){
		return i18nText;
	}
}
