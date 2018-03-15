package org.nightlabs.jfire.web.demoshop.resource;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages
{
	private static final String BUNDLE_NAME = "org.nightlabs.jfire.web.demoshop.resource.messages"; //$NON-NLS-1$

//	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
//			.getBundle(BUNDLE_NAME);

	private static final Map<Locale, ResourceBundle> bundles = new HashMap<Locale, ResourceBundle>();
	
	private static class ThreadLocale extends ThreadLocal<Locale> {
		@Override
		protected Locale initialValue() {
			return Locale.getDefault();
		}
	};
	
	private transient static ThreadLocale threadLocale;
	
	/**
	 * Set the Locale that will be used in {@link #getText()} for the current
	 * thread.
	 * 
	 * @param locale The locale to be used for the current Thread
	 */
	public synchronized static void setThreadLocale(Locale locale) {
		if (threadLocale == null)
			threadLocale = new ThreadLocale();
		threadLocale.set(locale);
	}

	public static synchronized ThreadLocale getThreadLocale()
	{
		return threadLocale;
	}
	
	/**
	 * Reset the Locale used for {@link #getText()} for the current thread.
	 */
	public synchronized static void removeThreadLocale() {
		if (threadLocale == null)
			return;
		threadLocale.remove();
	}
	
	
	private Messages() {
	}

	public static String getString(String key) {
		try {
			Locale locale = Locale.getDefault();
			if(threadLocale != null)
				locale = threadLocale.get();
			if(!bundles.containsKey(locale)) {
				Locale defLocale = null;
				if (locale != null && !locale.equals(Locale.getDefault())) {
					defLocale = Locale.getDefault();
					Locale.setDefault(locale);
				}
				try {
					bundles.put(locale, ResourceBundle.getBundle(BUNDLE_NAME, locale));
				} finally {
					if (defLocale != null) {
						Locale.setDefault(defLocale);
					}
				}
			}
			return bundles.get(locale).getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
