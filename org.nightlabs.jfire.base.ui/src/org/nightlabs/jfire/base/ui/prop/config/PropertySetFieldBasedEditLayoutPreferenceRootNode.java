/**
 * 
 */
package org.nightlabs.jfire.base.ui.prop.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.swt.graphics.Image;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.eclipse.preferences.ui.OverviewPage;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutUseCase;
import org.nightlabs.jfire.prop.dao.PropertySetFieldBasedEditLayoutUseCaseDAO;
import org.nightlabs.progress.ProgressMonitor;

/**
 * A root node that is put into the UIPreferencePage in the prefrence page tree.
 * The root node will have one child node for each {@link PropertySetFieldBasedEditLayoutUseCase}
 * found on the server.  
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public class PropertySetFieldBasedEditLayoutPreferenceRootNode implements IPreferenceNode {

	private List<IPreferenceNode> subNodes = new ArrayList<IPreferenceNode>();
	private IPreferenceNode[] subNodesArr;
	private OverviewPage page;
	private Job loadUseCasesJob;
	
	/**
	 * Create a new {@link PropertySetFieldBasedEditLayoutPreferenceRootNode}.
	 * The constructor will start a job that loads all
	 * {@link PropertySetFieldBasedEditLayoutUseCase}s and adds a child-node for each one found.
	 */
	public PropertySetFieldBasedEditLayoutPreferenceRootNode() {
		loadUseCasesJob = new Job(Messages.getString("org.nightlabs.jfire.base.ui.prop.config.PropertySetFieldBasedEditLayoutPreferenceRootNode.job.loadPropertySetEditUseCases")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				Collection<PropertySetFieldBasedEditLayoutUseCase> allUseCases = PropertySetFieldBasedEditLayoutUseCaseDAO.sharedInstance().getAllUseCases(
						new String[] {
							FetchPlan.DEFAULT, PropertySetFieldBasedEditLayoutUseCase.FETCH_GROUP_NAME,
							PropertySetFieldBasedEditLayoutUseCase.FETCH_GROUP_DESCRIPTION,
							PropertySetFieldBasedEditLayoutUseCase.FETCH_GROUP_STRUCT_LOCAL_ID
						}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
				for (PropertySetFieldBasedEditLayoutUseCase useCase : allUseCases) {
					add(new PropertySetFieldBasedEditLayoutPreferenceNode(useCase));
				}
				return Status.OK_STATUS;
			}
		};
		loadUseCasesJob.schedule();
	}
	
	@Override
	public IPreferenceNode[] getSubNodes() {
		try {
			loadUseCasesJob.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return subNodesArr != null ? subNodesArr : new IPreferenceNode[0];
	}

	private void reCreateSubNodesArr() {
		subNodesArr = subNodes.toArray(new IPreferenceNode[subNodes.size()]);
	}
	
	@Override
	public void add(IPreferenceNode node) {
		subNodes.add(node);
		reCreateSubNodesArr();
	}

	@Override
	public void createPage() {
		page = new OverviewPage();
		page.setTitle(getLabelText());
	}

	@Override
	public void disposeResources() {
		if (page != null) {
			page.dispose();
		}
	}

	@Override
	public IPreferenceNode findSubNode(String id) {
		for (IPreferenceNode subNode : subNodes) {
			if (id.equals(subNode.getId()))
				return subNode;
		}
		return null;
	}

	@Override
	public String getId() {
		return this.getClass().getName();
	}

	@Override
	public Image getLabelImage() {
		return null;
	}

	@Override
	public String getLabelText() {
		return Messages.getString("org.nightlabs.jfire.base.ui.prop.config.PropertySetFieldBasedEditLayoutPreferenceRootNode.label.propertySetEditors"); //$NON-NLS-1$
	}

	@Override
	public IPreferencePage getPage() {
		return page;
	}

	@Override
	public IPreferenceNode remove(String id) {
		IPreferenceNode node = findSubNode(id);
		if (node != null) {
			remove(node);
			return node;
		}
		return null;
	}

	@Override
	public boolean remove(IPreferenceNode node) {
		return subNodes.remove(node);
	}
}
