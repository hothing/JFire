/**
 * 
 */
package org.nightlabs.jfire.base.ui.jdo.tree;

import java.util.List;

import org.nightlabs.base.ui.tree.TreeContentProvider;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.ui.resource.Messages;

/**
 * A ContentProvider that can be used with a TreeViewer that is driven by an {@link ActiveJDOObjectTreeController}.
 * It assumes the initial input of the ContentProvider is the {@link ActiveJDOObjectTreeController}
 * and uses the controller to serve element.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public abstract class JDOObjectTreeContentProvider
<JDOObjectID extends ObjectID, 
 JDOObject, 
 TreeNode extends JDOObjectTreeNode<JDOObjectID, JDOObject, ? extends ActiveJDOObjectTreeController<JDOObjectID, JDOObject, TreeNode>>> 
extends TreeContentProvider {

	public JDOObjectTreeContentProvider() {
	}
	
	/**
	 * Default implementation calls {@link #getChildren(Object)}
	 */
	public Object[] getElements(Object inputElement)
	{
		return getChildren(inputElement);
	}
	
	@Override
	public Object[] getChildren(Object parentElement)
	{
		TreeNode parent = null;
		ActiveJDOObjectTreeController<JDOObjectID, JDOObject, TreeNode> controller = null;

		if (parentElement instanceof ActiveJDOObjectTreeController) {
			controller = naiveCast(controller, parentElement);
			parent = null;
		}
		else if (parentElement instanceof String) {
			return null;
		}
		else {
			parent = naiveCast(parent, parentElement);
			controller = parent.getActiveJDOObjectTreeController();
		}

		if (controller == null)
			return new Object[] { };

		List<TreeNode> res = controller.getNodes(parent);
		if (res == null)
			return new String[] { Messages.getString("org.nightlabs.jfire.base.ui.jdo.tree.JDOObjectTreeContentProvider.loading") }; //$NON-NLS-1$
		else
			return res.toArray();
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof String)
			return false;
		TreeNode node = null;
		node = naiveCast(node, element);
		return hasJDOObjectChildren((JDOObject) node.getJdoObject());
	}
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private <T> T naiveCast(T t, Object obj) {
		return (T) obj;
	}
	

	/**
	 * Implement this for custom checking whether the node/jdoObject has children.
	 * 
	 * @param jdoObject The {@link JDOObject} to check
	 * @return Whether the given {@link JDOObject} has children.
	 */
	public abstract boolean hasJDOObjectChildren(JDOObject jdoObject);
}
