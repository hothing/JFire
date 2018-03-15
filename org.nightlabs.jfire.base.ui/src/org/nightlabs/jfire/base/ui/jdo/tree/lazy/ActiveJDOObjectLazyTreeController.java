package org.nightlabs.jfire.base.ui.jdo.tree.lazy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.notification.NotificationAdapterJob;
import org.nightlabs.datastructure.IdentityHashSet;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleListener;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.ui.jdo.notification.JDOLifecycleAdapterJob;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.TreeLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.TreeNodeMultiParentResolver;
import org.nightlabs.jfire.jdo.notification.TreeNodeParentResolver;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * A controller to be used as datasource for JDO tree datastructures.
 * <p>
 * The controller is <em>active</em> as it tracks changes to the structure (new/deleted objects, changed objects)
 * keeps the data up-to-date and uses a callback to notify the user of the changes (see {@link #onJDOObjectsChanged(JDOLazyTreeNodesChangedEvent)}).
 * </p>
 * <p>
 * More details about how to use this class can be found in our wiki:
 * <a href="https://www.jfire.org/modules/phpwiki/index.php/ActiveJDOObjectLazyTreeController">https://www.jfire.org/modules/phpwiki/index.php/ActiveJDOObjectLazyTreeController</a>
 * </p>
 *
 * @author Marco Schulze
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @param <JDOObjectID> The type of the {@link ObjectID} the tree sturcture uses
 * @param <JDOObject> The type of the JDO object used
 * @param <TreeNode> The type of {@link JDOObjectLazyTreeNode} used to hold the data
 */
public abstract class ActiveJDOObjectLazyTreeController<JDOObjectID extends ObjectID, JDOObject, TreeNode extends JDOObjectLazyTreeNode>
{
	private static final Logger logger = Logger.getLogger(ActiveJDOObjectLazyTreeController.class);

	protected abstract Collection<JDOObjectID> retrieveChildObjectIDs(JDOObjectID parentID, ProgressMonitor monitor);

	protected abstract Map<JDOObjectID, Long> retrieveChildCount(Set<JDOObjectID> parentIDs, ProgressMonitor monitor);

	/**
	 * This method is called on a worker thread and must retrieve JDO objects for
	 * the given object-ids from the server. It is called when changes to the structure were tracked.
	 *
	 * @param objectIDs The jdo object ids representing the desired objects.
	 * @param monitor The monitor.
	 * @return Returns the jdo objects that correspond to the requested <code>objectIDs</code>.
	 */
	protected abstract Collection<JDOObject> retrieveJDOObjects(Set<JDOObjectID> objectIDs, ProgressMonitor monitor);

	/**
	 * Creates a subclass of {@link JDOObjectLazyTreeNode} which represents the node object of the active tree.
	 * @return the subclass of {@link JDOObjectLazyTreeNode} for this ActiveJDOObjectLazyTreeController.
	 */
	protected abstract TreeNode createNode();

	/**
	 * This pseudo-node is used to hold the real root elements. Its creation is synchronized via {@link #objectID2TreeNode} - ensuring
	 * it is not created twice.
	 */
	private TreeNode hiddenRootNode = null;

	private Map<JDOObjectID, List<TreeNode>> objectID2TreeNodeList = new HashMap<JDOObjectID, List<TreeNode>>();
	private Object mutex = this; // using 'mutex' prevents errors of using the wrong 'this' in synchronized(this) expressions in inner-classes.

	/**
	 * This method is called by the default implementation of {@link #createJDOLifecycleListenerFilter()}.
	 * It is responsible for creating a {@link TreeNodeParentResolver} for the actual
	 * type of JDOObject.
	 * <p>
	 * Instead of implementing this method, you might want to throw an {@link UnsupportedOperationException}
	 * and override {@link #createTreeNodeMultiParentResolver()} if your objects occur multiple
	 * times in the tree (and thus have multiple parents).
	 * </p>
	 */
	protected abstract TreeNodeParentResolver createTreeNodeParentResolver();

	/**
	 * This method is called by the default implementation of {@link #createJDOLifecycleListenerFilter()}.
	 * It is responsible for creating a {@link TreeNodeMultiParentResolver} for the actual
	 * type of JDOObject.
	 * <p>
	 * The default implementation of this method returns <code>null</code> and
	 * the framework will expect {@link #createTreeNodeParentResolver()} to return an
	 * instance.
	 * </p>
	 */
	protected TreeNodeMultiParentResolver createTreeNodeMultiParentResolver() {
		return null;
	}

	private TreeNodeParentResolver treeNodeParentResolver = null;

	private TreeNodeMultiParentResolver treeNodeMultiParentResolver = null;

//	/**
//	 * Get the {@link TreeNodeParentResolver} for this controller.
//	 * It will be created lazily by a call to {@link #createTreeNodeParentResolver()}.
//	 *
//	 * @return The {@link TreeNodeParentResolver} for this controller.
//	 */
//	public TreeNodeParentResolver getTreeNodeParentResolver()
//	{
//		if (treeNodeParentResolver == null)
//			treeNodeParentResolver = createTreeNodeParentResolver();
//
//		return treeNodeParentResolver;
//	}

	/**
	 * Get the {@link TreeNodeParentResolver} for this controller.
	 * It will be created lazily by a call to {@link #createTreeNodeParentResolver()}.
	 *
	 * @return The {@link TreeNodeParentResolver} for this controller.
	 */
	public TreeNodeParentResolver getTreeNodeParentResolver()
	{
		if (treeNodeParentResolver == null) {
			if (treeNodeMultiParentResolver != null)
				return null;

			treeNodeMultiParentResolver = createTreeNodeMultiParentResolver();
			if (treeNodeMultiParentResolver != null)
				return null;

			treeNodeParentResolver = createTreeNodeParentResolver();

			if (treeNodeParentResolver == null)
				throw new IllegalStateException("Both methods createTreeNodeMultiParentResolver() and createTreeNodeParentResolver() returned null! One of them must return an instance! Check your class: " + getClass().getName()); //$NON-NLS-1$
		}

		return treeNodeParentResolver;
	}

	public TreeNodeMultiParentResolver getTreeNodeMultiParentResolver()
	{
		if (treeNodeMultiParentResolver == null) {
			treeNodeMultiParentResolver = createTreeNodeMultiParentResolver();

			if (treeNodeMultiParentResolver == null) {
				if (getTreeNodeParentResolver() == null)
					throw new IllegalStateException("How the hell can there be no TreeNodeParentResolver and no TreeNodeMultiParentResolver?! Check your class: " + getClass().getName()); //$NON-NLS-1$
			}
		}

		return treeNodeMultiParentResolver;
	}

	/**
	 * Get the {@link Class} (type) of the JDO object this controller is for.
	 * Should be the same this controller was typed with.
	 *
	 * @return The {@link Class} (type) of the JDO object this controller is for.
	 */
	protected abstract Class<? extends JDOObject> getJDOObjectClass();

	protected Set<Class<? extends JDOObject>> getJDOObjectClasses()
	{
		Set<Class<? extends JDOObject>> c = new HashSet<Class<? extends JDOObject>>(1);
		c.add(getJDOObjectClass());
		return c;
	}

	/**
	 * Creates an {@link IJDOLifecycleListenerFilter} that will be used to
	 * track new objects that are children of one of the objects referenced by
	 * the given parentObjectIDs.
	 * By default this will create a {@link TreeLifecycleListenerFilter}
	 * for {@link JDOLifecycleState#NEW}.
	 *
	 * @param parentObjectIDs The {@link ObjectID}s of the parent objects new children should be tracked for.
	 * @return A new {@link IJDOLifecycleListenerFilter}
	 */
	protected IJDOLifecycleListenerFilter createJDOLifecycleListenerFilter(Set<? extends ObjectID> parentObjectIDs)
	{
//		return new TreeLifecycleListenerFilter(
//				getJDOObjectClass(), true,
//				parentObjectIDs, getTreeNodeParentResolver(),
//				new JDOLifecycleState[] { JDOLifecycleState.NEW });

		Set<Class<? extends JDOObject>> classes = getJDOObjectClasses();
		if (getTreeNodeMultiParentResolver() == null) {
			return new TreeLifecycleListenerFilter(
					classes.toArray(new Class[classes.size()]), true,
					parentObjectIDs, getTreeNodeParentResolver(),
					new JDOLifecycleState[] { JDOLifecycleState.NEW });
		}
		else {
			return new TreeLifecycleListenerFilter(
					classes.toArray(new Class[classes.size()]), true,
					parentObjectIDs, getTreeNodeMultiParentResolver(),
					new JDOLifecycleState[] { JDOLifecycleState.NEW });
		}
	}

	/**
	 * Creates a {@link JDOLifecycleListener} with the {@link IJDOLifecycleListenerFilter} obtained
	 * by {@link #createJDOLifecycleListenerFilter(Set)}.
	 *
	 * @param parentObjectIDs The {@link ObjectID}s of the parent objects new children should be tracked for.
	 * @return  A new {@link JDOLifecycleListener}
	 */
	protected JDOLifecycleListener createJDOLifecycleListener(Set<? extends ObjectID> parentObjectIDs)
	{
		IJDOLifecycleListenerFilter filter = createJDOLifecycleListenerFilter(parentObjectIDs);
		return new LifecycleListener(filter);
	}

	/**
	 * This will be called when a change in the tree structure was tracked and after the changes
	 * were retrieved. The {@link JDOLazyTreeNodesChangedEvent} contains references to the
	 * {@link TreeNode}s that need update or were removed.
	 * <p>
	 * This method is called on the UI thread.
	 * </p>
	 * <p>
	 * You can choose whether you want to override this method or register listeners via {@link #addJDOLazyTreeNodesChangedListener(JDOLazyTreeNodesChangedListener)}.
	 * In most use cases, simply overriding this method is easier and less code.
	 * </p>
	 *
	 * @param changedEvent The {@link JDOLazyTreeNodesChangedEvent} containing references to changed/new and deleted {@link TreeNode}s
	 */
	protected void onJDOObjectsChanged(JDOLazyTreeNodesChangedEvent<JDOObjectID, TreeNode> changedEvent)
	{
	}

	private NotificationListener changeListener;

//	@SuppressWarnings("unchecked") //$NON-NLS-1$
	protected void handleChangeNotification(NotificationEvent notificationEvent, ProgressMonitor monitor) {
		synchronized (mutex) {
			if (hiddenRootNode == null)
				hiddenRootNode = createNode();

			Collection<DirtyObjectID> dirtyObjectIDs = notificationEvent.getSubjects();
			final Set<TreeNode> parentsToRefresh = new HashSet<TreeNode>();
			final Map<JDOObjectID, List<TreeNode>> dirtyNodes = new HashMap<JDOObjectID, List<TreeNode>>();
			final Map<JDOObjectID, List<TreeNode>> deletedNodes = new HashMap<JDOObjectID, List<TreeNode>>();
			iterateDirtyObjectIDs: for (DirtyObjectID objectID : dirtyObjectIDs) {
				List<TreeNode> dirtyNodeList = objectID2TreeNodeList.get(objectID.getObjectID());
				if (dirtyNodeList == null || dirtyNodeList.isEmpty())
					continue iterateDirtyObjectIDs;

				for (TreeNode dirtyNode : dirtyNodeList) {
					@SuppressWarnings("unchecked")
					JDOObjectID jdoObjectID = (JDOObjectID) objectID.getObjectID();
					switch (objectID.getLifecycleState()) {
						case DIRTY: {
							List<TreeNode> nodeList = dirtyNodes.get(jdoObjectID);
							if (nodeList == null) {
								nodeList = new ArrayList<TreeNode>();
								dirtyNodes.put(jdoObjectID, nodeList);
							}
							// TODO handle reparenting (change of parent(s))!
							nodeList.add(dirtyNode);
						}
						break;
						case DELETED: {
							List<TreeNode> nodeList = deletedNodes.get(jdoObjectID);
							if (nodeList == null) {
								nodeList = new ArrayList<TreeNode>();
								deletedNodes.put(jdoObjectID, nodeList);
							}
							nodeList.add(dirtyNode);
						}
						break;
						case NEW: break; // do nothing for new objects
					}
				}
			}

			final Map<JDOObjectID, List<TreeNode>> ignoredNodes = new HashMap<JDOObjectID, List<TreeNode>>();
			ignoredNodes.putAll(dirtyNodes);
			Collection<JDOObject> retrievedObjects = retrieveJDOObjects(dirtyNodes.keySet(), monitor);
			for (JDOObject retrievedObject : retrievedObjects) {
				JDOObjectID retrievedID = (JDOObjectID) JDOHelper.getObjectId(retrievedObject);
				ignoredNodes.remove(retrievedID);
				List<TreeNode> nodeList = dirtyNodes.get(retrievedID);
				for (TreeNode node : nodeList) {
					node.setJdoObject(retrievedObject);
				}
			}

			for (Entry<JDOObjectID, List<TreeNode>> deletedEntry : deletedNodes.entrySet()) {
				Object jdoObject = null;
				for (TreeNode n : deletedEntry.getValue()) {
					n.setDeleted(true);

					// getJdoObject() might return null in a lazy tree!
					if (jdoObject == null)
						jdoObject = n.getJdoObject();
				}
				if (jdoObject == null) {
					// TODO this is not very efficient - can we do it better? Or do we not care, because deletions don't happen so often.
					// so often?
					Collection<JDOObject> objects = retrieveJDOObjects(Collections.singleton(deletedEntry.getKey()), new NullProgressMonitor()); // TODO handle monitor correctly!
					if (objects != null && !objects.isEmpty())
						jdoObject = objects.iterator().next();
				}
				if (jdoObject == null) {
					logger.warn("jdoObject is null! " + deletedEntry.getKey());
				}

				if (getTreeNodeMultiParentResolver() != null) {
					Collection<JDOObjectID> parentIDs = (Collection<JDOObjectID>) getTreeNodeMultiParentResolver().getParentObjectIDs(jdoObject);
					if (parentIDs != null) {
						for (JDOObjectID parentID : parentIDs) {
							List<TreeNode> parentNodes = objectID2TreeNodeList.get(parentID);
							if (parentNodes != null) {
								for (TreeNode parentNode : parentNodes) {
									for (TreeNode childNode : deletedEntry.getValue()) {
										parentNode.removeChildNode(childNode);
										parentsToRefresh.add(parentNode == hiddenRootNode ? null : parentNode);
									}
								}
							}
						}
					}
				}
				else if (getTreeNodeParentResolver() != null) {
					JDOObjectID parentID = (JDOObjectID) getTreeNodeParentResolver().getParentObjectID(jdoObject);
					List<TreeNode> parentNodes = objectID2TreeNodeList.get(parentID);
					if (parentNodes != null) {
						for (TreeNode parentNode : parentNodes) {
							for (TreeNode childNode : deletedEntry.getValue()) {
								parentNode.removeChildNode(childNode);
								parentsToRefresh.add(parentNode == hiddenRootNode ? null : parentNode);
							}
						}
					}
				}

				objectID2TreeNodeList.remove(deletedEntry.getKey());
			}

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					ArrayList<TreeNode> loadedNodes = new ArrayList<TreeNode>();
					for (List<TreeNode> nodeList : dirtyNodes.values()) {
						loadedNodes.addAll(nodeList);
					}

					fireJDOObjectsChangedEvent(new JDOLazyTreeNodesChangedEvent<JDOObjectID, TreeNode>(
							ActiveJDOObjectLazyTreeController.this,
							parentsToRefresh,
							loadedNodes,
							ignoredNodes,
							deletedNodes
					)
					);
				}
			});
		} // synchronized (mutex) {
	}

	protected class ChangeListener extends NotificationAdapterJob {

		public ChangeListener(String name) {
			super(name);
		}

		public void notify(NotificationEvent notificationEvent) {
			handleChangeNotification(notificationEvent, getProgressMonitor());
		}
	};

	protected void registerJDOLifecycleListener()
	{
		if (lifecycleListener != null) {
			if (logger.isDebugEnabled())
				logger.debug("registerJDOLifecycleListeners: removing old listener"); //$NON-NLS-1$

			JDOLifecycleManager.sharedInstance().removeLifecycleListener(lifecycleListener);
			lifecycleListener = null;
		}

		Set<JDOObjectID> activeParentObjectIDs = getActiveParentObjectIDs();

		if (logger.isDebugEnabled()) {
			logger.debug("registerJDOLifecycleListeners: creating and registering JDOLifecycleListener for " + activeParentObjectIDs.size() + " activeParentObjectIDs"); //$NON-NLS-1$ //$NON-NLS-2$
			if (logger.isTraceEnabled()) {
				for (JDOObjectID jdoObjectID : activeParentObjectIDs)
					logger.trace("  - " + jdoObjectID); //$NON-NLS-1$
			}
		}

		lifecycleListener = createJDOLifecycleListener(activeParentObjectIDs);
		JDOLifecycleManager.sharedInstance().addLifecycleListener(lifecycleListener);
	}

	protected void registerChangeListener() {
		if (changeListener == null) {
			changeListener = new ChangeListener("Loading changes");
			JDOLifecycleManager.sharedInstance().addNotificationListener(getJDOObjectClasses(), changeListener);
		}
	}

	protected void unregisterChangeListener() {
		if (changeListener != null) {
			JDOLifecycleManager.sharedInstance().removeNotificationListener(getJDOObjectClasses(), changeListener);
			changeListener = null;
		}
	}

	public void close()
	{
		if (lifecycleListener != null) {
			JDOLifecycleManager.sharedInstance().removeLifecycleListener(lifecycleListener);
			lifecycleListener = null;
		}
		unregisterChangeListener();
	}

	protected void addTreeNode(TreeNode treeNode)
	{
		@SuppressWarnings("unchecked")
		JDOObjectID objectID = (JDOObjectID) treeNode.getJdoObjectID();
		List<TreeNode> nl = objectID2TreeNodeList.get(objectID);
		if (nl == null) {
			nl = new ArrayList<TreeNode>();
			objectID2TreeNodeList.put(objectID, nl);
		}
		nl.add(treeNode);
	}

	private JDOLifecycleListener lifecycleListener = null;
	protected class LifecycleListener extends JDOLifecycleAdapterJob
	{
		private IJDOLifecycleListenerFilter filter;

		public LifecycleListener(IJDOLifecycleListenerFilter filter)
		{
			this.filter = filter;
		}

		public IJDOLifecycleListenerFilter getJDOLifecycleListenerFilter()
		{
			return filter;
		}

		public void notify(JDOLifecycleEvent event)
		{
			if (logger.isDebugEnabled())
				logger.debug("LifecycleListener#notify: enter"); //$NON-NLS-1$

			synchronized (mutex) {
				if (hiddenRootNode == null)
					hiddenRootNode = createNode();

				Set<JDOObjectID> objectIDs = new HashSet<JDOObjectID>(event.getDirtyObjectIDs().size());
				final Set<TreeNode> parentsToRefresh = new HashSet<TreeNode>();
				final List<TreeNode> loadedTreeNodes = new ArrayList<TreeNode>();

				if (logger.isDebugEnabled())
					logger.debug("LifecycleListener#notify: got notification with " + event.getDirtyObjectIDs().size() + " DirtyObjectIDs"); //$NON-NLS-1$ //$NON-NLS-2$

				for (DirtyObjectID dirtyObjectID : event.getDirtyObjectIDs()) {
					objectIDs.add((JDOObjectID) dirtyObjectID.getObjectID());

					if (logger.isDebugEnabled())
						logger.debug("LifecycleListener#notify:   - " + dirtyObjectID); //$NON-NLS-1$
				}

				Collection<JDOObject> objects = retrieveJDOObjects(objectIDs, getProgressMonitor());
				iterateObjects: for (JDOObject object : objects) {
					@SuppressWarnings("unchecked")
					JDOObjectID objectID = (JDOObjectID) JDOHelper.getObjectId(object);
					if (objectID == null)
						throw new IllegalStateException("JDOHelper.getObjectId(object) returned null! " + object); //$NON-NLS-1$

					// Find all 'parentObjectIDs' of the current 'object'. That's how things are in the server and should be here in the tree.
					Collection<ObjectID> parentObjectIDs;
					if (getTreeNodeMultiParentResolver() != null) {
						parentObjectIDs = getTreeNodeMultiParentResolver().getParentObjectIDs(object);
						if (parentObjectIDs == null || parentObjectIDs.isEmpty())
							parentObjectIDs = Collections.singletonList(null);
					}
					else
						parentObjectIDs = Collections.singletonList(getTreeNodeParentResolver().getParentObjectID(object));

					// Find all local tree nodes that reference parents which are no parents anymore and remove those nodes.
					// TODO implement!

					// Find all local tree nodes that need update and create missing local nodes.
					List<TreeNode> nodeList = objectID2TreeNodeList.get(objectID);
					Map<JDOObjectID, List<TreeNode>> parentID2TreeNodeList = new HashMap<JDOObjectID, List<TreeNode>>();
					if (nodeList != null) {
						for (TreeNode treeNode : nodeList) {
							@SuppressWarnings("unchecked")
							TreeNode parentNode = (TreeNode) treeNode.getParent();
							@SuppressWarnings("unchecked")
							JDOObjectID parentID = (JDOObjectID) parentNode.getJdoObjectID();
							List<TreeNode> nl = parentID2TreeNodeList.get(parentID);
							if (nl == null) {
								nl = new ArrayList<TreeNode>();
								parentID2TreeNodeList.put(parentID, nl);
							}
							nl.add(treeNode);
						}
					}

					for (ObjectID parentID : parentObjectIDs) {
						List<TreeNode> treeNodes = parentID2TreeNodeList.get(parentID); // These are *not* the parent-TreeNodes, but the nodes for the current object in the scope of the current parentID.
						List<TreeNode> parentNodes = objectID2TreeNodeList.get(parentID); // These are the parent-TreeNodes.
						if (parentNodes == null) {
							logger.warn("LifecycleListener#notify: ignoring new object, because its parent is unknown! objectID=\"" + objectID + "\" parentID=\"" + parentID + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							continue;
						}

						if (treeNodes == null || treeNodes.isEmpty()) {
							for (TreeNode parentNode : parentNodes) {
								TreeNode treeNode = createNode();
								treeNode.setActiveJDOObjectLazyTreeController(ActiveJDOObjectLazyTreeController.this);
								treeNode.setParent(parentNode);
								treeNode.setJdoObject(object);
								addTreeNode(treeNode);
								if (!parentNode.addChildNode(treeNode)) {
									long childCount = parentNode.getChildNodeCount();
									if (childCount >= 0) { // do not write the child count, if it has not yet been queried - there might be more than we know now.
										++childCount;
										parentNode.setChildNodeCount(childCount);
									}
								}

								parentsToRefresh.add(parentNode);
								loadedTreeNodes.add(treeNode);
							}
						}
					}

//						// Find out all parentNodes
//						List<TreeNode> parentNodes;
//						boolean ignoreNodeBecauseParentUnknown = false;
//	//					ObjectID parentID = getTreeNodeParentResolver().getParentObjectID(object);
//						if (parentID == null) {
//	//						parentNode = null;
//							parentNodes = Collections.singletonList(hiddenRootNode);
//						}
//						else {
//							parentNodes = objectID2TreeNodeList.get(parentID);
//							if (parentNodes == null)
//								ignoreNodeBecauseParentUnknown = true;
//						}
//
//						if (ignoreNodeBecauseParentUnknown) {
//							logger.warn("LifecycleListener#notify: ignoring new object, because its parent is unknown! objectID=\"" + JDOHelper.getObjectId(object) + "\" parentID=\"" + parentID + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//							continue;
//						}
//
//						// Remove
//
//						for (TreeNode parentNode : parentNodes) {
//							JDOObjectID objectID = (JDOObjectID) JDOHelper.getObjectId(object);
//							TreeNode tn = null;
//							List<TreeNode> tnList = objectID2TreeNodeList.get(objectID);
//							if (tnList != null) {
//								for (TreeNode treeNode : tnList) {
//									if (parentNode.equals(treeNode.getParent())) {
//										tn = treeNode;
//										break;
//									}
//								}
//							}
////							tn = objectID2TreeNode.get(objectID);
//
//							if (logger.isDebugEnabled())
//								logger.debug("LifecycleListener#notify: treeNodeAlreadyExists=\"" + (tn != null) + "\" objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//
//							if (tn != null && parentNode != tn.getParent()) { // parent changed, completely replace!
//								if (logger.isDebugEnabled())
//									logger.debug("LifecycleListener#notify: treeNode's parent changed! newParent=\"" + parentNode + "\" oldParent=\"" + tn.getParent() + "\" objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
//
//								TreeNode p = (TreeNode) tn.getParent();
//								parentsToRefresh.add(p == hiddenRootNode ? null : p);
//								if (p == null) {
//									throw new IllegalStateException("How the hell can TreeNode.getParent() return null?! If it is a root-node, it should have hiddenRootNode as its parent-node!"); //$NON-NLS-1$
//									//							if (rootElements != null) {
//									//								if (logger.isDebugEnabled())
//									//									logger.debug("LifecycleListener#notify: removing TreeNode from rootElements (for replacement)! objectID=\"" + objectID + "\"");
//									//
//									//								if (!rootElements.remove(p))
//									//									logger.warn("LifecycleListener#notify: removing TreeNode from rootElements (for replacement) failed - the TreeNode was not found in the rootElements! objectID=\"" + objectID + "\"");
//									//							}
//									//							else {
//									//								if (logger.isDebugEnabled())
//									//									logger.debug("LifecycleListener#notify: rootElements is null! Cannot remove old TreeNode! objectID=\"" + objectID + "\"");
//									//							}
//								}
//								else
//									p.removeChildNode(tn);
//
//								objectID2TreeNode.remove(objectID);
//								tn = null;
//							}
//
//							if (tn == null) {
//								if (logger.isDebugEnabled())
//									logger.debug("LifecycleListener#notify: creating TreeNode for objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$
//
//								tn = createNode();
//								tn.setActiveJDOObjectLazyTreeController(ActiveJDOObjectLazyTreeController.this);
//							}
//							else {
//								if (logger.isDebugEnabled())
//									logger.debug("LifecycleListener#notify: reusing existing TreeNode for objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$
//							}
//
//							tn.setJdoObject(object);
//							if (tn.getParent() != parentNode) {
//								if (logger.isDebugEnabled())
//									logger.debug("LifecycleListener#notify: tn.getParent() != parentNode for objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$
//
//								tn.setParent(parentNode);
//								//						if (parentNode != null) // should never be null now - we have introduced hiddenRootNode!
//								parentNode.addChildNode(tn);
//							}
//							else {
//								if (logger.isDebugEnabled())
//									logger.debug("LifecycleListener#notify: tn.getParent() == parentNode for objectID=\"" + objectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$
//							}
//
//							parentsToRefresh.add(parentNode == hiddenRootNode ? null : parentNode);
//							objectID2TreeNode.put(objectID, tn);
//							loadedTreeNodes.add(tn);
//						}
				}

				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						fireJDOObjectsChangedEvent(new JDOLazyTreeNodesChangedEvent<JDOObjectID, TreeNode>(this, parentsToRefresh, loadedTreeNodes));
					}
				});
			}
		} // synchronized (mutex) {
	}

	/**
	 * These objects will be watched for new children to pop up. May contain <code>null</code> for root-elements
	 * (which is very likely).
	 */
	private Set<JDOObjectID> _activeParentObjectIDs = new HashSet<JDOObjectID>();
	private Set<JDOObjectID> _activeParentObjectIDs_ro = null;

	protected Set<JDOObjectID> getActiveParentObjectIDs() {
		synchronized (_activeParentObjectIDs) {
			if (_activeParentObjectIDs_ro == null)
				_activeParentObjectIDs_ro = Collections.unmodifiableSet(new HashSet<JDOObjectID>(_activeParentObjectIDs));

			return _activeParentObjectIDs_ro;
		}
	}

	/**
	 * @param jdoObjectID The OID of the parent-object that should be surveilled for newly created children.
	 * @param autoReregister If <code>true</code>, the method {@link #registerJDOLifecycleListener()} will automatically be called
	 *		if necessary. If <code>false</code>, this method triggered, even if a truly new <code>jdoObjectID</code> has been added.
	 *
	 * @return <code>false</code>, if the given <code>jdoObjectID</code> was already previously surveilled; <code>true</code> if it
	 *		has been added.
	 */
	protected boolean addActiveParentObjectID(JDOObjectID jdoObjectID, boolean autoReregister) {
		synchronized (_activeParentObjectIDs) {
			if (_activeParentObjectIDs.contains(jdoObjectID))
				return false;

			_activeParentObjectIDs.add(jdoObjectID);
			_activeParentObjectIDs_ro = null;
		}

		if (autoReregister)
			registerJDOLifecycleListener();

		return true;
	}

	/**
	 * Get the number of either root-nodes, if <code>parent == null</code>, or child-nodes
	 * of the specified parent. Alternatively, this method can return <code>-1</code>,
	 * if the data is not yet available. In this case, a new {@link Job} will be spawned to load the data.
	 *
	 * @param _parent the parent node or <code>null</code>.
	 */
	public long getNodeCount(TreeNode _parent)
	{
		if (_parent != null && _parent == hiddenRootNode)
			throw new IllegalArgumentException("Why the hell is the hiddenRootNode passed to this method?! If this ever happens - maybe we should map it to null here?"); //$NON-NLS-1$

		long start = 0;
		if (logger.isDebugEnabled()) {
			logger.debug("getNodeCount: entered for parentTreeNode.jdoObjectID=\"" + (_parent == null ? null : _parent.getJdoObjectID()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			start = System.currentTimeMillis();
		}

		synchronized (mutex) {
			if (hiddenRootNode == null)
				hiddenRootNode = createNode();
		}

		long nodeCount = -1;

		registerChangeListener();

		if (_parent == null) {
			_parent = hiddenRootNode;
		}
//		final TreeNode parent = _parent;

		@SuppressWarnings("unchecked")
		JDOObjectID parentJdoObjectID = (JDOObjectID)_parent.getJdoObjectID();
		addActiveParentObjectID(parentJdoObjectID, true);
		nodeCount = _parent.getChildNodeCount();

		if (nodeCount >= 0) {
			if (logger.isDebugEnabled()) {
				logger.debug("getNodeCount: returning previously loaded count: " + nodeCount); //$NON-NLS-1$
				long duration = System.currentTimeMillis() - start;
				logger.debug("getNodeCount() took "+duration+" ms!");
			}

			return nodeCount;
		}

		synchronized(treeNodesWaitingForChildCountRetrieval) {
			// enqueue in the todo-list
			treeNodesWaitingForChildCountRetrieval.add(_parent);

			// and launch a new job, if there is none active (don't do this always in order to prevent millions of jobs to be queued).
			if (jobChildCountRetrieval != null) {
				if (logger.isDebugEnabled())
					logger.debug("getNodeCount: returning -1 but not spawning Job, since there is already one."); //$NON-NLS-1$
			}
			else {
				if (logger.isDebugEnabled())
					logger.debug("getNodeCount: returning -1 and spawning Job."); //$NON-NLS-1$

				Job job = new Job(Messages.getString("org.nightlabs.jfire.base.ui.jdo.tree.lazy.ActiveJDOObjectLazyTreeController.job.loadingChildCount")) { //$NON-NLS-1$
					@Override
					protected IStatus run(ProgressMonitor monitor)
					{
						if (logger.isDebugEnabled())
							logger.debug("getNodeCount.Job#run: entered"); //$NON-NLS-1$

						TreeNode _currentRootNode;
						synchronized (mutex) {
							if (hiddenRootNode == null)
								hiddenRootNode = createNode();

							_currentRootNode = hiddenRootNode;
						}
						final TreeNode currentRootNode = _currentRootNode;

						// Give it some time to collect objects in the treeNodesWaitingForChildCountRetrieval
						// before we start processing them.
						try { Thread.sleep(500); } catch (InterruptedException x) { } // ignore InterruptedException

						Set<TreeNode> parentTreeNodes;
						synchronized(treeNodesWaitingForChildCountRetrieval) {
							jobChildCountRetrieval = null;
							parentTreeNodes = new HashSet<TreeNode>(treeNodesWaitingForChildCountRetrieval);
							treeNodesWaitingForChildCountRetrieval.clear();
						}
						Map<JDOObjectID, List<TreeNode>> parentObjectID2ParentTreeNodeList = new HashMap<JDOObjectID, List<TreeNode>>(parentTreeNodes.size());
						boolean retrieveRootCount = false;
						final Set<TreeNode> parentsToRefresh = new HashSet<TreeNode>();

						for (TreeNode treeNode : parentTreeNodes) {
							// Check, if it is still necessary - the number of children might have already been fetched.
							// Thus, we should prevent calling the retrieve methods twice.
							if (treeNode.getChildNodeCount() >= 0) {
								parentsToRefresh.add(treeNode == hiddenRootNode ? null : treeNode); // still force refresh - even though we prevent loading.
								continue;
							}

							if (treeNode == hiddenRootNode) {
								retrieveRootCount = true;
							}
							else {
								@SuppressWarnings("unchecked")
								JDOObjectID parentJDOID = (JDOObjectID) treeNode.getJdoObjectID();
								List<TreeNode> nl = parentObjectID2ParentTreeNodeList.get(parentJDOID);
								if (nl == null) {
									nl = new ArrayList<TreeNode>();
									parentObjectID2ParentTreeNodeList.put(parentJDOID, nl);
								}
								nl.add(treeNode);
							}
						}

						if (retrieveRootCount) {
							Set<JDOObjectID> s = new HashSet<JDOObjectID>(1);
							s.add(null);
							Map<JDOObjectID, Long> parentOID2childCount = retrieveChildCount(parentTreeNodes,
									s, new SubProgressMonitor(monitor, 50) // TODO correct % numbers!
							);
							Long count = parentOID2childCount.isEmpty() ? 0 : parentOID2childCount.get(null);
							if (count == null)
								throw new IllegalStateException("retrieveChildCount(...) returned a null value (count) in its result map! Check your implementation in class " + ActiveJDOObjectLazyTreeController.this.getClass().getName() + "!!!"); //$NON-NLS-1$ //$NON-NLS-2$

							hiddenRootNode.setChildNodeCount(count);
							parentsToRefresh.add(null);
						}

						if (!parentObjectID2ParentTreeNodeList.isEmpty()) {
							Map<JDOObjectID, Long> parentOID2childCount = retrieveChildCount(parentTreeNodes,
									new HashSet<JDOObjectID>(parentObjectID2ParentTreeNodeList.keySet()), // We need to pass a new HashSet here, because the keySet is not serializable which is often necessary for retrieveChildCount implementations. Marco.
									new SubProgressMonitor(monitor, 50) // TODO correct % numbers!
							);

							synchronized (mutex) {
								if (currentRootNode != hiddenRootNode) {
									logger.debug("getNodeCount.job#run: clear() called before job started - cancelling expired job."); //$NON-NLS-1$
									return Status.CANCEL_STATUS;
								}

								for (Map.Entry<JDOObjectID, Long> me : parentOID2childCount.entrySet()) {
									JDOObjectID parentJDOID = me.getKey();
									if (parentJDOID == null)
										throw new IllegalStateException("retrieveChildCount(...) returned a null key (parent-OID) in its result map even though no null element (parent-OID) was passed to it! Check your implementation in class " + ActiveJDOObjectLazyTreeController.this.getClass().getName() + "!!!"); //$NON-NLS-1$ //$NON-NLS-2$

									Long childCount = me.getValue();
									if (childCount == null)
										throw new IllegalStateException("retrieveChildCount(...) returned a null value (count) in its result map! Check your implementation in class " + ActiveJDOObjectLazyTreeController.this.getClass().getName() + "!!!"); //$NON-NLS-1$ //$NON-NLS-2$

									List<TreeNode> pnl = parentObjectID2ParentTreeNodeList.get(parentJDOID);
									if (pnl == null)
										throw new IllegalStateException("Cannot find any TreeNode for parentJDOID: " + parentJDOID); //$NON-NLS-1$

									for (TreeNode parentTreeNode : pnl) {
										parentTreeNode.setChildNodeCount(childCount.longValue());
										parentsToRefresh.add(parentTreeNode);
									}
								}

							} // synchronized (mutex) {
						}

						Display.getDefault().asyncExec(new Runnable()
						{
							public void run()
							{
								fireJDOObjectsChangedEvent(new JDOLazyTreeNodesChangedEvent<JDOObjectID, TreeNode>(this, parentsToRefresh));
							}
						});

						return Status.OK_STATUS;
					}
				};
				jobChildCountRetrieval = job;
				job.setRule(schedulingRule_jobChildCountRetrieval);
				job.setPriority(Job.SHORT);
				job.schedule();
			}
		}
		return -1;
	}

	private Job jobChildCountRetrieval = null;
	private Set<TreeNode> treeNodesWaitingForChildCountRetrieval = new HashSet<TreeNode>();

	private ISchedulingRule schedulingRule_jobChildCountRetrieval = new SelfConflictingSchedulingRule();
	private ISchedulingRule schedulingRule_jobChildObjectIDRetrieval = new SelfConflictingSchedulingRule();
	private ISchedulingRule schedulingRule_jobObjectRetrieval = new SelfConflictingSchedulingRule();

	private static class SelfConflictingSchedulingRule implements ISchedulingRule
	{
		@Override
		public boolean contains(ISchedulingRule rule) {
			return this == rule;
		}
		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return this == rule;
		}
	};

	private Job jobObjectRetrieval = null;
	private IdentityHashSet<TreeNode> treeNodesWaitingForObjectRetrieval = new IdentityHashSet<TreeNode>();

	private IdentityHashSet<TreeNode> treeNodesWaitingForChildObjectIDRetrieval = new IdentityHashSet<TreeNode>();

	/**
	 * This method returns either a root-node, if <code>parent == null</code> or a child of the given
	 * <code>parent</code> (if non-<code>null</code>). Alternatively, this method can return <code>null</code>,
	 * if the data is not yet available. In this case, a new {@link Job} will be spawned to load the data.
	 * <p>
	 * If a {@link TreeNode} is returned, it might be partially loaded (only the ID)!!! In this case, another
	 * Job is spawned and the real object loaded as well.
	 * </p>
	 *
	 * @param _parent the parent node or <code>null</code>.
	 * @return a {@link TreeNode} or <code>null</code>, if data is not yet ready.
	 */
	public TreeNode getNode(TreeNode _parent, int index)
	{
		if (_parent != null && _parent == hiddenRootNode)
			throw new IllegalArgumentException("Why the hell is the hiddenRootNode passed to this method?! If this ever happens - maybe we should map it to null here?"); //$NON-NLS-1$

		long start = 0;
		if (logger.isDebugEnabled()) {
			logger.debug("getNode: entered for parentTreeNode.jdoObjectID=\"" + (_parent == null ? null : _parent.getJdoObjectID()) + "\" index=" + index); //$NON-NLS-1$ //$NON-NLS-2$
			start = System.currentTimeMillis();
		}

		TreeNode _currentRootNode;
		synchronized (mutex) {
			if (hiddenRootNode == null)
				hiddenRootNode = createNode();

			_currentRootNode = hiddenRootNode;
		}
		final TreeNode currentRootNode = _currentRootNode;

		TreeNode node = null;

		registerChangeListener();

		if (_parent == null) {
			_parent = hiddenRootNode;
		}

		if (_parent.isDeleted())
			return null;

		addActiveParentObjectID(
				(JDOObjectID)_parent.getJdoObjectID(), // this is null in case of hiddenRootNode
				true
		);
		List<TreeNode> childNodes = _parent.getChildNodes();

		if (childNodes != null) {
			if (index >= childNodes.size()) {
				logger.warn("getNode: index >= childNodes.size() :: " + index + " >= " + childNodes.size(), new Exception("StackTrace")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				_parent.setChildNodes(null);
				_parent.setChildNodeCount(-1);
			}
			else
				node = childNodes.get(index);
		}

		if (node != null && node.getJdoObject() != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("getNode: returning previously loaded complete child-node."); //$NON-NLS-1$
				long duration = System.currentTimeMillis() - start;
				logger.debug("getNode() took "+duration+" ms!");
			}

			return node;
		}

		if (node == null) {
			final TreeNode parent = _parent;
			synchronized (mutex) {
				if (!treeNodesWaitingForChildObjectIDRetrieval.add(parent)) {
					if (logger.isDebugEnabled())
						logger.debug("getNode: returning null but not spawning Job since there is already a job running/pending for this node."); //$NON-NLS-1$

					return null;
				}
			}

			if (logger.isDebugEnabled())
				logger.debug("getNode: returning null and spawning Job."); //$NON-NLS-1$

			Job job1 = new Job(Messages.getString("org.nightlabs.jfire.base.ui.jdo.tree.lazy.ActiveJDOObjectLazyTreeController.job.loadChildren")) { //$NON-NLS-1$
				@SuppressWarnings("unchecked")
				@Override
				protected IStatus run(ProgressMonitor monitor)
				{
					try {
						if (logger.isDebugEnabled())
							logger.debug("getNode.job1#run: entered for parentTreeNode.jdoObjectID=\"" + parent.getJdoObjectID() + "\""); //$NON-NLS-1$ //$NON-NLS-2$

						if (currentRootNode != hiddenRootNode) {
							logger.debug("getNode.job1#run[1]: clear() called before job started - cancelling expired job."); //$NON-NLS-1$
							return Status.CANCEL_STATUS;
						}

						final Set<TreeNode> parentsToRefresh = new HashSet<TreeNode>();
						parentsToRefresh.add(parent == hiddenRootNode ? null : parent);
						List<TreeNode> loadedNodes = null;

						// In the mean-time, the data for this parentTreeNode might already be retrieved - check it again (prevent multiple
						// calls to the retrieveChildObjectIDs(...) method for the same parent).
						if (parent.getChildNodes() != null) {
							if (logger.isDebugEnabled())
								logger.debug("getNode.job1#run: children already loaded for parentTreeNode.jdoObjectID=\"" + parent.getJdoObjectID() + "\". Skipping!"); //$NON-NLS-1$ //$NON-NLS-2$
						}
						else {
							if (logger.isDebugEnabled())
								logger.debug("getNode.job1#run: retrieving children for parentTreeNode.jdoObjectID=\"" + parent.getJdoObjectID() + "\""); //$NON-NLS-1$ //$NON-NLS-2$

							Collection<JDOObjectID> jdoObjectIDs = retrieveChildObjectIDs(parent, monitor); // Since 2010-02-19. Kai.
							if (jdoObjectIDs == null)
								throw new IllegalStateException("Your implementation of retrieveChildObjectIDs(...) returned null! The error is probably in class " + ActiveJDOObjectLazyTreeController.this.getClass().getName()); //$NON-NLS-1$


							loadedNodes = new ArrayList<TreeNode>(jdoObjectIDs.size());

							synchronized (mutex) {
								if (currentRootNode != hiddenRootNode) {
									logger.debug("getNode.job1#run[2]: clear() called before job started - cancelling expired job."); //$NON-NLS-1$
									return Status.CANCEL_STATUS;
								}

								for (JDOObjectID jdoObjectID : jdoObjectIDs) {
	//								TreeNode tn = objectID2TreeNode.get(jdoObjectID);
	//								if (tn != null && parent != tn.getParent()) { // parent changed, completely replace!
	//									if (logger.isDebugEnabled())
	//										logger.debug("getNode.job1#run: treeNode's parent changed! objectID=\"" + jdoObjectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$
	//
	//									TreeNode p = (TreeNode) tn.getParent();
	//									parentsToRefresh.add(p == hiddenRootNode ? null : p);
	//									if (p != null)
	//										p.removeChildNode(tn);
	//
	//									objectID2TreeNode.remove(jdoObjectID);
	//									tn = null;
	//								}
	//
	//								if (tn == null) {
	//									if (logger.isTraceEnabled())
	//										logger.trace("getNode.job1#run: creating node for objectID=\"" + jdoObjectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$
	//
	//									tn = createNode();
	//									tn.setActiveJDOObjectLazyTreeController(ActiveJDOObjectLazyTreeController.this);
	//								}
	//								else {
	//									if (logger.isTraceEnabled())
	//										logger.trace("getNode.job1#run: reusing existing node for objectID=\"" + jdoObjectID + "\""); //$NON-NLS-1$ //$NON-NLS-2$
	//								}
	//
	//								tn.setJdoObjectID(jdoObjectID);
	//								tn.setParent(parent);
	//								objectID2TreeNode.put(jdoObjectID, tn);
	//								loadedNodes.add(tn);

									TreeNode treeNode = createNode();
									treeNode.setActiveJDOObjectLazyTreeController(ActiveJDOObjectLazyTreeController.this);
									treeNode.setParent(parent);
									treeNode.setJdoObjectID(jdoObjectID);
									addTreeNode(treeNode);
									loadedNodes.add(treeNode);
								}

								parent.setChildNodes(loadedNodes);
							} // synchronized (mutex) {
						}

						final List<TreeNode> loadedNodes_final = loadedNodes;

						Display.getDefault().asyncExec(new Runnable()
						{
							public void run()
							{
								fireJDOObjectsChangedEvent(new JDOLazyTreeNodesChangedEvent<JDOObjectID, TreeNode>(this, parentsToRefresh, loadedNodes_final));
							}
						});

						return Status.OK_STATUS;
					} finally {
						synchronized (mutex) {
							treeNodesWaitingForChildObjectIDRetrieval.remove(parent);
						}
					}
				}
			};
			job1.setRule(schedulingRule_jobChildObjectIDRetrieval);
//			job1.setPriority(Job.SHORT);
//			job1.setSystem(true);
			job1.schedule();
			if (logger.isDebugEnabled()) {
				logger.debug("scheduled job1 at "+System.currentTimeMillis());
			}
		}
		else {
			if (logger.isDebugEnabled())
				logger.debug("getNode: returning previously loaded INcomplete child-node and spawning Job."); //$NON-NLS-1$

			Job job2 = new Job(Messages.getString("org.nightlabs.jfire.base.ui.jdo.tree.lazy.ActiveJDOObjectLazyTreeController.job.loadingTreeNodes")) { //$NON-NLS-1$
				@Override
				protected IStatus run(ProgressMonitor monitor)
				{
					if (logger.isDebugEnabled())
						logger.debug("getNode.job2#run: entered"); //$NON-NLS-1$

					// Give it some time to collect objects in the treeNodesWaitingForObjectRetrieval
					// before we start processing them.
//					try { Thread.sleep(500); } catch (InterruptedException x) { } // ignore InterruptedException
					try { Thread.sleep(250); } catch (InterruptedException x) { } // ignore InterruptedException

					Set<TreeNode> nodesWaitingForObjectRetrieval;
					synchronized (treeNodesWaitingForObjectRetrieval) {
						jobObjectRetrieval = null;
						nodesWaitingForObjectRetrieval = new HashSet<TreeNode>(treeNodesWaitingForObjectRetrieval);
						treeNodesWaitingForObjectRetrieval.clear();
					}

//					Map<JDOObjectID, List<TreeNode>> jdoObjectID2NodeWaitingForObjectRetrieval = new HashMap<JDOObjectID, List<TreeNode>>();
					Set<JDOObjectID> jdoObjectIDs = new HashSet<JDOObjectID>(nodesWaitingForObjectRetrieval.size());
					for (TreeNode treeNode : nodesWaitingForObjectRetrieval) {
						@SuppressWarnings("unchecked")
						JDOObjectID oid = (JDOObjectID) treeNode.getJdoObjectID();
						jdoObjectIDs.add(oid);

//						List<TreeNode> nl = jdoObjectID2NodeWaitingForObjectRetrieval.get(oid);
//						if (nl == null) {
//							nl = new ArrayList<TreeNode>();
//							jdoObjectID2NodeWaitingForObjectRetrieval.put(oid, nl);
//						}
//						nl.add(treeNode);
					}
//					Set<JDOObjectID> jdoObjectIDs = jdoObjectID2NodeWaitingForObjectRetrieval.keySet(); // TODO do we need to pass a new HashSet here? is this keySet serializable? Is it the duty of the implementor to copy the set if necessary? Marco.

					Collection<JDOObject> jdoObjects = retrieveJDOObjects(jdoObjectIDs, monitor);
					Set<JDOObjectID> ignoredJDOObjectIDs = new HashSet<JDOObjectID>(jdoObjectIDs);

					final List<TreeNode> loadedTreeNodes = new ArrayList<TreeNode>(jdoObjects.size());
					final Map<JDOObjectID, List<TreeNode>> ignoredJDOObjects = new HashMap<JDOObjectID, List<TreeNode>>();
					final Map<JDOObjectID, List<TreeNode>> deletedJDOObjects = null;

					synchronized (mutex) {
						if (currentRootNode != hiddenRootNode) {
							logger.debug("getNode.job2#run: clear() called before job started - cancelling expired job."); //$NON-NLS-1$
							return Status.CANCEL_STATUS;
						}

						for (JDOObject jdoObject : jdoObjects) {
							@SuppressWarnings("unchecked")
							JDOObjectID jdoObjectID = (JDOObjectID) JDOHelper.getObjectId(jdoObject);
							ignoredJDOObjectIDs.remove(jdoObjectID);
							List<TreeNode> treeNodes = objectID2TreeNodeList.get(jdoObjectID);
							if (treeNodes == null)
								logger.warn("getNode.job2#run#1: There is no TreeNode existing for objectID=\"" + jdoObjectID + "\"!", new IllegalStateException("StackTrace")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							else {
								for (TreeNode treeNode : treeNodes) {
									treeNode.setJdoObject(jdoObject);
									loadedTreeNodes.add(treeNode);
								}
							}
//							TreeNode treeNode = objectID2TreeNode.get(jdoObjectID);
//							if (treeNode == null)
//								logger.warn("getNode.job2#run: There is no TreeNode existing for objectID=\"" + jdoObjectID + "\"!", new IllegalStateException("StackTrace"));
//							else {
//								treeNode.setJdoObject(jdoObject);
//								loadedTreeNodes.add(treeNode);
//							}
						}
						for (JDOObjectID jdoObjectID : ignoredJDOObjectIDs) {
							List<TreeNode> treeNodes = objectID2TreeNodeList.get(jdoObjectID);
							if (treeNodes == null)
								logger.warn("getNode.job2#run#2: There is no TreeNode existing for objectID=\"" + jdoObjectID + "\"!", new IllegalStateException("StackTrace")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							else
								ignoredJDOObjects.put(jdoObjectID, treeNodes);
						}
					} // synchronized (mutex) {

					Display.getDefault().asyncExec(new Runnable()
					{
						public void run()
						{
							fireJDOObjectsChangedEvent(new JDOLazyTreeNodesChangedEvent<JDOObjectID, TreeNode>(this, loadedTreeNodes, ignoredJDOObjects, deletedJDOObjects));
						}
					});

					return Status.OK_STATUS;
				}
			};

			synchronized (treeNodesWaitingForObjectRetrieval) {
				treeNodesWaitingForObjectRetrieval.add(node);
				if (jobObjectRetrieval == null) {
					jobObjectRetrieval = job2;
					job2.setRule(schedulingRule_jobObjectRetrieval);
//					job2.setPriority(Job.SHORT);
//					job2.setSystem(true);
					job2.schedule();
					if (logger.isDebugEnabled()) {
						logger.debug("scheduled job2 at "+System.currentTimeMillis());
					}
				}
			}
		}

		return node;
	}

	private ListenerList treeNodesChangedListeners = new ListenerList();

	/**
	 * This method can be used to add {@link JDOLazyTreeNodesChangedListener}s which will be called on the UI thread whenever
	 * the tree's data has been changed.
	 * <p>
	 * This method is thread-safe.
	 * </p>
	 *
	 * @param listener The listener to be added.
	 * @see #removeJDOLazyTreeNodesChangedListener(JDOLazyTreeNodesChangedListener)
	 */
	public void addJDOLazyTreeNodesChangedListener(JDOLazyTreeNodesChangedListener<JDOObjectID, JDOObject, TreeNode> listener) {
		treeNodesChangedListeners.add(listener);
	}

	/**
	 * This method can be used to remove listeners which have been previously added by {@link #addJDOLazyTreeNodesChangedListener(JDOLazyTreeNodesChangedListener)}.
	 * <p>
	 * This method is thread-safe.
	 * </p>
	 *
	 * @param listener The listener to be removed.
	 * @see #addJDOLazyTreeNodesChangedListener(JDOLazyTreeNodesChangedListener)
	 */
	public void removeJDOLazyTreeNodesChangedListener(JDOLazyTreeNodesChangedListener<JDOObjectID, JDOObject, TreeNode> listener) {
		treeNodesChangedListeners.remove(listener);
	}

	private void fireJDOObjectsChangedEvent(JDOLazyTreeNodesChangedEvent<JDOObjectID, TreeNode> changedEvent)
	{
		if (logger.isDebugEnabled()) {
			logger.debug(
					"fireJDOObjectsChangedEvent: changedEvent.parentsToRefresh.size()=" + //$NON-NLS-1$
					(changedEvent.getParentsToRefresh() == null ? null : changedEvent.getParentsToRefresh().size()));
			if (logger.isTraceEnabled() && changedEvent.getParentsToRefresh() != null) {
				for (TreeNode treeNode : changedEvent.getParentsToRefresh()) {
					if (treeNode == null)
						logger.trace("    parentTreeNode=null"); //$NON-NLS-1$
					else
						logger.trace("    parentTreeNode.jdoObjectID=" + treeNode.getJdoObjectID()); //$NON-NLS-1$
				}
			}

			logger.debug(
					"fireJDOObjectsChangedEvent: changedEvent.ignoredJDOObjects.size()=" + //$NON-NLS-1$
					(changedEvent.getIgnoredJDOObjects() == null ? null : changedEvent.getIgnoredJDOObjects().size()));
			if (logger.isTraceEnabled() && changedEvent.getIgnoredJDOObjects() != null) {
				for (Map.Entry<JDOObjectID, List<TreeNode>> me : changedEvent.getIgnoredJDOObjects().entrySet())
					logger.trace("    " + me.getKey()); //$NON-NLS-1$
			}

			logger.debug(
					"fireJDOObjectsChangedEvent: changedEvent.loadedTreeNodes.size()=" + //$NON-NLS-1$
					(changedEvent.getLoadedTreeNodes() == null ? null : changedEvent.getLoadedTreeNodes().size()));
			if (logger.isTraceEnabled() && changedEvent.getLoadedTreeNodes() != null) {
				for (TreeNode treeNode : changedEvent.getLoadedTreeNodes())
					logger.trace("    " + treeNode.getJdoObjectID()); //$NON-NLS-1$
			}

			logger.debug(
					"fireJDOObjectsChangedEvent: changedEvent.deletedJDOObjects.size()=" + //$NON-NLS-1$
					(changedEvent.getDeletedJDOObjects() == null ? null : changedEvent.getDeletedJDOObjects().size()));
			if (logger.isTraceEnabled() && changedEvent.getDeletedJDOObjects() != null) {
				for (Map.Entry<JDOObjectID, List<TreeNode>> me : changedEvent.getDeletedJDOObjects().entrySet())
					logger.trace("    " + me.getKey()); //$NON-NLS-1$
			}
		}

		onJDOObjectsChanged(changedEvent);

		if (!treeNodesChangedListeners.isEmpty()) {
			Object[] listeners = treeNodesChangedListeners.getListeners();
			for (Object listener : listeners) {
				JDOLazyTreeNodesChangedListener<JDOObjectID, JDOObject, TreeNode> l = (JDOLazyTreeNodesChangedListener<JDOObjectID, JDOObject, TreeNode>) listener;
				l.onJDOObjectsChanged(changedEvent);
			}
		}
	}



	// -------------------------------------------------------------------------------------------------- ++ ------>>
	/**
	 * Returns a List of TreeNodes answering to the given JDOObjectID.
	 * Note that this is NOT ALWAYS a one-to-one mapping; while TreeNodes with the same JDOObjectID all have the same children,
	 * their ancestors are different. Thus one needs to check for the correct instance of the TreeNode if one is dealing with
	 * specific domain application.
	 */
	public List<TreeNode> getTreeNodeList(JDOObjectID jdoObjectID) {
		// Needs access to the map to perform filters.
		//   Filter #1: Ensures the tree-path terminates and stops the cyclic operation (at least with the PersonRelationTree).
		//   Filter #2: Ensures no child is repeated.
		return objectID2TreeNodeList.get(jdoObjectID);
	}

	/**
	 * Retrieves all child ObjectIDs given a very specific parentNode.
	 * FOR SPECIFICITY: Override this method to gain access to other information from the parentNode. For example, when filtering children is required.
	 */
	@SuppressWarnings("unchecked")
	protected Collection<JDOObjectID> retrieveChildObjectIDs(TreeNode parentNode, ProgressMonitor monitor) {
		return retrieveChildObjectIDs((JDOObjectID) parentNode.getJdoObjectID(), monitor); // <-- Original abstract method declared in this class.
	}

	/**
	 * Retrieves the number of children for each specific parent TreeNode.
	 * FOR SPECIFICITY: Override this method to gain access to other information from the parentNodes. For example, when filtering children is required.
	 */
	protected Map<JDOObjectID, Long> retrieveChildCount(Set<TreeNode> parentNodes, Set<JDOObjectID> parentIDs, ProgressMonitor monitor) {
		return retrieveChildCount(parentIDs, monitor); // <-- Original abstract method declared in this class.
	}
	// -------------------------------------------------------------------------------------------------- ++ ------>>


	protected TreeNode getHiddenRootNode() {
		return hiddenRootNode;
	}

	protected void clear()
	{
		synchronized (mutex) {
			hiddenRootNode = null;
			objectID2TreeNodeList.clear();

			synchronized (_activeParentObjectIDs) {
				_activeParentObjectIDs.clear();
				_activeParentObjectIDs_ro = null;
			}
			registerJDOLifecycleListener();
		}
		synchronized (treeNodesWaitingForObjectRetrieval) {
			treeNodesWaitingForObjectRetrieval.clear();
			jobObjectRetrieval = null;
		}
		synchronized (treeNodesWaitingForChildCountRetrieval) {
			treeNodesWaitingForChildCountRetrieval.clear();
			jobChildCountRetrieval = null;
		}
	}
}
