package org.nightlabs.jfire.base.ui.prop.structedit;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.language.LanguageChooser;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.base.ui.wizard.DynamicPathWizardDialog;
import org.nightlabs.base.ui.wizard.DynamicPathWizardPage;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.base.ui.resource.Messages;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.exception.IllegalStructureModificationException;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.Util;

/**
 * Editor to change the structure ({@link IStruct}) linked to a certain {@link Class}.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class StructEditor {
	private StructTree structTree;
	private StructPartEditor<?> currentStructPartEditor;
	private StructBlockEditor structBlockEditor;
	private LanguageChooser languageChooser;
	private TreeNode lastSelection;
	private boolean ignoreChangeEvent;

	private IStruct currentStruct;
	private StructEditorComposite structEditorComposite;
	private ListenerList changeListeners;
	private boolean changed = false;

	/**
	 * Create a new StructEditor. The constructor does not create any
	 * GUI components. Call {@link #createComposite(Composite, int)}
	 * to add the graphical Editor to a parent of your choice.
	 */
	public StructEditor() {
		changeListeners = new ListenerList();
		this.structBlockEditor = new StructBlockEditor();
	}

	private Map<StructField<?>, StructFieldEditor<?>> structField2structFieldEditorMap = new HashMap<StructField<?>, StructFieldEditor<?>>();

	/**
	 * Create the {@link Composite} of this editor.
	 *
	 * @param parent The parent to add the {@link Composite}.
	 * @param style The style for the outer {@link Composite} of the Editors GUI.
	 * @return The newly created {@link StructEditorComposite}.
	 */
	public StructEditorComposite createComposite(Composite parent, int style) {
		if (structEditorComposite == null) {
			structTree = new StructTree(this);
			structEditorComposite = new StructEditorComposite(parent, style, this, structTree);
			languageChooser = structEditorComposite.getLanguageChooser();

			structTree.addSelectionChangedListener(new ISelectionChangedListener() {
				@SuppressWarnings("unchecked") //$NON-NLS-1$
				public void selectionChanged(SelectionChangedEvent event) {
					if (ignoreChangeEvent)
						return;

					if (!validatePartEditor()) {
						// restore last selection
						ignoreChangeEvent = true;
						structTree.select(lastSelection);
						ignoreChangeEvent = false;
						return;
					}

					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					if (selection.isEmpty() && currentStructPartEditor != null) {
						currentStructPartEditor.setEnabled(false);
						return;
					}

					lastSelection = (TreeNode) ((IStructuredSelection)event.getSelection()).getFirstElement();

					TreeNode selected = (TreeNode) selection.getFirstElement();
					StructFieldFactoryRegistry structFieldFactoryRegistry = StructFieldFactoryRegistry.sharedInstance();

					boolean enabled = true;

					if (selected instanceof StructFieldNode) {
						StructField field = ((StructFieldNode) selected).getField();
						StructFieldEditor editor = structField2structFieldEditorMap.get(field);
						if (editor == null) {
							editor = structFieldFactoryRegistry.getStructFieldEditorFactory(field.getClass()).createStructFieldEditor();
							structField2structFieldEditorMap.put(field, editor);
						}

						structEditorComposite.setPartEditor(editor);
						editor.setData(field);
						currentStructPartEditor = editor;
						enabled = field.getStructBlock().isLocal();
						// save the data of the editor to make it able to be restored later
						editor.saveData();
					} else if (selected instanceof StructBlockNode) {
						StructBlock block = ((StructBlockNode) selected).getBlock();
						structEditorComposite.setPartEditor(structBlockEditor);
						structBlockEditor.setData(block);
						currentStructPartEditor = structBlockEditor;
						enabled = block.isLocal();
					}

					if (currentStructPartEditor != null) {
						currentStructPartEditor.addModifyListener(partEditorModifyListener);
						currentStructPartEditor.setEnabled(enabled);
						if (enabled)
							currentStructPartEditor.setFocus();
					}
				}
			});
		}

		return structEditorComposite;
	}

	public void setCurrentStructLocalID(final StructLocalID structLocalID) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				structEditorComposite.setLoadingText();
			}
		});
		new Job(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructEditor.loadStructJob.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(final ProgressMonitor monitor) throws Exception {
				final IStruct struct = fetchStructure(structLocalID, monitor);

				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setStruct( struct );
					}
				});
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	protected IStruct fetchStructure(final StructLocalID structLocalID, ProgressMonitor monitor) {
		return StructLocalDAO.sharedInstance().getStructLocal(structLocalID, monitor);
	}

	/**
	 * Sets the current Struct to be edited.
	 * <p>
	 * Note that the given {@link IStruct} will be
	 * copied by {@link Util#cloneSerializable(Object)}.
	 * </p>
	 * @param struct The {@link IStruct} to be edited.
	 */
	public void setStruct(IStruct struct) {
		currentStruct = Util.cloneSerializable(struct);
		structEditorComposite.setStruct(currentStruct);
//		structTree.setInput(currentStruct);
	}

	/**
	 * Sets the current Struct to be edited.
	 * <p>
	 * Note that the given {@link IStruct} will be
	 * copied by {@link Util#cloneSerializable(Object)}.
	 * </p>
	 * @param struct The {@link IStruct} to be edited.
	 * @param doCloneSerializable Whether the given struct should be cloned before editing.
	 * 		Note, that if it is cloned {@link #getStruct()} will not return the same instance
	 * 		that was passed to this method.
	 */
	public void setStruct(IStruct struct, boolean doCloneSerializable) {
		currentStruct = doCloneSerializable ? Util.cloneSerializable(struct) : struct;
		structEditorComposite.setStruct(currentStruct);
//		structTree.setInput(currentStruct);
	}

	/**
	 * Returns the currently edited {@link IStruct}.
	 * @return The currently edited {@link IStruct}.
	 */
	public IStruct getStruct() {
		return currentStruct;
	}

	private boolean validatePartEditor() {
		if (currentStructPartEditor instanceof StructBlockEditor)
			return true;

		if (currentStructPartEditor instanceof StructFieldEditor<?>) {
			StructFieldEditor<?> structFieldEditor = (StructFieldEditor<?>) currentStructPartEditor;
			if (!structFieldEditor.validateInput()) {
				MessageBox mb = new MessageBox(RCPUtil.getActiveShell(), SWT.YES | SWT.NO );
				mb.setMessage(
						String.format(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructEditor.messageBoxInvalidInput.message"), //$NON-NLS-1$
						new Object[] { structFieldEditor.getErrorMessage() })
				);
				mb.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructEditor.messageBoxInvalidInput.text")); //$NON-NLS-1$
				switch (mb.open()) {
					case SWT.YES:
						structFieldEditor.restoreData();
						return true;
					case SWT.NO:
						return false;
				}
			}
			return true;
		}

		return true;
	}

//	Removed changeListener as the editor is now embedded in an editor page with an active controller
//	private NotificationListener changeListener = new NotificationAdapterJob() {
//
//		public void notify(NotificationEvent notificationEvent) {
//			for (DirtyObjectID dirtyObjectID : (Set<? extends DirtyObjectID>)notificationEvent.getSubjects()) {
//				final StructLocalID currentStructID = (StructLocalID) (currentStruct == null ? null : JDOHelper.getObjectId(currentStruct));
//				if (dirtyObjectID.getObjectID().equals(currentStructID)) {
//					final IStruct struct = fetchStructure(currentStructID, getProgressMonitorWrapper());
////				 TODO: Same problem as with ConfigModules: we cannot check whether the content of two IStructs are identical or not.<
////					if (currentStruct.equals(struct))
////						return;
//
//					Display.getDefault().asyncExec(new Runnable() {
//						public void run() {
//							setStruct(struct);
//						}
//					});
//
//				}
//			}
//		}
//
//	};

//	private PropertyManagerRemote getPropertyManager() {
//		try {
//			return JFireEjb3Factory.getRemoteBean(PropertyManagerRemote.class, Login.getLogin().getInitialContextProperties());
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new RuntimeException(e);
//		}
//	}

	private org.nightlabs.jfire.prop.ModifyListener partEditorModifyListener = new org.nightlabs.jfire.prop.ModifyListener() {
		@Override
		public void modifyData() {
			StructEditor.this.setChanged(true);
			structTree.refreshSelected();
		}
	};

	public void setChanged(boolean changed) {
		this.changed = changed;
		if (changed)
			notifyChangeListeners();
	}

	public boolean isChanged() {
		return changed;
	}

	public boolean hasStructureLoaded() {
		return currentStruct != null;
	}

	public StructTree getStructTree() {
		return structTree;
	}

	public LanguageChooser getLanguageChooser() {
		return languageChooser;
	}

	public void addStructureChangedListener(StructureChangedListener listener) {
		changeListeners.add(listener);
	}

	public void removeStructureChangedListener(StructureChangedListener listener) {
		changeListeners.remove(listener);
	}

	private synchronized void notifyChangeListeners() {
		Object[] listeners = changeListeners.getListeners();
		for (Object l : listeners) {
			((StructureChangedListener)l).structureChanged();
		}
	}

	public void addStructBlock() {
		long newBlockID = IDGenerator.nextID(StructBlock.class);
		StructBlock newBlock;
		try {
			newBlock = new StructBlock(currentStruct, Login.getLogin().getOrganisationID(), ObjectIDUtil.longObjectIDFieldToString(newBlockID));
			newBlock.getName().setText(languageChooser.getLanguage().getLanguageID(), Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructEditor.newStructBlock.name")); //$NON-NLS-1$
			currentStruct.addStructBlock(newBlock);
			structTree.addStructBlock(newBlock);
			setChanged(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		structTree.select(newBlock);
	}

	public void addStructField(StructBlock structBlock) {
		StructFieldCreationWizard wiz = new StructFieldCreationWizard();
		DynamicPathWizardDialog dialog = new DynamicPathWizardDialog(wiz);

		int retVal = dialog.open();
		if (retVal == Window.CANCEL)
			return;

		StructFieldMetaData newFieldMetaData = wiz.getSelectedFieldMetaData();
		addStructField(structBlock, newFieldMetaData, wiz.getDetailsWizardPage());
	}

	private void addStructField(StructBlock toBlock, StructFieldMetaData newFieldMetaData, DynamicPathWizardPage detailsPage) {
		StructFieldFactory fieldFactory = newFieldMetaData.getFieldFactory();
		StructField newField;
		try {
			newField = fieldFactory.createStructField(toBlock, detailsPage);
			newField.getName().setText(languageChooser.getLanguage().getLanguageID(), Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructEditor.newStructField.name")); //$NON-NLS-1$
			toBlock.addStructField(newField);

			structTree.addStructField(structTree.getCurrentBlockNode(), newField);
			setChanged(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		structTree.select(newField);
	}

	public void removeSelectedItem() {
		TreeNode selected = structTree.getSelectedNode();
		if (selected instanceof StructBlockNode) {
			StructBlockNode block = (StructBlockNode) selected;
			removeStructBlock(block);
		} else if (selected instanceof StructFieldNode) {
			StructFieldNode field = (StructFieldNode) selected;
			removeStructField(field);
		}

		structTree.refresh();
	}

	private void removeStructBlock(StructBlockNode blockNode) {
		MessageBox mb = new MessageBox(RCPUtil.getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		mb.setMessage(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructEditor.messageBoxRemoveStructBlockConfirmation.message")); //$NON-NLS-1$
		mb.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructEditor.messageBoxRemoveStructBlockConfirmation.text")); //$NON-NLS-1$
		int result = mb.open();
		if (result == SWT.YES) {
			try {
				currentStruct.removeStructBlock(blockNode.getBlock());
			} catch (IllegalStructureModificationException e) {
				mb = new MessageBox(RCPUtil.getActiveShell(), SWT.ICON_ERROR | SWT.OK);
				// TODO Shouldn't we simply rethrow this exception as RuntimeException and leave the work to our general exception handler?
				mb.setMessage("Block could not be deleted: " + e.getMessage()); //$NON-NLS-1$
				mb.setText("Deleting failed"); //$NON-NLS-1$
				mb.open();
				return;
			}

			structTree.removeStructBlock(blockNode);
			setChanged(true);
		}
	}

	private void removeStructField(StructFieldNode fieldNode) {
		MessageBox mb = new MessageBox(RCPUtil.getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		String fieldName = fieldNode.getI18nText().getText(languageChooser.getLanguage().getLanguageID());
		long dataFieldInstanceCount;
		dataFieldInstanceCount = StructEditorUtil.getDataFieldInstanceCount(fieldNode.getField().getStructFieldIDObj());

		mb.setMessage(String.format(
				Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructEditor.messageBoxRemoveStructFieldConfirmation.message"), //$NON-NLS-1$
				new Object[] { fieldName, new Long(dataFieldInstanceCount) }));

		mb.setText(Messages.getString("org.nightlabs.jfire.base.ui.prop.structedit.StructEditor.messageBoxRemoveStructFieldConfirmation.text")); //$NON-NLS-1$
		int result = mb.open();
		if (result == SWT.YES) {
			try {
				fieldNode.getParentBlock().getBlock().removeStructField(fieldNode.getField());
				structTree.removeStructField(fieldNode.getParentBlock(), fieldNode);
				currentStructPartEditor.setData(null);
			} catch (IllegalStructureModificationException e) {
				mb = new MessageBox(RCPUtil.getActiveShell(), SWT.ICON_ERROR | SWT.OK);
				// TODO this message contradicts the information above which says "[...] instances of this struct field will be also deleted if you continue."
				// Maybe we should simply rethrow this exception and have our exception-handling-framework handle it? Is this exception really expected?
				mb.setMessage("You cannot delete a struct block that has already been persisted."); //$NON-NLS-1$
				mb.setText("Deletion failed"); //$NON-NLS-1$
				mb.open();
				return;
			}
		}

		setChanged(true);
	}

	public boolean canAddStructField() {
		StructBlockNode currentBlockNode = structTree.getCurrentBlockNode();
		if (currentBlockNode == null)
			return false;

		return currentBlockNode.getBlock().isLocal();
	}

	public boolean canRemoveCurrentElement() {
		TreeNode selectedNode = structTree.getSelectedNode();
		if (selectedNode instanceof StructBlockNode)
			return ((StructBlockNode) selectedNode).getBlock().isLocal();
		else if (selectedNode instanceof StructFieldNode)
			return ((StructFieldNode) selectedNode).getParentBlock().getBlock().isLocal();
		else
			return false;
	}
}
