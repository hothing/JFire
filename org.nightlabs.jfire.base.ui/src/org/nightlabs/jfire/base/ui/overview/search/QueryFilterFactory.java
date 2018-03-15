package org.nightlabs.jfire.base.ui.overview.search;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryProvider;
import org.nightlabs.jfire.base.ui.search.AbstractQueryFilterComposite;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public interface QueryFilterFactory<Q extends AbstractSearchQuery>
	extends IExecutableExtension, Comparable<QueryFilterFactory<Q>>
{
	public static final String GLOBAL_SCOPE = "global"; //$NON-NLS-1$
	
	/**
	 * Creates the filter composite with all the given parameters.
	 *  
	 * @param parent the parent composite to create the filter into.
	 * @param style the style to use.
	 * @param layoutMode the LayoutMode to use (see {@link XComposite.LayoutMode}).
	 * @param layoutDataMode the LayoutMode to use (see {@link XComposite.LayoutDataMode}).
	 * @param queryProvider the QueryProvider from which the filter shall retrieve the query and set/get
	 * 	its filter properties to/from.
	 * @return the filter composite that shall be displayed in the viewer for the registered
	 * 	{@link #getTargetClass()}.
	 */
	AbstractQueryFilterComposite<Q> createQueryFilter(
		Composite parent, int style,
		LayoutMode layoutMode, LayoutDataMode layoutDataMode,
		QueryProvider<? super Q> queryProvider);
	
	/**
	 * Returns the title of the section the filter will be instantiated into.
	 * @return the title of the section the filter will be instantiated into.
	 */
	String getTitle();
	
	/**
	 * Returns the target class of the viewer, e.g. for the DeliveryNoteEntryViewer it is DeliveryNote.class.
	 * The viewer will ask the registry to return all factories that are registered for his base class.
	 * @return the base class of the viewer.
	 */
	Class<?> getTargetClass();
	
	/**
	 * The scope of the created UI element. This scope is used to be able to register several GUI 
	 * elements for the same result type, which is then used in different contexts. 
	 * 
	 * @return the scope in which the UI element created via {@link #createQueryFilter(Composite, int, LayoutMode, LayoutDataMode, QueryProvider)}
	 *	is used.  
	 */
	String getScope();
	
	/**
	 * @return the order hint of this factory. In other words: The wanted position of the UI that will
	 * 	be created in the list of all UI elements in the same scope with the same targetClass.
	 */
	Integer getOrderHint();
}
