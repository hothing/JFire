package org.nightlabs.jfire.base.admin.ui.timer;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.nightlabs.base.ui.util.RCPUtil;

public class TimerPerspective
		implements IPerspectiveFactory
{
	public static final String ID_PERSPECTIVE = TimerPerspective.class.getName();

  public void createInitialLayout(IPageLayout layout)
  {
  	layout.setEditorAreaVisible(false);
  	layout.addView(TaskListView.ID_VIEW, IPageLayout.TOP, 1.0f, IPageLayout.ID_EDITOR_AREA);
  	layout.addView(TaskDetailView.ID_VIEW, IPageLayout.BOTTOM, 0.7f, TaskListView.ID_VIEW);
  	
  	layout.addShowViewShortcut(TaskListView.ID_VIEW);
  	layout.addShowViewShortcut(TaskDetailView.ID_VIEW);
  	
  	RCPUtil.addAllPerspectiveShortcuts(layout);
	}

}
