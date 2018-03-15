package org.nightlabs.jfire.base.ui.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Status;
import org.nightlabs.base.ui.extensionpoint.AbstractEPProcessor;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jfire.base.ui.JFireBasePlugin;
import org.nightlabs.jfire.base.ui.overview.search.QueryFilterFactory;
import org.nightlabs.jfire.base.ui.overview.search.QueryFilterFactoryRegistry;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class AbstractQueryFilterFactory<Q extends AbstractSearchQuery>
	implements QueryFilterFactory<Q>
{
	private Class<?> targetClass;
	private String sectionTitle;
	private String scope;
	private Integer orderHint;
	
	/**
	 * Default implementation compares factories according to their section titles.
	 */
	@Override
	public int compareTo(QueryFilterFactory<Q> other)
	{
		return getOrderHint().compareTo(other.getOrderHint());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
		throws CoreException
	{
		if (AbstractEPProcessor.checkString(
			config.getAttribute(QueryFilterFactoryRegistry.ATTRIBUTE_TARGET_CLASS)) )
		{
			try
			{
				final String viewerBaseClassName = 
					config.getAttribute(QueryFilterFactoryRegistry.ATTRIBUTE_TARGET_CLASS);
				
				targetClass = JFireBasePlugin.getDefault().getBundle().loadClass(viewerBaseClassName);
			}
			catch (InvalidRegistryObjectException e)
			{
				throw new CoreException(new Status(IStatus.ERROR, JFireBasePlugin.PLUGIN_ID, 
					"Invalid registry object!", e)); //$NON-NLS-1$
			}
			catch (ClassNotFoundException e)
			{
				throw new CoreException(new Status(IStatus.ERROR, JFireBasePlugin.PLUGIN_ID, 
					"Could not find Class:" + targetClass, e)); //$NON-NLS-1$
			}
		}
		else
		{
			// TODO: how to get to the plugin that defined this invalid extension??
			throw new CoreException(new Status(IStatus.ERROR, JFireBasePlugin.PLUGIN_ID, 
				"A viewer base class has to be defined, but an empty string was found! config:"+config.getName()));			 //$NON-NLS-1$
		}
		
		if (AbstractEPProcessor.checkString(
			config.getAttribute(QueryFilterFactoryRegistry.ATTRIBUTE_TITLE)) )
		{
			sectionTitle = config.getAttribute(QueryFilterFactoryRegistry.ATTRIBUTE_TITLE);
		}
		else
		{
			// TODO: how to get to the plugin that defined this invalid extension??
			throw new CoreException(new Status(IStatus.ERROR, JFireBasePlugin.PLUGIN_ID, 
				"No section title set! config:"+config.getName())); //$NON-NLS-1$
		}
		
		if (AbstractEPProcessor.checkString(
			config.getAttribute(QueryFilterFactoryRegistry.ATTRIBUTE_SCOPE)) )
		{
			scope = config.getAttribute(QueryFilterFactoryRegistry.ATTRIBUTE_SCOPE);
		}
		else
		{
			throw new CoreException(new Status(IStatus.ERROR, JFireBasePlugin.PLUGIN_ID, 
				"No scope set! config:"+config.getName()));			 //$NON-NLS-1$
		}
		
		if (AbstractEPProcessor.checkString(
			config.getAttribute(QueryFilterFactoryRegistry.ATTRIBUTE_ORDERHINT)) )
		{
			orderHint = Integer.parseInt(
				config.getAttribute(QueryFilterFactoryRegistry.ATTRIBUTE_ORDERHINT));
		}
				
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.overview.search.QueryFilterFactory#getSectionTitle()
	 */
	@Override
	public String getTitle()
	{
		return sectionTitle;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.overview.search.QueryFilterFactory#getTargetClass()
	 */
	@Override
	public Class<?> getTargetClass()
	{
		return targetClass;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.overview.search.QueryFilterFactory#getScope()
	 */
	public String getScope()
	{
		return scope;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.ui.overview.search.QueryFilterFactory#getOrderHint()
	 */
	public Integer getOrderHint()
	{
		return orderHint;
	}
}
