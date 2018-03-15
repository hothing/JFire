package org.nightlabs.jfire.base.ui.prop.structedit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.base.ui.JFireBasePlugin;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.util.NLLocale;
import org.nightlabs.util.Util;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class StructBlockNode extends TreeNode //implements Comparable<StructBlockNode>
{
	private StructBlock block;
	private List<StructFieldNode> fields;

	public StructBlockNode(StructBlock block)
	{
		if (block == null)
			throw new IllegalArgumentException("block must not be null!"); //$NON-NLS-1$

		this.block = block;
		fields = null; // new LinkedList<StructFieldNode>();
	}

	protected List<StructFieldNode> getFieldList() {
		if (fields == null) {
			List<StructField<? extends DataField>> structFields = block.getStructFields();
			fields = new ArrayList<StructFieldNode>(structFields.size());
			for (StructField<?> field : structFields)
				fields.add(new StructFieldNode(field, this));
		}
		return fields;
	}

	public void clearFieldReferences() {
		fields = null;
	}
	
	public void addField(StructFieldNode field)
	{
		getFieldList().add(field);
	}

	public void removeField(StructFieldNode field)
	{
		getFieldList().remove(field);
	}

	@Override
	public I18nText getI18nText()
	{
		return block.getName();
	}

	@Override
	public String getLabel()
	{
		return block.getName().getText(NLLocale.getDefault().getLanguage());
	}

	@Override
	public TreeNode[] getChildren()
	{
		return getFieldList().toArray(new TreeNode[0]);
	}

	@Override
	public boolean hasChildren()
	{
		return !block.getStructFields().isEmpty();
	}

	public StructBlock getBlock()
	{
		return block;
	}

	@Override
	public boolean isEditable()
	{
//		Login.getLogin().
		return false;
	}

	@Override
	public String toString() {
		return block.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof StructBlockNode)) return false;
		StructBlockNode o = (StructBlockNode) obj;
		return Util.equals(o.block, this.block);
	}
	@Override
	public int hashCode() {
		return Util.hashCode(this.block);
	}

	@Override
	public Image getImage() {
		if (block.isLocal())
			return SharedImages.getSharedImage(JFireBasePlugin.getDefault(), StructBlockNode.class, "StructBlockLocal"); //$NON-NLS-1$
		else
			return SharedImages.getSharedImage(JFireBasePlugin.getDefault(), StructBlockNode.class, "StructBlockNotLocal"); //$NON-NLS-1$
	}
}
