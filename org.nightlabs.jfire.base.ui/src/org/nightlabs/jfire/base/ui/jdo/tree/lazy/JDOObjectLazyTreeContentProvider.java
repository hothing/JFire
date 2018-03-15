package org.nightlabs.jfire.base.ui.jdo.tree.lazy;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.nightlabs.jdo.ObjectID;

public class JDOObjectLazyTreeContentProvider
<JDOObjectID extends ObjectID,
JDOObject,
TreeNode extends JDOObjectLazyTreeNode<JDOObjectID, JDOObject, ? extends ActiveJDOObjectLazyTreeController<JDOObjectID, JDOObject, TreeNode>>>
implements ILazyTreeContentProvider
{
	private static final Logger logger = Logger.getLogger(JDOObjectLazyTreeContentProvider.class);
	private TreeViewer treeViewer;
	private ActiveJDOObjectLazyTreeController<JDOObjectID, JDOObject, TreeNode> controller = null;

	protected TreeViewer getTreeViewer() {
		if (treeViewer == null)
			throw new IllegalStateException("There is no TreeViewer assigned; inputChanged(...) was not yet called!"); //$NON-NLS-1$

		return treeViewer;
	}

	protected ActiveJDOObjectLazyTreeController<JDOObjectID, JDOObject, TreeNode> getController() {
		if (controller == null)
			throw new IllegalStateException("There is no ActiveJDOObjectLazyTreeController assigned; inputChanged(...) was not yet called!"); //$NON-NLS-1$

		return controller;
	}

	@Override
	public Object getParent(Object element) {
		if (logger.isTraceEnabled())
			logger.trace("getParent: entered for element=" + element); //$NON-NLS-1$

		TreeNode child = null;
		if (element instanceof ActiveJDOObjectLazyTreeController) {
			// nothing
		}
		else if (element instanceof String) {
			// nothing
		}
		else {
			child = naiveCast(child, element);
		}
		if (child == null)
			return null;

		if (logger.isDebugEnabled())
			logger.debug("getParent: child.oid=" + child.getJdoObjectID()); //$NON-NLS-1$

		return child.getParent();
	}

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		TreeNode parent = null;

		if (element instanceof ActiveJDOObjectLazyTreeController) {
			// nothing
		}
		else if (element instanceof String) {
			// nothing
		}
		else {
			parent = naiveCast(parent, element);
		}

		long realChildCount;
		long childCount = getController().getNodeCount(parent);
		
		if (childCount < 0) // loading
			realChildCount = 1; // the "Loading..." message
		else
			realChildCount = childCount;

		if (logger.isDebugEnabled())
			logger.debug("updateChildCount: parent.oid=" + (parent == null ? null : parent.getJdoObjectID()) + " childCount=" + childCount); //$NON-NLS-1$ //$NON-NLS-2$

		long start = System.currentTimeMillis();
		if (realChildCount != currentChildCount) {			
			getTreeViewer().setChildCount(element, (int)realChildCount);			
			if (logger.isDebugEnabled()){
				long duration = System.currentTimeMillis() - start;
				logger.debug(duration+" ms took getTreeViewer().setChildCount("+element+", "+realChildCount+")");
			}
		}
	}

	protected Set<String> updateElementReplaceActiveIDSet = new HashSet<String>();

	@Override
	public void updateElement(final Object parentElement, final int index) {
		if (Display.getCurrent() == null)
			throw new IllegalStateException("Thread mismatch! This method must be called on the UI thread!"); //$NON-NLS-1$

		TreeNode parent = null;

		if (parentElement instanceof ActiveJDOObjectLazyTreeController) {
			// nothing
		}
		else if (parentElement instanceof String) {
			// nothing
		}
		else {
			parent = naiveCast(parent, parentElement);
		}

		if (parent != null) {
			TreeNode n = parent;
			while (n != null) {
				if (collapsedNodes.contains(n)) // WORKAROUND for bug in TreeViewer: it calls updateElement for all children when collapsing a node. Strange but true.
					return;

				@SuppressWarnings("unchecked")
				TreeNode p = (TreeNode) n.getParent();
				n = p;
			}
		}

		TreeNode child = getController().getNode(parent, index);
		if (child == null) { // loading
			if (logger.isDebugEnabled())
				logger.debug("updateElement: parent.oid=" + (parent == null ? null : parent.getJdoObjectID()) + " :: Child is not yet loaded!"); //$NON-NLS-1$ //$NON-NLS-2$

			String updateElementReplaceActiveID = parentElement == null ? "null" : parentElement.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(parentElement)) + '/' + Integer.toString(index, 36); //$NON-NLS-1$
			if (!updateElementReplaceActiveIDSet.contains(updateElementReplaceActiveID)) {
				updateElementReplaceActiveIDSet.add(updateElementReplaceActiveID);
				try {

					if (parent == null ? true : !parent.isDeleted())
						try {
							long start = System.currentTimeMillis();
							getTreeViewer().replace(parentElement, index, LOADING);
							if (logger.isDebugEnabled()) {
								long duration = System.currentTimeMillis() - start;
								logger.debug(duration+" ms took getTreeViewer().replace("+parentElement+", "+index+", "+LOADING);
							}
						} catch (NullPointerException x) {
							logger.warn("Hmmm... I thought the isDeleted() would solve the problem :-( Marco.", x); //$NON-NLS-1$
//						getTreeViewer().collapseAll(); // seems to cause a crash of SWT on linux - at least sometimes :-(
							return;
						}

				} finally {
					updateElementReplaceActiveIDSet.remove(updateElementReplaceActiveID);
				}
			}
		}
		else {
//			if (child.getJdoObject() == null)
//				getTreeViewer().replace(parentElement, index, child); // String.format(LOADING_OBJECT_ID, child.getJdoObjectID()));
//			else
//				getTreeViewer().replace(parentElement, index, child);
			if (parent == null ? true : !parent.isDeleted())
				try {
					long start = System.currentTimeMillis();
					getTreeViewer().replace(parentElement, index, child);
					if (logger.isDebugEnabled()) {
						long duration = System.currentTimeMillis() - start;
						logger.debug(duration+" ms took getTreeViewer().replace("+parentElement+", "+index+", "+LOADING);
					}
				} catch (NullPointerException x) {
					logger.warn("Hmmm... I thought the isDeleted() would solve the problem :-( Marco.", x); //$NON-NLS-1$
//				getTreeViewer().collapseAll(); // seems to cause a crash of SWT on linux - at least sometimes :-(
					return;
				}

			long childChildNodeCount = getController().getNodeCount(child);

			if (logger.isDebugEnabled())
				logger.debug("updateElement: parent.oid=" + (parent == null ? null : parent.getJdoObjectID()) + " child.oid=" + child.getJdoObjectID() + " child.childCount=" + childChildNodeCount); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			if (childChildNodeCount < 0)
				childChildNodeCount = 1; // the "Loading..." message

			long start = System.currentTimeMillis(); 
			getTreeViewer().setChildCount(child, (int)childChildNodeCount);
			if (logger.isDebugEnabled()) {
				long duration = System.currentTimeMillis() - start;
				logger.debug(duration+" ms took getTreeViewer().setChildCount("+child+", "+childChildNodeCount+")");
			}
		}
	}

	public static final String LOADING = "Loading..."; //$NON-NLS-1$
//	private static final String LOADING_OBJECT_ID = "Loading %s ...";

	@Override
	public void dispose() {
		// nothing
	}

	/**
	 * This field is a WORKAROUND for a bug in the TreeViewer: It calls {@link #updateElement(Object, int)} for
	 * all children when collapsing a node. Totally unnecessary and highly inefficient :-( but fortunately possible
	 * to work-around it.
	 */
	protected Set<TreeNode> collapsedNodes = new HashSet<TreeNode>();

	private ITreeViewerListener treeViewerListener = new ITreeViewerListener() {
		public void treeCollapsed(org.eclipse.jface.viewers.TreeExpansionEvent event) {
			if (event.getElement() instanceof String)
				return;

			@SuppressWarnings("unchecked")
			TreeNode node = (TreeNode) event.getElement();

			if (logger.isDebugEnabled())
				logger.debug("treeViewerListener.treeCollapsed: node=" + node); //$NON-NLS-1$

			collapsedNodes.add(node);
		}
		public void treeExpanded(org.eclipse.jface.viewers.TreeExpansionEvent event) {
			if (event.getElement() instanceof String)
				return;

			@SuppressWarnings("unchecked")
			TreeNode node = (TreeNode) event.getElement();

			if (logger.isDebugEnabled())
				logger.debug("treeViewerListener.treeExpanded: node=" + node); //$NON-NLS-1$

			if (collapsedNodes.remove(node))
				getTreeViewer().refresh();
		}
	};

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (this.treeViewer != null) {
			this.treeViewer.removeTreeListener(treeViewerListener);
		}

		this.treeViewer = (TreeViewer) viewer;
		this.collapsedNodes.clear();
		if (this.treeViewer != null) {
			this.treeViewer.addTreeListener(treeViewerListener);
		}

		ActiveJDOObjectLazyTreeController<JDOObjectID, JDOObject, TreeNode> controller = null;
		if (newInput instanceof ActiveJDOObjectLazyTreeController) {
			controller = naiveCast(controller, newInput);
		}
		else if (newInput instanceof String) {
			// nothing
		}
		else {
			TreeNode parent = null;
			parent = naiveCast(parent, newInput);
			if (parent != null)
				controller = parent.getActiveJDOObjectLazyTreeController();
		}

		if (controller != null)
			this.controller = controller;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	protected <T> T naiveCast(T t, Object obj) {
		return (T) obj;
	}
}
