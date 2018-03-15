package org.nightlabs.installer;

import org.nightlabs.installer.base.Executer;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ExecutionProgressEvent
{
	public enum Type {
		starting,
		progress,
		done
	}
	private Executer source;
	private String description;
	private Type type;
	private int workDone;
	
	/**
	 * Create a new ExecutionProgressEvent.
	 * @param source The source Executer
	 * @param description The progress description
	 * @param workDone The work done relative to {@link Executer#getTotalWork()}
	 */
	public ExecutionProgressEvent(Executer source, String description, Type type, int workDone)
	{
		super();
		this.source = source;
		this.description = description;
		this.type = type;
		this.workDone = workDone;
	}
	
	/**
	 * Create a new ExecutionProgressEvent.
	 * @param source The source Executer
	 * @param description The progress description
	 */
	public ExecutionProgressEvent(Executer source, String description, Type type)
	{
		this(source, description, type, -1);
	}

	/**
	 * Get the description.
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Get the source.
	 * @return the source
	 */
	public Executer getSource()
	{
		return source;
	}

	/**
	 * Get the workDone.
	 * @return the workDone
	 */
	public int getWorkDone()
	{
		return workDone;
	}

	/**
	 * Get the type.
	 * @return the type
	 */
	public Type getType()
	{
		return type;
	}
}
