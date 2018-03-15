/**
 * 
 */
package org.nightlabs.jfire.base.ui.jdo.tree.lazy;

import org.nightlabs.jdo.ObjectID;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface JDOLazyTreeNodesChangedListener<JDOObjectID extends ObjectID, JDOObject, TreeNode extends JDOObjectLazyTreeNode> {

	void onJDOObjectsChanged(JDOLazyTreeNodesChangedEvent<JDOObjectID, TreeNode> changedEvent);
}
