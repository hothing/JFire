package org.nightlabs.installer.base.defaults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.InstallationManager;
import org.nightlabs.installer.Logger;
import org.nightlabs.installer.Messages;
import org.nightlabs.installer.UIType;
import org.nightlabs.installer.base.Executer;
import org.nightlabs.installer.base.Initializer;
import org.nightlabs.installer.base.InstallationEntity;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.Navigator;
import org.nightlabs.installer.base.ResultVerifier;
import org.nightlabs.installer.base.UI;
import org.nightlabs.installer.base.ValueProvider;
import org.nightlabs.installer.base.VerificationException;
import org.nightlabs.installer.base.VisibilityDecider;
import org.nightlabs.installer.base.Navigator.Navigation;
import org.nightlabs.installer.util.Util;

/**
 * The default {@link InstallationEntity} implementation.
 *
 * @version $Revision: 1325 $ - $Date: 2008-07-04 17:44:23 +0200 (Fr, 04 Jul 2008) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DefaultInstallationEntity extends DefaultConfigurable implements InstallationEntity
{
	private String id;
	private InstallationEntity parent;
	private Properties results;
	private Properties defaults;
	private VisibilityDecider visibilityDecider;
	private Executer executer;
	private Initializer initializer;
	private ResultVerifier resultVerifier;
	private Navigator navigator;
	private ValueProvider valueProvider;
	private UI ui;

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#getVisibilityDecider()
	 */
	public VisibilityDecider getVisibilityDecider() throws InstallationException
	{
		try {
			if(visibilityDecider == null) {
				visibilityDecider = (VisibilityDecider)Util.getConfigurable(getConfig(), Constants.VISIBILITY_DECIDER+Constants.SEPARATOR, DefaultVisibilityDecider.class);
				visibilityDecider.setInstallationEntity(this);
			}
			return visibilityDecider;
		} catch(Throwable e) {
			throw new InstallationException(Messages.getString("DefaultInstallationEntity.visibilityDeciderError"), e); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#getExecuter()
	 */
	public Executer getExecuter() throws InstallationException
	{
		try {
			if(executer == null) {
				executer = (Executer)Util.getConfigurable(getConfig(), Constants.EXECUTER+Constants.SEPARATOR, DefaultExecuter.class);
				executer.setInstallationEntity(this);
			}
			return executer;
		} catch(Throwable e) {
			throw new InstallationException(Messages.getString("DefaultInstallationEntity.executerError"), e); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#getInitializer()
	 */
	public Initializer getInitializer() throws InstallationException
	{
		try {
			if(initializer == null) {
				initializer = (Initializer)Util.getConfigurable(getConfig(), Constants.INITIALIZER+Constants.SEPARATOR, DefaultInitializer.class);
				initializer.setInstallationEntity(this);
			}
			return initializer;
		} catch(Throwable e) {
			throw new InstallationException(Messages.getString("DefaultInstallationEntity.initializerError"), e); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#getResultVerifier()
	 */
	public ResultVerifier getResultVerifier() throws InstallationException
	{
		try {
			if(resultVerifier == null) {
				resultVerifier = (ResultVerifier)Util.getConfigurable(getConfig(), Constants.RESULT_VERIFIER+Constants.SEPARATOR, DefaultResultVerifier.class);
				resultVerifier.setInstallationEntity(this);
			}
			return resultVerifier;
		} catch(Throwable e) {
			throw new InstallationException(Messages.getString("DefaultInstallationEntity.resultVerifierError"), e); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#getNavigator()
	 */
	public Navigator getNavigator() throws InstallationException
	{
		try {
			if(navigator == null) {
				navigator = (Navigator)Util.getConfigurable(getConfig(), Constants.NAVIGATOR+Constants.SEPARATOR, DefaultNavigator.class);
				navigator.setInstallationEntity(this);
			}
			return navigator;
		} catch(Throwable e) {
			throw new InstallationException(Messages.getString("DefaultInstallationEntity.navigatorError"), e); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#setResult(java.lang.String, java.lang.String)
	 */
	public void setResult(String key, String result) throws InstallationException
	{
		if(results == null)
			results = new Properties();

		//Logger.out.println("Setting result: "+key+" -> "+result);

		// set result here
		results.setProperty(key, result);

		// set result in parent if it is not already the same value
		if(getParent() != null) {
			String parentResultId = getId()+Constants.SEPARATOR+key;
			if(!result.equals(parent.getResult(parentResultId)))
				getParent().setResult(parentResultId, result);
		}

		// set result in child if it is not already the same value
		List<? extends InstallationEntity> children = getChildren();
		if(children != null) {
			String childId = null;
			String childKey = null;
			int idx = key.indexOf('.');
			if(idx >= 0) {
				childId = key.substring(0, idx);
				childKey = key.substring(idx+1);
			}
			if(childId != null) {
				for (InstallationEntity child : getChildren()) {
					if(childId.equals(child.getId())) {
						if(!result.equals(child.getResult(childKey)))
							child.setResult(childKey, result);
						break;
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#getResult(java.lang.String)
	 */
	public String getResult(String key)
	{
		String result = null;
		if(results != null)
			result = results.getProperty(key);
		if(result == null) {
			Logger.out.println(toString()+" - Using default as result for "+key);
			result = getDefault(key);
		}
		Logger.out.println(toString()+" - Returning result for key "+key+": "+result);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#haveResult(java.lang.String)
	 */
	public boolean haveResult(String key)
	{
		return getResult(key) != null;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#setDefault(java.lang.String, java.lang.String)
	 */
	public void setDefault(String key, String defaultValue) throws InstallationException
	{
		if(defaults == null)
			defaults = new Properties();

		Logger.out.println(toString()+" - Setting default: "+key+" -> "+defaultValue);

		// set default here
		defaults.setProperty(key, defaultValue);

		// set default in parent if it is not already the same value
		if(getParent() != null) {
			String parentResultId = getId()+Constants.SEPARATOR+key;
			if(!defaultValue.equals(parent.getDefault(parentResultId)))
				getParent().setDefault(parentResultId, defaultValue);
		}

		// set default in child if it is not already the same value
		List<? extends InstallationEntity> children = getChildren();
		if(children != null) {
			String childId = null;
			String childKey = null;
			int idx = key.indexOf(Constants.SEPARATOR);
			if(idx >= 0) {
				childId = key.substring(0, idx);
				childKey = key.substring(idx+1);
			}
			if(childId != null) {
				for (InstallationEntity child : getChildren()) {
					if(childId.equals(child.getId())) {
						if(!defaultValue.equals(child.getDefault(childKey)))
							child.setDefault(childKey, defaultValue);
						break;
					}
				}
			}
		}
	}

	/**
	 * Add default values. Existing values are overridden.
	 * @param defaults The defaults to set
	 * @throws InstallationException In case of an error
	 */
	public void addDefaults(Properties defaults) throws InstallationException
	{
		for (Iterator<Object> iter = defaults.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			setDefault(key, defaults.getProperty(key));
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#getDefault(java.lang.String)
	 */
	public String getDefault(String key)
	{
		if(defaults == null)
			return null;
		String defaultValue = defaults.getProperty(key);
		Logger.out.println(toString()+" - Returning default for key "+key+": "+defaultValue);
		return defaultValue;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#haveDefault(java.lang.String)
	 */
	public boolean haveDefault(String key)
	{
		return getDefault(key) != null;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#getValueProvider()
	 */
	public ValueProvider getValueProvider() throws InstallationException
	{
		try {
			if(valueProvider == null) {
				valueProvider = (ValueProvider)Util.getConfigurable(getConfig(), Constants.VALUE_PROVIDER+Constants.SEPARATOR, DefaultValueProvider.class);
				valueProvider.setInstallationEntity(this);
			}
			return valueProvider;
		} catch(Throwable e) {
			throw new InstallationException(Messages.getString("DefaultInstallationEntity.valueProviderError"), e); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#getUI()
	 */
	public UI getUI() throws InstallationException
	{
		try {
			if(ui == null) {
				ui = (UI)Util.getConfigurable(getConfig(), Constants.UI+Constants.SEPARATOR+InstallationManager.getInstallationManager().getUiType()+Constants.SEPARATOR, DefaultUI.class);
				Logger.out.println("Have configUI: "+ui);

				if(ui == null || ui.getClass().isAssignableFrom(DefaultUI.class)) {
					UI standardUI = Util.getStandardUI(getClass(), this);
					if(standardUI != null) {
						ui = standardUI;
						Logger.out.println("Have standard UI: "+ui);
					}
				}

				ui.setInstallationEntity(this);
			}
			return ui;
		} catch(Throwable e) {
			throw new InstallationException(Messages.getString("DefaultInstallationEntity.uiError"), e); //$NON-NLS-1$
		}
	}

	/**
	 * Get the parent.
	 * @return the parent
	 */
	public InstallationEntity getParent()
	{
		return parent;
	}

	/**
	 * Set the parent.
	 * @param parent the parent to set
	 */
	public void setParent(InstallationEntity parent)
	{
		this.parent = parent;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#getId()
	 */
	public String getId()
	{
		return id;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#setId(java.lang.String)
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#getString(java.lang.String)
	 */
	public String getString(String key) throws InstallationException
	{
		if(getParent() == null)
			return InstallationManager.getInstallationManager().getString(Constants.INSTALLER+Constants.SEPARATOR+key);
		return getParent().getString(Constants.CHILD+Constants.SEPARATOR+getId()+Constants.SEPARATOR+key);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#haveString(java.lang.String)
	 */
	public boolean haveString(String key) throws InstallationException
	{
		if(getParent() == null)
			return InstallationManager.getInstallationManager().haveString(Constants.INSTALLER+Constants.SEPARATOR+key);
		return getParent().haveString(Constants.CHILD+Constants.SEPARATOR+getId()+Constants.SEPARATOR+key);
	}

	/**
	 * Get the children of this installation entity. This is a utility
	 * method for subclasses.
	 * @param <X> The installation entity implementation
	 * @param defaultClass The default implementation to use
	 * @return The list of children
	 * @throws InstallationException In case of an error
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	protected <X extends InstallationEntity> List<X> getChildren(Class<? extends InstallationEntity> defaultClass) throws InstallationException
	{
		try {
			List<X> target = new ArrayList<X>();
			Collection<Matcher> matches = Util.getPropertyKeyMatches(
					getConfig(),
					Pattern.compile("^"+Pattern.quote(Constants.CHILD)+Pattern.quote(Constants.SEPARATOR)+"([^.]+)\\.(.*)$")); //$NON-NLS-1$ //$NON-NLS-2$
			SortedSet<String> knownChildren = new TreeSet<String>();
			for(Matcher m : matches) {
				String childId = m.group(1);
				knownChildren.add(childId);
			}
			for(String childId : knownChildren) {
				X child = (X)Util.getConfigurable(getConfig(), Constants.CHILD+Constants.SEPARATOR+childId+Constants.SEPARATOR, defaultClass);
				child.setId(childId);
				child.setParent(this);
				target.add(child);
			}
			return target;
		} catch (Throwable e) {
			throw new InstallationException(Messages.getString("DefaultInstallationEntity.childrenError"), e); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#getChildren()
	 */
	public List<? extends InstallationEntity> getChildren() throws InstallationException
	{
		return null;
	}

	protected String getFullId()
	{
		StringBuffer fullid = new StringBuffer();
		InstallationEntity entity = this;
		while(entity != null) {
			String id = entity.getId();
			if(id == null)
				break;
			if(fullid.length() > 0)
				fullid.insert(0, Constants.SEPARATOR);
			fullid.insert(0, id);
			entity = entity.getParent();
		}
		return fullid.length() == 0 ? null : fullid.toString();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.InstallationEntity#run(boolean)
	 */
	public boolean run(boolean visible) throws InstallationException
	{
		getNavigator().setNavigation(Navigation.next);

		// value provider
		ValueProvider valueProvider = getValueProvider();
		Properties defaultValues = valueProvider.getValues();
		if(defaultValues != null) {
			//Logger.out.println("Setting defaults: "+defaultValues);
			addDefaults(defaultValues);
		}

		String masterDefault = InstallationManager.getInstallationManager().getMasterDefaultValue(getFullId());
		if(masterDefault != null) {
			Logger.out.println("Have master default for "+getId()+": "+masterDefault);
			setDefault(Constants.RESULT, masterDefault);
		}

		// ui
		UI ui = null;
		// visibility decider
		VisibilityDecider visibilityDecider = getVisibilityDecider();
		if(visible == true)
			visible = visibilityDecider.isVisible();


		if(visible) {
			ui = getUI();
			ui.renderBefore();

			// initializer
			// TODO: check this... is this right? only on next like executer??
			Initializer initializer = getInitializer();
			initializer.initialize();
		}

		// children:
		List<? extends InstallationEntity> children = getChildren();
		if(children == null || children.isEmpty()) {
			if(visible)
				ui.render();
		} else {
			Navigation lastNavigation = Navigation.next;
			for(int i=0; i<children.size();) {
				Navigation navigation;
				try {
					InstallationEntity child = children.get(i);
					boolean childVisible = child.run(visible);
					if(childVisible)
						navigation = getNavigator().getNavigation();
					else
						navigation = lastNavigation;
					if(navigation == Navigation.back) {
						if(i>0)
							i--;
					} else
						i++;
					lastNavigation = navigation;
					getNavigator().setNavigation(Navigation.next);
				} catch(VerificationException e) {
					boolean exit = InstallationManager.getInstallationManager().getUiType() == UIType.quiet;
					InstallationManager.getInstallationManager().getErrorHandler().handle(e, exit);
				}
			}
		}
		// children end
		if(visible) {
			ui.renderAfter();

			if(getNavigator().getNavigation() == Navigation.next) {
				// result verifier
				ResultVerifier resultVerifier = getResultVerifier();
				resultVerifier.verify();

				// executer
				Executer executer = getExecuter();
				executer.execute();
			}
		}
		return visible;
	}
}
