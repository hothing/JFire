package org.nightlabs.jfire.personrelation.ui;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.ui.jdo.tree.lazy.JDOObjectLazyTreeNode;
import org.nightlabs.jfire.personrelation.PersonRelation;
import org.nightlabs.jfire.prop.id.PropertySetID;

public class PersonRelationTreeNode
extends JDOObjectLazyTreeNode<ObjectID, Object, PersonRelationTreeController> {
	@Override
	public PersonRelationTreeNode getParent() {
		return (PersonRelationTreeNode)super.getParent();
	}

	/**
	 * @return the PropertySetID represented by this node. Returns null if node does not present a Person-related object.
	 */
	public PropertySetID getPropertySetID() {
		ObjectID jdoObjectID = getJdoObjectID();
		if (jdoObjectID instanceof PropertySetID)
			return (PropertySetID) jdoObjectID;

		else {
			Object jdoObject = getJdoObject();
			if (jdoObject instanceof PersonRelation)
				return ((PersonRelation)jdoObject).getToID();
		}

		return null;
	}

	/**
	 * @return a List of PropertySetIDs ordered from this node all the way up to the root.
	 */
	public List<PropertySetID> getPropertySetIDsToRoot() {
		return getPropertySetIDsToRoot(this, new LinkedList<PropertySetID>());
	}

	private List<PropertySetID> getPropertySetIDsToRoot(PersonRelationTreeNode node, List<PropertySetID> propSetIDs) {
		// Base case.
		if (node.getParent() == null)
			return propSetIDs;

		// Iterative case.
		ObjectID jdoObjectID = node.getJdoObjectID();
		if (jdoObjectID instanceof PropertySetID)
			propSetIDs.add((PropertySetID)jdoObjectID);
		else {
			Object jdoObject = node.getJdoObject();
			if (jdoObject instanceof PersonRelation)
				propSetIDs.add(((PersonRelation)jdoObject).getToID());
		}

		return getPropertySetIDsToRoot(node.getParent(), propSetIDs);
	}


	/**
	 * Checks the contents in a selection and returns a {@link PersonRelationTreeNode} if a valid one exists
	 * in the given {@link ISelection}. Assumes here that the selection contains at most one node, and that
	 * the given selection is an instance of the {@link IStructuredSelection}.
	 *
	 * Used very often:
	 * 1. In applications (eg. from a SelectionListener): We often need to know if an ISelection
	 *    contains an instance of the PersonRelationTreeNode. Currently, there are at least 5
	 *    Actions having the exact same codes.
	 *
	 * 2. Usually this is called where we already have access to a {@link PersonRelationTreeNode}.
	 *
	 * @return null if the selection does not contain a {@link PersonRelationTreeNode}.
	 */
	public static PersonRelationTreeNode getPersonRelationTreeNodeFromSelection(ISelection selection) {
		if (selection.isEmpty() || !(selection instanceof IStructuredSelection))
			return null;

		IStructuredSelection sel = (IStructuredSelection) selection;
		if (sel.size() != 1 || sel.getFirstElement() == null)
			return null;

		Object selObject = sel.getFirstElement();
		if (!(selObject instanceof PersonRelationTreeNode))
			return null;

		return (PersonRelationTreeNode) selObject;
	}
}
