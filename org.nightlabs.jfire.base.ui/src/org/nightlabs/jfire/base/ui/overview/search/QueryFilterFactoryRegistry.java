package org.nightlabs.jfire.base.ui.overview.search;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.nightlabs.base.ui.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.ui.extensionpoint.EPProcessorException;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class QueryFilterFactoryRegistry
	extends AbstractEPProcessor
{
	private static final String EXTENSION_POINT_ID = "org.nightlabs.jfire.base.ui.queryFilterComposite"; //$NON-NLS-1$
	private static final String ELEMENT_NAME = "QueryFilter"; //$NON-NLS-1$
	public static final String ATTRIBUTE_TARGET_CLASS = "targetClass";  //$NON-NLS-1$
	public static final String ATTRIBUTE_QUERY_FILTER_FACTORY_CLASS = "queryFilterFactoryClass"; //$NON-NLS-1$
	public static final String ATTRIBUTE_TITLE = "title"; //$NON-NLS-1$
	public static final String ATTRIBUTE_SCOPE = "scope"; //$NON-NLS-1$
	public static final String ATTRIBUTE_ORDERHINT = "orderHint"; //$NON-NLS-1$
	
	private static volatile QueryFilterFactoryRegistry sharedInstance = null;

	/**
	 * @return The shared Instance of this class.
	 */
	public static QueryFilterFactoryRegistry sharedInstance()
	{
		if (sharedInstance == null)
		{
			synchronized (QueryFilterFactoryRegistry.class)
			{
				if (sharedInstance == null)
					sharedInstance = new QueryFilterFactoryRegistry();
			}
		}
		return sharedInstance;
	}
	
	protected QueryFilterFactoryRegistry()
	{
		queryFilters = new HashMap<ScopeTargetKey, SortedSet<QueryFilterFactory>>();
	}

	@Override
	public String getExtensionPointID()
	{
		return EXTENSION_POINT_ID;
	}
	
	private Map<ScopeTargetKey, SortedSet<QueryFilterFactory>> queryFilters;
//	private Map<String, Map<Class<?>, List<QueryFilterFactory>>> scope2TargetUIMapping;
	
	@Override
	public void processElement(IExtension extension, IConfigurationElement element) throws Exception
	{
		if (! ELEMENT_NAME.equals(element.getName()))
		{
			throw new EPProcessorException("While Processing an element, the element name didn't match! given name="  //$NON-NLS-1$
				+ element.getName());
		}
		final QueryFilterFactory factory;
		String factoryClassName = element.getAttribute(ATTRIBUTE_QUERY_FILTER_FACTORY_CLASS);
		if (checkString(factoryClassName))
		{
			try
			{
				factory = (QueryFilterFactory) element.createExecutableExtension(ATTRIBUTE_QUERY_FILTER_FACTORY_CLASS);
			}
			catch (CoreException e) {
				throw new EPProcessorException("Coudn't instantiate the given factory object "+factoryClassName, e);  //$NON-NLS-1$
			}
		}
		else
		{
			throw new EPProcessorException("the given factory class string is null or empty!"); //$NON-NLS-1$
		}
		
		ScopeTargetKey key = new ScopeTargetKey(factory);
		SortedSet<QueryFilterFactory> registeredComposites = queryFilters.get(key);
		if (registeredComposites == null)
		{
			registeredComposites = new TreeSet<QueryFilterFactory>();
		}
		
		registeredComposites.add(factory);
		queryFilters.put(key, registeredComposites);
	}
	
	/**
	 * 
	 * @param scope TODO
	 * @param baseElementType
	 * @return
	 */
	public SortedSet<QueryFilterFactory> getQueryFilterCompositesFor(String scope, Class baseElementType)
	{
		checkProcessing();
		ScopeTargetKey key = new ScopeTargetKey(scope, baseElementType);
		return getQueryFilterComposites().get(key);
	}
	
	protected Map<ScopeTargetKey, SortedSet<QueryFilterFactory>> getQueryFilterComposites()
	{
		return queryFilters;
	}

	/**
	 * Helper class acting as key for all registered QueryFilters.
	 * 
	 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
	 */
	protected class ScopeTargetKey
	{
		private String scope;
		private Class<?> targetClass;
		
		/**
		 * @param scope
		 * @param targetClass
		 */
		public ScopeTargetKey(String scope, Class<?> targetClass)
		{
			this.scope = scope;
			this.targetClass = targetClass;
		}

		public ScopeTargetKey(QueryFilterFactory factory)
		{
			this(factory.getScope(), factory.getTargetClass());
		}

		/**
		 * @return the scope
		 */
		public String getScope()
		{
			return scope;
		}

		/**
		 * @return the targetClass
		 */
		public Class<?> getTargetClass()
		{
			return targetClass;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((scope == null) ? 0 : scope.hashCode());
			result = prime * result + ((targetClass == null) ? 0 : targetClass.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			final ScopeTargetKey other = (ScopeTargetKey) obj;
			if (scope == null)
			{
				if (other.scope != null) return false;
			}
			else
				if (!scope.equals(other.scope)) return false;
			if (targetClass == null)
			{
				if (other.targetClass != null) return false;
			}
			else
				if (!targetClass.equals(other.targetClass)) return false;
			return true;
		}
		
	}
}
