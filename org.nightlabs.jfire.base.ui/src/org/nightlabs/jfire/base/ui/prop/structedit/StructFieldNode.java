package org.nightlabs.jfire.base.ui.prop.structedit;

import org.eclipse.swt.graphics.Image;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.base.ui.JFireBasePlugin;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.util.NLLocale;
import org.nightlabs.util.Util;

public class StructFieldNode extends TreeNode //implements Comparable<StructFieldNode>
{
	private StructField field;
	private StructBlockNode parentBlock;

	/**
	 * Creates a new StructFieldNode.
	 * @param field The {@link StructField} to be represented. Can be null to indicate an
	 * 				{@link StructField} whose type has not been specified yet.
	 * @param parentBlock The parentBlock node.
	 * @param deletable Wether the node can be deleted or not.
	 */
	public StructFieldNode(StructField field, StructBlockNode parent)
	{
		if (field == null)
			throw new IllegalArgumentException("field must not be null!"); //$NON-NLS-1$

		this.field = field;
		this.parentBlock = parent;
	}

	@Override
	public I18nText getI18nText()
	{
		return field.getName();
	}

	@Override
	public String getLabel()
	{
		//return field.getStructFieldKey();
		return field.getName().getText(NLLocale.getDefault().getLanguage());
	}

	@Override
	public TreeNode[] getChildren()
	{
		return null;
	}

	@Override
	public boolean hasChildren()
	{
		return false;
	}

	public StructField getField()
	{
		return field;
	}

	public StructBlockNode getParentBlock()
	{
		return parentBlock;
	}

	@Override
	public boolean isEditable()
	{
		return false;
	}

	@Override
	public String toString() {
		return field.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof StructFieldNode)) return false;

		return Util.equals(field, ((StructFieldNode)obj).field);
	}

	@Override
	public int hashCode() {
		return Util.hashCode(field);
	}

	@Override
	public Image getImage() {
		if (field.getStructBlock().isLocal())
			return SharedImages.getSharedImage(JFireBasePlugin.getDefault(), StructFieldNode.class, "StructFieldLocal"); //$NON-NLS-1$
		else
			return SharedImages.getSharedImage(JFireBasePlugin.getDefault(), StructFieldNode.class, "StructFieldNotLocal"); //$NON-NLS-1$
	}
}
