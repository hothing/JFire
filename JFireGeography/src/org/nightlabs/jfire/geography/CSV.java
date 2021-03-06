package org.nightlabs.jfire.geography;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.zip.InflaterInputStream;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.geography.id.CSVID;
import org.nightlabs.util.IOUtil;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.geography.id.CSVID"
 *		detachable="true"
 *		table="JFireGeography_CSV"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, csvType, countryID"
 *
 * @jdo.fetch-group name="CSV.data" fields="data"
 */
@PersistenceCapable(
	objectIdClass=CSVID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireGeography_CSV")
@FetchGroups(
	@FetchGroup(
		name=CSV.FETCH_GROUP_DATA,
		members=@Persistent(name="data"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class CSV
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_DATA = "CSV.data";

	public static final String CSV_TYPE_COUNTRY = "Country";
	public static final String CSV_TYPE_REGION = "Region";
	public static final String CSV_TYPE_CITY = "City";
	public static final String CSV_TYPE_DISTRICT = "District";
	public static final String CSV_TYPE_ZIP = "Zip";
	public static final String CSV_TYPE_LOCATION = "Location";

	/**
	 * @param pm The PersistenceManager.
	 * @param organisationID This <code>must</code> be the root-organisation's ID, because only this organisation is allowed to edit the geography data.
	 * @param csvType One of the <code>CSV_TYPE_</code>-constants.
	 * @param countryID The country-ID or "" (empty string), if <code>csvType == {@link #CSV_TYPE_COUNTRY}</code>.
	 * @return the data (deflated csv)
	 */
	public static byte[] getCSVData(PersistenceManager pm, String organisationID, String csvType, String countryID)
	{
		try {
			CSV csv = (CSV) pm.getObjectById(CSVID.create(organisationID, csvType, countryID));

			// TODO remove this DEBUG stuff
			if ("FL".equals(countryID))
				csv.debugDumpCSV();
			// END DEBUG

			return csv.getData();
		} catch (JDOObjectNotFoundException x) {
			return null;
		}
	}

	/**
	 * @param pm The PersistenceManager.
	 * @param organisationID This <code>must</code> be the root-organisation's ID, because only this organisation is allowed to edit the geography data.
	 * @param csvType One of the <code>CSV_TYPE_</code>-constants.
	 * @param countryID The country-ID or "" (empty string), if <code>csvType == {@link #CSV_TYPE_COUNTRY}</code>.
	 * @param data The new data to set.
	 * @return the CSV object that has been modified.
	 */
	public static CSV setCSVData(PersistenceManager pm, String organisationID, String csvType, String countryID, byte[] data)
	{
		CSV csv;
		try {
			csv = (CSV) pm.getObjectById(CSVID.create(organisationID, csvType, countryID));
			csv.setData(data);

			if (data == null) {
				pm.deletePersistent(csv);
				csv = null;
			}
		} catch (JDOObjectNotFoundException x) {
			if (data == null)
				return null;

			csv = new CSV(organisationID, csvType, countryID);
			csv.setData(data);
			csv = pm.makePersistent(csv);
		}

		// TODO remove this DEBUG stuff
		if ("FL".equals(countryID))
			csv.debugDumpCSV();
		// END DEBUG

		return csv;
	}

	/**
	 * This field is always the root-organisationID, because only the root organisation is allowed to add/modify
	 * geography data.
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String csvType;

	/**
	 * Note, that this MUST be an empty string if <code>csvType == {@link #CSV_TYPE_COUNTRY}</code>!
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String countryID;

	/**
	 * This contains a <b>deflated</b> CSV file in the same format as the
	 * files in the package <code>org.nightlabs.jfire.geography.resource</code>
	 * and encoded using UTF-8.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="BLOB"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="BLOB")
	private byte[] data;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected CSV()
	{
	}

	public CSV(String organisationID, String csvType, String countryID)
	{
		this.organisationID = organisationID;
		this.csvType = csvType;
		this.countryID = countryID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getCsvType()
	{
		return csvType;
	}
	public String getCountryID()
	{
		return countryID;
	}

	public byte[] getData()
	{
		return data;
	}
	public void setData(byte[] data)
	{
		this.data = data;
	}

	private void debugDumpCSV()
	{
		Logger logger = Logger.getLogger(CSV.class);
		if (!logger.isDebugEnabled())
			return;

		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(new InflaterInputStream(new ByteArrayInputStream(getData())), IOUtil.CHARSET_UTF_8));
			try {
				String line;
				while (true) {
					line = r.readLine();
					if (line == null)
						break;

					logger.debug(line);
				}
			} finally {
				r.close();
			}
		} catch (Exception x) {
			logger.error("Reading CSV failed!", x);
		}
	}
}
