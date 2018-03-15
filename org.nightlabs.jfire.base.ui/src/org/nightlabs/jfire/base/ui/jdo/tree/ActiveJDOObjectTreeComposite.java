/**
 *
 */
package org.nightlabs.jfire.base.ui.jdo.tree;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.nightlabs.base.ui.tree.AbstractTreeComposite;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.util.Util;

/**
 * {@link AbstractTreeComposite} to be used with an {@link ActiveJDOObjectTreeController}.
 * It enables the programatic expansion of active trees (trees that get their data from an {@link ActiveJDOObjectTreeController}).
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public abstract class ActiveJDOObjectTreeComposite<JDOObjectID extends ObjectID, JDOObject, TreeNode extends JDOObjectTreeNode>
extends AbstractTreeComposite<JDOObject>
{
	private static final Logger logger = Logger.getLogger(ActiveJDOObjectTreeComposite.class);

	private Set<TreeNode> alreadyLoadedNodes = new HashSet<TreeNode>();

	/**
	 * Create a new {@link ActiveJDOObjectTreeComposite} for the given parent.
	 *
	 * @param parent The parent to use.
	 */
	public ActiveJDOObjectTreeComposite(Composite parent) {
		super(parent);
	}

	/**
	 * Create a new {@link ActiveJDOObjectTreeComposite} for the given parent.
	 *
	 * @param parent The parent to use.
	 * @param init Whether to initialize the tree (set content and label provider etc.)
	 */
	public ActiveJDOObjectTreeComposite(Composite parent, boolean init) {
		super(parent, init);
	}

	/**
	 * Create a new {@link ActiveJDOObjectTreeComposite} for the given parent
	 * with the given style and layout(data).
	 *
	 * @param parent The parent to use.
	 * @param style The style to apply to the new {@link ActiveJDOObjectTreeComposite} (surrounding the tree).
	 * @param setLayoutData Whether to set a layout-data that will cause the tree to fill in both directions inside a GridData.
	 * @param init Whether to initialize the tree (set content and label provider etc.)
	 * @param headerVisible Whether the header columns of the tree should be visible.
	 */
	public ActiveJDOObjectTreeComposite(Composite parent, int style,
			boolean setLayoutData, boolean init, boolean headerVisible) {
		super(parent, style, setLayoutData, init, headerVisible);
	}

	/**
	 * @param parent
	 * @param style
	 * @param setLayoutData
	 * @param init
	 * @param headerVisible
	 * @param sortColumns
	 */
	public ActiveJDOObjectTreeComposite(Composite parent, int style,
			boolean setLayoutData, boolean init, boolean headerVisible,
			boolean sortColumns) {
		super(parent, style, setLayoutData, init, headerVisible, sortColumns);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.base.ui.tree.AbstractTreeComposite#getSelectionObject(java.lang.Object)
	 */
	@Override
	protected JDOObject getSelectionObject(Object obj) {
		if (!(obj instanceof JDOObjectTreeNode))
			return null;
		return (JDOObject) ((TreeNode)obj).getJdoObject();
	}

	/**
	 * Here subclasses should provide the {@link ActiveJDOObjectTreeController} that
	 * can provide the data for this tree.
	 *
	 * @return The {@link ActiveJDOObjectTreeController} for this tree.
	 */
	protected abstract ActiveJDOObjectTreeController<JDOObjectID, JDOObject, TreeNode> getJDOObjectTreeController();

	/**
	 * (non-Javadoc)
	 * @see org.nightlabs.base.ui.tree.AbstractTreeComposite#createTreeViewer(int)
	 */
	@Override
	protected TreeViewer createTreeViewer(int style) {
		return new ActiveTreeViewer(this, style);
	}

	protected ActiveTreeViewer getActiveTreeViewer() {
		return (ActiveTreeViewer) getTreeViewer();
	}

	/**
	 * Selects the node in the tree which JDOObject has the corresponding JDOObjectID.
	 * If the node has not been loaded yet, it will be loaded lazily.
	 *
	 * @param jdoObjectID the JDOObjectID of the JDOObject to select the corresponding node for.
	 */
	public void setSelectedObjectID(JDOObjectID jdoObjectID) {
		long startFetchingParentIDs = System.currentTimeMillis();
		final List<JDOObjectID> parentIds = getParentIDsUntilAvailable(jdoObjectID, new ArrayList<JDOObjectID>());
		if (logger.isDebugEnabled()) {
			long duration = System.currentTimeMillis() - startFetchingParentIDs;
			logger.debug("Fetching parent ids took "+duration+" ms"); //$NON-NLS-1$ //$NON-NLS-2$
			logger.debug("parentIDs = "+parentIds); //$NON-NLS-1$
		}
		if (parentIds != null && !parentIds.isEmpty()) {
			JDOObjectID lastAvailableID = parentIds.get(0);
			final TreeNode lastAvailableNode = getJDOObjectTreeController().getTreeNode(lastAvailableID);
			alreadyLoadedNodes.clear();
			getActiveTreeViewer().loadChildren(lastAvailableNode, parentIds, true);
		}
	}

	/**
	 *
	 * @param jdoObjectID the JDOObjectID where to get all necessary parentIDs for until we reach node which is already visible and loaded.
	 * @param parentIDPath a List of all parentIDs which have been identified so far.
	 * @return the List of all necessary parentIDs for until we reach node which is already visible and loaded
	 */
	private List<JDOObjectID> getParentIDsUntilAvailable(JDOObjectID jdoObjectID, List<JDOObjectID> parentIDPath)
	{
		if (jdoObjectID == null)
			return parentIDPath;

		TreeNode treeNode = getJDOObjectTreeController().getTreeNode(jdoObjectID);
		if (treeNode != null) {
			return parentIDPath;
		}
		else {
			if (!parentIDPath.contains(jdoObjectID)) {
				parentIDPath.add(jdoObjectID);
			}

			Collection<JDOObject> jdoObjects = getJDOObjectTreeController().retrieveJDOObjects(Collections.singleton(jdoObjectID), new NullProgressMonitor());
			if (jdoObjects != null && !jdoObjects.isEmpty()) {
				JDOObject object = jdoObjects.iterator().next();
				JDOObjectID parentID = (JDOObjectID) getJDOObjectTreeController().getTreeNodeParentResolver().getParentObjectID(object);
				parentIDPath.add(0, parentID);
				return getParentIDsUntilAvailable(parentID, parentIDPath);
			}
			return null;
		}
	}

	/**
	 * Currently used for enabling programatic expansion of active trees.
	 */
	private class ActiveTreeViewer extends TreeViewer {

		public ActiveTreeViewer(Composite parent, int style) {
			super(parent, style);
		}

		/**
		 * {@inheritDoc}
		 * Overwrites and calls {@link #internalExpand(Object, Object, int, int, Set)}.
		 */
		@Override
		public void expandToLevel(Object elementOrTreePath, int level) {
			int startLevel = 0;
			if (elementOrTreePath instanceof ActiveJDOObjectTreeController)
				startLevel--;
			internalExpand(elementOrTreePath, elementOrTreePath, startLevel, level, new HashSet<LoadListener>());
		}

		/**
		 * Uses the {@link ActiveJDOObjectTreeController} of this tree
		 * to trigger/get data that needs to be expanded.
		 *
		 * The {@link LoadListener} created here recurses into this method.
		 */
		@SuppressWarnings("unchecked") //$NON-NLS-1$
		private void internalExpand(Object root, Object elementOrTreePath, int level, int totalLevel, Set<LoadListener> listenerStack) {
			LoadListener listener = new LoadListener(root, elementOrTreePath, level, totalLevel, listenerStack);
			getJDOObjectTreeController().addJDOTreeNodesChangedListener(listener);
			List<TreeNode> childNodes = null;
			if (elementOrTreePath instanceof ActiveJDOObjectTreeController) {
				childNodes = getJDOObjectTreeController().getNodes(null);
			}
			else {
				try {
					childNodes = getJDOObjectTreeController().getNodes((TreeNode) elementOrTreePath);
				} catch (ClassCastException e) {
					// do nothing, wrong type of elementOrTreePath was passed
				}
			}

			if (childNodes != null) {
				listener.handleLoad(childNodes);
			}
		}

		private TreeItem getItem(TreeNode node) {
			Widget item = doFindItem(node);
			return (TreeItem) item;
		}

		/**
		 * Makes the super expand method accessible
		 */
		private void superExpandToLevel(Object elementOrTreePath, int level) {
			super.expandToLevel(elementOrTreePath, level);
		}

		/**
		 * Send the SWT.Expand to the given TreeItem, this is necessary instead of calling TreeItem.setExpanded(true)
		 * because only this way the ContentProvider.getChildren() method of the TreeViewer is triggered.
		 *
		 * @param item the TreeItem to send the SWT expansion event for.
		 */
		private void sendEventExpandTreeItem(TreeItem item) {
			//Checks whether the item's already collapsed
			Event event = new Event();
			event.type = SWT.Expand;
			event.item = item;
			event.widget = item.getParent();
			Method method;
			try {
				method = Widget.class.getDeclaredMethod("sendEvent", new Class[] {int.class, Event.class}); //$NON-NLS-1$
				method.setAccessible(true);
				method.invoke(item.getParent(), event.type, event);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			item.setExpanded(true);
		}

		/**
		 * Searches the given List of TreeNode, for a node which ObjectID of the TreeNode.getJDOObject()
		 * equals the given one.
		 *
		 * @param nodes the List of nodes to check
		 * @param jdoObjectID the JDOObjectID to look for
		 * @return the TreeNode which ObjectID of the TreeNode.getJDOObject() equals the given one or null if not contained.
		 */
		private TreeNode getChild(List<TreeNode> nodes, JDOObjectID jdoObjectID)
		{
			if (nodes != null && jdoObjectID != null) {
				for (TreeNode node : nodes) {
					Object objectID = JDOHelper.getObjectId(node.getJdoObject());
					if (objectID != null && objectID.equals(jdoObjectID)) {
						return node;
					}
				}
			}
			return null;
		}

		/**
		 * Loads the children for all necessary parent nodes (given as objectIDs) until the given node is loaded.
		 *
		 * @param node the TreeNode for which the children should be loaded
		 * @param objectIDs a List of all parent JDOObjectIDs which children need to be loaded until the given treeNode is loaded as well.
		 * @param select determines if the given TreeNode should be selected as well
		 */
		protected void loadChildren(final TreeNode node, final List<JDOObjectID> objectIDs, final boolean select)
		{
			getJDOObjectTreeController().addJDOTreeNodesChangedListener(new JDOTreeNodesChangedListener<JDOObjectID, JDOObject, TreeNode>() {
				@Override
				public void onJDOObjectsChanged(JDOTreeNodesChangedEvent<JDOObjectID, TreeNode> changedEvent)
				{
					if (logger.isDebugEnabled()) {
						logger.debug("onJDOObjectsChanged!"); //$NON-NLS-1$
					}
					Set<TreeNode> treeNodes = changedEvent.getParentsToRefresh();
					if (treeNodes != null) {
						if (treeNodes.contains(node)) {
							getJDOObjectTreeController().removeJDOTreeNodesChangedListener(this);
							if (node != null) {
								Object objectID = JDOHelper.getObjectId(node.getJdoObject());
								if (objectID != null) {
									objectIDs.remove(objectID);
									if (objectIDs.isEmpty() && select) {
										setSelection(new StructuredSelection(node), true);
									}
								}
								if (!objectIDs.isEmpty()) {
									List<TreeNode> loadedTreeNodes = changedEvent.getLoadedTreeNodes();
									JDOObjectID nextID = objectIDs.iterator().next();
									TreeNode nextChild = getChild(loadedTreeNodes, nextID);
									loadChildren(nextChild, objectIDs, select);
//									alreadyLoadedNodes.add(nextChild);
								}
							}
						}
						else {
							List<TreeNode> loadedTreeNodes = changedEvent.getLoadedTreeNodes();
							if (!objectIDs.isEmpty()) {
								JDOObjectID nextID = objectIDs.iterator().next();
								TreeNode nextChild = getChild(loadedTreeNodes, nextID);
								loadChildren(nextChild, objectIDs, select);
//								alreadyLoadedNodes.add(nextChild);
							}
						}
					}
				}
			});

			Widget item = getItem(node);
			if (item != null && !alreadyLoadedNodes.contains(node)) {
				if (logger.isDebugEnabled()) {
					logger.debug("send expansion event for treeItem "+item); //$NON-NLS-1$
				}
				sendEventExpandTreeItem((TreeItem) item);
				alreadyLoadedNodes.add(node);
			}
		}
	}

	/**
	 * Handles the successful loading of data to expand.
	 */
	private class LoadListener implements JDOTreeNodesChangedListener<JDOObjectID, JDOObject, TreeNode> {

		private Object root;
		private Object element;
		private int expandLevel;
		private int totalLevel;
		private Set<LoadListener> listenerStack;

		public LoadListener(
				Object root,
				Object element,
				int expandLevel, int totalLevel,
				Set<LoadListener> listenerStack
			)
		{
			this.root = root;
			this.element = element;
			this.expandLevel = expandLevel;
			this.totalLevel = totalLevel;
			this.listenerStack = listenerStack;
			listenerStack.add(this);
		}

		/**
		 * Checks if theres need to recurse further and call {@link ActiveTreeViewer#internalExpand(Object, Object, int, int, Set)} if so.
		 * If this listener was the last waiting for data it will trigger {@link ActiveTreeViewer#superExpandToLevel(Object, int)}
		 * with the original parameters.
		 */
		public void handleLoad(final List<TreeNode> children) {
			getJDOObjectTreeController().removeJDOTreeNodesChangedListener(this);
			if (expandLevel + 1 <= totalLevel) {
				logger.debug(Util.addLeadingChars(element.toString(), element.toString().length() + expandLevel + 1, ' '));
				for (TreeNode childNode : children) {
					getActiveTreeViewer().internalExpand(root, childNode, expandLevel+1, totalLevel, listenerStack);
				}
			}
			listenerStack.remove(this);
			if (listenerStack.isEmpty())
				getActiveTreeViewer().superExpandToLevel(root, totalLevel);
		}

		public void onJDOObjectsChanged(JDOTreeNodesChangedEvent<JDOObjectID, TreeNode> changedEvent) {
			if (changedEvent.getParentsToRefresh().contains(element) || (element instanceof ActiveJDOObjectTreeController)) {
				handleLoad(changedEvent.getLoadedTreeNodes());
			}
		}
	}

}
