/**
 *
 */
package org.nightlabs.jfire.base.ui.prop.structedit.action;

import java.util.HashSet;
import java.util.Set;

import org.nightlabs.base.ui.action.SelectionListenerAction;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.jfire.base.ui.prop.structedit.StructBlockNode;
import org.nightlabs.jfire.base.ui.prop.structedit.StructEditor;
import org.nightlabs.jfire.base.ui.prop.structedit.StructFieldNode;
import org.nightlabs.jfire.base.ui.prop.structedit.StructureChangedListener;
import org.nightlabs.jfire.base.ui.prop.structedit.TreeNode;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.IStruct.OrderMoveDirection;

/**
 *
 * @author Marius Heinzmann [marius<at>NightLabs<dot>de]
 */
public class MoveStructElementAction
	extends SelectionListenerAction
{
	private StructEditor editor;
	private OrderMoveDirection orderMoveDirection;

	public MoveStructElementAction(StructEditor editor, OrderMoveDirection orderMoveDirection) {
		this(editor, orderMoveDirection, null);
	}

	public MoveStructElementAction(StructEditor editor, OrderMoveDirection orderMoveDirection, StructureChangedListener listener) {
		super(editor.getStructTree().getStructTreeComposite());
		addStructureChangedListener(listener);
		this.editor = editor;
		this.orderMoveDirection = orderMoveDirection;

		setText(orderMoveDirection == orderMoveDirection.up ? Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.action.MoveStructElementAction.text_up") : Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.action.MoveStructElementAction.text_down"));  //$NON-NLS-1$ //$NON-NLS-2$
		setImageDescriptor( orderMoveDirection == orderMoveDirection.up ? SharedImages.UP_16x16 : SharedImages.DOWN_16x16 );
		setToolTipText( orderMoveDirection == orderMoveDirection.up ? Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.action.MoveStructElementAction.toolTipText_up") :  //$NON-NLS-1$
												 Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.action.MoveStructElementAction.toolTipText_down")); //$NON-NLS-1$
	}

	@Override
	public void run() {
		TreeNode selectedNode = editor.getStructTree().getSelectedNode();
		if (selectedNode == null)
			return;

		StructLocal structLocal = (StructLocal) editor.getStruct();
		
		if (selectedNode instanceof StructBlockNode) {
			final StructBlock movingBlock = ((StructBlockNode) selectedNode).getBlock();
			structLocal.moveStructBlockInOrder(movingBlock.getStructBlockIDObj(), orderMoveDirection);
		} else if (selectedNode instanceof StructFieldNode) {
			StructFieldNode fieldNode = (StructFieldNode) selectedNode;
			final StructField<? extends DataField> movingField = fieldNode.getField();
			structLocal.moveStructFieldInOrder(movingField.getStructFieldIDObj(), orderMoveDirection);
			fieldNode.getParentBlock().clearFieldReferences();
		} else {
			throw new IllegalArgumentException("The returned selected TreeNode is neither a StructBlockNode " + //$NON-NLS-1$
					"nor a StructFieldNode! Seems like the NodeTypes have changed!"); //$NON-NLS-1$
		}

		editor.getStructTree().refresh();
		notifyListeners();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateEnabled()
	 */
	public boolean calculateEnabled() {
		return getSelection() != null;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.ui.action.IUpdateActionOrContributionItem#calculateVisible()
	 */
	public boolean calculateVisible() {
		return (editor.getStruct() instanceof StructLocal);
	}

	private Set<StructureChangedListener> listeners = new HashSet<StructureChangedListener>();

	public void addStructureChangedListener(StructureChangedListener listener) {
		if (! listeners.contains(listener))
			listeners.add(listener);
	}

	private void notifyListeners() {
		for (StructureChangedListener listener : listeners) {
			listener.structureChanged();
		}
	}

}
