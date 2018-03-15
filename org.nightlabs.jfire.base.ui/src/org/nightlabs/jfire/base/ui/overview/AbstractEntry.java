package org.nightlabs.jfire.base.ui.overview;

/**
 * The abstract base for {@link Entry}s holding their creating {@link EntryFactory}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public abstract class AbstractEntry implements Entry {

	private EntryFactory entryFactory;
	
	public AbstractEntry(EntryFactory entryFactory) {
		this.entryFactory = entryFactory;
	}

	@Override
	public EntryFactory getEntryFactory() {
		return entryFactory;
	}
}
