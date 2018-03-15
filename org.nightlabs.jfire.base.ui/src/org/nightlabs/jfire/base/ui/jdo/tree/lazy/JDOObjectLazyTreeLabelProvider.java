/**
 *
 */
package org.nightlabs.jfire.base.ui.jdo.tree.lazy;

import org.eclipse.swt.graphics.Image;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jdo.ObjectID;

/**
 * A LabelProvider that can be used with a TreeViewer that is driven by an {@link ActiveJDOObjectLazyTreeController}.
 * It assumes the elements of the tree are {@link JDOObject}s and provides methods that directly
 * operate on this type.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@SuppressWarnings("unchecked") //$NON-NLS-1$
public abstract class JDOObjectLazyTreeLabelProvider
<JDOObjectID extends ObjectID,
 JDOObject,
 TreeNode extends JDOObjectLazyTreeNode> //<JDOObjectID, JDOObject, ? super TreeNode>>
extends TableLabelProvider
{

	public JDOObjectLazyTreeLabelProvider() {
	}

	public String getColumnText(Object element, int columnIndex)
	{
		if (element instanceof String)
			return (String)element;

		TreeNode node = (TreeNode) element;
		return getJDOObjectText((JDOObjectID) node.getJdoObjectID(), (JDOObject) node.getJdoObject(), columnIndex);
	}

	/**
	 * Get the column text for the given {@link JDOObject}.
	 * @param jdoObjectID TODO
	 * @param jdoObject The {@link JDOObject} to get the text for.
	 * @param columnIndex The column to get the text for.
	 *
	 * @return The column text for the given {@link JDOObject}.
	 */
	protected abstract String getJDOObjectText(JDOObjectID jdoObjectID, JDOObject jdoObject, int columnIndex);

	@Override
	public Image getImage(Object element) {
		return getColumnImage(element, 0);
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof String)
			return null;

		TreeNode node = (TreeNode) element;
		return getJDOObjectImage((JDOObjectID) node.getJdoObjectID(), (JDOObject) node.getJdoObject(), columnIndex);
	}

	/**
	 * Get the {@link Image} for the given {@link JDOObject}.
	 * @param jdoObjectID TODO
	 * @param jdoObject The {@link JDOObject} to get the image for.
	 * @param columnIndex The columnIndex to get the image for.
	 *
	 * @return The {@link Image} for the given {@link JDOObject}.
	 */
	protected Image getJDOObjectImage(JDOObjectID jdoObjectID, JDOObject jdoObject, int columnIndex) {
		return null;
	}
}
