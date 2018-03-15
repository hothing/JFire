package org.nightlabs.jfire.base.ui.jdo.tree.lazy;

import java.util.Map;

import org.nightlabs.jdo.ObjectID;

public class ActiveJDOObjectLazyTreeState
{
	private JDOObjectLazyTreeNode<?, ?, ?> hiddenRootNode;
	private Map<ObjectID, JDOObjectLazyTreeNode<?, ?, ?>> objectID2TreeNode;

	protected ActiveJDOObjectLazyTreeState(JDOObjectLazyTreeNode<?, ?, ?> hiddenRootNode, Map<ObjectID, JDOObjectLazyTreeNode<?, ?, ?>> objectID2TreeNode) {
		this.hiddenRootNode = hiddenRootNode;
		this.objectID2TreeNode = objectID2TreeNode;
	}

	protected JDOObjectLazyTreeNode<?, ?, ?> getHiddenRootNode() {
		return hiddenRootNode;
	}
	protected Map<ObjectID, JDOObjectLazyTreeNode<?, ?, ?>> getObjectID2TreeNode() {
		return objectID2TreeNode;
	}
}
