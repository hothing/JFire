/* *****************************************************************************
 * org.nightlabs.base.ui - NightLabs Eclipse utilities                            *
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

package org.nightlabs.base.ui.language;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.nightlabs.base.ui.NLBasePlugin;
import org.nightlabs.base.ui.config.LanguageCfMod;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.l10n.GlobalL10nSettings;
import org.nightlabs.language.LanguageCf;
import org.nightlabs.util.NLLocale;

// TODO: use language and country (complete locale)
public class LanguageManager
implements ILanguageManager
{
	private static LanguageManager sharedInstance = null;
	public static LanguageManager sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new LanguageManager();

		return sharedInstance;
	}

	private LanguageCfMod langCfMod;
	private GlobalL10nSettings globalL10nSettings;


	/**
	 * @return the default Language, by default the corresponding languageID is <code>NLLocale.getDefault().getLanguage()</code>.
	 */
	public static LanguageCf createDefaultLanguage() {
		LanguageCf lcf = new LanguageCf(NLLocale.getDefault().getLanguage());
		lcf.init(null);
		return lcf;
	}

	/**
	 * @param languageID the id (see {@link Locale#getLanguage()}) of the LanguageCf
	 * @return a LanguageCf with the given languageID
	 */
	public static LanguageCf createLanguage(String languageID) {
		LanguageCf lcf = new LanguageCf(languageID);
		lcf.init(null);
		return lcf;
	}

	/**
	 * @param locale The Locale to get the languageID from
	 * @return the languageID for the given java.util.Locale
	 */
	public static String getLanguageID(Locale locale)
	{
		if (locale == null)
			throw new IllegalArgumentException("Param locale must not be null!"); //$NON-NLS-1$

		return locale.getLanguage();
	}

	/**
	 * @param languageID
	 * @return a Locale for the given languageID
	 */
	public static Locale getLocale(String languageID) {
		return new Locale(languageID);
	}

	public LanguageManager()
	{
		super();
		try {
			langCfMod = Config.sharedInstance().createConfigModule(LanguageCfMod.class);
			for (LanguageCf langCf : langCfMod.getLanguages())
				languageID2LanguageCf.put(langCf.getLanguageID(), langCf);
			globalL10nSettings = Config.sharedInstance().createConfigModule(GlobalL10nSettings.class);
		} catch (ConfigException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Set the default value of Locale to a valid value.
	 * It will be either the system default, the value of the nl parameter,
	 * or the languageID stored in the {@link LanguageCfMod} if present and valid.
	 */
	public void setLanguage() {
		boolean haveGoodValue = false;
		if (globalL10nSettings.getLanguage() != null) {
			if (checkLanguageID(globalL10nSettings.getLanguage())) {
				haveGoodValue = true;
				Locale.setDefault(new Locale(globalL10nSettings.getLanguage(), globalL10nSettings.getCountry()));
			}
		}
		if(!haveGoodValue) {
			// if the current default locale is
			// invalid for some reason, we switch to default english
			Locale.setDefault(Locale.ENGLISH);
		}
		currentLanguage = getLanguage(NLLocale.getDefault(), false);
		if (currentLanguage == null) {
			currentLanguage = createDefaultLanguage();
			addLanguage(currentLanguage);
		}
	}

	/**
	 * Calls {@link LanguageCfMod#setLanguageID(String)} and {@link #setLanguage()}.
	 *
	 * @param languageID The languageID to set.
	 */
	public void setLanguageID(String languageID) {
		globalL10nSettings.setAutoDetect(false);
		globalL10nSettings.setCountry(""); //$NON-NLS-1$
		globalL10nSettings.setLanguage(languageID);
		setLanguage();
	}

	private boolean checkLanguageID(String languageID) {
		String[] isoLangs = Locale.getISOLanguages();
		for (String lang : isoLangs) {
			if (lang.equals(languageID))
				return true;
		}
		return false;
	}

	/**
	 *
	 * @param langCf The LanguageCf to add
	 */
	public void addLanguage(LanguageCf langCf) {
		if (languageID2LanguageCf.containsKey(langCf.getLanguageID()))
			return;
		if (!checkLanguageID(langCf.getLanguageID()))
			throw new IllegalArgumentException("The languageID " + langCf.getLanguageID() + " is invalid!"); //$NON-NLS-1$ //$NON-NLS-2$
		langCf.init(langCfMod.getLanguageIDs());
		langCfMod.getLanguages().add(langCf);
		languageID2LanguageCf.put(langCf.getLanguageID(), langCf);
		// config is saved on exit.
//		try {
//			langCfMod._getConfig().saveConfFile();
//		} catch (ConfigException e) {
//			throw new RuntimeException(e);
//		}
	}

	/**
	 * creates an LanguageCF with the given languageID and adds it
	 * @param languageID
	 */
	public void addLanguage(String languageID) {
		if (languageID2LanguageCf.containsKey(languageID))
			return;

		if (!checkLanguageID(languageID))
			throw new IllegalArgumentException("The languageID " + languageID + " is invalid!"); //$NON-NLS-1$ //$NON-NLS-2$
		LanguageCf langCf = createLanguage(languageID);
		addLanguage(langCf);
	}

	/**
	 * creates an LanguageCF based on the given Locale and adds it
	 * @param locale
	 */
	public void addLanguage(Locale locale) {
		if (languageID2LanguageCf.containsKey(locale.getLanguage()))
			return;

		if (!checkLanguageID(locale.getLanguage()))
			throw new IllegalArgumentException("The languageID " + locale.getLanguage() + " is invalid!"); //$NON-NLS-1$ //$NON-NLS-2$
		LanguageCf langCf = createLanguage(getLanguageID(locale));
		addLanguage(langCf);
	}

	protected Map<String, LanguageCf> languageID2LanguageCf = new HashMap<String, LanguageCf>();

	private Collection<LanguageCf> unmodifiableLanguages = null;
	/**
	 * @return a java.util.Collection which contains all added {@link org.nightlabs.language.LanguageCf}s.
	 */
	public Collection<LanguageCf> getLanguages() {
		if (unmodifiableLanguages == null)
			unmodifiableLanguages = Collections.unmodifiableCollection(langCfMod.getLanguages());
		return unmodifiableLanguages;
	}

	public Image getFlag16x16Image(String languageID)
	{
		String imageRegistryKey = getImageRegistryKeyForFlag16x16(languageID);
		getFlag16x16ImageDescriptor(languageID); // this ensures that the ImageDescriptor is registered in the ImageRegistry
		return NLBasePlugin.getDefault().getImageRegistry().get(imageRegistryKey); // the ImageRegistry creates an Image instance for the descriptor if necessary
	}

	private static String getImageRegistryKeyForFlag16x16(String languageID)
	{
		return LanguageManager.class.getName() + "-flag-" + languageID + ".16x16"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public ImageDescriptor getFlag16x16ImageDescriptor(String languageID)
	{
		String imageRegistryKey = getImageRegistryKeyForFlag16x16(languageID);
		ImageRegistry imageRegistry = NLBasePlugin.getDefault().getImageRegistry();
		ImageDescriptor imageDescriptor = imageRegistry.getDescriptor(imageRegistryKey);
		if (imageDescriptor == null) {
			LanguageCf languageCf = getLanguage(languageID, true);
			byte[] flagData = languageCf._getFlagIcon16x16();

			InputStream in = flagData != null ? new ByteArrayInputStream(flagData) : LanguageCf.class.getResourceAsStream("resource/Flag-fallback.16x16.png"); //$NON-NLS-1$
			try {
				ImageData imageData = new ImageData(in);
				imageDescriptor = ImageDescriptor.createFromImageData(imageData);
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			imageRegistry.put(imageRegistryKey, imageDescriptor);
		}

		return imageDescriptor;
	}

	/**
	 * Convenience method which calls {@link #getLanguage(String, boolean)}.
	 */
	public LanguageCf getLanguage(Locale locale, boolean throwExceptionIfNotFound)
	{
		return getLanguage(locale.getLanguage(), throwExceptionIfNotFound);
	}

	/**
	 * @param languageID The 2-char-iso-code of the desired Language.
	 * @param throwExceptionIfNotFound Whether or not to throw an exception if the language couldn't be found.
	 *		If this is <tt>true</tt>, this method will never return <tt>null</tt>.
	 * @return The found <tt>LanguageCf</tt> or <tt>null</tt>, if not found and <tt>throwExceptionIfNotFound == false</tt>.
	 */
	public LanguageCf getLanguage(String languageID, boolean throwExceptionIfNotFound)
	{
		LanguageCf res = languageID2LanguageCf.get(languageID);

		if (res == null && throwExceptionIfNotFound)
			throw new IllegalArgumentException("No language registered for languageID=\"" + languageID + "\"!"); //$NON-NLS-1$ //$NON-NLS-2$

		return res;
	}

	/**
	 * @param languageID
	 * @return the Native Name of the Language
	 */
	public static String getNativeLanguageName(String languageID) {
		Locale l = getLocale(languageID);
		return l.getDisplayLanguage(l);
	}

	protected LanguageCf currentLanguage;

	/**
	 *
	 * @return The current Language
	 */
	public LanguageCf getCurrentLanguage() {
		if (currentLanguage == null)
			setLanguage();
		return currentLanguage;
	}

	/**
	 *
	 * @return the languageID of the currentLanguageCf
	 */
	public String getCurrentLanguageID() {
		return getCurrentLanguage().getLanguageID();
	}

	public void removeLanguage(String languageID) {
		if (!languageID2LanguageCf.containsKey(languageID))
			return;
		LanguageCf langCf = languageID2LanguageCf.remove(languageID);
		langCfMod.getLanguages().remove(langCf);
	}

	/**
	 * Calling this method will cause the config module to be marked as changed.
	 *
	 * @param languageCf The <tt>LanguageCf</tt> which has been changed.
	 */
	public void makeDirty(LanguageCf languageCf)
	{
		langCfMod.setChanged();
	}
}
