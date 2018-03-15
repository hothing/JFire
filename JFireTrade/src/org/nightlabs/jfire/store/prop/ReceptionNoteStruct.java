package org.nightlabs.jfire.store.prop;

import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropHelper;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.structfield.I18nTextStructField;
import org.nightlabs.jfire.store.ReceptionNote;

/**
 * Defines the standard structure for the {@link PropertySet} of {@link ReceptionNote}s.
 *
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 */
public class ReceptionNoteStruct
{
	public static final String DEV_ORGANISATION_ID = Organisation.DEV_ORGANISATION_ID;
	public static final StructBlockID COMMENT_BLOCK = StructBlockID.create(DEV_ORGANISATION_ID, "ReceptionNote.comment");
	public static final StructFieldID COMMENT_FIELD = StructFieldID.create(COMMENT_BLOCK, "ReceptionNote.comment");

	public static IStruct getReceptionNoteStructLocal(PersistenceManager pm) {
		String devOrganisationID = Organisation.DEV_ORGANISATION_ID;
		Struct articleContainerStruct = null;
		StructLocal articleContainerStructLocal = null;
		try {
			articleContainerStruct = Struct.getStruct(devOrganisationID, ReceptionNote.class, Struct.DEFAULT_SCOPE, pm);
		} catch (JDOObjectNotFoundException e) {
			// articleContainerStruct not persisted yet.
			articleContainerStruct = new Struct(devOrganisationID, ReceptionNote.class.getName(), Struct.DEFAULT_SCOPE);
			createDefaultStructure(articleContainerStruct);
			// TODO use MultiLanguagePropertiesBundle for externalization
			articleContainerStruct.getName().setText(Locale.ENGLISH.getLanguage(), "ReceptionNotes");
			articleContainerStruct.getName().setText(Locale.GERMAN.getLanguage(), "Empfangsbestätigung");
			articleContainerStruct = pm.makePersistent(articleContainerStruct);
		}

		try {
			articleContainerStructLocal = StructLocal.getStructLocal(pm, devOrganisationID, ReceptionNote.class, articleContainerStruct.getStructScope(), StructLocal.DEFAULT_SCOPE);
		} catch (JDOObjectNotFoundException e) {
			articleContainerStructLocal = new StructLocal(articleContainerStruct, StructLocal.DEFAULT_SCOPE);
			// TODO use MultiLanguagePropertiesBundle for externalization
			articleContainerStructLocal.getName().setText(Locale.ENGLISH.getLanguage(), "Default reception note structure");
			articleContainerStructLocal.getName().setText(Locale.GERMAN.getLanguage(), "Standardstruktur für Empfangsbestätigungen");
			articleContainerStructLocal = pm.makePersistent(articleContainerStructLocal);
		}
		return articleContainerStructLocal;
	}

	private static void createDefaultStructure(IStruct articleContainerStruct) {
		try {
			// TODO use MultiLanguagePropertiesBundle for externalization
			StructBlock sb = PropHelper.createStructBlock(articleContainerStruct, COMMENT_BLOCK, "Comment", "Kommentar");
			sb.setUnique(false);
			// TODO use MultiLanguagePropertiesBundle for externalization
			I18nTextStructField comment = PropHelper.createI18nTextField(sb, COMMENT_FIELD, "Comment", "Kommentar");
			comment.setLineCount(10);
			sb.addStructField(comment);
			articleContainerStruct.addStructBlock(sb);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
