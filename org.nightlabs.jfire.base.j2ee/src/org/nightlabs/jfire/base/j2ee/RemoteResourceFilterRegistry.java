/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2006 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.j2ee;

import java.util.LinkedList;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.nightlabs.jfire.classloader.remote.JFireRCDLDelegateFilter;

/**
 * This extension-point registry manages extensions to the point
 * <code>org.nightlabs.jfire.base.j2ee.remoteResourceFilter</code>.
 * It implements {@link JFireRCDLDelegateFilter} and thus allows to
 * exclude resources (including classes) from the remote-loading,
 * even if the server publishes them.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class RemoteResourceFilterRegistry
implements JFireRCDLDelegateFilter
{
	private static RemoteResourceFilterRegistry _sharedInstance = null;

	public synchronized void process() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		if (registry != null)
		{
			IExtensionPoint extensionPoint = registry.getExtensionPoint(getExtensionPointID());
			if (extensionPoint == null) {
				throw new IllegalStateException("Unable to resolve extension-point: " + getExtensionPointID()); //$NON-NLS-1$
			}

			IExtension[] extensions = extensionPoint.getExtensions();
			// For each extension ...
			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] elements =
					extension.getConfigurationElements();
				// For each member of the extension ...
				for (int j = 0; j < elements.length; j++) {
					IConfigurationElement element = elements[j];
					try {
						processElement(extension, element);
					} catch (Throwable e) { // we must catch Throwable instead of Exception since we often have NoClassDefFoundErrors (during first start or when server's class configuration changes)
						// Only log the error and continue
						System.err.println("Error processing extension element. The element is located in an extension in bundle: " + extension.getNamespaceIdentifier()); //$NON-NLS-1$
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static RemoteResourceFilterRegistry sharedInstance()
	{
		if (_sharedInstance == null) {
			synchronized (RemoteResourceFilterRegistry.class) {
				if (_sharedInstance == null) {
					RemoteResourceFilterRegistry reg = new RemoteResourceFilterRegistry();
					reg.process();
					_sharedInstance = reg;
				}
			}
		}
		return _sharedInstance;
	}

	public String getExtensionPointID()
	{
		return "org.nightlabs.jfire.base.j2ee.remoteResourceFilter";
	}

	private LinkedList<Pattern> exclusionPatterns = new LinkedList<Pattern>();

	public void processElement(IExtension extension, IConfigurationElement element)
			throws Exception
	{
			String pattern = element.getAttribute("pattern");
			exclusionPatterns.add(Pattern.compile(pattern));
	}

	public boolean includeResource(String name)
	{
		for (Pattern pattern : exclusionPatterns) {
			if (pattern.matcher(name).matches()) {
				return false;
			}
		}
		return true;
	}
}
