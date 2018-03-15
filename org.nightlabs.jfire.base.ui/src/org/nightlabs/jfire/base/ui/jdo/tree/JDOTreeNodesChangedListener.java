/**
 *
 */
package org.nightlabs.jfire.base.ui.jdo.tree;

import org.nightlabs.jdo.ObjectID;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@SuppressWarnings("unchecked")
public interface JDOTreeNodesChangedListener<JDOObjectID extends ObjectID, JDOObject, TreeNode extends JDOObjectTreeNode> {

	void onJDOObjectsChanged(JDOTreeNodesChangedEvent<JDOObjectID, TreeNode> changedEvent);
}
