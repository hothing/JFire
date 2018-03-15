package org.nightlabs.jfire.base.ui.jdo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableContentProvider;
import org.nightlabs.jfire.base.jdo.JDOObjectsChangedEvent;
import org.nightlabs.jfire.base.jdo.JDOObjectsChangedListener;
import org.nightlabs.util.CollectionUtil;

/**
 * A base class for active tables.
 * Subclasses must provide a {@link ActiveJDOObjectController} that provides the data.
 * <p>
 * You should not use the {@link #setInput(Object)} method directly for this class,
 * neither the one of the contained {@link TableViewer}. Use the {@link #load()} method instead.
 * </p>
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public abstract class ActiveJDOObjectTableComposite<JDOObjectID, JDOObject> extends
		AbstractTableComposite<JDOObject> {

	/**
	 * Used internally
	 */
	class ContentProvider extends TableContentProvider {
		private List<JDOObject> input;
		@Override
		public Object[] getElements(Object inputElement) {
			if (input == null) {
				return new Object[0];
			}
			return input.toArray();
		}
		public void setInput(Collection<JDOObject> inp) {
			this.input = new ArrayList<JDOObject>(inp);
		}
		public boolean isInputSet() {
			return input != null;
		}
		public boolean containsElement(JDOObject element) {
			return input != null ? input.contains(element) : false;
		}
		public void add(JDOObject element) {
			if (input == null) {
				input = new ArrayList<JDOObject>();
			}
			input.add(element);
		}
	}

	private ActiveJDOObjectController<JDOObjectID, JDOObject> controller;

	private ListenerList tableListeners = new ListenerList();

	public ActiveJDOObjectTableComposite(Composite parent, int style) {
		super(parent, style);
	}

	public ActiveJDOObjectTableComposite(Composite parent, int style, int viewerStyle) {
		super(parent, style, true, viewerStyle);
	}

	protected abstract ActiveJDOObjectController<JDOObjectID, JDOObject> createActiveJDOObjectController();

	protected ActiveJDOObjectController<JDOObjectID, JDOObject> getActiveJDOObjectController() {
		if (controller == null) {
			controller = createActiveJDOObjectController();
			initController(controller);
		}
		return controller;
	}

	protected abstract ITableLabelProvider createLabelProvider();

	/**
	 * Sets the {@link ContentProvider} and the LabelProvider {@link #createLabelProvider()}.
	 */
	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setContentProvider(new ContentProvider());
		ITableLabelProvider labelProvider = createLabelProvider();
		if (labelProvider != null)
			tableViewer.setLabelProvider(labelProvider);
	}

	private ContentProvider getContentProvider() {
		return (ContentProvider) getTableViewer().getContentProvider();
	}

	/**
	 * You might override this method in order to provide a consistent filtering of modified objects
	 * according to the remote filter defined by the {@link ActiveJDOObjectController#createJDOLifecycleListenerFilter()}.
	 *
	 * @param loadedObject the object that is currently considered
	 * @return <code>true</code> iff the given object shall not be displayed.
	 */
	protected boolean filter(JDOObject loadedObject)
	{
		return false;
	}

	/**
	 * Initialises the controller for this table and adds a listener to react to
	 * changed and deleted objects.
	 */
	private void initController(final ActiveJDOObjectController<JDOObjectID, JDOObject> controller)
	{
		assert controller != null;
		// add the listener.
		final JDOObjectsChangedListener<JDOObjectID, JDOObject> jdoObjectsChangedListener = new JDOObjectsChangedListener<JDOObjectID, JDOObject>() {
			public void onJDOObjectsChanged(JDOObjectsChangedEvent<JDOObjectID, JDOObject> event) {
				// refresh changed and new objects
				final Collection<JDOObject> loadedObjects = event.getLoadedJDOObjects();
				if (loadedObjects != null) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							if (isDisposed())
								return;

							if (!getContentProvider().isInputSet()) {
								getContentProvider().setInput(loadedObjects);
								getTableViewer().setInput(loadedObjects);
								fireSetInputEvent();
							} else {
								for (JDOObject loadedObject : loadedObjects) {
									if (getContentProvider().containsElement(loadedObject)) {
										if (filter(loadedObject))
										{
											getTableViewer().remove(loadedObject);
											fireElementRemovedEvent();
										}
										else
										{
											getTableViewer().refresh(loadedObject, true);
										}
									} else {
										getTableViewer().add(loadedObject);
										getContentProvider().add(loadedObject);
									}
									fireElementAddedEvent();
								}
							}
						}
					});
				}

				// remove deleted objects.
				Map<JDOObjectID, JDOObject> deletedObjects = event.getDeletedJDOObjects();
				if (deletedObjects != null) {
					Collection<JDOObject> delObjects = new HashSet<JDOObject>(deletedObjects.size());
					for (JDOObject delObject : deletedObjects.values()) {
						if (delObject != null) {
							delObjects.add(delObject);
						}
					}
					final Object[] removeElements = delObjects.toArray();
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							getTableViewer().remove(removeElements);
						}
					});
					fireElementRemovedEvent();
				}
			}
		};
		controller.addJDOObjectsChangedListener(jdoObjectsChangedListener);

		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent arg0)
			{
				controller.close();
				tableListeners.clear();
			}
		});

		controller.getJDOObjects();
	}

	/**
	 * Will cause this table to load its contents
	 * via the {@link ActiveJDOObjectController}.
	 */
	public void load() {
		getActiveJDOObjectController().getJDOObjects();
	}

	protected void fireSetInputEvent() {
		if (checkedElements != null) {
			setCheckedElements(checkedElements);
			checkedElements = null;
		}

		if (selectedElements != null) {
			if (revealSelectedElements != null)
				setSelection(selectedElements, revealSelectedElements);
			else
				setSelection(selectedElements);

			revealSelectedElements = null;
			selectedElements = null;
		}

		for (Object listener : tableListeners.getListeners())
			((ActiveJDOObjectTableListener) listener).inputSet();
	}

	protected void fireElementAddedEvent() {
		for (Object listener : tableListeners.getListeners())
			((ActiveJDOObjectTableListener) listener).elementAdded();
	}

	protected void fireElementRemovedEvent() {
		for (Object listener : tableListeners.getListeners())
			((ActiveJDOObjectTableListener) listener).elementRemoved();
	}

	public void addTableListener(ActiveJDOObjectTableListener listener) {
		tableListeners.add(listener);
	}

	public void removeTableListener(ActiveJDOObjectTableListener listener) {
		tableListeners.add(listener);
	}

	/**
	 * Used to temporarily store the checked elements, just in case {@link #setCheckedElements(Collection)} is
	 * called before the data is loaded. This is set to <code>null</code> whenever the user
	 * selects sth.
	 */
	private Collection<JDOObject> checkedElements;
	/**
	 * Used to temporarily store the selected elements, just in case {@link #setCheckedElements(Collection)} is
	 * called before the data is loaded. This is set to <code>null</code> whenever the user
	 * selects sth.
	 */
	private List<JDOObject> selectedElements;

	private Boolean revealSelectedElements;

	{
		addCheckStateChangedListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkedElements = null;
			}
		});

		addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				selectedElements = null;
				revealSelectedElements = null;
			}
		});
	}

	@Override
	public void setCheckedElements(Collection<JDOObject> elements) {
		this.checkedElements = elements;
		super.setCheckedElements(elements);
	}

	@Override
	public void setSelection(List<JDOObject> elements) {
		this.selectedElements = elements;
		revealSelectedElements = null;
		super.setSelection(elements);
	}


	@Override
	public void setSelection(List<JDOObject> elements, boolean reveal) {
		this.selectedElements = elements;
		revealSelectedElements = Boolean.valueOf(reveal);
		super.setSelection(elements, reveal);
	}

	@Override
	public void setSelection(ISelection selection) {
		selectedElements = null;
		if (selection instanceof IStructuredSelection) {
			selectedElements = CollectionUtil.castList(((IStructuredSelection)selection).toList());
		}

		super.setSelection(selection);
	}
}
