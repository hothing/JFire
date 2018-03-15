package org.nightlabs.jfire.base.ui.overview.search;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.nightlabs.base.ui.extensionpoint.AbstractEPProcessor;

/**
 * Registry for the extension-point org.nightlabs.jfire.base.ui.quickSearchEntry
 * 
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 */
public class QuickSearchEntryRegistry
extends AbstractEPProcessor
{
	private static final Logger logger = Logger.getLogger(QuickSearchEntryRegistry.class);
	public static final String EXTENSION_POINT_ID = "org.nightlabs.jfire.base.ui.quickSearchEntry"; //$NON-NLS-1$
	
	public static String ELEMENT_QUICK_SEARCH_ENTRY_FACTORY = "quickSearchEntryFactory"; //$NON-NLS-1$
	public static String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
	public static String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	public static String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	public static String ATTRIBUTE_IMAGE = "image"; //$NON-NLS-1$
	public static String ATTRIBUTE_DEFAULT = "default"; //$NON-NLS-1$
	public static String ATTRIBUTE_DECORATOR_IMAGE = "decoratorImage"; //$NON-NLS-1$
	
	private static QuickSearchEntryRegistry sharedInstance;
	public static QuickSearchEntryRegistry sharedInstance() {
		if (sharedInstance == null) {
			synchronized (QuickSearchEntryRegistry.class) {
				if (sharedInstance == null) {
					sharedInstance = new QuickSearchEntryRegistry();
				}
			}
		}
		return sharedInstance;
	}
	
	private Map<String, SortedSet<QuickSearchEntryFactory>> id2Factories =
		new HashMap<String, SortedSet<QuickSearchEntryFactory>>();
			
	@Override
	public String getExtensionPointID() {
		return EXTENSION_POINT_ID;
	}

	@Override
	public void processElement(IExtension extension, IConfigurationElement element)
	throws Exception
	{
		if (element.getName().equals(ELEMENT_QUICK_SEARCH_ENTRY_FACTORY)) {
			if (checkString(element.getAttribute(ATTRIBUTE_CLASS))) {
				try {
					QuickSearchEntryFactory factory = (QuickSearchEntryFactory) element.createExecutableExtension(ATTRIBUTE_CLASS);
					if (factory != null && factory.getId() != null) {
						String id = factory.getId();
						SortedSet<QuickSearchEntryFactory> factories = id2Factories.get(id);
						if (factories == null)
							factories = new TreeSet<QuickSearchEntryFactory>();
						factories.add(factory);
						id2Factories.put(id, factories);
					}
				} catch (Exception e) {
					logger.error("There occured an error during initalizing the class "+element.getAttribute(ATTRIBUTE_CLASS), e); //$NON-NLS-1$
				}
			}
		}
	}

	public SortedSet<QuickSearchEntryFactory> getFactories(String id) {
		checkProcessing();
		return id2Factories.get(id);
	}
}
