package org.nightlabs.jfire.web.demoshop;

import java.util.ArrayList;
import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.spi.PersistenceCapable;

public class JDOObjectCarrier<T extends PersistenceCapable>
{
	private T jdoObject;
	private Object jdoObjectID;
	
	public JDOObjectCarrier(T jdoObject)
	{
		this.jdoObject = jdoObject;
		this.jdoObjectID = JDOHelper.getObjectId(jdoObject);
	}
	
	public static Collection<JDOObjectCarrier<PersistenceCapable>> createCollection(Collection<PersistenceCapable> src)
	{
		Collection<JDOObjectCarrier<PersistenceCapable>> result = new ArrayList<JDOObjectCarrier<PersistenceCapable>>(src.size());
		for (PersistenceCapable o : src)
			result.add(new JDOObjectCarrier<PersistenceCapable>(o));
		return result;
	}

	/**
	 * @return the jdoObject
	 */
	public T getJdoObject() {
		return jdoObject;
	}

	/**
	 * @return the jdoObjectID
	 */
	public Object getJdoObjectID() {
		return jdoObjectID;
	}
}
