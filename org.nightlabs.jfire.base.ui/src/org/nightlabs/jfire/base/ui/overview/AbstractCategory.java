/**
 * 
 */
package org.nightlabs.jfire.base.ui.overview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract {@link Category} that manages its entries.
 * <p>
 * This class is intended to be subclassed to define the {@link Composite}
 * used to display the entries.
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public abstract class AbstractCategory implements Category {

	private List<Entry> entries = null;
	private CategoryFactory categoryFactory;
	

	/**
	 * Create a new {@link DefaultCategoryComposite}
	 * 
	 * @param categoryFactory The factory creating this category.
	 */
	public AbstractCategory(CategoryFactory categoryFactory) {
		this.categoryFactory = categoryFactory;
	}

	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.ui.overview.Category#createEntries()
	 */
	public void createEntries() {
		if (entries != null) {
			throw new IllegalStateException("createEntries() should not be called more than once for a Category."); //$NON-NLS-1$
		}
		entries = new ArrayList<Entry>();
		for (EntryFactory entryFactory : getCategoryFactory().getEntryFactories()) {
			addEntry(entryFactory.createEntry());
		}
	}

	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation will call {@link #updateCategoryComposite()}
	 * after the entry is added.
	 * </p>
	 * @see org.nightlabs.jfire.base.ui.overview.Category#addEntry(org.nightlabs.jfire.base.ui.overview.Entry)
	 */
	public void addEntry(Entry entry) {
		synchronized (entries) {
			entries.add(entry);
		}
		updateCategoryComposite();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation will call {@link #updateCategoryComposite()}
	 * after the entry is removed.
	 * </p>
	 * @see org.nightlabs.jfire.base.ui.overview.Category#removeEntry(org.nightlabs.jfire.base.ui.overview.Entry)
	 */
	public void removeEntry(Entry entry) {
		synchronized (entries) {
			entries.remove(entry);
		}
		updateCategoryComposite();
	}
	
	/**
	 * Override this method to update the GUI
	 * created with {@link Category#createComposite()}.
	 * <p>
	 * Note that this method should only do something, if the
	 * Composite was already created!
	 * </p>
	 */
	protected void updateCategoryComposite() {
	}

	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.ui.overview.Category#getEntries()
	 */
	public List<Entry> getEntries() {
		return Collections.unmodifiableList(entries);
	}

	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.ui.overview.Category#getCategoryFactory()
	 */
	public CategoryFactory getCategoryFactory() {
		return categoryFactory;
	}

}
