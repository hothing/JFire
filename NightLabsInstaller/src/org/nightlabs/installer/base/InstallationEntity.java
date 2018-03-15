package org.nightlabs.installer.base;

import java.util.List;

/**
 * The base interface for all installation entities.
 *
 * @version $Revision: 1325 $ - $Date: 2008-07-04 17:44:23 +0200 (Fr, 04 Jul 2008) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public interface InstallationEntity extends UIProvider
{
	/**
	 * Get the entity id.
	 * @return the entity id.
	 */
	String getId();

	/**
	 * Set the entity id.
	 * @param id the entity id to set
	 */
	void setId(String id);

	/**
	 * Get the visibility decider for this entity.
	 * @return the visibility decider for this entity
	 * @throws InstallationException In case of an error
	 */
	VisibilityDecider getVisibilityDecider() throws InstallationException;

	/**
	 * Get the value provider for this entity.
	 * @return the visibility decider for this entity
	 * @throws InstallationException In case of an error
	 */
	ValueProvider getValueProvider() throws InstallationException;

	/**
	 * Get the result verifier for this entity.
	 * @return the result verifier for this entity
	 * @throws InstallationException In case of an error
	 */
	ResultVerifier getResultVerifier() throws InstallationException;

	/**
	 * Get the executer for this entity.
	 * @return the executer for this entity
	 * @throws InstallationException In case of an error
	 */
	Executer getExecuter() throws InstallationException;

	/**
	 * Get the initializer for this entity.
	 * @return the initializer for this entity
	 * @throws InstallationException In case of an error
	 */
	Initializer getInitializer() throws InstallationException;

	/**
	 * Get the navigator for this entity.
	 * @return the navigator for this entity
	 * @throws InstallationException In case of an error
	 */
	Navigator getNavigator() throws InstallationException;

	/**
	 * Set a result by key. By convention the result should also
	 * be stored in parent and children results.
	 * @param key The result key
	 * @param result The result value
	 * @throws InstallationException In case of an error
	 */
	void setResult(String key, String result) throws InstallationException;

	/**
	 * Get the result (or default value) for this entity.
	 * @param key The result key
	 * @return The result string or <code>null</code> if the result
	 * 		does not exist.
	 */
	String getResult(String key);

	/**
	 * Check wether a result (or default value) exists.
	 * @param key The result to check for.
	 * @return <code>true</code> if the result exists - <code>false</code> otherwise.
	 */
	boolean haveResult(String key);

	/**
	 * Set a default value by key. By convention the default should also
	 * be stored in parent and children defaults.
	 * @param key The default key
	 * @param result The default value
	 * @throws InstallationException In case of an error
	 */
	void setDefault(String key, String result) throws InstallationException;

	/**
	 * Get the default value for this entity.
	 * @param key The default key
	 * @return The default value string or <code>null</code> if the default
	 * 		does not exist.
	 */
	String getDefault(String key);

	/**
	 * Check wether a default value exists.
	 * @param key The default value to check for.
	 * @return <code>true</code> if the default value exists - <code>false</code> otherwise.
	 */
	boolean haveDefault(String key);


	/**
	 * Get the children of this entity.
	 * @return The list of children or <code>null</code> if this entity
	 * 		has no children.
	 * @throws InstallationException In case of an error
	 */
	List<? extends InstallationEntity> getChildren() throws InstallationException;

	/**
	 * Get the parent of this entity.
	 * @return The parent of this entity or <code>null</code> if
	 * 		this is the top-level entity.
	 */
	InstallationEntity getParent();

	/**
	 * Set the parent for this entity.
	 * @param parent The parent to set.
	 */
	void setParent(InstallationEntity parent);

	/**
	 * Get a localised message for this entity. By convention,
	 * the entity should ask its parent if the string was not
	 * found.
	 * @param key The message key
	 * @return The localised string for the key.
	 * @throws InstallationException In case of an error
	 */
	String getString(String key) throws InstallationException;

	/**
	 * Check whether a localised message exists for the given key.
	 * @param key The message key
	 * @return <code>true</code> if the message exists - <code>false</code> otherwise.
	 * @throws InstallationException In case of an error
	 */
	boolean haveString(String key) throws InstallationException;

	boolean run(boolean visible) throws InstallationException;
}