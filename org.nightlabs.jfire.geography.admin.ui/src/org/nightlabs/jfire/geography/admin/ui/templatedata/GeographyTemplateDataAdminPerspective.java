package org.nightlabs.jfire.geography.admin.ui.templatedata;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.geography.admin.ui.templatedata.editor.name.GeographyTemplateDataNameEditorView;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataInformationView;
import org.nightlabs.jfire.geography.admin.ui.templatedata.management.GeographyTemplateDataTreeView;

public class GeographyTemplateDataAdminPerspective implements IPerspectiveFactory {
	
	public static final String ID_PERSPECTIVE = GeographyTemplateDataAdminPerspective.class.getName();
	
	private static final String GEOGRAPHY_TEMPLATE_DATE_EDITOR_ID =
		"org.nightlabs.jfire.geography.admin.ui.templatedata.editor.GeographyTemplateDataEditor"; //$NON-NLS-1$
	
	public void createInitialLayout(IPageLayout layout) {
		// Get the editor area.
		String editorArea = layout.getEditorArea();

		// Put the Geography Data View on the left.
		layout.addView(
				GeographyTemplateDataTreeView.ID_VIEW,
				IPageLayout.LEFT,
				0.25f,
				editorArea);

		// Put the Geography Editor on the center
		layout.addView(
				GeographyTemplateDataNameEditorView.ID_VIEW,
				IPageLayout.TOP,
				IPageLayout.DEFAULT_VIEW_RATIO,
				editorArea);

		// Put the view on the bottom with
		IFolderLayout bottom =
			layout.createFolder(
					"bottom", //$NON-NLS-1$
					IPageLayout.BOTTOM,
					IPageLayout.DEFAULT_VIEW_RATIO,
					editorArea);
		bottom.addView(GeographyTemplateDataInformationView.ID_VIEW);
		bottom.addPlaceholder(IPageLayout.ID_PROBLEM_VIEW);
		bottom.addPlaceholder(IPageLayout.ID_EDITOR_AREA);
		bottom.addPlaceholder(GEOGRAPHY_TEMPLATE_DATE_EDITOR_ID);

		// Add the Favorites action set.
		layout.addActionSet(GeographyTemplateDataTreeView.ID_VIEW);
		layout.setEditorAreaVisible(false);
		
		layout.addShowViewShortcut(GeographyTemplateDataTreeView.ID_VIEW);
		layout.addShowViewShortcut(GeographyTemplateDataNameEditorView.ID_VIEW);
		layout.addShowViewShortcut(GeographyTemplateDataInformationView.ID_VIEW);
		
		RCPUtil.addAllPerspectiveShortcuts(layout);
	}
}
