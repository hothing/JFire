/* *****************************************************************************
 * DelegatingClassLoader - NightLabs extendable classloader                    *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.classloader.osgi;

import java.io.IOException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader;
import org.nightlabs.classloader.delegating.ClassDataLoaderDelegate;
import org.nightlabs.classloader.delegating.ClassLoaderDelegate;
import org.nightlabs.classloader.delegating.ClassLoadingDelegator;
import org.nightlabs.classloader.delegating.IClassLoaderDelegate;
import org.nightlabs.classloader.delegating.IClassLoadingDelegator;
import org.nightlabs.classloader.delegating.LogUtil;

/**
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class DelegatingClassLoaderOSGI 
	extends DefaultClassLoader 
	implements IClassLoadingDelegator, IClassLoaderDelegate
{

	private ClassLoadingDelegator classLoadingDelegator;
	
	public DelegatingClassLoaderOSGI(ClassLoader parent, org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate delegate, ProtectionDomain domain, BaseData bundledata, String[] classpath)
	{
		super(parent, delegate, domain, bundledata, classpath);
		classLoadingDelegator = new ClassLoadingDelegator(this);
//		loaderThread.start();
		LogUtil.log_info(this.getClass(), "init", "DelegatingClassLoader instantiated.");
	}

//	private List<String> classnamesToLoad = Collections.synchronizedList(new LinkedList<String>());
//	private Map<String, Class> classesLoaded = Collections.synchronizedMap(new HashMap<String, Class>());
//
//	private Thread loaderThread = new Thread()
//	{
//		private boolean reallyInterrupted = false;
//
//		public void run() {
//			while (!isInterrupted()) {
//				try {
//					if (classnamesToLoad.isEmpty()) {
//						synchronized (classnamesToLoad) {
//							try {
//								classnamesToLoad.wait(1000);
//							} catch (InterruptedException e) {
//								// ignore
//							}
//						}
//					}
//
//					while (!classnamesToLoad.isEmpty()) {
//						String classname = classnamesToLoad.get(0);
//						LogUtil.log_debug(this.getClass(), "run", "loading class on loaderThread: " + classname);
//
//						Class clazz;
//						try {
//							clazz = findLocalClass(classname);
//						} catch (ClassNotFoundException x) {
//							clazz = null;
//						}
//						classesLoaded.put(classname, clazz);
//						classnamesToLoad.remove(0);
//						synchronized (DelegatingClassLoaderOSGI.this) {
//							DelegatingClassLoaderOSGI.this.notifyAll();
//						}
//					}
//				} catch (Throwable t) {
//					LogUtil.log_error(this.getClass(), "run", t.getMessage(), t);
//				}
//			}
//		}
//
//		@Override
//		public boolean isInterrupted()
//		{
//			return reallyInterrupted || super.isInterrupted();
//		}
//
//		public void interrupt() {
//			reallyInterrupted = true;
//			super.interrupt();
//		}
//	};

	/**
	 * DelegatingClassLoader overrides findLocal* methods to check its
	 * delegates when classes or resources could not be found by the
	 * parent implementation.
	 *
	 * TODO I synchronized this method, because
	 * we often had deadlocks with the ClassPathManager which tries to synchronise on this object as well.
	 */
	@Override
	public Class<?> findLocalClass(String classname) throws ClassNotFoundException {
		LogUtil.log_debug(this.getClass(), "findLocalClass", "Asked for "+classname);
		Class<?> result = null;

//		ClassLoader cl = this;
//		String prefix = "";
//		while (cl != null) {
//			LogUtil.log_debug(this.getClass(), "findLocalClass", prefix + String.valueOf(cl));
//			cl = cl.getParent();
//			prefix = prefix + "  ";
//		}

//		if (Thread.currentThread() != loaderThread) {
//			LogUtil.log_debug(this.getClass(), "findLocalClass", "Delegating classload to loaderThread for "+classname);
//
//			classnamesToLoad.add(classname);
//			synchronized (classnamesToLoad) {
//				classnamesToLoad.notifyAll();
//			}
//
//			synchronized (this) {
//				while (!classesLoaded.containsKey(classname)) {
//					try {
//						this.wait(1000);
//					} catch (InterruptedException e) {
//						// ignore
//					}
//				};
//			}
//			result = classesLoaded.get(classname);
//
//			if (result == null)
//				throw new ClassNotFoundException(classname);
//
//			return result;
//		}

		try {
			result = super.findLocalClass(classname);
		} catch (ClassNotFoundException e) {
			// ignore
		}
		if (result == null) {
			LogUtil.log_debug(this.getClass(), "findLocalClass", "Not found locally ask delegates.");
			result = findDelegateClass(classname);
		}
		LogUtil.log_debug(this.getClass(), "findLocalClass", "Returning "+result);
		return result;		
	}

	/**
	 * DelegatingClassLoader overrides findLocal* methods to check its
	 * delegates when classes or resources could not be found by the
	 * parent implementation.
	 */
	@Override
	public URL findLocalResource(String resource) {
		LogUtil.log_debug(this.getClass(), "findLocalResource", "Asked for "+resource);
		URL result = null;
		result = super.findLocalResource(resource);
		if (result == null) {
			LogUtil.log_debug(this.getClass(), "findLocalResource", "Not found locally ask delegates.");
			List<URL> res = null;
			try {
				res = findDelegateResources(resource, true);
			} catch (IOException e) {
				result = null;
			}
			if (res != null && res.iterator().hasNext())				
				result = res.iterator().next();
		}
		LogUtil.log_debug(this.getClass(), "findLocalResource", "Returning "+result);
		return result;		
	}
	
	/**
	 * DelegatingClassLoader overrides findLocal* methods to check its
	 * delegates when classes or resources could not be found by the
	 * parent implementation.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<URL> findLocalResources(String resource) {
		LogUtil.log_debug(this.getClass(), "findLocalResources", "Asked for "+resource);
		Enumeration<URL> result = null;
		result = super.findLocalResources(resource);
		if (result == null) {
			LogUtil.log_debug(this.getClass(), "findLocalResources", "Not found locally ask delegates.");
			List<URL> res = null;
			try {
				res = findDelegateResources(resource, false);
			} catch (IOException e) {
				result = null;
			}
			if (res != null)				
				result = new ClassLoadingDelegator.ResourceEnumeration<URL>(res.iterator());
		}
		LogUtil.log_debug(this.getClass(), 	"findLocalResources", "Returning "+result);
		return result;		
	}

	public void addDelegate(ClassDataLoaderDelegate delegate) {
		classLoadingDelegator.addDelegate(delegate);
	}

	public void addDelegate(ClassLoaderDelegate delegate) {
		classLoadingDelegator.addDelegate(delegate);
	}

	public Class<?> findDelegateClass(String name) throws ClassNotFoundException {
		return classLoadingDelegator.findDelegateClass(name);
	}

	public List<URL> findDelegateResources(String name, boolean returnAfterFoundFirst) throws IOException {
		return classLoadingDelegator.findDelegateResources(name, returnAfterFoundFirst);
	}

	public void removeDelegate(Object delegate) {
		classLoadingDelegator.removeDelegate(delegate);
	}

	public Class<?> delegateDefineClass(String name, byte[] b, int off, int len, ProtectionDomain protectionDomain) {
		return defineClass(name, b, off, len, protectionDomain);
	}
	
	private static DelegatingClassLoaderOSGI sharedInstance;
	
	public static DelegatingClassLoaderOSGI createSharedInstance(ClassLoader parent, org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate delegate, ProtectionDomain domain, BaseData bundledata, String[] classpath) {
		if (sharedInstance != null)
			throw new IllegalStateException("Multiple calls to createSharedInstance()!");
		sharedInstance = new DelegatingClassLoaderOSGI(parent, delegate, domain, bundledata, classpath);
		return sharedInstance;
	}
	
	public static DelegatingClassLoaderOSGI getSharedInstance() {
		if (sharedInstance == null)
			throw new IllegalStateException("SharedInstance is null. Call createSharedInstance() before accessing it.");		
		return sharedInstance;
	}
}
