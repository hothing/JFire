package org.nightlabs.jfire.base.ui.jdo.tree.lazy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectID;

/**
 *
 * @author Marco Schulze
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class JDOObjectLazyTreeNode
<JDOObjectID extends ObjectID,
 JDOObject,
 Controller extends ActiveJDOObjectLazyTreeController<JDOObjectID, JDOObject, ? extends JDOObjectLazyTreeNode<JDOObjectID, JDOObject, ? super Controller>>>
{
	private static final Logger logger = Logger.getLogger(JDOObjectLazyTreeNode.class);

	private Controller activeJDOObjectLazyTreeController;
	private JDOObjectLazyTreeNode<JDOObjectID, JDOObject, Controller> parent;
	private volatile JDOObjectID jdoObjectID;
	private volatile JDOObject jdoObject;

	public void setActiveJDOObjectLazyTreeController(Controller activeJDOObjectLazyTreeController)
	{
		this.activeJDOObjectLazyTreeController = activeJDOObjectLazyTreeController;
	}
	public Controller getActiveJDOObjectLazyTreeController()
	{
		return activeJDOObjectLazyTreeController;
	}

	public void setParent(JDOObjectLazyTreeNode<JDOObjectID, JDOObject, Controller> parent)
	{
		this.parent = parent;
	}
	public JDOObjectLazyTreeNode<JDOObjectID, JDOObject, Controller> getParent()
	{
		return parent;
	}

	public void setJdoObjectID(JDOObjectID jdoObjectID) {
		this.jdoObjectID = jdoObjectID;
	}
	public JDOObjectID getJdoObjectID() {
		return jdoObjectID;
	}

	public void setJdoObject(JDOObject jdoObject)
	{
		if (this.jdoObjectID == null) {
			@SuppressWarnings("unchecked")
			JDOObjectID jdoObjectID = (JDOObjectID) JDOHelper.getObjectId(jdoObject);
			if (jdoObjectID == null)
				throw new IllegalStateException("JDOHelper.getObjectId(jdoObject) returned null! jdoObject=" + jdoObject); //$NON-NLS-1$

			this.jdoObjectID = jdoObjectID;
		}
		else {
			if (!this.jdoObjectID.equals(JDOHelper.getObjectId(jdoObject)))
				throw new IllegalArgumentException("this.jdoObjectID != JDOHelper.getObjectId(jdoObject) :: " + this.jdoObjectID + " != " + JDOHelper.getObjectId(jdoObject)); //$NON-NLS-1$ //$NON-NLS-2$
		}

		this.jdoObject = jdoObject;
	}
	public JDOObject getJdoObject()
	{
		return jdoObject;
	}

	private volatile long childNodeCount = -1;

	public long getChildNodeCount() {
		return childNodeCount;
	}
	public void setChildNodeCount(long childCount) {
		this.childNodeCount = childCount;
	}

	private boolean deleted = false;

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	private List<JDOObjectLazyTreeNode<JDOObjectID, JDOObject, Controller>> childNodes = null;
	private List<JDOObjectLazyTreeNode<JDOObjectID, JDOObject, Controller>> childNodes_ro = null;

	/**
	 * @return a read-only copy of the child-nodes of this node or <code>null</code>, if there are no child-nodes assigned (i.e. {@link #setChildNodes(List)} has never been called).
	 */
	public synchronized List<JDOObjectLazyTreeNode<JDOObjectID, JDOObject, Controller>> getChildNodes()
	{
		if (childNodes == null)
			return null;

		if (childNodes_ro == null)
			childNodes_ro = Collections.unmodifiableList(new ArrayList<JDOObjectLazyTreeNode<JDOObjectID, JDOObject, Controller>>(childNodes));

		return childNodes_ro;
	}

	public synchronized void setChildNodes(List<JDOObjectLazyTreeNode<JDOObjectID, JDOObject, Controller>> childNodes)
	{
		this.childNodes = childNodes;
		this.childNodes_ro = null;
		this.childNodeCount = childNodes == null ? 0 : childNodes.size();

		if (logger.isDebugEnabled())
			logger.debug("setChildNodes: childNodes.size()=\"" + (childNodes == null ? null : childNodes.size()) + "\" this.jdoObjectID=\"" + JDOHelper.getObjectId(this.jdoObject) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @param childNode The child-node to be added.
	 * @return <code>true</code>, if the child-node has been added - i.e. {@link #setChildNodes(List)} has already been
	 *		called and a List of child-nodes assigned. <code>false</code>, if the <code>List</code> of child-nodes does not
	 *		yet exist (is <code>null</code>) and the child-node could therefore not be added.
	 */
	public synchronized boolean addChildNode(JDOObjectLazyTreeNode<JDOObjectID, JDOObject, Controller> childNode)
	{
		if (childNodes == null) {
			if (logger.isDebugEnabled())
				logger.debug("addChildNode: this.childNodes=null => do nothing + return false! childNode.jdoObjectID=\"" + JDOHelper.getObjectId(childNode.getJdoObject()) + "\" this.jdoObjectID=\"" + JDOHelper.getObjectId(this.jdoObject) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			return false;
		}

		childNodes.add(childNode);
		this.childNodes_ro = null;
		this.childNodeCount = childNodes == null ? 0 : childNodes.size();

		if (logger.isDebugEnabled())
			logger.debug("addChildNode: added childNode! childNode.jdoObjectID=\"" + JDOHelper.getObjectId(childNode.getJdoObject()) + "\" this.jdoObjectID=\"" + JDOHelper.getObjectId(this.jdoObject) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		return true;
	}

	/**
	 * @param childNode The child-node to be removed.
	 * @return <code>true</code>, if the child-node has been removed - i.e. {@link #setChildNodes(List)} has already been
	 *		called and a List of child-nodes assigned <b>and</b> the given <code>childNode</code> did exist in this
	 *		<code>List</code>. <code>false</code>, if the <code>List</code> of child-nodes does not
	 *		yet exist (is <code>null</code>) or the child-node was not contained in it.
	 */
	public synchronized boolean removeChildNode(JDOObjectLazyTreeNode<JDOObjectID, JDOObject, Controller> childNode)
	{
		if (childNodes == null) {
			if (logger.isDebugEnabled())
				logger.debug("removeChildNode: this.childNodes=null => do nothing + return false! childNode.jdoObjectID=\"" + JDOHelper.getObjectId(childNode.getJdoObject()) + "\" this.jdoObjectID=\"" + JDOHelper.getObjectId(this.jdoObject) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			return false;
		}

		boolean res = childNodes.remove(childNode);
		this.childNodes_ro = null;
		this.childNodeCount = childNodes == null ? 0 : childNodes.size();

		if (logger.isDebugEnabled())
			logger.debug("removeChildNode: removed childNode (return " + res + ")! childNode.jdoObjectID=\"" + JDOHelper.getObjectId(childNode.getJdoObject()) + "\" this.jdoObjectID=\"" + JDOHelper.getObjectId(this.jdoObject) + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		return res;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + jdoObjectID  + ']';
	}


	// ---- Nodal methods ------------------------------------------------------------------------------------------->> Kai >>
	/**
	 * Returns the root node, related to the Tree.
	 */
	@SuppressWarnings("unchecked")
	public <N extends JDOObjectLazyTreeNode<JDOObjectID, JDOObject, Controller>> N getRoot() {
		return (N)(parent.parent == null ? this : parent.getRoot());
	}

	/**
	 * @return a List of JDOObjectIDs contained in this node up until the root.
	 */
	public List<JDOObjectID> getJDOObjectIDsToRoot() {
		return getJDOObjectIDsToRoot(this, new LinkedList<JDOObjectID>());
	}

	private <N extends JDOObjectLazyTreeNode<JDOObjectID, JDOObject, Controller>> List<JDOObjectID> getJDOObjectIDsToRoot(N node, List<JDOObjectID> jdoObjectIDs) {
		// Base case.
		if (node.parent == null)
			return jdoObjectIDs;

		// Iterative case.
		jdoObjectIDs.add(node.jdoObjectID);
		return getJDOObjectIDsToRoot(node.parent, jdoObjectIDs);
	}

	/**
	 * @return the {@link ObjectID}s of the children of this node.
	 */
	public synchronized List<JDOObjectID> getChildrenJDOObjectIDs() {
		List<JDOObjectID> objIDs = new LinkedList<JDOObjectID>();
		List<JDOObjectLazyTreeNode<JDOObjectID,JDOObject,Controller>> childNodes = getChildNodes();
		if (childNodes != null) {
			for (JDOObjectLazyTreeNode<JDOObjectID, JDOObject, Controller> child : childNodes) {
				if (child != null)
					objIDs.add(child.jdoObjectID);
			}
		}

		return objIDs;
	}
}
