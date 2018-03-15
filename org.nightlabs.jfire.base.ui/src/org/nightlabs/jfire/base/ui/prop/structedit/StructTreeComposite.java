
package org.nightlabs.jfire.base.ui.prop.structedit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.nightlabs.base.ui.language.LanguageChangeEvent;
import org.nightlabs.base.ui.language.LanguageChangeListener;
import org.nightlabs.base.ui.language.LanguageChooser;
import org.nightlabs.base.ui.tree.AbstractTreeComposite;
import org.nightlabs.base.ui.tree.TreeContentProvider;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;

public class StructTreeComposite extends AbstractTreeComposite<TreeNode> implements LanguageChangeListener {
	private List<StructBlockNode> blockNodes;
	private String currLanguageId;

	public StructTreeComposite(Composite parent, boolean init, LanguageChooser langChooser) {
		super(parent, getBorderStyle(parent) | SWT.H_SCROLL, true, init, false);
		getGridLayout().marginHeight = 2;
		getGridLayout().marginWidth = 2;
		if (langChooser != null) {
			langChooser.addLanguageChangeListener(this);
			this.currLanguageId = langChooser.getLanguage().getLanguageID();
		}
		else {
			this.currLanguageId = Locale.getDefault().getLanguage();
		}
	}

	@Override
	public void createTreeColumns(Tree tree) {
	}

	@Override
	public void setTreeProvider(TreeViewer treeViewer) {
		treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.setContentProvider(new ContentProvider());
	}

	// public void setInput(String organisationID, Class linkClass)
	// {
	// id = StructID.create(organisationID, linkClass.getName());
	// getTreeViewer().setInput(id);
	// }

	public StructBlockNode getCurrentBlockNode() {
		TreeNode current = (TreeNode) ((IStructuredSelection) getSelection()).getFirstElement();
		if (current instanceof StructBlockNode)
			return (StructBlockNode) current;
		else if (current instanceof StructFieldNode)
			return ((StructFieldNode) current).getParentBlock();
		return null;
	}

	public StructBlockNode addStructBlock(StructBlock psb) {
		StructBlockNode blockNode = new StructBlockNode(psb);
		blockNodes.add(blockNode);
		refresh();
		return blockNode;
	}

	public StructFieldNode addStructField(StructBlockNode parentNode, StructField field) {
		StructFieldNode fieldNode = new StructFieldNode(field, parentNode);
		parentNode.addField(fieldNode);
		refresh();
		return fieldNode;
	}
	
	public StructFieldNode getStructFieldNode() {
		TreeNode current = (TreeNode) ((IStructuredSelection) getSelection()).getFirstElement();
		if (current instanceof StructFieldNode)
			return (StructFieldNode) current;
		return null;
	}

	public void removeStructBlock(StructBlockNode blockNode) {
		blockNodes.remove(blockNode);
		refresh();
	}

	public void removeStructField(StructBlockNode parentBlock, StructFieldNode fieldNode) {
		parentBlock.removeField(fieldNode);
		refresh();
	}

	private class TreeLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object element) {
			if (element instanceof TreeNode) {
				return ((TreeNode) element).getImage();
			}
			return null;
		}

		@Override
		public String getText(Object element) {
			return getColumnText(element, 0);
		}

		public String getColumnText(Object element, int columnIndex) {
			String label = ""; //$NON-NLS-1$
			if (element instanceof TreeNode)
				label = ((TreeNode) element).getI18nText().getText(currLanguageId);
			if (element instanceof String)
				label = (String) element;

			return label;
		}
	}

	private class ContentProvider extends TreeContentProvider {
		private String text;
		private IStruct struct;

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			super.inputChanged(viewer, oldInput, newInput);
			blockNodes = null;

			if (newInput instanceof IStruct) {
				text = null;
				struct = (IStruct) newInput;
			} else if (newInput instanceof String) {
				struct = null;
				text = (String) newInput;
			}
		}

		public Object[] getElements(Object inputElement) {
			if (struct != null && blockNodes == null) {
				blockNodes = new ArrayList<StructBlockNode>(struct.getStructBlocks().size());

				for (StructBlock psb : struct.getStructBlocks()) {
					StructBlockNode newBlockNode = new StructBlockNode(psb);
					blockNodes.add(newBlockNode);					
				}
			}

			if (blockNodes != null) {
//				Collections.sort(blockNodes);
				return blockNodes.toArray(new StructBlockNode[blockNodes.size()]);
			}
			else if (text != null) {
				return new Object[] { text };
			}

			return new Object[0];
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof TreeNode)
				return ((TreeNode) element).hasChildren();
			else
				return false;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof TreeNode)
				return ((TreeNode) parentElement).getChildren();
			else
				return null;
		}
	}

	public void languageChanged(LanguageChangeEvent event) {
		currLanguageId = event.getNewLanguage().getLanguageID();
		refresh(true);
	}

	protected void clearCache()
	{
		blockNodes = null;
	}

	@Override
	public void refresh() {
		clearCache();
		super.refresh();
	}
	@Override
	public void refresh(boolean updateLabels) {
		clearCache();
		super.refresh(updateLabels);
	}
	@Override
	public void refresh(Object element) {
		clearCache();
		super.refresh(element);
	}
	@Override
	public void refresh(Object element, boolean updateLabels) {
		clearCache();
		super.refresh(element, updateLabels);
	}
}