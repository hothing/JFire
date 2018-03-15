package org.nightlabs.jfire.web.demoshop.customer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.FetchPlan;
import javax.servlet.ServletException;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.datafield.II18nTextDataField;
import org.nightlabs.jfire.prop.datafield.PhoneNumberDataField;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.web.demoshop.Util;
import org.nightlabs.jfire.web.demoshop.WebShopException;
import org.nightlabs.jfire.web.demoshop.WebShopServlet;
import org.nightlabs.jfire.web.demoshop.resource.Messages;
import org.nightlabs.jfire.web.login.Login;
import org.nightlabs.jfire.web.webshop.DuplicateIDException;
import org.nightlabs.jfire.web.webshop.WebCustomer;
import org.nightlabs.jfire.web.webshop.WebShopRemote;
import org.nightlabs.jfire.webshop.id.WebCustomerID;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author khaled
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Attapol Thomprasert - attapol[at]nightlabs[dot]de
 */
public class CustomerServlet extends WebShopServlet
{
	/**
	 * LOG4J logger used by this class.
	 */
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CustomerServlet.class);

	private static final String ACTION_REGISTER = "register";
	private static final String ACTION_LOGOUT = "logout";
	private static final String ACTION_LOGIN = "login";
	private static final String ACTION_SHOW_EDIT_DATA_PAGE = "showEditDataPage";
	private static final String ACTION_EDITDATA = "editData";
	private static final String ACTION_LOSTPASSWORD = "lostPassword";
	private static final String ACTION_SENDPASSWORD = "sendPassword";
	private static final String ACTION_CONFIRM_EMAIL_ADDRESS = "confirm";

	// REMEMBER: the String has the equivalent static field name in PersonStruct.class
	public static final String PARAMETER_CUSTOMER_ID = "customerId";
	public static final String PARAMETER_CUSTOMER_EMAIL = "customerEmail";
	public static final String PARAMETER_CUSTOMER_PASSWORD = "customerPassword";
	public static final String PARAMETER_CUSTOMER_PASSWORD_CONFIRM = "customerPasswordConfirm";

	public static final String PARAMETER_LOGIN_CUSTOMER_ID = "loginCustomerId";
	public static final String PARAMETER_LOGIN_CUSTOMER_PASSWORD = "loginCustomerPassword";
	public static final String ERROR_MESSAGE_WRONG_ID_PASSWORD = "checkuserandpassword";

	public static final String PARAMETER_SALUTATION_MR  ="PERSONALDATA_SALUTATION_MR";
	public static final String PARAMETER_SALUTATION_MRS  ="PERSONALDATA_SALUTATION_MRS";
	public static final String PARAMETER_FIRSTNAME = "PERSONALDATA_FIRSTNAME";
	public static final String PARAMETER_NAME = "PERSONALDATA_NAME";
	public static final String PARAMETER_ADDRESS = "POSTADDRESS_ADDRESS";
	public static final String PARAMETER_POSTCODE = "POSTADDRESS_POSTCODE";
	public static final String PARAMETER_CITY = "POSTADDRESS_CITY";
	public static final String PARAMETER_COUNTRY = "POSTADDRESS_COUNTRY";
	public static final String PARAMETER_PHONE = "PHONE_PRIMARY";
	public static final String PARAMETER_EMAIL = "INTERNET_EMAIL";

	public static final String PARAMETER_CONFIRMATION_STRING = "cf";

	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.web.demoshop.BaseServlet#doGet()
	 */
	@Override
	protected void doGet() throws ServletException, IOException
	{
		try {
			String action = getAction();
			if(ACTION_LOGOUT.equals(action)) {
				doLogout();
			} else if(ACTION_SHOW_EDIT_DATA_PAGE.equals(action)) {
				setAttribute("customerData", getCustomerData(WebCustomerID.create(Login.getLogin().getOrganisationID(), getLoggedInCustomerId())));
				showPage("/jsp-customer/editCustomerData.jsp");
			} else if(ACTION_LOSTPASSWORD.equals(action)) {
				showPage("/jsp-customer/lostPassword.jsp");
			} else if(ACTION_SENDPASSWORD.equals(action)) {
				doSendPassword();
			} else if(ACTION_CONFIRM_EMAIL_ADDRESS.equals(action)) {
				doEmailConfirmation();
			}
		} catch (WebShopException e) {
			addError(e);
		}
		showDefaultPage();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.web.demoshop.BaseServlet#doPost()
	 */
	@Override
	protected void doPost() throws ServletException, IOException
	{
		try {
			String action = getAction();
			if(ACTION_LOGIN.equals(action)) {
				doLogin();
			} else if(ACTION_REGISTER.equals(action)) {
				doRegister();
			} else if(ACTION_EDITDATA.equals(action)) {
				doEditData();
			} else {
				doGet();
			}
		} catch (WebShopException e) {
			addError(e);
		}
		showDefaultPage();
	}

	private void showDefaultPage() throws ServletException, IOException
	{
		// default pages:
		if(!isForwarded()) {
			logger.debug("Show default page...");
			// show a page by login state only if not already forwarded
			if(isCustomerLoggedIn())
			{
				setAttribute("customerData", getCustomerData( WebCustomerID.create(Login.getLogin().getOrganisationID(), getLoggedInCustomerId())));
				// show customer data page
				showPage("/jsp-customer/customerData.jsp");
			}else{
				// show login page
				showPage("/jsp-customer/customerLogin.jsp");
			}
		}
	}

	private void doLogout()
	{
		// do logout
		setLoggedInCustomerId(null);
		getRequest().getSession().invalidate();
	}

	private void doRegister() throws WebShopException
	{
		// do register
		try {
			String customerId = getParameter(PARAMETER_CUSTOMER_ID).toLowerCase();
			String customerPassword = getParameter(PARAMETER_CUSTOMER_PASSWORD);
			WebShopRemote webShop = JFireEjb3Factory.getRemoteBean(WebShopRemote.class, Login.getLogin().getInitialContextProperties());
			WebCustomerID webCustomerID = WebCustomerID.create(Login.getLogin().getOrganisationID(), customerId);

			// THE REQUIRED ENTRIES
			if (!checkParameters(
					PARAMETER_CUSTOMER_ID,
					PARAMETER_CUSTOMER_PASSWORD,
					PARAMETER_CUSTOMER_PASSWORD_CONFIRM,
					PARAMETER_FIRSTNAME,
					PARAMETER_NAME,
					PARAMETER_ADDRESS,
					PARAMETER_POSTCODE,
					PARAMETER_CITY,
					PARAMETER_COUNTRY,
					PARAMETER_EMAIL)) {
				logger.warn("Missing registration parameter");
				return;
			}
			// PASSWORD CONFIRMATION CHECK
			if(!getParameter(PARAMETER_CUSTOMER_PASSWORD).equals(getParameter(PARAMETER_CUSTOMER_PASSWORD_CONFIRM)))
				throw new WebShopException(Messages.getString("error.wrongpassword"));
			// EMAIL VALIDITY CHECK
			if(!Util.isValidEmailAddress(getParameter(PARAMETER_EMAIL)))
				throw new WebShopException(Messages.getString("error.notvalidemail"));
			// IS UNIQUE EMAIL ?
			if(webShop.isEmailExisting(getParameter(PARAMETER_EMAIL)))
				throw new WebShopException("A WebCustomer with email " + getParameter(PARAMETER_EMAIL) + " is already existing.");
			//IS UNIQUE CUSTOMERID ?
			if(webShop.isWebCustomerIDExisting(webCustomerID))
				throw new WebShopException(Messages.getString("error.useridalreadyinuse")+customerId);

			logger.info("Registration of new web customer...");
			logger.info("Creating person...");
			Person person = new Person(Login.getLogin().getOrganisationID(), IDGenerator.nextID(PropertySet.class));
			StructLocal struct = StructLocalDAO.sharedInstance().getStructLocal(
					person.getStructLocalObjectID(), new NullProgressMonitor()
			);
			person.inflate(struct);
			logger.info("Setting person struct data...");
			person.setAutoGenerateDisplayName(true);
			// Those are the obligatory entrys
			person.getDataField(PersonStruct.PERSONALDATA_NAME).setData(getParameter(PARAMETER_NAME));
			person.getDataField(PersonStruct.PERSONALDATA_FIRSTNAME).setData(getParameter(PARAMETER_FIRSTNAME));
			person.getDataField(PersonStruct.POSTADDRESS_ADDRESS).setData(getParameter(PARAMETER_ADDRESS));
			person.getDataField(PersonStruct.POSTADDRESS_POSTCODE).setData(getParameter(PARAMETER_POSTCODE));
			person.getDataField(PersonStruct.POSTADDRESS_CITY).setData(getParameter(PARAMETER_CITY));
			person.getDataField(PersonStruct.POSTADDRESS_COUNTRY).setData(getParameter(PARAMETER_COUNTRY));
			person.getDataField(PersonStruct.INTERNET_EMAIL).setData(getParameter(PARAMETER_EMAIL));
			if(haveParameter(PARAMETER_PHONE))
				((PhoneNumberDataField)person.getDataField(PersonStruct.PHONE_PRIMARY)).parsePhoneNumber(getParameter(PARAMETER_PHONE));
			person.deflate();
			try {
				logger.info("Creating web customer for person...");
				//String encryptedPassword = UserLocal.encryptPassword(customerPassword);
				webShop.createWebCustomer(webCustomerID, customerPassword, person, false,  new String[] {FetchPlan.DEFAULT},
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			} catch (DuplicateIDException e) {
				throw new WebShopException("This should never happen", e);
			}
			logger.info("Sending registration confirmation mail...");
			String confStr = UserLocal.createHumanPassword(10, 10); // a random confirmation String
			String subject = String.format(Messages.getString("customer.emailconfirmationsubject"),webCustomerID.webCustomerID);
			StringBuilder targetURL = new StringBuilder();
			targetURL.append(getRequest().getScheme());
			targetURL.append("://");
			targetURL.append(getRequest().getServerName());
			if(getRequest().getServerPort() != 80) {
				targetURL.append(":");
				targetURL.append(getRequest().getServerPort());
			}
			targetURL.append(getServletContext().getContextPath());
			targetURL.append("/customer/?customerId="+webCustomerID.webCustomerID +"&action=confirm&cf="+ confStr);
			String message = String.format(
					Messages.getString("customer.emailconfirmationcontent"),
					webCustomerID.webCustomerID,
					targetURL.toString());
			webShop.storeAndSendConfirmation(webCustomerID, subject, message, confStr);
			showPage("/jsp-customer/registerConfirmation.jsp");
			logger.info("Registration done without errors.");

		} catch (WebShopException e) {
			throw e;
		} catch (Exception e) {
//			logger.error("Unexpected error in registration", e);
			throw new WebShopException("Registration failed", e);
		}
	}
	private void doLogin() throws WebShopException
	{
		// do login
		try {
			String customerId = requireParameter(PARAMETER_LOGIN_CUSTOMER_ID).toLowerCase();
			String customerPassword = requireParameter(PARAMETER_LOGIN_CUSTOMER_PASSWORD);

			logger.debug("Trying login for customer "+customerId+"...");
			WebShopRemote webShop = JFireEjb3Factory.getRemoteBean(WebShopRemote.class, Login.getLogin().getInitialContextProperties());
			WebCustomerID webCustomerID = WebCustomerID.create(Login.getLogin().getOrganisationID(), customerId);
			if(webShop.tryCustomerLogin(webCustomerID, customerPassword)) {
				setLoggedInCustomerId(customerId);
				logger.info("Login succeeded for customer "+customerId+".");
			} else {
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!Username Password Error!!!!!!!!!!!!!!!!!!!!!!!!!");
				addError(Messages.getString("error.checkuserandpassword"));
				logger.info("Login failed for customer: "+webCustomerID);
			}
		} catch(Exception e) {
			throw new WebShopException(Messages.getString("error.loginfailed"), e);
		}

		getRequest().getSession().setAttribute("user", getCustomerData(WebCustomerID.create(Login.getLogin().getOrganisationID(), getLoggedInCustomerId())));
	}

	private void doEmailConfirmation() throws WebShopException
	{
		WebShopRemote webShop;
		try {
			webShop = JFireEjb3Factory.getRemoteBean(WebShopRemote.class, Login.getLogin().getInitialContextProperties());
			String customerId = requireParameter(PARAMETER_CUSTOMER_ID).toLowerCase();
			String confirmation = requireParameter(PARAMETER_CONFIRMATION_STRING);
			if(isCustomerLoggedIn()) {
				logger.info("Customerallreadyregistered");
				throw new WebShopException(Messages.getString("error.allreadyregistered"));
			}
			WebCustomerID webCustomerID = WebCustomerID.create(Login.getLogin().getOrganisationID(), customerId);
			logger.debug("customerID: "+customerId +" confirmationString: "+confirmation +"webCustomerID:"+webCustomerID);
			if(webShop.checkEmailConfirmation(webCustomerID, confirmation) == false) {
				throw new WebShopException(Messages.getString("error.wrongconfirmation"));
			}
			if(webShop.hasEmailConfirmationExpired(webCustomerID)) {
				throw new WebShopException(Messages.getString("error.confirmationexpired"));
			}
			webShop.setConfirmationString(webCustomerID,null);
			setLoggedInCustomerId(customerId);
		} catch(WebShopException e) {
			throw e;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doSendPassword() throws WebShopException
	{
		String customerEmail = requireParameter(PARAMETER_CUSTOMER_EMAIL);
		WebShopRemote webShop;
		try {
			webShop = JFireEjb3Factory.getRemoteBean(WebShopRemote.class, Login.getLogin().getInitialContextProperties());
			// this will handle the entire workflow(create,e-mail new password)
			WebCustomer webCustomer = webShop.getWebCustomerByEmail(customerEmail).iterator().next();
			WebCustomerID webCustomerID = WebCustomerID.create(webCustomer.getOrganisationID(), webCustomer.getWebCustomerID());


			webShop.createPassword(webCustomerID);
			showPage("/jsp-customer/sendPasswordConfirmation.jsp");

		} catch (Exception e) {
			throw new WebShopException(Messages.getString("error.mailaddressnotfound"));
		}
	}

	private void doEditData() throws WebShopException
	{
		// do edit data
		try {
			String customerId = getLoggedInCustomerId();
			String customerPassword = getParameter(PARAMETER_CUSTOMER_PASSWORD);
			WebShopRemote webShop = JFireEjb3Factory.getRemoteBean(WebShopRemote.class, Login.getLogin().getInitialContextProperties());
			WebCustomerID webCustomerID = WebCustomerID.create(Login.getLogin().getOrganisationID(), customerId);

			// THE REQUIRED ENTRIES
			if (!checkParameters(
					PARAMETER_CUSTOMER_PASSWORD,
					PARAMETER_CUSTOMER_PASSWORD_CONFIRM,
					PARAMETER_FIRSTNAME,
					PARAMETER_NAME,
					PARAMETER_ADDRESS,
					PARAMETER_POSTCODE,
					PARAMETER_CITY,
					PARAMETER_COUNTRY,
					PARAMETER_EMAIL)) {
				logger.warn("Missing registration parameter");
				System.out.println("Missing registration parameter");
				return;
			}
			// PASSWORD CONFIRMATION CHECK
			if(!getParameter(PARAMETER_CUSTOMER_PASSWORD).equals(getParameter(PARAMETER_CUSTOMER_PASSWORD_CONFIRM))){
				System.out.print("Wrong Password");
				throw new WebShopException(Messages.getString("error.wrongpassword"));
			}
			// EMAIL VALIDITY CHECK
			if(!Util.isValidEmailAddress(getParameter(PARAMETER_EMAIL))){
				System.out.print("not valid email");
				throw new WebShopException(Messages.getString("error.notvalidemail"));
			}
			logger.info("Edit Data of web customer...");
			System.out.println("Edit Data of web customer...");

			WebCustomer webCustomer = webShop.getPerson(
					webCustomerID,
					new String[] {
							FetchPlan.DEFAULT,
							WebCustomer.FETCH_GROUP_LEGAL_ENTITY,
							WebCustomer.FETCH_GROUP_WEB_CUSTOMER_ID,
							WebCustomer.FETCH_GROUP_PASSWORD,
							LegalEntity.FETCH_GROUP_PERSON,
							PropertySet.FETCH_GROUP_DATA_FIELDS,
							PropertySet.FETCH_GROUP_FULL_DATA
					},
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
			);

			Person person = webCustomer.getLegalEntity().getPerson();
			StructLocal struct = StructLocalDAO.sharedInstance().getStructLocal(
					person.getStructLocalObjectID(),
//					Organisation.DEV_ORGANISATION_ID,
//					Person.class, Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE,
					new NullProgressMonitor()
			);
			person.inflate(struct);

			logger.info("Setting person struct data...");
			System.out.print("Setting person struct data...");
			// Those are the obligatory entrys
			person.getDataField(PersonStruct.PERSONALDATA_NAME).setData(getParameter(PARAMETER_NAME));
			person.getDataField(PersonStruct.PERSONALDATA_FIRSTNAME).setData(getParameter(PARAMETER_FIRSTNAME));
			person.getDataField(PersonStruct.POSTADDRESS_ADDRESS).setData(getParameter(PARAMETER_ADDRESS));
			person.getDataField(PersonStruct.POSTADDRESS_POSTCODE).setData(getParameter(PARAMETER_POSTCODE));
			person.getDataField(PersonStruct.POSTADDRESS_CITY).setData(getParameter(PARAMETER_CITY));
			person.getDataField(PersonStruct.POSTADDRESS_COUNTRY).setData(getParameter(PARAMETER_COUNTRY));
			person.getDataField(PersonStruct.INTERNET_EMAIL).setData(getParameter(PARAMETER_EMAIL));
			if(haveParameter(PARAMETER_PHONE))
				((PhoneNumberDataField)person.getDataField(PersonStruct.PHONE_PRIMARY)).parsePhoneNumber(getParameter(PARAMETER_PHONE));
			person.deflate();

			webCustomer.setPassword(customerPassword);
			webShop.storeWebCustomer(webCustomer, false, null, 1); // No need to declare any fetch-groups, if we don't retrieve anything.
			webShop.storeWebCustomerPassword(webCustomerID, customerPassword);

			showDefaultPage();
			logger.info("Edit done without errors.");
			System.out.println("Edit done without errors");

		} catch (WebShopException e) {
			throw e;
		} catch (Exception e) {
//			logger.error("Unexpected error in registration", e);
			throw new WebShopException("Edit Data failed", e);
		}
	}

	private Map<String, String> getCustomerData(WebCustomerID webCustomerID) throws WebShopException
	{
		Map<String, String> customerData= new HashMap<String, String>() ;
		try{
			WebShopRemote webShop = JFireEjb3Factory.getRemoteBean(WebShopRemote.class, Login.getLogin().getInitialContextProperties());
			WebCustomer webCustomer = webShop.getPerson(webCustomerID,
									new String[] {	WebCustomer.FETCH_GROUP_LEGAL_ENTITY,
													WebCustomer.FETCH_GROUP_WEB_CUSTOMER_ID,
													WebCustomer.FETCH_GROUP_PASSWORD,
													LegalEntity.FETCH_GROUP_PERSON,
													PropertySet.FETCH_GROUP_DATA_FIELDS,
													PropertySet.FETCH_GROUP_FULL_DATA
												} , NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

			StructLocal struct = StructLocalDAO.sharedInstance().getStructLocal(
					webCustomer.getLegalEntity().getPerson().getStructLocalObjectID(),
//					Organisation.DEV_ORGANISATION_ID,
//					Person.class, Person.STRUCT_SCOPE, Person.STRUCT_LOCAL_SCOPE,
					new NullProgressMonitor()
			);
			webCustomer.getLegalEntity().getPerson().inflate(struct);


			customerData.put("displayname",
					webCustomer.getLegalEntity().getPerson().getDisplayName());
			customerData.put("username",
					webCustomer.getWebCustomerID());
			customerData.put("password",
					webCustomer.getPassword());
			customerData.put("email",
					((II18nTextDataField)webCustomer.getLegalEntity().getPerson().getDataField(PersonStruct.INTERNET_EMAIL)).getI18nText().getText());
			customerData.put("name",
					((II18nTextDataField)webCustomer.getLegalEntity().getPerson().getDataField(PersonStruct.PERSONALDATA_NAME)).getI18nText().getText());
			customerData.put("firstname",
					((II18nTextDataField)webCustomer.getLegalEntity().getPerson().getDataField(PersonStruct.PERSONALDATA_FIRSTNAME)).getI18nText().getText());
			customerData.put("address",
					((II18nTextDataField)webCustomer.getLegalEntity().getPerson().getDataField(PersonStruct.POSTADDRESS_ADDRESS)).getI18nText().getText());
			customerData.put("city",
					((II18nTextDataField)webCustomer.getLegalEntity().getPerson().getDataField(PersonStruct.POSTADDRESS_CITY)).getI18nText().getText());
			customerData.put("country",
					((II18nTextDataField)webCustomer.getLegalEntity().getPerson().getDataField(PersonStruct.POSTADDRESS_COUNTRY)).getI18nText().getText());
			customerData.put("postcode",
					((II18nTextDataField)webCustomer.getLegalEntity().getPerson().getDataField(PersonStruct.POSTADDRESS_POSTCODE)).getI18nText().getText());
			customerData.put("phone",
					((PhoneNumberDataField)webCustomer.getLegalEntity().getPerson().getDataField(PersonStruct.PHONE_PRIMARY)).getLocalNumber());


		}catch(Exception e){
			throw new WebShopException("This should never happen", e);
		}
		return customerData;
	}
}
