package org.nightlabs.jfire.base.ui.overview.search;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.graphics.Image;
import org.nightlabs.base.ui.validation.InputValidator;
import org.nightlabs.jdo.query.AbstractSearchQuery;

/**
 * An QuickSearchEntryFactory holds decorative data, like
 * name, images etc. for a certain kind of QuickSearchEntryTypes
 *
 * Instances of this interface, can create instances of {@link QuickSearchEntry}
 *
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public interface QuickSearchEntryFactory<Q extends AbstractSearchQuery>
	extends IExecutableExtension, Comparable<QuickSearchEntryFactory<Q>>
{
	/**
	 * returns the optional image, may be null
	 * should be 16x16 pixels
	 *
	 * @return the optional image
	 */
	Image getImage();

	/**
	 * returns the optional decorator image, may be null
	 * should be 8x8 pixels
	 *
	 * @return the optional decorator image
	 */
	Image getDecoratorImage();

//	/**
//	 * returns the optional composed decorator image {@link SearchCompositeImage}
//	 * @return the composed Image out of {@link #getDecoratorImage()} if it is not null
//	 */
//	Image getComposedDecoratorImage();

	/**
	 * returns the id of the QuickSearchEntryFactory
	 * @return the id of the QuickSearchEntryFactory
	 */
	String getId();

	/**
	 * returns the name of the QuickSearchEntry
	 * @return the name of the QuickSearchEntry
	 */
	String getName();

	/**
	 * returns an instance of {@link QuickSearchEntry}
	 * @return an instance of {@link QuickSearchEntry}
	 */
	QuickSearchEntry<Q> createQuickSearchEntry();

	/**
	 * Returns whether the quick search entries created by this factory are considered the default of
	 * all quick search entries to the related object.
	 *
	 * @return whether the quick search entries created by this factory are considered the default of
	 * all quick search entries to the related object.
	 */
	boolean isDefault();

	/**
	 * Returns the class of the query type needed by me to perform the filter setup.
	 * @return the class of the query type needed by me to perform the filter setup.
	 */
	Class<? extends Q> getQueryType();

	/**
	 * Returns the input validator needed for all {@link QuickSearchEntry}s created by this factory.
	 * @return the input validator needed for all {@link QuickSearchEntry}s created by this factory.
	 */
	InputValidator<?> getInputValidator();
}
